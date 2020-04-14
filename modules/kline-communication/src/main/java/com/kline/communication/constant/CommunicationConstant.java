package com.kline.communication.constant;

public class CommunicationConstant {

    public static final String TEXT_HTML = "text/html";
    public static final String SEND_EMAIL_SUCCESSFULLY = "Send Email Successfully";
    public static final String SEND_SMS_SUCCESSFULLY = "Send SMS Successfully";

    public static final String TAG_IMAGE = "data:image/jpeg;base64";
    public static final String TYPE_EMAIL = "email";
    public static final String TYPE_SMS = "sms";

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_FAILED = "Fail";
    public static final String STATUS_SUCCESS = "Success";


    public enum ERROR {
        EXCEPTION_OCCURRED_WHILE_SENDING_EMAIL("Cannot send email, please contact administrator."),
        EXCEPTION_OCCURRED_WHILE_SENDING_SMS("Cannot send sms, please contact administrator."),

        EMAIL_TO_IS_REQUIRED("Email To is required."),
        EMAIL_SUBJECT_IS_REQUIRED("Email Subject is required."),
        EMAIL_CONTENT_IS_REQUIRED("Email Content is required."),
        EMAIL_BODY_IS_INVALID("Email Body cannot contain image."),
        PLEASE_SET_EMAIL_ADDRESS_IN_YOUR_PROFILE("Please set email address in your profile."),
        SMS_CONTENT_IS_REQUIRED("Sms Content is required."),
        SMS_MOBILE_NO_IS_REQUIRED("Mobile Number is required."),

        ;
        private String message;
        ERROR(String error) {
            this.message = error;
        }

        public String getMessage() {
            return message;
        }
    }
}
