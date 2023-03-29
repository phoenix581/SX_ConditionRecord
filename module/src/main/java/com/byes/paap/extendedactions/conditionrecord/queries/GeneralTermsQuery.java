package com.byes.paap.extendedactions.conditionrecord.queries;

import com.planonsoftware.platform.backend.querybuilder.v3.IQueryBuilder;
import com.planonsoftware.platform.backend.querybuilder.v3.IQueryDefinition;
import com.planonsoftware.platform.backend.querybuilder.v3.IQueryDefinitionContext;

public class GeneralTermsQuery implements IQueryDefinition
{
    @Override
	public void create(IQueryBuilder aBuilder, IQueryDefinitionContext aContext) {
        aBuilder.addSelectField("Syscode");
        aBuilder.addSearchField("ContractRef");
	}

	@Override
	public String getBOName() {
		return "GeneralTerms";
	}
}