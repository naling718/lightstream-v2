package com.naling.lightstream.history;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "history_items")
public class HistoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShareType type;

    @Column(name = "claim_code", nullable = false)
    private String claimCode;

    @Column(name = "text_content")
    private String textContent;

    @Column(name = "object_key")
    private String objectKey;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    private Long size;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected HistoryItem() {
    }

    private HistoryItem(Long userId, ShareType type, String claimCode, Instant createdAt, Instant expiresAt) {
        this.userId = userId;
        this.type = type;
        this.claimCode = claimCode;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static HistoryItem text(Long userId, String claimCode, String text, Instant createdAt, Instant expiresAt) {
        HistoryItem item = new HistoryItem(userId, ShareType.TEXT, claimCode, createdAt, expiresAt);
        item.textContent = text;
        return item;
    }

    public static HistoryItem file(Long userId, String claimCode, String objectKey, String fileName,
                                   String contentType, long size, Instant createdAt, Instant expiresAt) {
        HistoryItem item = new HistoryItem(userId, ShareType.FILE, claimCode, createdAt, expiresAt);
        item.objectKey = objectKey;
        item.fileName = fileName;
        item.contentType = contentType;
        item.size = size;
        return item;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public ShareType getType() { return type; }
    public String getClaimCode() { return claimCode; }
    public String getTextContent() { return textContent; }
    public String getObjectKey() { return objectKey; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public Long getSize() { return size; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
}

