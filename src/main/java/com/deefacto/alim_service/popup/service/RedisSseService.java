package com.deefacto.alim_service.popup.service;

import com.deefacto.alim_service.popup.domain.dto.RedisAlert;
import com.deefacto.alim_service.popup.domain.dto.RedisConnectedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
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
public class RedisSseService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // userId → (userRole + emitter)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, RedisConnectedUser> connectedUsers = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId, String userRole, String userShift) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        Set<String> zoneSet = Arrays.stream(userRole.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        connectedUsers.put(userId, new RedisConnectedUser(userId, zoneSet, userShift));

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

    public void sendAlert(RedisAlert alert) {
        String alertZone = alert.getZoneId();

        for (String userId : emitters.keySet()) {
            RedisConnectedUser user = connectedUsers.get(userId);
            if (user == null) continue;

            // 1. 권한 있는 zone인지 확인
            if (!user.getZoneIds().contains(alertZone)) continue;

            // 2. Shift 조건 확인
            OffsetDateTime alertTime = alert.getTimestamp();
            String shift = user.getUserShift();
            int hour = alertTime.getHour();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + hour);

            boolean isWithinShift = false;

            if ("A".equalsIgnoreCase(shift)) {
                // A조: 07:00 ~ 19:00
                isWithinShift = hour >= 7 && hour < 19;
            } else if ("B".equalsIgnoreCase(shift)) {
                // B조: 19:00 ~ 다음날 07:00
                isWithinShift = hour >= 19 || hour < 7;
            }

            if (!isWithinShift) continue; // 근무시간 아닐 경우 스킵

            // 3. 보내기
            try {
                emitters.get(userId).send(
                        SseEmitter.event().name("alert").data(alert)
                );
                System.out.println("Sending alert to: " + userId +
                        " zone: " + alert.getZoneId() +
                        " shift: " + user.getUserShift() +
                        " at " + alertTime);
            } catch (IOException e) {
                remove(userId);
            }
        }

    }
}

