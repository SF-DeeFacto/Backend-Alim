package com.deefacto.alim_service.alertNoti.controller;

import com.deefacto.alim_service.alertNoti.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestHeader("X-Employee-Id") String employeeId,
                                @RequestHeader("X-User_Id") Long userId,
                                @RequestHeader("X-Role") String userRole,
                                @RequestHeader("X-Shift") String userShift,
                                @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        return sseService.subscribe(employeeId, userRole, userShift, lastEventId);
    }
}

