package com.ifpr.backend.controller;

import com.ifpr.backend.realtime.WalletRealtimeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/wallets/{walletId}")
public class RealtimeController {
    private final WalletRealtimeService realtimeService;

    public RealtimeController(WalletRealtimeService realtimeService) {
        this.realtimeService = realtimeService;
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable Long walletId) {
        return realtimeService.subscribe(walletId);
    }
}
