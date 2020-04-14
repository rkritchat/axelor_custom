package com.kline.communication;

import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.kline.communication.db.KlineSmsTemplate;
import com.kline.communication.exception.KLineException;
import com.kline.communication.model.SmsRequest;
import com.kline.communication.model.SmsResponse;
import com.kline.communication.model.TransactionModel;
import com.kline.communication.service.SmsService;

import static com.kline.communication.constant.CommunicationConstant.*;
import static com.kline.communication.constant.CommunicationConstant.ERROR.EXCEPTION_OCCURRED_WHILE_SENDING_EMAIL;
import static com.kline.communication.constant.CommunicationConstant.ERROR.EXCEPTION_OCCURRED_WHILE_SENDING_SMS;

public class SmsController {

    @Inject
    private SmsService smsService;

    public void sendSms(ActionRequest request, ActionResponse response) {
        TransactionModel transactionModel = null;
        try {
            SmsRequest req = smsService.validateRequest(request);
            transactionModel = smsService.initTransaction(req);
            SmsResponse smsResponse = smsService.sendSms(req);
            System.out.println("res" + smsResponse);
            smsService.validateResult(smsResponse, transactionModel);
            response.setNotify(SEND_SMS_SUCCESSFULLY);
            clear(request, response);
        } catch (KLineException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            System.out.println("exception occurred..." + e.getMessage());
            smsService.updateTransaction(transactionModel, STATUS_FAILED, e.getMessage());
            response.setError(EXCEPTION_OCCURRED_WHILE_SENDING_SMS.getMessage());
        }
    }

    public void clear(ActionRequest request, ActionResponse response){
        response.setValue("klineSmsTemplate",null);
        response.setValue("smsContent",null);
        response.setValue("smsTo",null);
    }

    public void template(ActionRequest request, ActionResponse response){
        response.setView(
                ActionView.define(I18n.get("SMS Template"))
                        .model("com.kline.communication.db.KlineSmsTemplate")
                        .add("grid", "communication-template-grid")
                        .add("form", "communication-template-form")
                        .map());
    }

    public void getSmsTemplateDetail(ActionRequest request, ActionResponse response){
        KlineSmsTemplate smsTemplate = (KlineSmsTemplate) request.getContext().get("klineSmsTemplate");
        System.out.println(smsTemplate);
        if (smsTemplate != null) {
            response.setValue("smsContent", smsTemplate.getContent());
        }
    }
}
