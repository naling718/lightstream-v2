package com.naling.lightstream.dto;

import com.naling.lightstream.history.HistoryItem;
import com.naling.lightstream.history.ShareType;

import java.time.Instant;

public record HistoryItemResponse(
        Long id,
        ShareType type,
        String claimCode,
        String text,
        String fileName,
        Long size,
        Instant createdAt,
        Instant expiresAt) {

    public static HistoryItemResponse from(HistoryItem item) {
        return new HistoryItemResponse(item.getId(), item.getType(), item.getClaimCode(), item.getTextContent(),
                item.getFileName(), item.getSize(), item.getCreatedAt(), item.getExpiresAt());
    }
}
