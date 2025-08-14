package com.deefacto.alim_service.remote.service;

import com.deefacto.alim_service.commonNoti.domain.entity.Notification;
import com.deefacto.alim_service.commonNoti.service.NotificationUserService;
import com.deefacto.alim_service.remote.dto.ReportGeneratedEvent;
import com.deefacto.alim_service.remote.dto.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

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
        Notification notification = notificationUserService.convertReportToNotification(response.getZoneId(), response.getTimestamp());

        // 2. 생성한 Notification saveNotification() 호출
        notificationUserService.saveNotification(notification);
        ack.acknowledge();
    }
}
