package com.naling.lightstream.service;

import com.naling.lightstream.dto.HistoryItemResponse;
import com.naling.lightstream.history.HistoryItem;
import com.naling.lightstream.history.HistoryItemRepository;
import com.naling.lightstream.model.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class HistoryService {
    private final HistoryItemRepository historyItemRepository;
    private final Duration retention;

    public HistoryService(HistoryItemRepository historyItemRepository,
                          @Value("${lightstream.history.retention}") Duration retention) {
        this.historyItemRepository = historyItemRepository;
        this.retention = retention;
    }

    public void recordText(Long userId, String claimCode, String text) {
        Instant now = Instant.now();
        historyItemRepository.save(HistoryItem.text(userId, claimCode, text, now, now.plus(retention)));
    }

    public void recordFile(Long userId, String claimCode, FileMetadata metadata) {
        Instant now = Instant.now();
        historyItemRepository.save(HistoryItem.file(userId, claimCode, metadata.objectKey(), metadata.fileName(),
                metadata.contentType(), metadata.size(), now, now.plus(retention)));
    }

    public List<HistoryItemResponse> list(Long userId) {
        return historyItemRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(HistoryItemResponse::from)
                .toList();
    }
}

