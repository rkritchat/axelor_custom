package com.kline.knowledge.service;

import com.kline.knowledge.db.KnowledgeDocument;
import com.kline.knowledge.db.KnowledgeManagement;
import com.kline.knowledge.exception.KLineException;
import com.kline.knowledge.model.KnowledgeModel;

import java.util.List;

public interface KnowledgeManagementService {
    void validateRequest(KnowledgeModel knowledgeModel) throws KLineException;
    void save(KnowledgeModel knowledgeModel);
    KnowledgeManagement getKnowledgeManagementById(long id);
    List<KnowledgeDocument> getKnowledgeDocument(long id);
}
