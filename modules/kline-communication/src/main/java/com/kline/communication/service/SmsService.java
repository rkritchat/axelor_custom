package com.kline.communication.service;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.kline.communication.exception.KLineException;
import com.kline.communication.model.SmsRequest;
import com.kline.communication.model.SmsResponse;
import com.kline.communication.model.TransactionModel;

public interface SmsService {
    SmsRequest validateRequest(ActionRequest request) throws KLineException;
    SmsResponse sendSms(SmsRequest req) throws KLineException;
    TransactionModel initTransaction(SmsRequest req);
    void updateTransaction(TransactionModel transactionModel, String status, String statusDesc);
    void validateResult(SmsResponse smsResponse, TransactionModel transactionModel) throws KLineException;
    void generateEmailValue(ActionRequest request, ActionResponse response);
}
