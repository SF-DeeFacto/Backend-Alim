package com.deefacto.alim_service.alertNoti.service;

import com.deefacto.alim_service.alertNoti.domain.dto.Alert;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsPollingService {

    private final SqsClient sqsClient;
    private final SseService sseService;
    private final ObjectMapper objectMapper;
    private final AlertRedisService alertRedisService;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    // 서버 시작 시 호출할 큐 비우기 메서드 예시
    public void flushQueue() {
        log.info("Starting to flush SQS queue...");

        while (true) {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(1)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

            if (messages.isEmpty()) {
                log.info("SQS queue is now empty.");
                break;
            }

            for (Message msg : messages) {
                DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(msg.receiptHandle())
                        .build();
                sqsClient.deleteMessage(deleteRequest);
                log.info("Deleted message with ID: {}", msg.messageId());
            }
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void pollMessages() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();

        try {
            for (Message message : messages) {
                try {
                    Alert alert = objectMapper.readValue(message.body(), Alert.class);

                    // UUID 없는 경우 생성
                    if (alert.getId() == null || alert.getId().isEmpty()) {
                        alert.setId(UUID.randomUUID().toString());
                    }

                    log.info("!!!!!!!!!!!!!!!!Received SQS message: {}", alert);

                    // Redis에 저장
                    alertRedisService.saveAlert(alert);

                    // SSE 구독자에게 전송
                    sseService.sendAlert(alert);

                    // 메시지 삭제
                    DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build();
                    sqsClient.deleteMessage(deleteRequest);

                } catch (Exception e) {
                    log.error("Failed to process SQS message: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("SQS polling failed: {}", e.getMessage(), e);
        }
    }
}

