package com.deefacto.alim_service.remote.service;

import com.deefacto.alim_service.commonNoti.service.NotificationUserService;
import com.deefacto.alim_service.remote.dto.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

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
        notificationUserService.assignNotificationToUsers(response.getNotificationId(), response.getUserIds());
        ack.acknowledge();
    }
}
