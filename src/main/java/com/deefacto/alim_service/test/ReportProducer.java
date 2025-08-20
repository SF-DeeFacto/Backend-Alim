package com.deefacto.alim_service.test;

import com.deefacto.alim_service.remote.dto.ReportGeneratedEvent;
import com.deefacto.alim_service.remote.dto.ReportGeneratedEvent;
import com.deefacto.alim_service.remote.dto.UserMessage;
import com.deefacto.alim_service.remote.service.ReportConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ReportProducer {

    private final KafkaTemplate<String, ReportGeneratedEvent> kafkaTemplate;

    public void requestAlimForStore() {
        System.out.println("Report 정기 리포트 생성 후, 저장 로직 시작");
        ReportGeneratedEvent message = new ReportGeneratedEvent();
        message.setZoneId("a01");
        message.setTimestamp(OffsetDateTime.now());
        try {
            kafkaTemplate.send("report.generated.v1", message).get();
            System.out.println("Kafka message sent successfully.");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            // 여기서 예외 로그를 확인하고, 원인 분석 가능
        }
    }
}
