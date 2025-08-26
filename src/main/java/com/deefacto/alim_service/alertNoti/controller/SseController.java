package com.deefacto.alim_service.alertNoti.controller;

import com.deefacto.alim_service.alertNoti.domain.dto.UserCacheDto;
import com.deefacto.alim_service.alertNoti.service.SseService;
import com.deefacto.alim_service.alertNoti.service.UserRedisService;
import com.deefacto.alim_service.common.exception.CustomException;
import com.deefacto.alim_service.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/noti/sse")
public class SseController {

    private final SseService sseService;
    private final UserRedisService userRedisService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestHeader("X-Employee-Id") String employeeId,
                                @RequestHeader("X-User-Id") Long userId,
                                @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        // API Gateway에서 전달받은 헤더 검증
        if (userId == null || employeeId == null || employeeId.isEmpty()) {
            log.warn("[팝업 알림 구독]: 잘못된 파라미터 userId: {}, employeeId: {}", userId, employeeId);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "X-User-Id, X-Employee-Id header is required");
        }
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        return sseService.subscribe(employeeId, userInfo.getScope(), userInfo.getShift(), lastEventId);
    }

    @GetMapping(value = "/test")
    public String test() {
        return "test";
    }
}

