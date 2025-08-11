package com.deefacto.alim_service.common.config;

import com.deefacto.alim_service.alertNoti.service.SqsPollingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {

    private final SqsPollingService sqsPollingService;

    @Override
    public void run(String... args) {
        sqsPollingService.flushQueue();
    }
}

