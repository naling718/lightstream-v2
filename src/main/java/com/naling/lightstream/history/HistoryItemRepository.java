package com.naling.lightstream.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryItemRepository extends JpaRepository<HistoryItem, Long> {
    List<HistoryItem> findByUserIdOrderByCreatedAtDesc(Long userId);
}
