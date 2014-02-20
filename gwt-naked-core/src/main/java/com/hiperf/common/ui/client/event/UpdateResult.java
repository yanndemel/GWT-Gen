package com.hiperf.common.ui.client.event;

public class UpdateResult {

	private String message;
	private int beforeState;

	public UpdateResult(String message) {
		super();
		this.message = message;
	}

	public UpdateResult(int beforeState) {
		super();
		this.message = null;
		this.beforeState = beforeState;
	}

	public String getMessage() {
		return message;
	}

	public int getBeforeState() {
		return beforeState;
	}



}
