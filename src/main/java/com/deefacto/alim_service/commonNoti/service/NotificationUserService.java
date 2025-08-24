package com.deefacto.alim_service.commonNoti.service;

import com.deefacto.alim_service.alertNoti.domain.dto.Alert;
import com.deefacto.alim_service.commonNoti.domain.entity.NotiType;
import com.deefacto.alim_service.commonNoti.domain.entity.Notification;
import com.deefacto.alim_service.commonNoti.domain.entity.NotificationUser;
import com.deefacto.alim_service.commonNoti.repository.NotificationRepository;
import com.deefacto.alim_service.commonNoti.repository.NotificationUserRepository;
import com.deefacto.alim_service.remote.service.UserRequestProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationUserService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final NotificationRepository notificationRepository;
    private final UserRequestProducer userRequestProducer;
    private final NotificationUserRepository notificationUserRepository;

    // Alert → Notification 변환
    public Notification convertAlertToNotification(Alert alert) {
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

    // kafka(report) consumer에서 호출됨
    public Notification convertReportToNotification(String zoneId, OffsetDateTime timestamp) {
        String formattedTime = timestamp != null
                ? timestamp.format(FORMATTER)
                : "";
        return Notification.builder()
                .notiType(NotiType.REPORT)
                .zoneId(Objects.toString(zoneId, ""))
                .title(String.format("[%s] %s Zone 리포트 생성 알림", formattedTime, zoneId))
                .content(String.format(
                        "현재시간 %s<br>%s 구역 정기 리포트가 생성되었습니다.<br>이슈 확인 후 근무 바랍니다.",
                        formattedTime,
                        Objects.toString(zoneId, "")
                ))
                .timestamp(timestamp)
                .build();
    }

    // Notification 저장 및 NotiUser 저장 (Producer 기반 noti 정보 전달 시, user 정보 받아와 저장)
    // kafka(user) produce 호출함
    public Notification saveNotification(Notification notification) {
        int hour = notification.getTimestamp().getHour();
        String shift = "None";
        if(hour >= 7 && hour < 19) {
            shift = "DAY";
        } else if (hour >= 19 || hour < 7){
            shift = "NIGHT";
        }

        Notification result = notificationRepository.save(notification);
        userRequestProducer.requestUsersForNotification(result.getNotiId(), result.getZoneId(), shift);
        log.info("Notification saved successfully");
        return result;
    }

    // 알림과 대상 사용자 리스트를 받아 각각 알림-사용자 row를 생성
    // kafka(user) consumer에서 호출됨
    public void assignNotificationToUsers(Long notificationId, List<Long> userIds) {
        List<NotificationUser> notificationUsers = userIds.stream()
                .map(userId -> NotificationUser.builder()
                        .notiId(notificationId)
                        .userId(userId)
                        .readStatus(false)
                        .flagStatus(false)
                        .build())
                .collect(Collectors.toList());

        notificationUserRepository.saveAll(notificationUsers);
        log.info("notification_user data stored complete.");
    }




}
