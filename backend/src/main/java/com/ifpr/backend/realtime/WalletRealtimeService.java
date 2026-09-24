package com.ifpr.backend.realtime;

import com.ifpr.backend.service.WalletAuthorizationService;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class WalletRealtimeService {
    private static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L;

    private final WalletAuthorizationService walletAuthorizationService;
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    public WalletRealtimeService(WalletAuthorizationService walletAuthorizationService) {
        this.walletAuthorizationService = walletAuthorizationService;
    }

    public SseEmitter subscribe(Long walletId) {
        walletAuthorizationService.requireMember(walletId);

        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        subscribers.computeIfAbsent(walletId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> remove(walletId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ignored -> cleanup.run());

        try {
            emitter.send(SseEmitter.event()
                .name("wallet-update")
                .data(WalletRealtimeEvent.connected(walletId)));
        } catch (IOException | IllegalStateException ex) {
            cleanup.run();
            emitter.completeWithError(ex);
        }

        return emitter;
    }

    public void broadcast(WalletRealtimeEvent event) {
        List<SseEmitter> emitters = subscribers.getOrDefault(event.walletId(), new CopyOnWriteArrayList<>());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("wallet-update")
                    .data(event));
            } catch (IOException | IllegalStateException ex) {
                remove(event.walletId(), emitter);
                emitter.complete();
            }
        }
    }

    private void remove(Long walletId, SseEmitter emitter) {
        subscribers.computeIfPresent(walletId, (ignored, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
