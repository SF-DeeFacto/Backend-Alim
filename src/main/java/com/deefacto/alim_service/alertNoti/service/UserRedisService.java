package com.deefacto.alim_service.alertNoti.service;

import com.deefacto.alim_service.alertNoti.domain.dto.UserCacheDto;
import com.deefacto.alim_service.common.exception.CustomException;
import com.deefacto.alim_service.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRedisService {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "user:";

    public UserCacheDto getUserInfo(String employeeId) {
        String key = KEY_PREFIX + employeeId;
        // 캐스팅 없이 바로 UserCacheDto 타입으로 반환됩니다.
        Object obj = redisTemplate.opsForValue().get(key);
        UserCacheDto userInfo = objectMapper.convertValue(obj, UserCacheDto.class);
        if(userInfo == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND_IN_TOKEN, "토큰에 해당하는 사용자가 메모리에 존재하지 않습니다.");
        }
        return userInfo;
    }

}
