package com.deefacto.alim_service.commonNoti.controller;

import com.deefacto.alim_service.common.dto.ApiResponseDto;
import com.deefacto.alim_service.common.exception.CustomException;
import com.deefacto.alim_service.common.exception.ErrorCode;
import com.deefacto.alim_service.commonNoti.domain.dto.NotificationReadDTO;
import com.deefacto.alim_service.commonNoti.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
        // API Gateway에서 전달받은 헤더 검증
        if (userId == null || employeeId == null || employeeId.isEmpty()) {
            log.warn("[알림 리스트 조회]: 잘못된 파라미터 userId: {}, employeeId: {}", userId, employeeId);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "X-User-Id, X-Employee-Id header is required");
        }
        Page<NotificationReadDTO> notificationList = notificationService.getNotificationsForUser(userId, isRead, isFlagged, pageable);
        return ApiResponseDto.createOk(notificationList, "알림 리스트 조회 성공");
    }

    // 안읽은 알림 개수 조회: userId에 따라 조회
    // "noti_count": 21
    @GetMapping("/count")
    public ApiResponseDto<Integer> getUnreadNotiCountForUser(@RequestHeader("X-Employee-Id") String employeeId,
                                                            @RequestHeader("X-User-Id") Long userId
    ) {
        // API Gateway에서 전달받은 헤더 검증
        if (userId == null || employeeId == null || employeeId.isEmpty()) {
            log.warn("[안읽은 알림 개수 조회]: 잘못된 파라미터 userId: {}, employeeId: {}", userId, employeeId);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "X-User-Id, X-Employee-Id header is required");
        }
        Integer notiCount = notificationService.getUnreadNotiCountForUser(userId);
        return ApiResponseDto.createOk(notiCount, "안읽은 알림 개수 조회 성공");
    }

    // 알림 읽음 처리
    @PostMapping("/read/{notiId}")
    public ApiResponseDto<Integer> updateReadStatus(@RequestHeader("X-Employee-Id") String employeeId,
                                                    @RequestHeader("X-User-Id") Long userId,
                                                    @PathVariable Long notiId
    ) {
        // API Gateway에서 전달받은 헤더 검증
        if (userId == null || employeeId == null || employeeId.isEmpty()) {
            log.warn("[알림 읽음 처리]: 잘못된 파라미터 userId: {}, employeeId: {}", userId, employeeId);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "X-User-Id, X-Employee-Id header is required");
        }
        int update = notificationService.updateReadStatus(userId, notiId);
        return ApiResponseDto.createOk(update, "알림 읽음 처리 성공");
    }

    // 알림 일괄 읽음 처리
    @PostMapping("/read/all")
    public ApiResponseDto<Integer> updateAllReadStatus(@RequestHeader("X-Employee-Id") String employeeId,
                                              @RequestHeader("X-User-Id") Long userId
    ) {
        // API Gateway에서 전달받은 헤더 검증
        if (userId == null || employeeId == null || employeeId.isEmpty()) {
            log.warn("[일괄 알림 읽음 처리]: 잘못된 파라미터 userId: {}, employeeId: {}", userId, employeeId);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "X-User-Id, X-Employee-Id header is required");
        }
        int update = notificationService.updateAllReadStatus(userId);
        return ApiResponseDto.createOk(update, "알림 일괄 읽음 처리 성공");
    }

    // 알림 즐겨찾기/해제
    @PostMapping("/favorite/{notiId}")
    public ApiResponseDto<Integer> toggleNotificationFlag(@RequestHeader("X-Employee-Id") String employeeId,
                                                          @RequestHeader("X-User-Id") Long userId,
                                                          @PathVariable Long notiId
    ) {
        // API Gateway에서 전달받은 헤더 검증
        if (userId == null || employeeId == null || employeeId.isEmpty()) {
            log.warn("[알림 즐겨찾기/해제]: 잘못된 파라미터 userId: {}, employeeId: {}", userId, employeeId);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "X-User-Id, X-Employee-Id header is required");
        }
        int updated = notificationService.toggleNotificationFlag(userId, notiId);
        return ApiResponseDto.createOk(updated, "알림 즐겨찾기/해제 성공");
    }
}
