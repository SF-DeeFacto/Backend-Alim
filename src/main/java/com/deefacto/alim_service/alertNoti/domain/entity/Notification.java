package com.deefacto.alim_service.alertNoti.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id")
    private Long notiId;

    @Enumerated(EnumType.STRING)
    @Column(name = "noti_type", nullable = false, length = 20)
    private NotiType notiType;

    @Column(name = "zone_id", nullable = false, length = 100)
    private String zoneId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    public enum NotiType {
        REPORT,
        ALERT
    }
}