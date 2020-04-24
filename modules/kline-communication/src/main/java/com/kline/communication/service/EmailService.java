package com.kline.communication.service;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.kline.communication.exception.KLineException;
import com.kline.communication.model.EmailRequest;
import com.kline.communication.model.TransactionModel;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import java.io.IOException;

public interface EmailService {
    EmailRequest validateRequest(ActionRequest request) throws KLineException, AddressException;

    void initEmailFrom(Message msg) throws MessagingException, KLineException;

    Session initEmailSession();

    void initEmailTo(EmailRequest req, Message msg) throws MessagingException;

    void initEmailContent(EmailRequest req, Multipart multipart) throws MessagingException;

    void initEmailSubject(EmailRequest req, Message msg) throws MessagingException;

    void initEmailAttachment(EmailRequest req, Multipart multipart, ActionRequest request) throws IOException, MessagingException;

    void removeAttachFileOnSystem(EmailRequest req);

    TransactionModel initTransaction(EmailRequest req);

    void updateTransaction(TransactionModel transactionModel, String status, String statusDesc);

    void generateEmailValue(ActionRequest request, ActionResponse response);
}
