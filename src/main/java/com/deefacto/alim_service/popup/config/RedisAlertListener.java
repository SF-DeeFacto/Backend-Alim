package com.deefacto.alim_service.popup.config;

import com.deefacto.alim_service.popup.domain.dto.Alert;
import com.deefacto.alim_service.popup.service.SseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisAlertListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final SseService sseService;
    private final ObjectMapper objectMapper;

    private static final String ALERT_QUEUE = "alert";

    @PostConstruct
    public void startListener() {
        Thread thread = new Thread(() -> {
            System.out.println("Listening Redis alerts...");
            while (true) {
                try {
                    // 블로킹 pop (timeout: 0은 무한 대기)
//                    List<String> result = redisTemplate.opsForList().leftPop(ALERT_QUEUE, Duration.ofSeconds(0));
                    // TODO: Duration 고민 필요
                    String alertJson = redisTemplate.opsForList().leftPop(ALERT_QUEUE, Duration.ofSeconds(0));
                    if (alertJson == null) return;

                    Alert alert = objectMapper.readValue(alertJson, Alert.class);
                    System.out.println("RedisAlertListener received: " + alertJson);
                    sseService.sendAlert(alert);

                } catch (Exception e) {
                    System.err.println("Redis listener error: " + e.getMessage());
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}

