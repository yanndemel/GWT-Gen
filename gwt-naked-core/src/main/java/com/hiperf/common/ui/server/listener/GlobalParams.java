package com.hiperf.common.ui.server.listener;

public class GlobalParams {
	
	private static final GlobalParams instance = new GlobalParams();

	public static GlobalParams getInstance() {
		return instance;
	}
	
	private String transactionType;
	private String unitName; 	
	
	private GlobalParams() {}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	
	
	

}
