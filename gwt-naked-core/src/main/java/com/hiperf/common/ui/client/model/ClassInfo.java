package com.hiperf.common.ui.client.model;

import com.hiperf.common.ui.client.IClassInfo;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IRowFormatter;
import com.hiperf.common.ui.client.IUpdateHandler;

public class ClassInfo<T extends INakedObject> implements IClassInfo<T> {

	private String formTitle = null;
	private String tableLabelKey = null;
	private String formLabelKey = null;
	private String updateHandlerClassName;
	private String validatorClassName;
	private IUpdateHandler<T> updateHandler;
	private boolean editable;
	private String rowFormatterClassName;
	private IRowFormatter<T> rowFormatter;
	private boolean importable;
	private boolean entity;
	private String createForm;
	private String commitHandler;
	

	public ClassInfo(boolean editable, String formTitle) {
		super();
		this.editable = editable;
		this.formTitle = formTitle;
	}

	public ClassInfo(boolean editable, boolean importable, String tableLabelKey, String formLabelKey,
			IUpdateHandler<T> updateHandler, IRowFormatter<T> rowFormatter, boolean entity) {
		super();
		this.editable = editable;
		this.importable = importable;
		this.tableLabelKey = tableLabelKey;
		this.formLabelKey = formLabelKey;
		this.updateHandler = updateHandler;
		this.rowFormatter = rowFormatter;
		this.entity = entity;
	}

	public ClassInfo(boolean editable, String tableLabelKey, String formLabelKey) {
		super();
		this.editable = editable;
		this.tableLabelKey = tableLabelKey != null && tableLabelKey.length() > 0 ? tableLabelKey : null;
		this.formLabelKey = formLabelKey != null && formLabelKey.length() > 0 ? formLabelKey : null;
		this.updateHandlerClassName = null;
		this.rowFormatterClassName = null;
	}

	public String getTableLabelKey() {
		return tableLabelKey;
	}

	public String getFormLabelKey() {
		return formLabelKey;
	}

	@Override
	public String getUpdateHandlerClassName() {
		return updateHandlerClassName;
	}

	public IUpdateHandler<T> getUpdateHandler() {
		return updateHandler;
	}

	public boolean isEditable() {
		return editable;
	}

	public IRowFormatter<T> getRowFormatter() {
		return rowFormatter;
	}

	public String getRowFormatterClassName() {
		return rowFormatterClassName;
	}

	public void setUpdateHandlerClassName(String updateHandlerClassName) {
		this.updateHandlerClassName = (updateHandlerClassName.length() > 0) ? updateHandlerClassName
				: null;
	}

	public void setRowFormatterClassName(String rowFormatterClassName) {
		this.rowFormatterClassName = (rowFormatterClassName.length() > 0) ? rowFormatterClassName
				: null;
	}

	public String getValidatorClassName() {
		return validatorClassName;
	}

	public void setValidatorClassName(String validatorClassName) {
		this.validatorClassName = (validatorClassName.length() > 0 )? validatorClassName : null;
	}

	public String getFormTitle() {
		return formTitle;
	}

	@Override
	public void setFormTitle(String t) {
		formTitle = t;
	}

	@Override
	public boolean isImportable() {
		return importable;
	}

	public void setImportable(boolean importable) {
		this.importable = importable;
	}

	@Override
	public boolean isEntity() {
		return entity;
	}

	public String getCreateForm() {
		return createForm;
	}

	public void setCreateForm(String createForm) {
		this.createForm = createForm;
	}

	public String getCommitHandler() {
		return commitHandler;
	}

	public void setCommitHandler(String commitHandler) {
		this.commitHandler = commitHandler;
	}




}
