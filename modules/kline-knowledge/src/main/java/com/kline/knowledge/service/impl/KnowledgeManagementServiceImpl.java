package com.kline.knowledge.service.impl;

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.persist.Transactional;
import com.kline.knowledge.db.KnowledgeDocument;
import com.kline.knowledge.db.KnowledgeManagement;
import com.kline.knowledge.db.repo.KnowledgeDocumentRepository;
import com.kline.knowledge.db.repo.KnowledgeManagementRepository;
import com.kline.knowledge.exception.KLineException;
import com.kline.knowledge.model.KnowledgeModel;
import com.kline.knowledge.service.KnowledgeManagementService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.kline.knowledge.constant.CommonConstant.ERROR.*;

public class KnowledgeManagementServiceImpl implements KnowledgeManagementService {

    @Override
    public void validateRequest(KnowledgeModel knowledgeModel) throws KLineException {
        if (knowledgeModel.getCategory() == null || knowledgeModel.getCategory().getId() == 0) {
            throw new KLineException(CATEGORY_IS_REQUIRED_OR_INVALID);
        }
        if (StringUtils.isEmpty(knowledgeModel.getContent())) {
            throw new KLineException(CONTENT_IS_REQUIRED);
        }
        if (StringUtils.isEmpty(knowledgeModel.getTitle())) {
            throw new KLineException(TITLE_IS_REQUIRED);
        }
    }

    @Override
    @Transactional
    public void save(KnowledgeModel knowledgeModel) {
        KnowledgeManagementRepository repository = Beans.get(KnowledgeManagementRepository.class);
        if(knowledgeModel.getId()!=null){
            KnowledgeManagement inDB = repository.find(knowledgeModel.getId());
            if(inDB!=null){
                inDB.setKnowledgeTitle(knowledgeModel.getTitle());
                inDB.setKnowledgeContent(knowledgeModel.getContent());
                inDB.setKnowledgeCategory(knowledgeModel.getCategory());
                System.out.println("UPDATE");
                KnowledgeManagement result = repository.save(inDB);
                updateKnowledgeDocument(knowledgeModel.getDocuments(), result);
            }
        }else{
            KnowledgeManagement knowledgeManagement = new KnowledgeManagement();
            knowledgeManagement.setId(knowledgeModel.getId());
            knowledgeManagement.setKnowledgeCategory(knowledgeModel.getCategory());
            knowledgeManagement.setKnowledgeTitle(knowledgeModel.getTitle());
            knowledgeManagement.setKnowledgeContent(knowledgeModel.getContent());
            knowledgeManagement.setKnowledgeOwner(knowledgeModel.getOwner());
            knowledgeManagement.setKnowledgePublishDate(LocalDateTime.now());
            System.out.println("INSERT");
            KnowledgeManagement result = repository.save(knowledgeManagement);
            saveKnowledgeDocument(knowledgeModel.getDocuments(), result);
        }
    }

    private void updateKnowledgeDocument(List<KnowledgeDocument> documents, KnowledgeManagement knowledgeManagement) {
        removeOldDocument(documents, knowledgeManagement);
        saveKnowledgeDocument(documents, knowledgeManagement);
    }

    private void removeOldDocument(List<KnowledgeDocument> documents, KnowledgeManagement knowledgeManagement){
        KnowledgeDocumentRepository documentRepository = Beans.get(KnowledgeDocumentRepository.class);
        List<KnowledgeDocument> oldDocument = getDocumentByKnowledgeManagementId(knowledgeManagement.getId());
        for(KnowledgeDocument e : oldDocument){
            documentRepository.remove(e);
        }
    }

    private List<KnowledgeDocument> getDocumentByKnowledgeManagementId(long managementId) {
        return Beans.get(KnowledgeDocumentRepository.class)
                .all()
                .filter("self.knowledgeId = ? ", managementId)
                .fetch();
    }

    private void saveKnowledgeDocument(List<KnowledgeDocument> documents, KnowledgeManagement knowledgeManagement){
        if(!CollectionUtils.isEmpty(documents)){
            KnowledgeDocumentRepository documentRepository = Beans.get(KnowledgeDocumentRepository.class);
            for (KnowledgeDocument e : documents) {
                if(e.getDocument()!=null){
                    KnowledgeDocument mock = new KnowledgeDocument();
                    mock.setMetaFileId(e.getDocument().getId());
                    mock.setKnowledgeId(knowledgeManagement.getId());
                    documentRepository.save(mock);
                }
            }
        }
    }

    @Override
    @Transactional
    public KnowledgeManagement getKnowledgeManagementById(long id) {
        return Beans.get(KnowledgeManagementRepository.class).find(id);
    }

    @Override
    public List<KnowledgeDocument> getKnowledgeDocument(long id) {
        List<KnowledgeDocument> documents = getDocumentByKnowledgeManagementId(id);
        List<KnowledgeDocument> result = new ArrayList<>();
        //init meta file
        for (KnowledgeDocument e : documents) {
            MetaFile metaFile = Beans.get(MetaFileRepository.class).find(e.getMetaFileId());
            KnowledgeDocument mock = new KnowledgeDocument();
            mock.setDocument(metaFile);
            mock.setMetaFileId(metaFile.getId());
            mock.setKnowledgeId(id);
            result.add(mock);
        }
        return result;
    }
}
