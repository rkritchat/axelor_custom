package com.kline.communication.model;

public class SmsRequest {
    private String[] mobileNo;
    private String content;

    public SmsRequest() {
    }

    public SmsRequest(String[] mobileNo, String content) {
        this.mobileNo = mobileNo;
        this.content = content;
    }

    public String[] getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String[] mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
