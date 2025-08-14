package com.deefacto.alim_service.commonNoti.service;

import com.deefacto.alim_service.common.exception.CustomException;
import com.deefacto.alim_service.common.exception.ErrorCode;
import com.deefacto.alim_service.commonNoti.domain.dto.NotificationReadDTO;
import com.deefacto.alim_service.commonNoti.repository.NotificationRepository;
import com.deefacto.alim_service.commonNoti.repository.NotificationUserRepository;
import com.deefacto.alim_service.remote.service.UserRequestProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final NotificationRepository notificationRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final UserRequestProducer userRequestProducer;

    // 알림 리스트 조회: userId, isRead, isFlagged에 따라 조회
    // (isRead, isFlagged Null 경우도 쿼리에서 자동 처리)
    public Page<NotificationReadDTO> getNotificationsForUser(Long userId, Boolean isRead, Boolean isFlagged, Pageable pageable) {
        return notificationUserRepository.findNotificationsWithMetaByReadFlagUserId(userId, isRead, isFlagged, pageable);
    }

    // 안읽은 알림 개수 조회: userId에 따라 조회
    public Integer getUnreadNotiCountForUser(Long userId) {
        return notificationUserRepository.findNotificationsWithMetaByUserId(userId).size();
    }

    // 알림 읽음 처리 (단건)
    public Integer updateReadStatus(Long userId, Long notiId) {
        // 1. 존재 여부 및 권한 확인
        notificationUserRepository.findByUserIdAndNotiId(userId, notiId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));
        // 2. 읽음 처리
        return notificationUserRepository.markNotificationAsRead(userId, notiId, OffsetDateTime.now());
    }

    // 알림 일괄 읽음 처리
    public Integer updateAllReadStatus(Long userId) {
        return notificationUserRepository.markNotificationAsRead(userId, null, OffsetDateTime.now());
    }

    // 알림 즐겨찾기/해제
    public int toggleNotificationFlag(Long userId, Long notiId) {
        int updatedRows = notificationUserRepository.toggleFlagStatus(userId, notiId);
        if (updatedRows == 0) {
            System.out.println("잘못된 요청입니다. (userId - notiId)");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        return updatedRows;
    }
}
