# Deploy — Local & AWS

## Cấu trúc file

| File | Dùng ở đâu | Nội dung |
|---|---|---|
| `docker-compose.yml` | Local | Hạ tầng: mysql, kafka, influxdb, mailpit, keycloak, prometheus, grafana, kafka-ui |
| `docker-compose.apps.yml` | Local | 7 service, **build từ source** |
| `docker-compose.prod.yml` | AWS EC2 | 6 service **kéo image từ ECR** + hạ tầng (không có mysql — dùng RDS) |

> **`insight-service` không deploy lên AWS.** Nó cần Ollama, mà EC2 không GPU chạy rất chậm và ngốn RAM. Service này vẫn chạy bình thường ở local. Trên AWS nó nằm sau profile `ai` — muốn bật sau này (ví dụ sau khi chuyển sang Amazon Bedrock) thì thêm `--profile ai`.
| `.env.example` | AWS EC2 | Mẫu biến môi trường, copy thành `.env` |
| `<service>/Dockerfile` | cả hai | Multi-stage: Maven build → JRE runtime |

---

## 1. Chạy ở máy local

```bash
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build
```

Lần đầu build khoảng 10–20 phút (tải dependency Maven cho 7 service). Các lần sau nhanh hơn nhiều nhờ Docker cache lớp `pom.xml`.

```bash
# Xem trạng thái
docker compose -f docker-compose.yml -f docker-compose.apps.yml ps

# Xem log 1 service
docker compose -f docker-compose.yml -f docker-compose.apps.yml logs -f user-service

# Dừng (giữ data)
docker compose -f docker-compose.yml -f docker-compose.apps.yml down

# Dừng + XOÁ SẠCH data
docker compose -f docker-compose.yml -f docker-compose.apps.yml down -v
```

Kiểm tra nhanh:

```bash
curl http://localhost:8080/actuator/health     # user-service
curl http://localhost:9000/actuator/health     # api-gateway
curl http://localhost:9000/api/v1/user/1       # qua gateway
```

| Giao diện | URL |
|---|---|
| Swagger tổng hợp (gateway) | http://localhost:9000/swagger-ui.html |
| Grafana | http://localhost:3000 |
| Kafka UI | http://localhost:8070 |
| Mailpit | http://localhost:8025 |
| Keycloak | http://localhost:8091 |

**Vẫn chạy service bằng IntelliJ được bình thường.** Config dùng `${BIEN:mac-dinh}` nên khi không có biến môi trường, nó tự dùng `localhost` như trước. Chỉ cần bật hạ tầng:

```bash
docker compose -f docker-compose.yml up -d
```

---

## 2. Đẩy image lên ECR

Làm 1 lần — tạo 6 repository (`insight-service` không lên AWS nên không cần):

```bash
export AWS_REGION=ap-southeast-2
for s in user-service device-service ingestion-service usage-service \
         alert-service api-gateway; do
  aws ecr create-repository --repository-name het/$s --region $AWS_REGION
done
```

Build và push (chạy ở máy local hoặc trong CI):

```bash
export AWS_REGION=ap-southeast-2
export ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export ECR_REGISTRY=$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
export IMAGE_TAG=$(git rev-parse --short HEAD)

aws ecr get-login-password --region $AWS_REGION \
  | docker login --username AWS --password-stdin $ECR_REGISTRY

for s in user-service device-service ingestion-service usage-service \
         alert-service api-gateway; do
  docker build -t $ECR_REGISTRY/het/$s:$IMAGE_TAG -t $ECR_REGISTRY/het/$s:latest ./$s
  docker push $ECR_REGISTRY/het/$s:$IMAGE_TAG
  docker push $ECR_REGISTRY/het/$s:latest
done
```

Tag bằng git SHA để rollback được: muốn quay lại bản cũ chỉ cần đổi `IMAGE_TAG` trong `.env` rồi `up -d`.

---

## 3. Chạy trên EC2

**Yêu cầu:** EC2 có IAM role cho phép `AmazonEC2ContainerRegistryReadOnly` để `docker pull` từ ECR mà không cần lưu mật khẩu.

```bash
# Trên EC2
git clone <repo-url> home-energy-tracker
cd home-energy-tracker

cp .env.example .env
nano .env          # điền DB_HOST, DB_PASSWORD, ECR_REGISTRY, IMAGE_TAG...

# Nạp .env vào shell hiện tại.
# docker compose tự đọc .env, NHUNG lệnh docker login bên dưới là lệnh shell
# thường - không có bước này thì $ECR_REGISTRY rỗng và login vào registry rỗng.
set -a; . ./.env; set +a

# Đăng nhập ECR (dùng IAM role, không cần key)
aws ecr get-login-password --region ap-southeast-2 \
  | docker login --username AWS --password-stdin $ECR_REGISTRY

docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
docker compose -f docker-compose.prod.yml ps
```

Bật thêm Prometheus/Grafana/Kafka-UI (tốn thêm ~1GB RAM):

```bash
docker compose -f docker-compose.prod.yml --profile observability up -d
```

### Cổng trên EC2

Chỉ **9000 (api-gateway)** mở ra Internet. Mọi service khác bind `127.0.0.1` — kể cả Security Group có mở cổng thì từ ngoài vẫn không vào được. Muốn xem thì SSH tunnel:

```bash
ssh -i key.pem \
  -L 3000:localhost:3000 \
  -L 8070:localhost:8070 \
  -L 8080:localhost:8080 \
  -L 8091:localhost:8091 \
  ec2-user@<ip-ec2>
```

### Chuẩn bị database trên RDS

```sql
CREATE DATABASE IF NOT EXISTS home_energy_tracker;
CREATE DATABASE IF NOT EXISTS keycloak;
```

Bảng do Flyway của `user-service` tự tạo lúc khởi động lần đầu. **Chỉ `user-service` có Flyway** — nó chứa cả V1 `users`, V2 `device`, V3 `alert`. Nếu `user-service` start lỗi thì `device-service` và `alert-service` sẽ chạy nhưng query vào bảng không tồn tại.

### Chuẩn bị realm Keycloak — BẮT BUỘC làm trước

Thư mục `docker/keycloak/realms/` rỗng thì `--import-realm` không import gì cả. Trên AWS database Keycloak là mới toanh nên realm `het-security-realm` sẽ không tồn tại → **mọi request qua gateway trả 401**.

Lỗi này không lộ ra lúc khởi động: `SecurityConfig` dùng `NimbusJwtDecoder.withJwkSetUri()` (lazy, không gọi HTTP lúc start) nên container vẫn lên bình thường.

Export realm từ Keycloak đang chạy ở local rồi commit file JSON vào repo:

```bash
docker exec keycloak /opt/keycloak/bin/kc.sh export \
  --realm het-security-realm --file /tmp/realm.json
docker cp keycloak:/tmp/realm.json ./docker/keycloak/realms/het-realm.json
git add docker/keycloak/realms/het-realm.json && git commit -m "add keycloak realm export"
```

### Kích thước EC2

Tổng `mem_limit` trong `docker-compose.prod.yml`: kafka 1G + keycloak 768M + influxdb 512M + mailpit 128M + 6 service × 512M ≈ **5.4 GB**, chưa tính OS. `t3.medium` (4GB) sẽ bị OOM-kill (exit 137) — cần **`t3.large` (8GB)** trở lên.

---

## 4. Bảng biến môi trường

| Biến | Mặc định (local) | Trong Docker | Trên AWS |
|---|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/home_energy_tracker` | `jdbc:mysql://mysql:3306/...` | `jdbc:mysql://<rds-endpoint>:3306/...` |
| `DB_USERNAME` | `root` | `root` | `admin` |
| `DB_PASSWORD` | `password` | `password` | (từ `.env`) |
| `KAFKA_BROKERS` | `localhost:9094` | `kafka:9092` | `kafka:9092` |
| `INFLUX_URL` | `http://localhost:8072` | `http://influxdb:8086` | `http://influxdb:8086` |
| `MAIL_HOST` / `MAIL_PORT` | `localhost` / `1025` | `mailpit` / `1025` | `mailpit` / `1025` |
| `USER_SERVICE_URL` | `http://localhost:8080` | `http://user-service:8080` | như Docker |
| `DEVICE_SERVICE_URL` | `http://localhost:8081` | `http://device-service:8081` | như Docker |
| `USAGE_SERVICE_URL` | `http://localhost:8083` | `http://usage-service:8083` | như Docker |
| `INSIGHT_SERVICE_URL` | `http://localhost:8085` | `http://insight-service:8085` | như Docker |
| `INGESTION_SERVICE_URL` | `http://localhost:8082` | `http://ingestion-service:8082` | như Docker |
| `KEYCLOAK_URL` | `http://localhost:8091` | `http://keycloak:8080` | `http://keycloak:8080` |
| `SIMULATION_REQUESTS` | `1000` | `0` | `0` |
| `OLLAMA_URL` | `http://localhost:11434` | `http://host.docker.internal:11434` | — |

**Vì sao cổng khác nhau:** `9094`, `8072`, `8091` là cổng *map ra host* để máy bạn truy cập. Bên trong mạng Docker, container nghe ở cổng gốc: Kafka `9092`, InfluxDB `8086`, Keycloak `8080`.

---

## 5. Gỡ lỗi thường gặp

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| `Communications link failure` | App khởi động trước khi MySQL sẵn sàng | Đã có healthcheck; nếu vẫn lỗi thì `restart: unless-stopped` sẽ tự thử lại |
| `Unknown database 'home_energy_tracker'` | RDS chưa tạo schema | `CREATE DATABASE home_energy_tracker;` |
| `Connection refused` tới `localhost` | Còn sót hardcode `localhost` | Trong container, `localhost` = chính container đó |
| Container bị kill, exit code 137 | Hết RAM (OOM) | Giảm `mem_limit` hoặc bớt service |
| `no basic auth credentials` khi pull | Chưa `docker login` vào ECR | Chạy lại lệnh `aws ecr get-login-password` |
| Kafka `UnknownTopicOrPartition` | Topic chưa được tạo | Đã bật `KAFKA_AUTO_CREATE_TOPICS_ENABLE` |

Kiểm tra RAM trên EC2:

```bash
free -h
docker stats --no-stream
```

---

## 6. Bước tiếp theo: CI/CD

Sườn GitHub Actions:

1. **Auth bằng OIDC** (`aws-actions/configure-aws-credentials` với `role-to-assume`) — không lưu access key trong Secrets.
2. **Build + push** 6 image lên ECR, tag = `github.sha`.
3. **Deploy** bằng SSM Run Command — không cần nhét SSH private key vào GitHub:

```bash
aws ssm send-command \
  --instance-ids i-xxxxxxxx \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=[
    "cd /home/ec2-user/home-energy-tracker",
    "sed -i s/^IMAGE_TAG=.*/IMAGE_TAG='"$GITHUB_SHA"'/ .env",
    "docker compose -f docker-compose.prod.yml pull",
    "docker compose -f docker-compose.prod.yml up -d"
  ]'
```

---

## 7. Chi phí & tắt khi hết credit

> **AWS KHÔNG tự dừng khi hết credit.** Credit chỉ là số dư được trừ vào hoá đơn. Hết credit thì tài nguyên vẫn chạy tiếp và tiền chuyển sang thẻ đã đăng ký. Phải tự đặt hàng rào.

### Chi phí ước tính (ap-southeast-2, chạy 24/7)

| Tài nguyên | ~USD/tháng |
|---|---|
| EC2 `t3.large` on-demand | ~77 |
| RDS `db.t3.micro` MySQL, Single-AZ | ~19 |
| EBS gp3 30GB (EC2) + 20GB (RDS) | ~6 |
| ECR (6 image, ~1.5GB) | ~0.2 |
| **Tổng** | **~100** |

Con số tham khảo, giá thay đổi theo thời điểm — kiểm tra lại ở AWS Pricing Calculator. Setup này **không nằm trong Free Tier**: free tier chỉ cho `t3.micro`, mà 5.4GB RAM thì không chạy nổi.

### Hàng rào tự động (làm ngay sau khi tạo tài nguyên)

**1. AWS Budgets + Budget Action** — cách duy nhất tự dừng máy:

```bash
# Bật Cost Explorer trước (Billing console), sau đó tạo budget trong console:
# Billing > Budgets > Create budget > Cost budget
#   - Ngưỡng: đặt bằng ~80% số credit
#   - Alert: gửi email ở 50% / 80% / 100%
#   - Budget action: "Stop EC2 instances" gắn với instance của dự án
```

Budget Action là phần quan trọng — alert chỉ gửi mail, action mới thực sự stop máy.

**2. CloudWatch alarm dừng EC2 khi không dùng:**

```bash
aws cloudwatch put-metric-alarm \
  --alarm-name het-ec2-idle-stop \
  --namespace AWS/EC2 --metric-name CPUUtilization \
  --dimensions Name=InstanceId,Value=i-xxxxxxxx \
  --statistic Average --period 3600 --evaluation-periods 3 \
  --threshold 5 --comparison-operator LessThanThreshold \
  --alarm-actions arn:aws:automate:ap-southeast-2:ec2:stop
```

### Cách rẻ nhất: chỉ bật khi cần demo

Stop (không phải terminate) thì **không tính tiền giờ máy**, chỉ còn tiền EBS (~$3/tháng). Data trong Docker named volume nằm trên EBS nên vẫn còn nguyên sau khi start lại.

```bash
aws ec2 stop-instances  --instance-ids i-xxxxxxxx
aws rds  stop-db-instance --db-instance-identifier energy-tracker

# Khi cần demo
aws ec2 start-instances --instance-ids i-xxxxxxxx
aws rds  start-db-instance --db-instance-identifier energy-tracker
```

Hai lưu ý:
- **RDS stop tối đa 7 ngày** rồi AWS tự bật lại. Muốn tắt lâu hơn thì snapshot rồi delete instance.
- Stop/start EC2 làm **đổi public IP**. Cần IP cố định thì cấp Elastic IP — nhưng EIP không gắn vào máy đang chạy lại bị tính tiền, nên nếu tắt dài ngày thì đừng giữ EIP.

Chạy 3 tiếng/ngày để demo thì chi phí rơi xuống còn khoảng $15–20/tháng.

### Xoá sạch khi kết thúc dự án

Terminate EC2 thôi là chưa đủ — mấy thứ dưới đây vẫn âm thầm tính tiền:

```bash
# 1. EC2
aws ec2 terminate-instances --instance-ids i-xxxxxxxx

# 2. RDS (bỏ --skip-final-snapshot nếu muốn giữ bản sao lưu, nhưng snapshot cũng tốn tiền)
aws rds delete-db-instance --db-instance-identifier energy-tracker \
  --skip-final-snapshot --delete-automated-backups

# 3. EBS volume mồ côi (terminate EC2 không phải lúc nào cũng xoá volume)
aws ec2 describe-volumes --filters Name=status,Values=available

# 4. Snapshot cũ
aws ec2 describe-snapshots --owner-ids self

# 5. Elastic IP chưa release
aws ec2 describe-addresses

# 6. Image trong ECR
aws ecr delete-repository --repository-name het/user-service --force   # lặp cho 6 repo
```

Sau đó vào **Billing > Bills** khoảng 1–2 ngày sau, xác nhận mọi dòng đã về 0.
