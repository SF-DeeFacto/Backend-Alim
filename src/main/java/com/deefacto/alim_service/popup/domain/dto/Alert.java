package com.deefacto.alim_service.popup.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private String id;
    private String sensorId;
    private String zoneId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private OffsetDateTime timestamp;

    private String sensorType;
//    private String unit;
    private Double val;
}
