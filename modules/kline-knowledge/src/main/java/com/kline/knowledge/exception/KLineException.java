package com.kline.knowledge.exception;


import com.kline.knowledge.constant.CommonConstant;

public class KLineException extends Exception {
    public KLineException(CommonConstant.ERROR err) {
        super(err.getMessage());
    }

    public KLineException(CommonConstant.ERROR err, Throwable e) {
        super(err.getMessage(), e);
    }
}
