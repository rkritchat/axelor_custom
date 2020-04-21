package com.kline.knowledge;

import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.kline.knowledge.db.KnowledgeDocument;
import com.kline.knowledge.db.KnowledgeManagement;
import com.kline.knowledge.exception.KLineException;
import com.kline.knowledge.model.KnowledgeModel;
import com.kline.knowledge.service.KnowledgeManagementService;
import com.kline.knowledge.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.kline.knowledge.constant.CommonConstant.ERROR.EXCEPTION_OCCURRED_WHILE_SAVE;
import static com.kline.knowledge.constant.CommonConstant.SAVE_SUCCESS_FULLY;

public class KnowledgeManagementController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private KnowledgeManagementService knowledgeManagement;

    public void initEditForm(ActionRequest request, ActionResponse response) {
        String knowledgeOwner = StringUtils.toString(request.getContext().get("knowledgeOwner"));
        System.out.println("Come " + knowledgeOwner);
        if (AuthUtils.getUser().getName().equals(knowledgeOwner)) {
            System.out.println("Set editable to True");
            response.setValue("isEditAble", true);
        }else{
            System.out.println("Set editable to FALSE");
            response.setValue("isEditAble", false);
        }

        List<KnowledgeDocument> documents = knowledgeManagement.getKnowledgeDocument(Long.parseLong(String.valueOf(request.getContext().get("id"))));
        response.setValue("knowledgeDocument", documents);
        response.setValue("showDocument", !CollectionUtils.isEmpty(documents));
    }

    public void save(ActionRequest request, ActionResponse response) {
        try{
            System.out.println("Start save ");
            List<KnowledgeDocument> knowledgeDocumentList = (List<KnowledgeDocument>) request.getContext().get("knowledgeDocument");
            System.out.println(knowledgeDocumentList);
            KnowledgeModel knowledgeModel = new KnowledgeModel(request);
            knowledgeManagement.validateRequest(knowledgeModel);
            knowledgeManagement.save(knowledgeModel);
            response.setNotify(SAVE_SUCCESS_FULLY);
        }catch(KLineException e){
            response.setError(e.getMessage());
        }catch (Exception e){
            logger.error("exception occurred...", e);
            response.setError(EXCEPTION_OCCURRED_WHILE_SAVE.getMessage());
        }
    }

    public void clear(ActionRequest request, ActionResponse response) {
        response.setValue("knowledgeTitle", null);
        response.setValue("knowledgeCategory", null);
        response.setValue("knowledgeContent", null);
    }

    public void edit(ActionRequest request, ActionResponse response) {
        response.setView(
                ActionView.define(I18n.get("Knowledge Management"))
                        .param("id", String.valueOf(request.getContext().get("id")))
                        .context("id", String.valueOf(request.getContext().get("id")))
                        .param("popup","reload")
                        .param("show-confirm","false")
                        .param("popup-save","false")
                        .model("com.kline.knowledge.db.KnowledgeManagement")
                        .add("form", "knowledge-management-create-form")
                        .map());
    }

    public void initUpdate(ActionRequest request, ActionResponse response) {
        System.out.println("ID IS " + request.getContext().get("id"));
        String id = StringUtils.toString(request.getContext().get("id"));
        if(id!=null){
            KnowledgeManagement result = knowledgeManagement.getKnowledgeManagementById(Long.parseLong(id));
            List<KnowledgeDocument> documents = knowledgeManagement.getKnowledgeDocument(result.getId());
            response.setValue("id", id);
            response.setValue("knowledgeTitle", result.getKnowledgeTitle());
            response.setValue("knowledgeCategory", result.getKnowledgeCategory());
            response.setValue("knowledgeContent", result.getKnowledgeContent());
            response.setValue("knowledgeDocument", documents);
        }
    }
}
