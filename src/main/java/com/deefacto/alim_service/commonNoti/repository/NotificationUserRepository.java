package com.deefacto.alim_service.commonNoti.repository;

import com.deefacto.alim_service.commonNoti.domain.entity.NotificationUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationUserRepository extends JpaRepository<NotificationUser, Long> {
}
