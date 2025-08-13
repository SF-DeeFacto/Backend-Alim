package com.deefacto.alim_service.commonNoti.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_user_id")
    private Long id;

    @Column(name = "noti_id", nullable = false)
    private Long notiId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "read_time")
    private OffsetDateTime readTime;

    @Column(name = "read_status", nullable = false)
    private Boolean readStatus = false;

    @Column(name = "flag_status", nullable = false)
    private Boolean flagStatus = false;
}