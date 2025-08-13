package com.deefacto.alim_service.commonNoti.domain.dto;

import com.deefacto.alim_service.commonNoti.domain.entity.NotiType;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationReadDTO {
    private Long notiId;
    private NotiType notiType;
    private String title;
    private String content;
    private OffsetDateTime timestamp;
    private String zoneId;
    private Boolean flagStatus;
    private Boolean readStatus;
    private OffsetDateTime readTime;
}
