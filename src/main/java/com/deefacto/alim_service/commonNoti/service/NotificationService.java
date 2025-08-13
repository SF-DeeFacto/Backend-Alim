package com.deefacto.alim_service.commonNoti.service;

import com.deefacto.alim_service.alertNoti.domain.dto.Alert;
import com.deefacto.alim_service.commonNoti.domain.entity.Notification;
import com.deefacto.alim_service.commonNoti.domain.entity.NotificationUser;
import com.deefacto.alim_service.commonNoti.repository.NotificationRepository;
import com.deefacto.alim_service.remote.service.UserRequestProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final NotificationRepository notificationRepository;
    private final UserRequestProducer userRequestProducer;

    // Alert → Notification 변환
    public Notification convertToNotification(Alert alert) {
        String formattedTime = alert.getTimestamp() != null
                ? alert.getTimestamp().format(FORMATTER)
                : "";

        return Notification.builder()
                .notiType(Notification.NotiType.ALERT)
                .zoneId(Objects.toString(alert.getZoneId(), ""))
                .title(String.format("[%s] %s 센서 이상치 초과 알림", formattedTime, Objects.toString(alert.getSensorId(), "")))
                .content(String.format(
                        "현재시간 %s<br>%s 구역 %s 타입 센서 %s에서 값 %s%s가 정상 범위를 초과했습니다.",
                        formattedTime,
                        Objects.toString(alert.getZoneId(), ""),
                        Objects.toString(alert.getSensorType(), ""),
                        Objects.toString(alert.getSensorId(), ""),
                        Objects.toString(alert.getVal(), ""),
                        Objects.toString(alert.getUnit(), "")
                ))
                .timestamp(alert.getTimestamp())
                .build();
    }

    // Notification 저장과 동시에 NotiUser도 추가되어야 함
    public Notification saveNotification(Notification notification) {
        System.out.println("!!!!!!!!!!!!!!!!!!saveNotification 실행됨!!!!!!!!");
        int hour = notification.getTimestamp().getHour();
        String shift = "None";
        if(hour >= 7 && hour < 19) {
            shift = "A";
        } else if (hour >= 19 || hour < 7){
            shift = "B";
        }

        Notification result = notificationRepository.save(notification);
        userRequestProducer.requestUsersForNotification(result.getNotiId(), result.getZoneId(), shift);
        return result;
    }
}
