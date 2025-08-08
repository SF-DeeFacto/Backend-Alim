package com.deefacto.alim_service.popup.service;

import com.deefacto.alim_service.popup.domain.dto.Alert;
import com.deefacto.alim_service.popup.domain.dto.ConnectedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SseService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // userId → (userRole + emitter)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, ConnectedUser> connectedUsers = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId, String userRole, Character userShift) {
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
            // TODO shift 정보 필터링 필요
            if (user != null && user.getZoneIds().contains(alertZone)) {
                try {
                    emitters.get(userId).send(
                            SseEmitter.event().name("alert").data(alert)
                    );
                    System.out.println("Sending alert to: " + userId + " zone: " + alert.getZoneId());
                } catch (IOException e) {
                    remove(userId);
                }
            }
        }
    }


    public void broadcastAlertsFromRedis() {
        Set<String> keys = redisTemplate.keys("alert:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            try {
                String json = redisTemplate.opsForValue().get(key);
                if (json == null) continue;

                Alert alert = objectMapper.readValue(json, Alert.class);
                String alertZone = alert.getZoneId();

                for (String userId : emitters.keySet()) {
                    ConnectedUser user = connectedUsers.get(userId);
                    if (user != null && user.getZoneIds().contains(alertZone)) {
                        try {
                            emitters.get(userId).send(
                                    SseEmitter.event().name("alert").data(alert)
                            );
                        } catch (IOException e) {
                            remove(userId);
                        }
                    }
                }
            } catch (Exception e) {
                // 로깅 혹은 무시
            }
        }
    }
}

