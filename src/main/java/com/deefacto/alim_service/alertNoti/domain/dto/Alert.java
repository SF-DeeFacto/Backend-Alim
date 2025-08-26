package com.deefacto.alim_service.alertNoti.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private String id;
    @JsonProperty("sensor_id")
    private String sensorId;
    @JsonProperty("zone_id")
    private String zoneId;
    // 🔥 Local SQS 사용 시
    // @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private OffsetDateTime timestamp;
    @JsonProperty("sensor_type")
    private String sensorType;
    private String unit;
    private Double val;
}
