package com.byes.paap.extendedactions.conditionrecord;

import java.awt.Dimension;

import com.planonsoftware.platform.data.v1.AuthorizationException;
import com.planonsoftware.platform.data.v1.BusinessException;
import com.planonsoftware.platform.tsi.action.v3.ITSIAction;
import com.planonsoftware.platform.tsi.action.v3.ITSIActionContext;

import org.apache.wicket.markup.html.panel.Panel;

import com.byes.paap.extendedactions.conditionrecord.ui.ConditionRecordPanel;

public class ConditionRecord implements ITSIAction
{

	@Override
	public void execute(ITSIActionContext aTSIActionContext) {

        Panel aDemoPanel;
        try {
            aDemoPanel = new ConditionRecordPanel("ContidionRecordPanel", aTSIActionContext);
            Dimension panelDimension = new Dimension(585, 276);
            aTSIActionContext.getViewService().show(aDemoPanel, panelDimension, "Condition Record");
        } catch (BusinessException | IllegalArgumentException | AuthorizationException e1) {
            e1.printStackTrace();
        }
	}    
}
