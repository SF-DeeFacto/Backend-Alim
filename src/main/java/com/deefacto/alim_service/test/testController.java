package com.deefacto.alim_service.test;

import com.deefacto.alim_service.common.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class testController {

    private final ReportProducer reportProducer;

    @GetMapping("/noti/test")
    public ApiResponseDto<String> reportKafkaTest() {
        reportProducer.requestAlimForStore();
        return ApiResponseDto.createOk("test success");
    }
}
