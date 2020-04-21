package com.kline.knowledge.constant;

public class CommonConstant {

    public static final String SAVE_SUCCESS_FULLY = "Saved Successfully";


    public enum ERROR {
        EXCEPTION_OCCURRED_WHILE_SAVE("Cannot save, please contact administrator."),

        TITLE_IS_REQUIRED("Title is required."),
        CONTENT_IS_REQUIRED("Content is required."),
        CATEGORY_IS_REQUIRED_OR_INVALID("Category is required or invalid."),


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
