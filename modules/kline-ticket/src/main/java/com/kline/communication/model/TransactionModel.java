package com.kline.communication.model;

import com.kline.communication.db.KlineEmailTransaction;
import com.kline.communication.db.KlineTransaction;

public class TransactionModel {
    private KlineEmailTransaction emailTransaction;
    private KlineTransaction klineTransaction;

    public TransactionModel() {
    }

    public TransactionModel(KlineEmailTransaction emailTransaction, KlineTransaction klineTransaction) {
        this.emailTransaction = emailTransaction;
        this.klineTransaction = klineTransaction;
    }

    public KlineEmailTransaction getEmailTransaction() {
        return emailTransaction;
    }

    public void setEmailTransaction(KlineEmailTransaction emailTransaction) {
        this.emailTransaction = emailTransaction;
    }

    public KlineTransaction getKlineTransaction() {
        return klineTransaction;
    }

    public void setKlineTransaction(KlineTransaction klineTransaction) {
        this.klineTransaction = klineTransaction;
    }
}
