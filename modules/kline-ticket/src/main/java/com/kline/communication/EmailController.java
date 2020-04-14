package com.kline.communication;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.kline.communication.db.KlineEmailTemplate;
import com.kline.communication.exception.KLineException;
import com.kline.communication.model.EmailRequest;
import com.kline.communication.model.TransactionModel;
import com.kline.communication.service.EmailService;
import org.springframework.util.StringUtils;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static com.kline.communication.constant.CommunicationConstant.ERROR.EXCEPTION_OCCURRED_WHILE_SENDING_EMAIL;
import static com.kline.communication.constant.CommunicationConstant.ERROR.PLEASE_SET_EMAIL_ADDRESS_IN_YOUR_PROFILE;
import static com.kline.communication.constant.CommunicationConstant.*;

public class EmailController {

    @Inject
    private EmailService emailService;

    public void onLoad(ActionRequest request, ActionResponse response) {
        User user = AuthUtils.getUser();
        if (StringUtils.isEmpty(user.getEmail())) {
            response.setError(PLEASE_SET_EMAIL_ADDRESS_IN_YOUR_PROFILE.getMessage());
        }
        response.setValue("emailFrom", user.getEmail());
    }

    public void send(ActionRequest request, ActionResponse response) {
        TransactionModel transactionModel = null;
        try {
            Session session = emailService.initEmailSession();
            Message msg = new MimeMessage(session);
            Multipart multipart = new MimeMultipart();
            emailService.initEmailFrom(msg);

            EmailRequest req = emailService.validateRequest(request);
            System.out.println(req);
            transactionModel = emailService.initTransaction(req);
            System.out.println("start business---");
            emailService.initEmailTo(req, msg);
            emailService.initEmailSubject(req, msg);
            emailService.initEmailContent(req, multipart);
            emailService.initEmailAttachment(req, multipart);
            msg.setContent(multipart);

            // sends the e-mail
            Transport.send(msg);
            //emailService.removeAttachFileOnSystem(req);
            emailService.updateTransaction(transactionModel, STATUS_SUCCESS, null);
            response.setNotify(SEND_EMAIL_SUCCESSFULLY);
            clear(request, response);
        } catch (KLineException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            System.out.println("exception occurred..." + e.getMessage());
            emailService.updateTransaction(transactionModel, STATUS_FAILED, e.getMessage());
            response.setError(EXCEPTION_OCCURRED_WHILE_SENDING_EMAIL.getMessage());
        }
    }

    public void clear(ActionRequest request, ActionResponse response) {
        response.setValue("klineEmailTemplate", null);
        response.setValue("emailTo", null);
        response.setValue("emailCC", null);
        response.setValue("emailBCC", null);
        response.setValue("emailSubject", null);
        response.setValue("emailContent", null);
        response.setValue("enableAttachment", false);
        response.setValue("klineEmailAttachment", null);
    }

    public void template(ActionRequest request, ActionResponse response){
        response.setView(
                ActionView.define(I18n.get("Email Template"))
                        .model("com.kline.communication.db.KlineEmailTemplate")
                        .add("grid", "communication-email-template-grid")
                        .add("form", "communication-email-template-form")
                        .map());
    }

    public void getTemplateDetail(ActionRequest request, ActionResponse response){
        KlineEmailTemplate emailTemplate = (KlineEmailTemplate) request.getContext().get("klineEmailTemplate");
        if (emailTemplate != null) {
            response.setValue("emailCC", emailTemplate.getToCC());
            response.setValue("emailBCC", emailTemplate.getToBCC());
            response.setValue("emailSubject", emailTemplate.getSubject());
            response.setValue("emailContent", emailTemplate.getBody());
        }
    }

    public void showEmailDetail(ActionRequest request, ActionResponse response){
        String emailTranId = com.kline.communication.utils.StringUtils.toString(request.getContext().get("id"));
//        System.out.println("email detail id is " + emailTranId);




        response.setValue("emailBody", "test");

    }
}
