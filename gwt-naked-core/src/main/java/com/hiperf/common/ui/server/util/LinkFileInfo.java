package com.hiperf.common.ui.server.util;

import java.lang.reflect.Method;

public class LinkFileInfo {

	private Method localFileNameSetter;
	private Method localFileIdGetter;
	private String fileClassName;
	private String fileNameField;
	
	public LinkFileInfo(Method localFileNameSetter, Method localFileIdGetter,
			String fileClassName, String fileNameField) {
		super();
		this.localFileNameSetter = localFileNameSetter;
		this.localFileIdGetter = localFileIdGetter;
		this.fileClassName = fileClassName;
		this.fileNameField = fileNameField;
	}

	public Method getLocalFileNameSetter() {
		return localFileNameSetter;
	}

	public Method getLocalFileIdGetter() {
		return localFileIdGetter;
	}

	public String getFileClassName() {
		return fileClassName;
	}

	public String getFileNameField() {
		return fileNameField;
	}
	
	
	
}
