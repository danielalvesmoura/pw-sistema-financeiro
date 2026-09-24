package com.ifpr.backend.realtime;

import java.time.Instant;

public record WalletRealtimeEvent(
    Type type,
    Long walletId,
    Long transactionId,
    Instant occurredAt
) {
    public enum Type {
        CONNECTED,
        TRANSACTION_CREATED,
        TRANSACTION_UPDATED,
        TRANSACTION_DELETED
    }

    public static WalletRealtimeEvent connected(Long walletId) {
        return new WalletRealtimeEvent(Type.CONNECTED, walletId, null, Instant.now());
    }

    public static WalletRealtimeEvent transactionChanged(Type type, Long walletId, Long transactionId) {
        return new WalletRealtimeEvent(type, walletId, transactionId, Instant.now());
    }
}
