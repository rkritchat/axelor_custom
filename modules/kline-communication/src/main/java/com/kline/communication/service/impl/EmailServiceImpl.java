package com.kline.communication.service.impl;

import com.axelor.app.AppSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;
import com.kline.communication.db.KlineEmailAttachment;
import com.kline.communication.db.KlineEmailAttachmentTransaction;
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
import java.util.*;
import java.util.regex.Pattern;

import static com.kline.communication.constant.CommunicationConstant.*;
import static com.kline.communication.constant.CommunicationConstant.ERROR.*;
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
        if(!Pattern.matches(EMAIL_PATTERN, req.getTo())){
            throw new KLineException(INVALID_EMAIL_TO);
        }
        if(!StringUtils.isEmpty(req.getToCC()) && !Pattern.matches(EMAIL_PATTERN, req.getToCC())){
            throw new KLineException(INVALID_EMAIL_CC);
        }
        if(!StringUtils.isEmpty(req.getToBCC()) && !Pattern.matches(EMAIL_PATTERN, req.getToBCC())){
            throw new KLineException(INVALID_EMAIL_BCC);
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
    public void initEmailAttachment(EmailRequest req, Multipart multipart, ActionRequest request) throws IOException, MessagingException {
        if(!CollectionUtils.isEmpty(req.getAttachments()) && req.isAttachment()){
            req.setContainAttachment(true);
            //The framework cannot save meta, then go this way to sending email with attachment
            List<KlineEmailAttachment> reqAttach = (List<KlineEmailAttachment>) request.getContext().get("klineEmailAttachment");
            for (KlineEmailAttachment e : reqAttach) {
                System.out.println(MetaFiles.getPath(e.getMetaFile()).toString());
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(MetaFiles.getPath(e.getMetaFile()).toString());
                multipart.addBodyPart(attachPart);
            }
        }
    }

    private  List<KlineEmailAttachment> initMetaFileId(EmailRequest req){
        if(!CollectionUtils.isEmpty(req.getAttachments()) && req.isAttachment()) {
            List<KlineEmailAttachment> attachments = new ArrayList<>();
            for (KlineEmailAttachment e : req.getAttachments()) {
                saveAttachmentTransaction(e.getMetaFile(), attachments);
            }
            return attachments;
        }
        return Collections.emptyList();
    }

    @Transactional
    public void saveAttachmentTransaction(MetaFile metaFile, List<KlineEmailAttachment> attachments){
        try {
            KlineEmailAttachment mock = new KlineEmailAttachment();
            mock.setMetaFileId(metaFile.getId());
            attachments.add(mock);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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

    @Override
    @Transactional
    public void generateEmailValue(ActionRequest request, ActionResponse response) {
        String emailTranId = com.kline.communication.utils.StringUtils.toString(request.getContext().get("emailTranId"));
        KlineEmailTransactionRepository emailTransactionRepository = Beans.get(KlineEmailTransactionRepository.class);
        KlineEmailTransaction emailTransaction = emailTransactionRepository.find(Long.parseLong(emailTranId));

        response.setValue("emailFrom", defaultIfNull(emailTransaction.getEmailFrom()));
        response.setValue("emailTo", defaultIfNull(emailTransaction.getEmailTo()));
        response.setValue("emailCc", defaultIfNull(emailTransaction.getEmailCc()));
        response.setValue("emailBcc", defaultIfNull(emailTransaction.getEmailBcc()));
        response.setValue("emailSubject", defaultIfNull(emailTransaction.getEmailSubject()));
        response.setValue("emailBody", defaultIfNull(emailTransaction.getEmailBody()));
        response.setValue("emailAttachmentTransaction", initMetaFile(emailTransaction));
    }

    @Transactional
    public List<KlineEmailAttachmentTransaction> initMetaFile(KlineEmailTransaction emailTransaction){
        if (!CollectionUtils.isEmpty(emailTransaction.getKlineEmailAttachment())) {
            List<KlineEmailAttachmentTransaction> metaFiles = new ArrayList<>();
            for (KlineEmailAttachment e : emailTransaction.getKlineEmailAttachment()) {
                MetaFile metaFile = Beans.get(MetaFileRepository.class).find(e.getMetaFileId());
                KlineEmailAttachmentTransaction mock = new KlineEmailAttachmentTransaction();
                mock.setMetaFile(metaFile);
                metaFiles.add(mock);
            }
            return metaFiles;
        }
        return Collections.emptyList();
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
        emailRequest.setAttachments(initMetaFileId(emailRequest));
        emailTransaction.setEmailFrom(AuthUtils.getUser().getEmail());
        emailTransaction.setDateTime(now);
        emailTransaction.setEmailTo(emailRequest.getTo());
        emailTransaction.setEmailCc(emailRequest.getToCC());
        emailTransaction.setEmailBcc(emailRequest.getToBCC());
        emailTransaction.setEmailSubject(emailRequest.getSubject());
        emailTransaction.setEmailBody(emailRequest.getContent());
        emailTransaction.setKlineEmailAttachment(emailRequest.getAttachments());
        emailTransaction.setStatus(STATUS_PENDING);
        KlineEmailTransactionRepository emailTransactionRepository = Beans.get(KlineEmailTransactionRepository.class);
        return emailTransactionRepository.save(emailTransaction);
    }

    private Properties initProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", AppSettings.get().get("email.host"));
        properties.put("mail.smtp.port", AppSettings.get().get("email.port"));
        properties.put("mail.smtp.auth", AppSettings.get().get("email.auth"));
        properties.put("mail.user", AppSettings.get().get("email.user"));
        properties.put("mail.password", AppSettings.get().get("email.pwd"));
        properties.put("mail.smtp.ssl.trust", "*");
        return properties;
    }

    private Authenticator initAuthenticator() {
        return new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(AppSettings.get().get("email.user"), AppSettings.get().get("email.pwd"));
            }
        };
    }

    private String defaultIfNull(String input) {
        return StringUtils.isEmpty(input) ? " - " : input;
    }
}
