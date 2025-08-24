package com.deefacto.alim_service.commonNoti.controller;

import com.deefacto.alim_service.common.dto.ApiResponseDto;
import com.deefacto.alim_service.commonNoti.domain.dto.NotificationReadDTO;
import com.deefacto.alim_service.commonNoti.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/noti")
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 리스트 조회: userId, isRead, isFlagged에 따라 조회 (+ Page)
    // /noti/list?isRead=false&isFlagged=true&page=0&size=10
    @GetMapping("/list")
    public ApiResponseDto<Page<NotificationReadDTO>> getNotificationsForUser(@RequestHeader("X-Employee-Id") String employeeId,
                                               @RequestHeader("X-User-Id") Long userId,
                                                @RequestParam(required = false) Boolean isRead,
                                                @RequestParam(required = false) Boolean isFlagged,
                                                @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Page<NotificationReadDTO> notificationList = notificationService.getNotificationsForUser(userId, isRead, isFlagged, pageable);
        return ApiResponseDto.createOk(notificationList, "알림 리스트 조회 성공");
    }

    // 안읽은 알림 개수 조회: userId에 따라 조회
    // "noti_count": 21
    @GetMapping("/count")
    public ApiResponseDto<Integer> getUnreadNotiCountForUser(@RequestHeader("X-Employee-Id") String employeeId,
                                                            @RequestHeader("X-User-Id") Long userId
    ) {
        Integer notiCount = notificationService.getUnreadNotiCountForUser(userId);
        return ApiResponseDto.createOk(notiCount, "안읽은 알림 개수 조회 성공");
    }

    // 알림 읽음 처리
    @GetMapping("/read/{notiId}")
    public ApiResponseDto<Integer> updateReadStatus(@RequestHeader("X-Employee-Id") String employeeId,
                                                    @RequestHeader("X-User-Id") Long userId,
                                                    @PathVariable Long notiId
    ) {

        int update = notificationService.updateReadStatus(userId, notiId);
        return ApiResponseDto.createOk(update, "알림 읽음 처리 성공");
    }

    // 알림 일괄 읽음 처리
    @GetMapping("/read/all")
    public ApiResponseDto<Integer> updateAllReadStatus(@RequestHeader("X-Employee-Id") String employeeId,
                                              @RequestHeader("X-User-Id") Long userId
    ) {
        int update = notificationService.updateAllReadStatus(userId);
        return ApiResponseDto.createOk(update, "알림 일괄 읽음 처리 성공");
    }

    // 알림 즐겨찾기/해제
    @GetMapping("/favorite/{notiId}")
    public ApiResponseDto<Integer> toggleNotificationFlag(@RequestHeader("X-Employee-Id") String employeeId,
                                                          @RequestHeader("X-User-Id") Long userId,
                                                          @PathVariable Long notiId
    ) {
        int updated = notificationService.toggleNotificationFlag(userId, notiId);
        return ApiResponseDto.createOk(updated, "알림 즐겨찾기/해제 성공");
    }
}
