package com.hiperf.common.ui.shared.util;

public class LinkedFileInfo {
	private boolean linkedFileDownload;
	private boolean linkedFileUpload;
	private String linkedFileClassName;
	//BLOB field
	private String linkedFileStorageFieldName;
	//File name field
	private String linkedFileName;
	private String linkedFileLocalKeyField;
	
	public LinkedFileInfo(boolean linkedFileDownload, boolean linkedFileUpload,
			String linkedFileClassName, String linkedFileStorageFieldName, String  linkedFileName, 
			String linkedFileLocalKeyField) {
		super();
		this.linkedFileDownload = linkedFileDownload;
		this.linkedFileUpload = linkedFileUpload;
		this.linkedFileClassName = linkedFileClassName;
		this.linkedFileStorageFieldName = linkedFileStorageFieldName;
		this.linkedFileName = linkedFileName;
		this.linkedFileLocalKeyField = linkedFileLocalKeyField;
	}
	public boolean isLinkedFileDownload() {
		return linkedFileDownload;
	}
	public boolean isLinkedFileUpload() {
		return linkedFileUpload;
	}
	public String getLinkedFileClassName() {
		return linkedFileClassName;
	}
	public String getLinkedFileStorageFieldName() {
		return linkedFileStorageFieldName;
	}
	public String getLinkedFileLocalKeyField() {
		return linkedFileLocalKeyField;
	}
	public String getLinkedFileName() {
		return linkedFileName;
	}
	
	
}
