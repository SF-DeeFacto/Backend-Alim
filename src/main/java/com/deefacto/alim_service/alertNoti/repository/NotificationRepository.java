package com.deefacto.alim_service.alertNoti.repository;

import com.deefacto.alim_service.alertNoti.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
