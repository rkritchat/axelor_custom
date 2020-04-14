package com.kline.communication.model;

import com.axelor.rpc.ActionRequest;
import com.kline.communication.db.KlineEmailAttachment;
import com.kline.communication.utils.StringUtils;

import java.util.List;

public class EmailRequest {
    private String to;
    private String toCC;
    private String toBCC;
    private String subject;
    private String content;
    private boolean attachment;
    private List<KlineEmailAttachment> attachments;
    private boolean containAttachment;

    public EmailRequest() {
    }

    public EmailRequest(ActionRequest request) {
        to = StringUtils.toString(request.getContext().get("emailTo"));
        toCC = StringUtils.toString(request.getContext().get("emailCC"));
        toBCC = StringUtils.toString(request.getContext().get("emailBCC"));
        subject = StringUtils.toString(request.getContext().get("emailSubject"));
        content = StringUtils.toString(request.getContext().get("emailContent"));
        attachment = Boolean.parseBoolean(String.valueOf(request.getContext().get("enableAttachment")));
        attachments = (List<KlineEmailAttachment>) request.getContext().get("klineEmailAttachment");
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getToCC() {
        return toCC;
    }

    public void setToCC(String toCC) {
        this.toCC = toCC;
    }

    public String getToBCC() {
        return toBCC;
    }

    public void setToBCC(String toBCC) {
        this.toBCC = toBCC;
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

    @Override
    public String toString() {
        return "EmailRequest{" +
                "to='" + to + '\'' +
                ", toCC='" + toCC + '\'' +
                ", toBCC='" + toBCC + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", attachment=" + attachment + '}';
    }
}
