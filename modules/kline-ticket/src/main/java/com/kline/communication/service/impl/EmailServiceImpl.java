package com.kline.communication.service.impl;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.google.inject.persist.Transactional;
import com.kline.communication.db.KlineEmailAttachment;
import com.kline.communication.db.KlineEmailTransaction;
import com.kline.communication.db.KlineTransaction;
import com.kline.communication.db.repo.KlineEmailTransactionRepository;
import com.kline.communication.db.repo.KlineTransactionRepository;
import com.kline.communication.exception.KLineException;
import com.kline.communication.model.EmailRequest;
import com.kline.communication.model.TransactionModel;
import com.kline.communication.service.EmailService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

import static com.kline.communication.constant.CommunicationConstant.ERROR.*;
import static com.kline.communication.constant.CommunicationConstant.*;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

public class EmailServiceImpl implements EmailService {

    @Override
    public EmailRequest validateRequest(ActionRequest request) throws KLineException {
        EmailRequest req = new EmailRequest(request);
        if (StringUtils.isEmpty(req.getTo())) {
            throw new KLineException(EMAIL_TO_IS_REQUIRED);
        }
        if (StringUtils.isEmpty(req.getSubject())) {
            throw new KLineException(EMAIL_SUBJECT_IS_REQUIRED);
        }
        if(StringUtils.isEmpty(req.getContent())){
            throw new KLineException(EMAIL_CONTENT_IS_REQUIRED);
        }
        if(req.getContent().contains(TAG_IMAGE)){
            throw new KLineException(EMAIL_BODY_IS_INVALID);
        }
        return req;
    }

    @Override
    public void initEmailFrom(Message msg) throws MessagingException, KLineException {
        User user = AuthUtils.getUser();
        if (StringUtils.isEmpty(user.getEmail())) {
            throw new KLineException(PLEASE_SET_EMAIL_ADDRESS_IN_YOUR_PROFILE);
        }
        msg.setFrom(new InternetAddress(user.getEmail()));
    }

    @Override
    public Session initEmailSession(){
        Properties properties = initProperties();
        Authenticator auth = initAuthenticator();
        return Session.getInstance(properties, auth);
    }

    @Override
    public void initEmailTo(EmailRequest request, Message msg) throws MessagingException {
        msg.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(request.getTo())});
        if (!StringUtils.isEmpty(request.getToCC())) {
            msg.addRecipients(Message.RecipientType.CC, new InternetAddress[]{new InternetAddress(request.getToCC())});
        }
        if (!StringUtils.isEmpty(request.getToBCC())) {
            msg.addRecipients(Message.RecipientType.BCC, new InternetAddress[]{new InternetAddress(request.getToBCC())});
        }
    }

    @Override
    public void initEmailContent(EmailRequest req, Multipart multipart) throws MessagingException {
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(req.getContent(), TEXT_HTML);
        multipart.addBodyPart(bodyPart);
    }

    @Override
    public void initEmailSubject(EmailRequest req, Message msg) throws MessagingException {
        msg.setSubject(req.getSubject());
        msg.setSentDate(new Date());
    }

    @Override
    public void initEmailAttachment(EmailRequest req, Multipart multipart) throws IOException, MessagingException {
        if(!CollectionUtils.isEmpty(req.getAttachments()) && req.isAttachment()){
            req.setContainAttachment(true);
            for (KlineEmailAttachment e : req.getAttachments()) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(MetaFiles.getPath(e.getMetaFile()).toString());
                multipart.addBodyPart(attachPart);
            }
        }
    }

    @Override
    @Transactional
    public void removeAttachFileOnSystem(EmailRequest req) {
        if (req.isContainAttachment()) {
            for (KlineEmailAttachment e : req.getAttachments()) {
                System.out.println("File is " + e.getMetaFile().getId());
                String file = MetaFiles.getPath(e.getMetaFile()).toString();
                try {
                    Path path = Paths.get(file);
                    boolean isRemoved = Files.deleteIfExists(path);
                    System.out.println("Removed file status | " + isRemoved +" | on " + file);

                    //remove from db
                    MetaFileRepository metaFileRepository = Beans.get(MetaFileRepository.class);
                    MetaFile metaFile = metaFileRepository.find(e.getMetaFile().getId());
                    metaFileRepository.remove(metaFile);
                } catch (Exception err) {
                    //need to store somewhere to remove after sometime
                    System.out.println(err.getMessage());
                    System.out.println("Exception occurred while remove file " + file);
                }
            }
        }
    }

    @Override
    @Transactional
    public TransactionModel initTransaction(EmailRequest req) {
        try{
            LocalDateTime now = LocalDateTime.now();
            KlineEmailTransaction emailTransaction = initKlineEmailTransaction(req, now);
            KlineTransaction klineTransaction = initTransaction(req, emailTransaction.getId(), now);
            return new TransactionModel(emailTransaction, klineTransaction);
        } catch (Exception e) {
            System.out.println("Exception occurred while initTransaction" + e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional
    public void updateTransaction(TransactionModel transactionModel, String status, String statusDesc) {
        if(transactionModel == null) return;
        try {
            KlineEmailTransaction emailTransaction = transactionModel.getEmailTransaction();
            emailTransaction.setStatus(status);
            Beans.get(KlineEmailTransactionRepository.class).save(emailTransaction);

            KlineTransaction klineTransaction = transactionModel.getKlineTransaction();
            klineTransaction.setStatus(status);
            klineTransaction.setStatusDesc(statusDesc);
            Beans.get(KlineTransactionRepository.class).save(klineTransaction);
        } catch (Exception e) {
            System.out.println("Exception occurred while updateTransaction" + e.getMessage());
        }
    }

    private KlineTransaction initTransaction(EmailRequest emailRequest, Long tranId, LocalDateTime now) {
        User user = AuthUtils.getUser();
        KlineTransaction transaction = new KlineTransaction();
        transaction.setType(TYPE_EMAIL);
        transaction.setEmail(emailRequest.getTo());
        transaction.setStatus(STATUS_PENDING);
        transaction.setTranDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        transaction.setTranTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        transaction.setEmailTranId(String.valueOf(tranId));
        transaction.setOwner(user.getName());
        KlineTransactionRepository klineTransactionRepository = Beans.get(KlineTransactionRepository.class);
        return klineTransactionRepository.save(transaction);
    }

    private KlineEmailTransaction initKlineEmailTransaction(EmailRequest emailRequest, LocalDateTime now){
        KlineEmailTransaction emailTransaction = new KlineEmailTransaction();
        emailTransaction.setEmailFrom(AuthUtils.getUser().getName());
        emailTransaction.setDateTime(now);
        emailTransaction.setEmailTo(emailRequest.getTo());
        emailTransaction.setEmailCc(emailRequest.getToCC());
        emailTransaction.setEmailBcc(emailRequest.getToBCC());
        emailTransaction.setEmailSubject(emailRequest.getSubject());
        emailTransaction.setEmailBody(emailRequest.getContent());
        emailTransaction.setKlineEmailAttachment(emailRequest.getAttachments());
        emailTransaction.setStatus(STATUS_PENDING);
        KlineEmailTransactionRepository metaFileRepository = Beans.get(KlineEmailTransactionRepository.class);
        return metaFileRepository.save(emailTransaction);
    }

    private Properties initProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.sendgrid.net");
        properties.put("mail.smtp.port", 25);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.user", "apikey");
        properties.put("mail.password", "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");
        return properties;
    }

    private Authenticator initAuthenticator() {
        return new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("apikey", "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");
            }
        };
    }
}
