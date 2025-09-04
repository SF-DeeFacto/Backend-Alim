package com.deefacto.alim_service.remote.service;

import com.deefacto.alim_service.commonNoti.service.NotificationUserService;
import com.deefacto.alim_service.remote.dto.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserResponseConsumer {

    private final NotificationUserService notificationUserService;

    @KafkaListener(
            topics = "user.response",
            groupId = "notification-service-group",
            properties = {
                    JsonDeserializer.VALUE_DEFAULT_TYPE + ":com.deefacto.alim_service.remote.dto.UserMessage$UserResponseMessage"
            }
    )
    public void consumeUserResponse(UserMessage.UserResponseMessage response, Acknowledgment ack) {
        log.info("[UserResponseConsumer] - Kafka Consume Success");
        notificationUserService.assignNotificationToUsers(response.getNotificationId(), response.getUserIds());
        log.info("[UserResponseConsumer] - Kafka User Response received Successful");
        ack.acknowledge();
    }
}
