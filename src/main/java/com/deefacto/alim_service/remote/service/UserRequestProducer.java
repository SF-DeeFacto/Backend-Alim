package com.deefacto.alim_service.remote.service;

import com.deefacto.alim_service.remote.dto.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class UserRequestProducer {

    private final KafkaTemplate<String, UserMessage.UserRequestMessage> kafkaTemplate;

    public void requestUsersForNotification(Long notificationId, String zoneId, String shift) {
        System.out.println("!!!!!!!!!!!!!!!!!!kafka request 메소드 실행됨!!!!!!!!");
        UserMessage.UserRequestMessage request = new UserMessage.UserRequestMessage();
        request.setNotificationId(notificationId);
        request.setZoneId(zoneId);
        request.setShift(shift);

        try {
            kafkaTemplate.send("user.request", request).get();
            System.out.println("Kafka message sent successfully.");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            // 여기서 예외 로그를 확인하고, 원인 분석 가능
        }
    }
}

