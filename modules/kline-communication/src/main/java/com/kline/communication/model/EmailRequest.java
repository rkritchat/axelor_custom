package com.kline.communication.model;

import com.axelor.apps.message.db.EmailAddress;
import com.axelor.rpc.ActionRequest;
import com.kline.communication.db.KlineEmailAttachment;
import com.kline.communication.utils.StringUtils;

import javax.mail.internet.InternetAddress;
import java.util.List;
import java.util.Set;

public class EmailRequest {
    private Set<EmailAddress> to;
    private Set<EmailAddress> toCC;
    private Set<EmailAddress> toBCC;
    private String subject;
    private String content;
    private boolean attachment;
    private List<KlineEmailAttachment> attachments;
    private boolean containAttachment;
    private List<InternetAddress> emailTo;
    private List<InternetAddress> emailCc;
    private List<InternetAddress> emailBcc;

    public EmailRequest() {
    }

    public EmailRequest(ActionRequest request) {
        to = (Set<EmailAddress>) request.getContext().get("emailTo");
        toCC = (Set<EmailAddress>) request.getContext().get("emailCC");
        toBCC = (Set<EmailAddress>) request.getContext().get("emailBCC");
        subject = StringUtils.toString(request.getContext().get("emailSubject"));
        content = StringUtils.toString(request.getContext().get("emailContent"));
        attachment = Boolean.parseBoolean(String.valueOf(request.getContext().get("enableAttachment")));
        attachments = (List<KlineEmailAttachment>) request.getContext().get("klineEmailAttachment");
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<KlineEmailAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<KlineEmailAttachment> attachments) {
        this.attachments = attachments;
    }

    public boolean isAttachment() {
        return attachment;
    }

    public void setAttachment(boolean attachment) {
        this.attachment = attachment;
    }

    public boolean isContainAttachment() {
        return containAttachment;
    }

    public void setContainAttachment(boolean containAttachment) {
        this.containAttachment = containAttachment;
    }

    public Set<EmailAddress> getTo() {
        return to;
    }

    public void setTo(Set<EmailAddress> to) {
        this.to = to;
    }

    public Set<EmailAddress> getToCC() {
        return toCC;
    }

    public void setToCC(Set<EmailAddress> toCC) {
        this.toCC = toCC;
    }

    public Set<EmailAddress> getToBCC() {
        return toBCC;
    }

    public void setToBCC(Set<EmailAddress> toBCC) {
        this.toBCC = toBCC;
    }

    public List<InternetAddress> getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(List<InternetAddress> emailTo) {
        this.emailTo = emailTo;
    }

    public List<InternetAddress> getEmailCc() {
        return emailCc;
    }

    public void setEmailCc(List<InternetAddress> emailCc) {
        this.emailCc = emailCc;
    }

    public List<InternetAddress> getEmailBcc() {
        return emailBcc;
    }

    public void setEmailBcc(List<InternetAddress> emailBcc) {
        this.emailBcc = emailBcc;
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "to=" + to +
                ", toCC=" + toCC +
                ", toBCC=" + toBCC +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", attachment=" + attachment +
                ", attachments=" + attachments +
                ", containAttachment=" + containAttachment +
                '}';
    }
}
