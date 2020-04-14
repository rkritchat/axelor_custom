package com.kline.communication;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.kline.communication.service.EmailService;
import com.kline.communication.service.SmsService;

import static com.kline.communication.constant.CommunicationConstant.TYPE_EMAIL;

public class CommonController {

    @Inject
    private EmailService emailService;

    @Inject
    private SmsService smsService;

    public void showTransactionDetail(ActionRequest request, ActionResponse response){
        String type = com.kline.communication.utils.StringUtils.toString(request.getContext().get("type"));
        if (TYPE_EMAIL.equals(type)) {
            emailService.generateEmailValue(request, response);
        }else{
            smsService.generateEmailValue(request, response);
        }
    }
}

