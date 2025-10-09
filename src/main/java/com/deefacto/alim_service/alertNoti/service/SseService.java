package com.deefacto.alim_service.alertNoti.service;

import com.deefacto.alim_service.alertNoti.domain.dto.Alert;
import com.deefacto.alim_service.alertNoti.domain.dto.ConnectedUser;
import com.deefacto.alim_service.common.exception.CustomException;
import com.deefacto.alim_service.common.exception.ErrorCode;
import com.deefacto.alim_service.commonNoti.domain.entity.Notification;
import com.deefacto.alim_service.commonNoti.service.NotificationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final AlertRedisService alertRedisService;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, ConnectedUser> connectedUsers = new ConcurrentHashMap<>();

    private final NotificationUserService notificationUserService;

    private static final long TIMEOUT = 30 * 60 * 1000L; // 30분

    // SSE 구독 (팝업 알림 구독)
    public SseEmitter subscribe(String userId, String userScope, String userShift, String lastEventId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(userId, emitter);

        Set<String> zoneSet = Optional.ofNullable(userScope)
                .filter(s -> !s.isBlank())
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .filter(str -> !str.isEmpty())
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        connectedUsers.put(userId, new ConnectedUser(userId, zoneSet, userShift));

        emitter.onCompletion(() -> {
            remove(userId);
            log.info("SSE connection completed: userId={}", userId);
        });
        emitter.onTimeout(() -> {
            remove(userId);
            log.info("SSE connection timeout: userId={}", userId);
        });
        emitter.onError((e) -> {
            remove(userId);
            log.warn("SSE connection error: userId={}, error={}", userId, e.getMessage());
        });

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            remove(userId);
            throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR,"SSE 외부 서비스 호출 중 오류가 발생했습니다.");
        }

        // 누락 메시지 재전송 (lastEventId 방어)
        try {
            if (lastEventId != null && !lastEventId.isBlank()) {
                List<Alert> missedAlerts = alertRedisService.getAlertsAfter(lastEventId);
                missedAlerts.forEach(this::sendAlert);
            }
        } catch (Exception e) {
            log.error("Failed to resend missed alerts for userId={}, lastEventId={}, error={}",
                    userId, lastEventId, e.getMessage(), e);
        }
        return emitter;
    }

    // SSE 구독 해제 (팝업 알림 구독 끊기)
    private void remove(String userId) {
        emitters.remove(userId);
        connectedUsers.remove(userId);
    }

    // DB 적재 (Notification Table) & 팝업 Noti 전송
    public void sendAlert(Alert alert) {
        String alertZone = alert.getZoneId();

        Notification noti = saveAlert(alert);

        for (String userId : emitters.keySet()) {
            ConnectedUser user = connectedUsers.get(userId);
            if (user == null) continue;

            // zone 매칭
            if (!user.getZoneIds().contains(String.valueOf(alertZone.charAt(0)))) continue;

            // shift 확인
            OffsetDateTime alertTime = alert.getTimestamp();
            String shift = user.getUserShift();
            int hour = alertTime.getHour();

            boolean isWithinShift = false;

            if ("DAY".equalsIgnoreCase(shift)) {
                isWithinShift = hour >= 7 && hour < 19;
            } else if ("NIGHT".equalsIgnoreCase(shift)) {
                isWithinShift = hour >= 19 || hour < 7;
            }

            if (!isWithinShift) continue;

            // 안전 전송
            try {
                SseEmitter emitter = emitters.get(userId);
                if (emitter != null) {
                    emitter.send(SseEmitter.event().id(alert.getId()).name("alert").data(noti));
                }
            } catch (IOException e) {
                log.warn("SSE send failed (IO) for userId={}, alertId={}, removing emitter.", userId, alert.getId());
                remove(userId);
            } catch (Exception e) {
                log.error("Unexpected error while sending SSE to userId={}, alertId={}, error={}",
                        userId, alert.getId(), e.getMessage(), e);
                remove(userId);
            }
        }
    }

    // DB 적재 (Notification Table)
    @Transactional
    private Notification saveAlert(Alert alert) {
        Notification receivedNoti = notificationUserService.convertAlertToNotification(alert);
        return notificationUserService.saveNotification(receivedNoti);
    }
}

