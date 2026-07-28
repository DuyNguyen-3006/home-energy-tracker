package com.andrew.alert_service.service;

import com.andrew.kafka.event.AlertingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlertService {
    private final EmailService emailService;
    public AlertService(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void energyUsageAlertEvent(AlertingEvent event) {
        log.info("Received alert: {}", event);

        final String subject = "Energy Usage Alert for User "
                + event.getUserId();
        final String message = "Alert: " + event.getMessage() +
                "\nThreshold: " + event.getThreshold() +
                "\nEnergy Consumed: " + event.getEnergyConsumed();
        emailService.sendEmail(event.getEmail(),
                subject,
                message,
                event.getUserId());
    }
}
