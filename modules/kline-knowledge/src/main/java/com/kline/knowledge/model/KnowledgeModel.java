package com.kline.knowledge.model;

import com.axelor.auth.AuthUtils;
import com.axelor.rpc.ActionRequest;
import com.kline.knowledge.db.KnowledgeDocument;
import com.kline.knowledge.utils.StringUtils;
import com.kline.config.db.km.Category;

import java.util.List;


public class KnowledgeModel {
    private Long id;
    private String title;
    private Category category;
    private String content;
    private String owner;
    private List<KnowledgeDocument> documents;

    public KnowledgeModel() {
    }

    public KnowledgeModel(ActionRequest request) {
        String reqId = StringUtils.toString(request.getContext().get("id"));
        if(reqId != null){
            id = Long.parseLong(reqId);
        }
        title = StringUtils.toString(request.getContext().get("knowledgeTitle"));
        category = (Category)request.getContext().get("knowledgeCategory");
        content = StringUtils.toString(request.getContext().get("knowledgeContent"));
        owner = AuthUtils.getUser().getName();
        documents = (List<KnowledgeDocument>) request.getContext().get("knowledgeDocument");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<KnowledgeDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<KnowledgeDocument> documents) {
        this.documents = documents;
    }
}
