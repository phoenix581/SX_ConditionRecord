package com.byes.paap.extendedactions.conditionrecord.ui;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.Form;

public abstract class SingleClickAjaxButton extends IndicatingAjaxButton {
    private final boolean enableButtonAfterSubmit;
 
    public SingleClickAjaxButton(String id, Form<?> form, boolean enableButtonAfterSubmit) {
        super(id, form);
        this.enableButtonAfterSubmit = enableButtonAfterSubmit;
    }
 
    @Override
    protected void onError(AjaxRequestTarget target) {
        //on error, just re-enable
        enableButton(target);
    }
 
    @Override
    protected void onAfterSubmit(AjaxRequestTarget target) {
        if(enableButtonAfterSubmit) {
            enableButton(target);
        }
    }
 
    protected void enableButton(AjaxRequestTarget target) {
        target.appendJavaScript("$('#"+this.getMarkupId()+"').prop('disabled', false);");
    }
 
    @Override
    protected String getOnClickScript() {
        //if the button should be re-enabled but hasnt due to some error, automatically re-enable it after 10 seconds
        if(enableButtonAfterSubmit) {
            return "$('#"+this.getMarkupId()+"').prop('disabled', true);";
        }
        return null;
    }
}