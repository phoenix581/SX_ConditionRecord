package com.byes.paap.extendedactions.conditionrecord;

import java.io.Serializable;

public class SelectOption implements Serializable {

	/**
     *
     */
    private static final long serialVersionUID = 1L;
    String code;
	String name;
	
	public SelectOption(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}