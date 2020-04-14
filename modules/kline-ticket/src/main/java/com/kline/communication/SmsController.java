package com.kline.communication;

import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SmsController {

    public void sendSms(ActionRequest request, ActionResponse response){
        String textMessage = String.valueOf(request.getContext().get("template"));
        String to = String.valueOf(request.getContext().get("to"));
        String to2 = String.valueOf(request.getContext().get("to2"));
        boolean isManual = Boolean.parseBoolean(String.valueOf(request.getContext().get("freeText")));
        //response.setNotify("this is notify");

        System.out.println("isManual : "+ isManual);
        System.out.println("template: "+ textMessage);
        System.out.println("to: "+ to);
        System.out.println("to2: "+ to2);
    }

    public void clear(ActionRequest request, ActionResponse response){
        //response.setError("This is error");
        response.setValue("template","");
        response.setValue("to","");
        response.setValue("to2","");

    }

    public void template(ActionRequest request, ActionResponse response){
        response.setView(
                ActionView.define(I18n.get("SMS Template"))
                        .model("com.kline.communication.db.KlineSmsTemplate")
                        .add("grid", "communication-template-grid")
                        .add("form", "communication-template-form")
                        .map());
    }
}
