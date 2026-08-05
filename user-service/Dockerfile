# ============ Stage 1: build ============
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom truoc, tai dependency truoc.
# Sua code ma khong sua pom -> Docker dung lai lop cache nay, build lai chi vai giay.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ============ Stage 2: runtime ============
# Chi JRE + file jar. Khong con Maven/JDK -> image nho hon ~4 lan.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Khong chay bang root.
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /build/target/*.jar app.jar
USER app

EXPOSE 8080

# MaxRAMPercentage: JVM tinh heap theo memory limit CUA CONTAINER.
# Thieu dong nay JVM doc RAM cua ca may -> 7 service cong lai se bi OOM-kill.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "/app/app.jar"]
