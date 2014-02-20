package com.hiperf.common.ui.client.exception;


public class PersistenceException extends Exception {

	private boolean dbError = false;
	
	public PersistenceException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public PersistenceException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public PersistenceException(Throwable cause) {
		super(cause != null && cause.getMessage() != null ? cause.getMessage() : null);
	}

	public PersistenceException(String string, Throwable cause, boolean dbError) {
		this(string, cause);
		this.dbError = dbError;
	}

	@Override
	public String toString() {
		return getLocalizedMessage();
	}

	public boolean isDbError() {
		return dbError;
	}



}
