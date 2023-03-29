package com.byes.paap.extendedactions.conditionrecord.queries;

import com.planonsoftware.platform.backend.querybuilder.v3.IQueryBuilder;
import com.planonsoftware.platform.backend.querybuilder.v3.IQueryDefinition;
import com.planonsoftware.platform.backend.querybuilder.v3.IQueryDefinitionContext;

public class MaterialTermsQuery implements IQueryDefinition
{
    @Override
	public void create(IQueryBuilder aBuilder, IQueryDefinitionContext aContext) {
        aBuilder.addSelectField("Name");
        aBuilder.addSearchField("ServiceAgreementRef");
	}

	@Override
	public String getBOName() {
		return "MaterialTerms";
	}
}