package com.deefacto.alim_service.commonNoti.service;

import com.deefacto.alim_service.commonNoti.domain.entity.NotificationUser;
import com.deefacto.alim_service.commonNoti.repository.NotificationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationUserService {

    private final NotificationUserRepository notificationUserRepository;

    // 알림과 대상 사용자 리스트를 받아 각각 알림-사용자 row를 생성
    public void assignNotificationToUsers(Long notificationId, List<Long> userIds) {
        List<NotificationUser> notificationUsers = userIds.stream()
                .map(userId -> NotificationUser.builder()
                        .notiId(notificationId)
                        .userId(userId)
                        .readStatus(false)
                        .flagStatus(false)
                        .build())
                .collect(Collectors.toList());

        notificationUserRepository.saveAll(notificationUsers);
    }
}
