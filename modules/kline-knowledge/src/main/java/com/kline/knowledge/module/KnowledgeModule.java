package com.kline.knowledge.module;

import com.axelor.app.AxelorModule;
import com.kline.knowledge.service.KnowledgeManagementService;
import com.kline.knowledge.service.impl.KnowledgeManagementServiceImpl;

public class KnowledgeModule extends AxelorModule {
    @Override
    protected void configure() {
        bind(KnowledgeManagementService.class).to(KnowledgeManagementServiceImpl.class);
    }
}
