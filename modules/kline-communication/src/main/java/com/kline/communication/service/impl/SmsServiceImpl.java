package com.kline.communication.service.impl;

import com.axelor.app.AppSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.gson.Gson;
import com.google.inject.persist.Transactional;
import com.kline.communication.db.KlineSmsTransaction;
import com.kline.communication.db.KlineTransaction;
import com.kline.communication.db.repo.KlineEmailTransactionRepository;
import com.kline.communication.db.repo.KlineSmsTransactionRepository;
import com.kline.communication.db.repo.KlineTransactionRepository;
import com.kline.communication.exception.KLineException;
import com.kline.communication.model.SmsRequest;
import com.kline.communication.model.SmsResponse;
import com.kline.communication.model.TransactionModel;
import com.kline.communication.service.SmsService;
import com.kline.communication.utils.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.kline.communication.constant.CommunicationConstant.ERROR.*;
import static com.kline.communication.constant.CommunicationConstant.*;

public class SmsServiceImpl implements SmsService {

    @Override
    public SmsRequest validateRequest(ActionRequest request) throws KLineException {
        String smsContent = StringUtils.toString(request.getContext().get("smsContent"));
        String smsTo = StringUtils.toString(request.getContext().get("smsTo"));
        if (org.springframework.util.StringUtils.isEmpty(smsContent)) {
            throw new KLineException(SMS_CONTENT_IS_REQUIRED);
        } else if (org.springframework.util.StringUtils.isEmpty(smsTo)) {
            throw new KLineException(SMS_MOBILE_NO_IS_REQUIRED);
        }
        String[] mobileNo = {smsTo};
        return new SmsRequest(mobileNo, smsContent);
    }

    @Override
    public SmsResponse sendSms(SmsRequest req) throws KLineException {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(AppSettings.get().get("sms.url"));
            StringEntity entity = new StringEntity(new Gson().toJson(req));
            post.setEntity(entity);
            post.setHeader("content-type", "application/json");
            post.setHeader("requestor", "CRM");
            post.setHeader("transactionId", "11111");
            post.setHeader("transactionDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
            HttpResponse response = client.execute(post);
            return new Gson().fromJson(EntityUtils.toString(response.getEntity(), "UTF-8"), SmsResponse.class);
        } catch (Exception e) {
            System.out.println("Exception while send sms " + e.getMessage());
            throw new KLineException(EXCEPTION_OCCURRED_WHILE_SENDING_SMS, e);
        }
    }

    @Override
    @Transactional
    public TransactionModel initTransaction(SmsRequest req) {
        LocalDateTime now = LocalDateTime.now();
        KlineSmsTransaction smsTransaction = initSmsTransaction(req, now);
        KlineTransaction klineTransaction = initKlineTransaction(req, smsTransaction.getId(), now);
        return new TransactionModel(smsTransaction, klineTransaction);
    }

    @Override
    @Transactional
    public void updateTransaction(TransactionModel transactionModel, String status, String statusDesc) {
        if(transactionModel == null) return;
        try {
            KlineSmsTransaction smsTransaction = transactionModel.getSmsTransaction();
            smsTransaction.setStatus(status);
            Beans.get(KlineSmsTransactionRepository.class).save(smsTransaction);

            KlineTransaction klineTransaction = transactionModel.getKlineTransaction();
            klineTransaction.setStatus(status);
            klineTransaction.setStatusDesc(statusDesc);
            Beans.get(KlineTransactionRepository.class).save(klineTransaction);
        } catch (Exception e) {
            System.out.println("Exception occurred while updateTransaction" + e.getMessage());
        }
    }

    @Override
    public void validateResult(SmsResponse smsResponse, TransactionModel transactionModel) throws KLineException {
        //case success
        if ("0".equals(smsResponse.getResultCode())) {
            updateTransaction(transactionModel, STATUS_SUCCESS, SEND_SMS_SUCCESSFULLY);
        } else {
            updateTransaction(transactionModel, STATUS_FAILED, smsResponse.getResultDesc());
            throw new KLineException(EXCEPTION_OCCURRED_WHILE_SENDING_SMS);
        }
    }

    @Override
    public void generateEmailValue(ActionRequest request, ActionResponse response) {
        String smsTranId = com.kline.communication.utils.StringUtils.toString(request.getContext().get("smsTranId"));
        KlineSmsTransactionRepository smsTransactionRepository = Beans.get(KlineSmsTransactionRepository.class);
        KlineSmsTransaction smsTransaction = smsTransactionRepository.find(Long.parseLong(smsTranId));
        response.setValue("smsTo", defaultIfNull(smsTransaction.getSmsTo()));
        response.setValue("smsMessage", defaultIfNull(smsTransaction.getMessage()));
    }

    private KlineTransaction initKlineTransaction(SmsRequest smsRequest, Long tranId, LocalDateTime now) {
        KlineTransaction transaction = new KlineTransaction();
        transaction.setType(TYPE_SMS);
        transaction.setMobileNo(smsRequest.getMobileNo()[0]);
        transaction.setStatus(STATUS_PENDING);
        transaction.setTranDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        transaction.setTranTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        transaction.setSmsTranId(String.valueOf(tranId));
        transaction.setOwner(AuthUtils.getUser().getName());
        KlineTransactionRepository klineTransactionRepository = Beans.get(KlineTransactionRepository.class);
        return klineTransactionRepository.save(transaction);
    }

    private KlineSmsTransaction initSmsTransaction(SmsRequest smsRequest, LocalDateTime now){
        KlineSmsTransaction smsTransaction = new KlineSmsTransaction();
        smsTransaction.setOwner(AuthUtils.getUser().getName());
        smsTransaction.setMessage(smsRequest.getContent());
        smsTransaction.setSmsTo(smsRequest.getMobileNo()[0]);
        smsTransaction.setDateTime(now);
        return Beans.get(KlineSmsTransactionRepository.class).save(smsTransaction);
    }

    private String defaultIfNull(String input) {
        return org.springframework.util.StringUtils.isEmpty(input) ? " - " : input;
    }
}
