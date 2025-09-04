package com.deefacto.alim_service.remote.service;

import com.deefacto.alim_service.commonNoti.domain.entity.Notification;
import com.deefacto.alim_service.commonNoti.service.NotificationUserService;
import com.deefacto.alim_service.remote.dto.ReportGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportConsumer {

    private final NotificationUserService notificationUserService;

    @KafkaListener(
            topics = "report.generated.v1",
            groupId = "notification-service-group",
            properties = {
                    JsonDeserializer.VALUE_DEFAULT_TYPE + ":com.deefacto.alim_service.remote.dto.ReportGeneratedEvent"
            }
    )
    public void consumeReportTopic(ReportGeneratedEvent response, Acknowledgment ack) {
        // 1. Notification 생성
        log.info("[ReportConsumer] - Kafka Consume Success");
        Notification notification = notificationUserService.convertReportToNotification(response.getZoneId(), response.getTimestamp());

        // 2. 생성한 Notification saveNotification() 호출
        notificationUserService.saveNotification(notification);
        log.info("[ReportConsumer] - Kafka Report Notification save successful");
        ack.acknowledge();
    }
}
