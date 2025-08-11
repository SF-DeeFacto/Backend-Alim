package com.deefacto.alim_service.alertNoti.service;

import com.deefacto.alim_service.alertNoti.dto.Alert;
import com.deefacto.alim_service.alertNoti.dto.ConnectedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SseService {

    private final ObjectMapper objectMapper;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, ConnectedUser> connectedUsers = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId, String userRole, String userShift) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        Set<String> zoneSet = Arrays.stream(userRole.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        connectedUsers.put(userId, new ConnectedUser(userId, zoneSet, userShift));

        emitter.onCompletion(() -> remove(userId));
        emitter.onTimeout(() -> remove(userId));
        emitter.onError((e) -> remove(userId));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            throw new RuntimeException("SSE 연결 실패", e);
        }

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
                        SseEmitter.event().name("alert").data(alert)
                );
            } catch (IOException e) {
                remove(userId);
            }
        }
    }
}

