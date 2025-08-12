package com.deefacto.alim_service.alertNoti.service;

import com.deefacto.alim_service.alertNoti.domain.dto.Alert;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AlertRedisService {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "alert:";

    public void saveAlert(Alert alert) {
        String key = KEY_PREFIX + alert.getId();
//        redisTemplate.opsForValue().set(key, alert, Duration.ofHours(1));
        redisTemplate.opsForValue().set(key, alert, Duration.ofMinutes(5));

    }

    public List<Alert> getAlertsAfter(String lastEventId) {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<Alert> allAlerts = keys.stream()
                .map(k -> redisTemplate.opsForValue().get(k))
                .filter(Objects::nonNull)
                .map(obj -> objectMapper.convertValue(obj, Alert.class))  // LinkedHashMap -> Alert 변환
                .sorted(Comparator.comparing(Alert::getTimestamp))
                .collect(Collectors.toList());

        if (lastEventId == null || lastEventId.isEmpty()) {
            return allAlerts;
        }

        // lastEventId 이후 메시지만 필터링
        int index = IntStream.range(0, allAlerts.size())
                .filter(i -> allAlerts.get(i).getId().equals(lastEventId))
                .findFirst()
                .orElse(-1);

        if (index == -1 || index == allAlerts.size() - 1) {
            return Collections.emptyList();
        }

        return allAlerts.subList(index + 1, allAlerts.size());
    }
}

