package com.hiperf.common.ui.client;





/**
 * Information retrieved from the {@link com.hiperf.common.ui.shared.annotation.UIClass} annotation
 *
 * Accessible for each class via {@link com.hiperf.common.ui.shared.WrapperContext#getClassInfoByName()}
 *
 * */
public interface IClassInfo<T extends INakedObject> {

	boolean isEditable();

	String getTableLabelKey();

	String getFormLabelKey();

	String getFormTitle();

	void setFormTitle(String t);

	String getUpdateHandlerClassName();

	IUpdateHandler<T> getUpdateHandler();

	IRowFormatter<T> getRowFormatter();

	public abstract boolean isImportable();

	public abstract boolean isEntity();

}
