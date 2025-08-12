package com.deefacto.alim_service.popup.config;

import com.deefacto.alim_service.popup.domain.dto.RedisAlert;
import com.deefacto.alim_service.popup.service.RedisSseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisAlertListener {

    private final RedisSseService sseService;
    private final ObjectMapper objectMapper;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener((message, pattern) -> {
            try {
                String alertJson = new String(message.getBody());
                RedisAlert alert = objectMapper.readValue(alertJson, RedisAlert.class);
                sseService.sendAlert(alert);
            } catch (Exception e) {
                // 로그 등 처리
            }
        }, new ChannelTopic("alert_channel"));
        return container;
    }
}
