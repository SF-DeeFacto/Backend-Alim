package com.deefacto.alim_service.commonNoti.service;

import com.deefacto.alim_service.alertNoti.domain.dto.Alert;
import com.deefacto.alim_service.commonNoti.domain.dto.NotificationReadDTO;
import com.deefacto.alim_service.commonNoti.domain.entity.NotiType;
import com.deefacto.alim_service.commonNoti.domain.entity.Notification;
import com.deefacto.alim_service.commonNoti.repository.NotificationRepository;
import com.deefacto.alim_service.commonNoti.repository.NotificationUserRepository;
import com.deefacto.alim_service.remote.service.UserRequestProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final NotificationRepository notificationRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final UserRequestProducer userRequestProducer;

    // 알림 리스트 조회: userId, isRead, isFlagged에 따라 조회
    // (isRead, isFlagged Null 경우도 쿼리에서 자동 처리)
    public Page<NotificationReadDTO> getNotificationsForUser(Long userId, Boolean isRead, Boolean isFlagged, Pageable pageable) {
        return notificationUserRepository.findNotificationsWithMetaByReadFlagUserId(userId, isRead, isFlagged, pageable);
    }

    // 안읽은 알림 개수 조회: userId에 따라 조회
    public Integer getUnreadNotiCountForUser(Long userId) {
        return notificationUserRepository.findNotificationsWithMetaByUserId(userId).size();
    }

    // 알림 읽음 처리
    public Integer updateReadStatus(Long userId, Long notiId) {
        return notificationUserRepository.markNotificationAsRead(userId, notiId, OffsetDateTime.now());
    }

    // 알림 일괄 읽음 처리
    public Integer updateAllReadStatus(Long userId) {
        return notificationUserRepository.markNotificationAsRead(userId, null, OffsetDateTime.now());
    }

    // Alert → Notification 변환
    public Notification convertToNotification(Alert alert) {
        String formattedTime = alert.getTimestamp() != null
                ? alert.getTimestamp().format(FORMATTER)
                : "";

        return Notification.builder()
                .notiType(NotiType.ALERT)
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

    // Notification 저장 및 NotiUser 저장 (Producer 기반 noti 정보 전달 시, user 정보 받아와 저장)
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
