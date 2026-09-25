package com.ifpr.backend.realtime;

public record TransactionChangedEvent(
    WalletRealtimeEvent.Type type,
    Long walletId,
    Long transactionId
) {
}
