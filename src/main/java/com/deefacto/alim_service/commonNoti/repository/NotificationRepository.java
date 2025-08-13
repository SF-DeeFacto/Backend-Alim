package com.deefacto.alim_service.commonNoti.repository;

import com.deefacto.alim_service.commonNoti.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
