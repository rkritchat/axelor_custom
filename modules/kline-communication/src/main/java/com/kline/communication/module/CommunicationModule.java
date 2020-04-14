package com.kline.communication.module;

import com.axelor.app.AxelorModule;
import com.kline.communication.service.EmailService;
import com.kline.communication.service.SmsService;
import com.kline.communication.service.impl.EmailServiceImpl;
import com.kline.communication.service.impl.SmsServiceImpl;

public class CommunicationModule extends AxelorModule {
    @Override
    protected void configure() {
        bind(EmailService.class).to(EmailServiceImpl.class);
        bind(SmsService.class).to(SmsServiceImpl.class);
    }
}
