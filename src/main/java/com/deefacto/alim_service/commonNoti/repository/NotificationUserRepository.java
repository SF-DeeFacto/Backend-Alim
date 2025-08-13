package com.deefacto.alim_service.commonNoti.repository;

import com.deefacto.alim_service.commonNoti.domain.dto.NotificationReadDTO;
import com.deefacto.alim_service.commonNoti.domain.entity.NotificationUser;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationUserRepository extends JpaRepository<NotificationUser, Long> {

    // 알림 리스트 조회
    @Query("""
    SELECT new com.deefacto.alim_service.commonNoti.domain.dto.NotificationReadDTO(
        n.notiId, n.notiType, n.title, n.content, n.timestamp, n.zoneId,
        nu.flagStatus, nu.readStatus, nu.readTime
    )
    FROM NotificationUser nu
    JOIN Notification n ON nu.notiId = n.notiId
    WHERE nu.userId = :userId
      AND (:isRead IS NULL OR nu.readStatus = :isRead)
      AND (:isFlagged IS NULL OR nu.flagStatus = :isFlagged)
    ORDER BY n.timestamp DESC
""")
    List<NotificationReadDTO> findNotificationsWithMetaByReadFlagUserId(
            @Param("userId") Long userId,
            @Param("isRead") Boolean isRead,
            @Param("isFlagged") Boolean isFlagged
    );

    // 안읽은 알림 개수 조회
    @Query("""
        SELECT new com.deefacto.alim_service.commonNoti.domain.dto.NotificationReadDTO(
            n.notiId, n.notiType, n.title, n.content, n.timestamp, n.zoneId,
            nu.flagStatus, nu.readStatus, nu.readTime
        )
        FROM NotificationUser nu
        JOIN Notification n ON nu.notiId = n.notiId
        WHERE nu.userId = :userId AND nu.readStatus = false
        ORDER BY n.timestamp DESC
    """)
    List<NotificationReadDTO> findNotificationsWithMetaByUserId(@Param("userId") Long userId);
}
