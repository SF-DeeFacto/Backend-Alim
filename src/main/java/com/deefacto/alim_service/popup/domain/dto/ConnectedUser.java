package com.deefacto.alim_service.popup.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectedUser {
    private String userId;
    private Set<String> zoneIds;
    private String userShift;
}


