package com.deefacto.alim_service.alertNoti.service;

import com.deefacto.alim_service.alertNoti.domain.dto.Alert;
import com.deefacto.alim_service.alertNoti.domain.dto.ConnectedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final AlertRedisService alertRedisService;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, ConnectedUser> connectedUsers = new ConcurrentHashMap<>();

    private static final long TIMEOUT = 30 * 60 * 1000L; // 30분

    public SseEmitter subscribe(String userId, String userRole, String userShift, String lastEventId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(userId, emitter);

        Set<String> zoneSet = Arrays.stream(userRole.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

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
            throw new RuntimeException("SSE 연결 실패", e);
        }

        // 누락 메시지 재전송
        List<Alert> missedAlerts = alertRedisService.getAlertsAfter(lastEventId);
        missedAlerts.forEach(alert -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(alert.getId())
                        .name("alert")
                        .data(alert));
            } catch (IOException e) {
                log.error("Failed to resend alert to userId={}: {}", userId, e.getMessage());
            }
        });

        return emitter;
    }

    private void remove(String userId) {
        emitters.remove(userId);
        connectedUsers.remove(userId);
    }

    public void sendAlert(Alert alert) {
        String alertZone = alert.getZoneId();

        for (String userId : emitters.keySet()) {
            ConnectedUser user = connectedUsers.get(userId);
            if (user == null) continue;

            if (!user.getZoneIds().contains(alertZone)) continue;

            OffsetDateTime alertTime = alert.getTimestamp();
            String shift = user.getUserShift();
            int hour = alertTime.getHour();

            boolean isWithinShift = false;

            if ("A".equalsIgnoreCase(shift)) {
                isWithinShift = hour >= 7 && hour < 19;
            } else if ("B".equalsIgnoreCase(shift)) {
                isWithinShift = hour >= 19 || hour < 7;
            }

            if (!isWithinShift) continue;

            try {
                emitters.get(userId).send(
                        SseEmitter.event().id(alert.getId()).name("alert").data(alert)
                );
            } catch (IOException e) {
                remove(userId);
            }
        }
    }
}

