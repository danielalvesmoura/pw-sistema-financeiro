package com.ifpr.backend.realtime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransactionRealtimeListener {
    private final WalletRealtimeService realtimeService;

    public TransactionRealtimeListener(WalletRealtimeService realtimeService) {
        this.realtimeService = realtimeService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionChanged(TransactionChangedEvent event) {
        realtimeService.broadcast(WalletRealtimeEvent.transactionChanged(
            event.type(),
            event.walletId(),
            event.transactionId()
        ));
    }
}
