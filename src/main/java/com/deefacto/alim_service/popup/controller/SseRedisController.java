package com.deefacto.alim_service.popup.controller;

import com.deefacto.alim_service.popup.service.RedisSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse/redis")
public class SseRedisController {

    private final RedisSseService sseService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestHeader("X-Employee-Id") String userId,
                                @RequestHeader("X-Role") String userRole,
                                @RequestHeader("X-Shift") String userShift) {
        return sseService.subscribe(userId, userRole, userShift);
    }
}

