package com.deefacto.alim_service.alertNoti.controller;

import com.deefacto.alim_service.alertNoti.domain.dto.UserCacheDto;
import com.deefacto.alim_service.alertNoti.service.SseService;
import com.deefacto.alim_service.alertNoti.service.UserRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        return sseService.subscribe(employeeId, userInfo.getScope(), userInfo.getShift(), lastEventId);
    }
}

