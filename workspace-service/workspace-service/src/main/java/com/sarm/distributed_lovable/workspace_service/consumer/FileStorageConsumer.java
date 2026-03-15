package com.sarm.distributed_lovable.workspace_service.consumer;

import com.sarm.distributed_lovable.common_lib.events.FileStoreRequestEvent;
import com.sarm.distributed_lovable.common_lib.events.FileStoreResponseEvent;
import com.sarm.distributed_lovable.workspace_service.entity.ProcessedEvent;
import com.sarm.distributed_lovable.workspace_service.repository.ProcessedEventRepository;
import com.sarm.distributed_lovable.workspace_service.service.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageConsumer {

    private final FileService fileService;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @KafkaListener(topics = "file-storage-request-event", groupId = "workspace-group")
    public void consumeFileEvent(FileStoreRequestEvent requestEvent) {

        // Idempotency check
        if (processedEventRepository.existsById(requestEvent.sagaId())) {
            log.info("Duplicate Saga detected: {}. Resending previous ACK.", requestEvent.sagaId());
            sendResponse(requestEvent, true, null);
            return;
        }

        try {
            log.info("Saving file: {}", requestEvent.filePath());

            fileService.saveFile(requestEvent.projectId(), requestEvent.filePath(), requestEvent.content());
            processedEventRepository.save(new ProcessedEvent(
                    requestEvent.sagaId(), LocalDateTime.now()
            ));

            sendResponse(requestEvent, true, null);
        } catch (Exception e) {
            log.error("Error saving file: {}", e.getMessage());
            sendResponse(requestEvent, false, e.getMessage());
        }

    }

    private void sendResponse(FileStoreRequestEvent req, boolean success, String error) {
        FileStoreResponseEvent response = FileStoreResponseEvent.builder()
                .sagaId(req.sagaId())
                .projectId(req.projectId())
                .success(success)
                .errorMessage(error)
                .build();
        kafkaTemplate.send("file-store-responses", response);
    }
}
