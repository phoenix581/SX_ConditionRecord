package com.byes.paap.extendedactions.conditionrecord.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.byes.paap.extendedactions.conditionrecord.SelectOption;
import com.planonsoftware.platform.data.v1.ActionNotFoundException;
import com.planonsoftware.platform.data.v1.AuthorizationException;
import com.planonsoftware.platform.data.v1.BusinessException;
import com.planonsoftware.platform.data.v1.FieldNotFoundException;
import com.planonsoftware.platform.data.v1.IAction;
import com.planonsoftware.platform.data.v1.IBusinessObject;
import com.planonsoftware.platform.data.v1.IDatabaseQuery;
import com.planonsoftware.platform.data.v1.IResultSet;
import com.planonsoftware.platform.data.v1.Operator;
import com.planonsoftware.platform.tsi.action.v3.ITSIActionContext;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

public class ConditionRecordPanel extends Panel
{   
    private static final long serialVersionUID = 1L;

    public transient static IDatabaseQuery dbQuery = null;
    public transient static IResultSet queryResults = null;
    
    public transient static IDatabaseQuery baseServiceAgreementQuery = null;
	public transient static IResultSet baseServiceAgreementQueryResults = null;
	
	public transient static IDatabaseQuery generalTermsQuery = null;
	public transient static IResultSet generalTermsQueryResults = null;
	
	public transient static IDatabaseQuery timeTermsQuery = null;
	public transient static IResultSet timeTermsQueryResults = null;
	
	public transient static IDatabaseQuery manHourTermsQuery = null;
	public transient static IResultSet manHourTermsQueryResults = null;

	public transient static IDatabaseQuery subContractorTermsQuery = null;
	public transient static IResultSet subContractorTermsQueryResults = null;

	public transient static IDatabaseQuery materialTermsQuery = null;
	public transient static IResultSet materialTermsQueryResults = null;

	public transient static IDatabaseQuery travelTermsQuery = null;
	public transient static IResultSet travelTermsQueryResults = null;

	public transient static IDatabaseQuery tradeQuery = null;
    public transient static IResultSet tradeQueryResults = null;
    
	public ConditionRecordPanel(final String id, final ITSIActionContext aTSIActionContext) throws BusinessException, IllegalStateException, AuthorizationException {
		super(id);

        IBusinessObject selectedProperty = aTSIActionContext.getSelectionService().getSelectedBO();	
        String buildingName = selectedProperty.getStringField("Name").getValue();
		int buildingPrimaryKey = selectedProperty.getPrimaryKey();
        int customerPrimaryKey = selectedProperty.getReferenceField("FreeInteger1").getValue();
        
        this.dbQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("ConditionRecordTemplateQuery");
        this.queryResults = this.dbQuery.execute();

        List<SelectOption> contionRecordTemplateList = new ArrayList<>();
        while (this.queryResults.next()) {
            SelectOption selecOption = new SelectOption(queryResults.getPrimaryKeyAsString(), this.queryResults.getString("Name"));
            if (this.queryResults.getBOType().getPnName().equals("UsrConditionRecordTemplate")) {
                contionRecordTemplateList.add(selecOption);
                queryResults.next();
            }
        }

        Model<SelectOption> conditionRecordModel = new Model<SelectOption>();
		ChoiceRenderer<SelectOption> choiceRenderer = new ChoiceRenderer<SelectOption>("name", "code");
		DropDownChoice<SelectOption> listConditionRecordTeamplates = new DropDownChoice<SelectOption>("conditionRecordTemplates", conditionRecordModel, contionRecordTemplateList, choiceRenderer);
		 
		Form<Void> exampleForm = new Form<Void>("exampleForm");

        FeedbackPanel feedbackMessage = new FeedbackPanel("feedbackMessage");
        feedbackMessage.setOutputMarkupId(true);
        exampleForm.add(feedbackMessage);

        AjaxButton saveButton = new AjaxButton("saveButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target) {
                try {
                    IBusinessObject template = aTSIActionContext.getDataService().getActionListManager("UsrConditionRecordTemplate").executeRead(Integer.valueOf(conditionRecordModel.getObject().getCode()));

                    IBusinessObject newCR = createUsrConditionRecord(aTSIActionContext, template, customerPrimaryKey, buildingName);
                    int newCRKey = newCR.getPrimaryKey();
                    ConditionRecordPanel.dbQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("SLAAmountQuery");
                    ConditionRecordPanel.dbQuery.getSearchExpression("ContractRef", Operator.EQUAL).setValue(template.getPrimaryKey());
                    ConditionRecordPanel.queryResults = ConditionRecordPanel.dbQuery.execute();

                    while (ConditionRecordPanel.queryResults.next()) {
                        IBusinessObject conditionRecordLine = aTSIActionContext.getDataService().getActionListManager("UsrSLAAmount").executeRead(ConditionRecordPanel.queryResults.getPrimaryKey());
                        IBusinessObject newConditionRecordLine = createUsrSLAAmount(aTSIActionContext, conditionRecordLine, newCRKey, buildingPrimaryKey);
                        ConditionRecordPanel.baseServiceAgreementQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("ContractLineServiceAgreementQuery");
                        ConditionRecordPanel.baseServiceAgreementQuery.setPageSize(1000);
                        ConditionRecordPanel.baseServiceAgreementQuery.getSearchExpression("ContractLineRef", Operator.EQUAL).setValue(conditionRecordLine.getPrimaryKey());
                        ConditionRecordPanel.baseServiceAgreementQueryResults = ConditionRecordPanel.baseServiceAgreementQuery.execute();

                        while (ConditionRecordPanel.baseServiceAgreementQueryResults.next()) {
                            IBusinessObject baseServiceAgreement = aTSIActionContext.getDataService().getActionListManager("ContractLineServiceAgreement").executeRead(ConditionRecordPanel.baseServiceAgreementQueryResults.getPrimaryKey());
                            IBusinessObject newBaseServiceAgreement = createBaseServiceAgreement(aTSIActionContext, baseServiceAgreement, newConditionRecordLine.getReferenceField("PivotLifecycleRef").getValue());
                            ConditionRecordPanel.generalTermsQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("GeneralTermsQuery");
                            ConditionRecordPanel.generalTermsQuery.setPageSize(1000);
                            ConditionRecordPanel.generalTermsQuery.getSearchExpression("ContractlineRef", Operator.EQUAL).setValue(conditionRecordLine.getPrimaryKey());
                            ConditionRecordPanel.generalTermsQueryResults = ConditionRecordPanel.generalTermsQuery.execute();
                            while (ConditionRecordPanel.generalTermsQueryResults.next()) {
                                IBusinessObject generalTerms = aTSIActionContext.getDataService().getActionListManager("GeneralTerms").executeRead(ConditionRecordPanel.generalTermsQueryResults.getPrimaryKey());
                                IBusinessObject newGeneralTerms = createGeneralTerms(aTSIActionContext, generalTerms, newConditionRecordLine.getReferenceField("PivotLifecycleRef").getValue());
                                ConditionRecordPanel.generalTermsQueryResults.next();
                            }
                            ConditionRecordPanel.timeTermsQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("TimeTermsQuery");
                            ConditionRecordPanel.timeTermsQuery.setPageSize(1000);
                            ConditionRecordPanel.timeTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                            ConditionRecordPanel.timeTermsQueryResults = ConditionRecordPanel.timeTermsQuery.execute();
                            while (ConditionRecordPanel.timeTermsQueryResults.next()) {
                                IBusinessObject timeTerms = aTSIActionContext.getDataService().getActionListManager("TimeTerms").executeRead(ConditionRecordPanel.timeTermsQueryResults.getPrimaryKey());
                                IBusinessObject newTimeTerms = createTimeTerms(aTSIActionContext, timeTerms, newBaseServiceAgreement.getReferenceField("PivotLifecycleRef").getValue());
                                ConditionRecordPanel.timeTermsQueryResults.next();
                            }
                            ConditionRecordPanel.manHourTermsQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("ManHourTermsQuery");
                            ConditionRecordPanel.manHourTermsQuery.setPageSize(1000);
                            ConditionRecordPanel.manHourTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                            ConditionRecordPanel.manHourTermsQueryResults = ConditionRecordPanel.manHourTermsQuery.execute();
                            while (ConditionRecordPanel.manHourTermsQueryResults.next()) {
                                IBusinessObject manHourTerms = aTSIActionContext.getDataService().getActionListManager("ManHourTerms").executeRead(ConditionRecordPanel.manHourTermsQueryResults.getPrimaryKey());
                                IBusinessObject newManHourTerms = createManHourTerms(aTSIActionContext, manHourTerms, newBaseServiceAgreement.getReferenceField("PivotLifecycleRef").getValue());
                                ConditionRecordPanel.manHourTermsQueryResults.next();
                            }
                            ConditionRecordPanel.subContractorTermsQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("SubContractorTermsQuery");
                            ConditionRecordPanel.subContractorTermsQuery.setPageSize(1000);
                            ConditionRecordPanel.subContractorTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                            ConditionRecordPanel.subContractorTermsQueryResults = ConditionRecordPanel.subContractorTermsQuery.execute();
                            while (ConditionRecordPanel.subContractorTermsQueryResults.next()) {
                                IBusinessObject subContractorTerms = aTSIActionContext.getDataService().getActionListManager("SubContractorTerms").executeRead(ConditionRecordPanel.subContractorTermsQueryResults.getPrimaryKey());
                                IBusinessObject newSubContractorTerms = createSubContractorTerms(aTSIActionContext, subContractorTerms, newBaseServiceAgreement.getReferenceField("PivotLifecycleRef").getValue());
                                ConditionRecordPanel.subContractorTermsQueryResults.next();
                            }
                            ConditionRecordPanel.materialTermsQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("MaterialTermsQuery");
                            ConditionRecordPanel.materialTermsQuery.setPageSize(1000);
                            ConditionRecordPanel.materialTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                            ConditionRecordPanel.materialTermsQueryResults = ConditionRecordPanel.materialTermsQuery.execute();
                            while (ConditionRecordPanel.materialTermsQueryResults.next()) {
                                IBusinessObject materialTerms = aTSIActionContext.getDataService().getActionListManager("MaterialTerms").executeRead(ConditionRecordPanel.materialTermsQueryResults.getPrimaryKey());
                                IBusinessObject newMaterialTerms = createMaterialTerms(aTSIActionContext, materialTerms, newBaseServiceAgreement.getReferenceField("PivotLifecycleRef").getValue());
                                ConditionRecordPanel.materialTermsQueryResults.next();
                            }
                            ConditionRecordPanel.travelTermsQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("TravelTermsQuery");
                            ConditionRecordPanel.travelTermsQuery.setPageSize(1000);
                            ConditionRecordPanel.travelTermsQuery.getSearchExpression("ServiceAgreementRef", Operator.EQUAL).setValue(baseServiceAgreement.getPrimaryKey());
                            ConditionRecordPanel.travelTermsQueryResults = ConditionRecordPanel.travelTermsQuery.execute();
                            while (ConditionRecordPanel.travelTermsQueryResults.next()) {
                                IBusinessObject travelTerms = aTSIActionContext.getDataService().getActionListManager("TravelTerms").executeRead(ConditionRecordPanel.travelTermsQueryResults.getPrimaryKey());
                                IBusinessObject newTravelTerms = createTravelTerms(aTSIActionContext, travelTerms, newBaseServiceAgreement.getReferenceField("PivotLifecycleRef").getValue());
                                ConditionRecordPanel.travelTermsQueryResults.next();
                            }
                            ConditionRecordPanel.baseServiceAgreementQueryResults.next();
                        }
                        ConditionRecordPanel.queryResults.next();
                    }

                } catch (NumberFormatException | ActionNotFoundException | BusinessException | FieldNotFoundException | ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ModalWindow.closeCurrent(target);                
            }
        };

        this.dbQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("ConditionRecordQuery");
        this.dbQuery.getStringSearchExpression("Name", Operator.CONTAINS).addValue(buildingName);
        int count = this.dbQuery.executeCount();

        if (count > 0) {
			feedbackMessage.error("Konditionssatz für dieses Objekte wurde bereits erstellt");
			contionRecordTemplateList = new ArrayList<>();
			listConditionRecordTeamplates = new DropDownChoice<SelectOption>("conditionRecordTemplates", conditionRecordModel, contionRecordTemplateList, choiceRenderer);
            saveButton.setEnabled(false);
        }

        exampleForm.add(listConditionRecordTeamplates);
        saveButton.setOutputMarkupId(true);
        exampleForm.add(saveButton);
        
        Button cancelButton = new Button("cancelButton");
        cancelButton.add(new AjaxEventBehavior("click") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onEvent(final AjaxRequestTarget target) {
                ModalWindow.closeCurrent(target);
            }
        });
        exampleForm.add(cancelButton);

        add(exampleForm);
	}

    @Override
    public final void renderHead(final IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(new CssResourceReference(ConditionRecordPanel.class, "style.css")));       
    }

    public IBusinessObject createUsrConditionRecord(ITSIActionContext aTSIActionContext, IBusinessObject template, int customerPrimaryKey, String buildingName) throws ActionNotFoundException, BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject conditionRecordBO = aTSIActionContext.getDataService().getActionListManager("UsrConditionRecord").getAction("BomAdd").execute();
        
        int numberOfFields= template.getBODefinition().getNumberOfFieldDefinitions();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        for (int i = 0; i < numberOfFields; i++) {
            if (template.getBODefinition().getFieldDefinition(i).isInUse() && !template.getBODefinition().getFieldDefinition(i).isReadOnly()) {
                String systemName = template.getBODefinition().getFieldDefinition(i).getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    conditionRecordBO.getField(systemName).setValue(template.getStringField("Code").getValue() + "_" + buildingName);
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    conditionRecordBO.getDateField(systemName).setValue(date);
                    continue;
                }
                if ("CustomerRef".equals(systemName)) {
                    conditionRecordBO.getField(systemName).setValue(customerPrimaryKey);
                    continue;
                }
                conditionRecordBO.getField(systemName).setValue(template.getField(systemName).getValue());
            }
        }

        conditionRecordBO.executeSave();

        return conditionRecordBO;
     }
     
     public IBusinessObject createUsrSLAAmount(ITSIActionContext aTSIActionContext, IBusinessObject template, int primaryKey, int buildingPrimaryKey) throws ActionNotFoundException, BusinessException, FieldNotFoundException, ParseException {
        IAction addAction = aTSIActionContext.getDataService().getActionListManager("UsrSLAAmount").getAction("BomAdd");
        addAction.getReferenceArgument("ContractRef").setValue(primaryKey);
        IBusinessObject newConditionRecordLineBO = addAction.execute();

        int numberOfFields= template.getBODefinition().getNumberOfFieldDefinitions();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        for (int i = 0; i < numberOfFields; i++) {
            if (template.getBODefinition().getFieldDefinition(i).isInUse() && !template.getBODefinition().getFieldDefinition(i).isReadOnly()) {
                String systemName = template.getBODefinition().getFieldDefinition(i).getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newConditionRecordLineBO.getField(systemName).setValue(template.getStringField("Code").getValue() + "_" + template.getStringField("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName)) {
                    newConditionRecordLineBO.getDateField(systemName).setValue(date);
                    continue;
                }
                if ("PropertyRef".equals(systemName)) {
                    newConditionRecordLineBO.getField(systemName).setValue(buildingPrimaryKey);
                    continue;
                }

                if ("ContractRef".equals(systemName)) {
                    continue;
                }
                newConditionRecordLineBO.getField(systemName).setValue(template.getField(systemName).getValue());
            }
        }

        newConditionRecordLineBO.executeSave();

        return newConditionRecordLineBO;
     }
     
     public IBusinessObject createBaseServiceAgreement(ITSIActionContext aTSIActionContext, IBusinessObject template, int primaryKey) throws ActionNotFoundException, BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newConditionRecordLineBO = aTSIActionContext.getDataService().getActionListManager("ContractLineServiceAgreement").getAction("BomAdd").execute();
        
        int numberOfFields= template.getBODefinition().getNumberOfFieldDefinitions();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        for (int i = 0; i < numberOfFields; i++) {
            if (template.getBODefinition().getFieldDefinition(i).isInUse() && !template.getBODefinition().getFieldDefinition(i).isReadOnly()) {
                String systemName = template.getBODefinition().getFieldDefinition(i).getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newConditionRecordLineBO.getField(systemName).setValue(template.getStringField("Code").getValue() + "_" + template.getStringField("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName)) {
                    newConditionRecordLineBO.getDateField(systemName).setValue(date);
                    continue;
                }
                if ("ContractLineRef".equals(systemName)) {
                    newConditionRecordLineBO.getField(systemName).setValue(primaryKey);
                    continue;
                }
                newConditionRecordLineBO.getField(systemName).setValue(template.getField(systemName).getValue());
            }
        }

        newConditionRecordLineBO.executeSave();

        return newConditionRecordLineBO;
     }
     
     public IBusinessObject createGeneralTerms(ITSIActionContext aTSIActionContext, IBusinessObject template, int primaryKey) throws ActionNotFoundException, BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newGeneralTermsBO = aTSIActionContext.getDataService().getActionListManager("GeneralTerms").getAction("BomAdd").execute();
        
        int numberOfFields= template.getBODefinition().getNumberOfFieldDefinitions();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        for (int i = 0; i < numberOfFields; i++) {
            if (template.getBODefinition().getFieldDefinition(i).isInUse() && !template.getBODefinition().getFieldDefinition(i).isReadOnly()) {
                String systemName = template.getBODefinition().getFieldDefinition(i).getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newGeneralTermsBO.getField(systemName).setValue(template.getStringField("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newGeneralTermsBO.getDateField(systemName).setValue(date);
                    continue;
                }
                if ("ContractlineRef".equals(systemName)) {
                    newGeneralTermsBO.getField(systemName).setValue(primaryKey);
                    continue;
                }
                newGeneralTermsBO.getField(systemName).setValue(template.getField(systemName).getValue());
            }
        }

        newGeneralTermsBO.executeSave();

        return newGeneralTermsBO;
     }
     
     public IBusinessObject createTimeTerms(ITSIActionContext aTSIActionContext, IBusinessObject template, int primaryKey) throws ActionNotFoundException, BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newTimeTermsBO = aTSIActionContext.getDataService().getActionListManager("TimeTerms").getAction("BomAdd").execute();
        
        int numberOfFields= template.getBODefinition().getNumberOfFieldDefinitions();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        for (int i = 0; i < numberOfFields; i++) {
            if (template.getBODefinition().getFieldDefinition(i).isInUse() && !template.getBODefinition().getFieldDefinition(i).isReadOnly()) {
                String systemName = template.getBODefinition().getFieldDefinition(i).getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newTimeTermsBO.getField(systemName).setValue(template.getStringField("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newTimeTermsBO.getDateField(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newTimeTermsBO.getField(systemName).setValue(primaryKey);
                    continue;
                }
                newTimeTermsBO.getField(systemName).setValue(template.getField(systemName).getValue());
            }
        }

        newTimeTermsBO.executeSave();

        return newTimeTermsBO;
     }
     
     public IBusinessObject createManHourTerms(ITSIActionContext aTSIActionContext, IBusinessObject template, int primaryKey) throws ActionNotFoundException, BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newManHourTerms = aTSIActionContext.getDataService().getActionListManager("ManHourTerms").getAction("BomAdd").execute();
        
        int numberOfFields= template.getBODefinition().getNumberOfFieldDefinitions();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        for (int i = 0; i < numberOfFields; i++) {
            if (template.getBODefinition().getFieldDefinition(i).isInUse() && !template.getBODefinition().getFieldDefinition(i).isReadOnly()) {
                String systemName = template.getBODefinition().getFieldDefinition(i).getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newManHourTerms.getField(systemName).setValue(template.getStringField("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newManHourTerms.getDateField(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newManHourTerms.getField(systemName).setValue(primaryKey);
                    continue;
                }
                newManHourTerms.getField(systemName).setValue(template.getField(systemName).getValue());
            }
        }

        newManHourTerms.executeSave();

        ConditionRecordPanel.tradeQuery = aTSIActionContext.getDataService().getPVDatabaseQuery("TradeQuery");
        ConditionRecordPanel.tradeQuery.getSearchExpression("ManHourTermRef", Operator.EQUAL).setValue(template.getPrimaryKey());
        ConditionRecordPanel.tradeQueryResults = ConditionRecordPanel.tradeQuery.execute();
        while (ConditionRecordPanel.tradeQueryResults.next()) {
            IBusinessObject manHourTermMToNTrade = aTSIActionContext.getDataService().getActionListManager("ManHourTermMToNTrade").getAction("BomAdd").execute();
            manHourTermMToNTrade.getReferenceField("ManHourTermRef").setValue(newManHourTerms.getPrimaryKey()-1);
            manHourTermMToNTrade.getReferenceField("TradeRef").setValue(ConditionRecordPanel.tradeQueryResults.getReference("TradeRef"));
            manHourTermMToNTrade.getDateField("BeginDate").setValue(date);
            manHourTermMToNTrade.executeSave();
        }
        
        return newManHourTerms;
     }
     
     public IBusinessObject createSubContractorTerms(ITSIActionContext aTSIActionContext, IBusinessObject template, int primaryKey) throws ActionNotFoundException, BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newSubContractorTerms = aTSIActionContext.getDataService().getActionListManager("SubContractorTerms").getAction("BomAdd").execute();
        
        int numberOfFields= template.getBODefinition().getNumberOfFieldDefinitions();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        for (int i = 0; i < numberOfFields; i++) {
            if (template.getBODefinition().getFieldDefinition(i).isInUse() && !template.getBODefinition().getFieldDefinition(i).isReadOnly()) {
                String systemName = template.getBODefinition().getFieldDefinition(i).getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newSubContractorTerms.getField(systemName).setValue(template.getStringField("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newSubContractorTerms.getDateField(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newSubContractorTerms.getField(systemName).setValue(primaryKey);
                    continue;
                }
                newSubContractorTerms.getField(systemName).setValue(template.getField(systemName).getValue());
            }
        }

        newSubContractorTerms.executeSave();

        return newSubContractorTerms;
     }
     
     public IBusinessObject createMaterialTerms(ITSIActionContext aTSIActionContext, IBusinessObject template, int primaryKey) throws ActionNotFoundException, BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newMaterialTerms = aTSIActionContext.getDataService().getActionListManager("MaterialTerms").getAction("BomAdd").execute();
        
        int numberOfFields= template.getBODefinition().getNumberOfFieldDefinitions();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        for (int i = 0; i < numberOfFields; i++) {
            if (template.getBODefinition().getFieldDefinition(i).isInUse() && !template.getBODefinition().getFieldDefinition(i).isReadOnly()) {
                String systemName = template.getBODefinition().getFieldDefinition(i).getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newMaterialTerms.getField(systemName).setValue(template.getStringField("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newMaterialTerms.getDateField(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newMaterialTerms.getField(systemName).setValue(primaryKey);
                    continue;
                }
                newMaterialTerms.getField(systemName).setValue(template.getField(systemName).getValue());
            }
        }

        newMaterialTerms.executeSave();

        return newMaterialTerms;
     }
     
     public IBusinessObject createTravelTerms(ITSIActionContext aTSIActionContext, IBusinessObject template, int primaryKey) throws ActionNotFoundException, BusinessException, FieldNotFoundException, ParseException {
        IBusinessObject newTravelTerms = aTSIActionContext.getDataService().getActionListManager("TravelTerms").getAction("BomAdd").execute();
        
        int numberOfFields= template.getBODefinition().getNumberOfFieldDefinitions();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        String dateInString = "01.01.2010";
        Date date = formatter.parse(dateInString);
        for (int i = 0; i < numberOfFields; i++) {
            if (template.getBODefinition().getFieldDefinition(i).isInUse() && !template.getBODefinition().getFieldDefinition(i).isReadOnly()) {
                String systemName = template.getBODefinition().getFieldDefinition(i).getPnName();
                if ("RefBOStateUserDefined".equals(systemName) || "SystemState".equals(systemName) || "Code".equals(systemName) ||
                    "PivotLifecycleRef".equals(systemName) || "PreviousLifecycleRef".equals(systemName)) {
                    continue;
                }
                if ("Name".equals(systemName)) {
                    newTravelTerms.getField(systemName).setValue(template.getStringField("Name").getValue());
                    continue;
                }

                if ("ActualBeginDate".equals(systemName) || "PlanonBeginDate".equals(systemName) || "BeginDate".equals(systemName)) {
                    newTravelTerms.getDateField(systemName).setValue(date);
                    continue;
                }
                if ("ServiceAgreementRef".equals(systemName)) {
                    newTravelTerms.getField(systemName).setValue(primaryKey);
                    continue;
                }
                newTravelTerms.getField(systemName).setValue(template.getField(systemName).getValue());
            }
        }

        newTravelTerms.executeSave();

        return newTravelTerms;
	 }
}
