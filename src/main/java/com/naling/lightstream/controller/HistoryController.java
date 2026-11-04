package com.naling.lightstream.controller;

import com.naling.lightstream.dto.HistoryItemResponse;
import com.naling.lightstream.service.HistoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/history")
public class HistoryController {
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public List<HistoryItemResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return historyService.list(Long.valueOf(jwt.getSubject()));
    }
}
