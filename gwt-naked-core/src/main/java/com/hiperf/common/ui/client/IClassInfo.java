package com.hiperf.common.ui.client;

import org.gwtgen.api.shared.INakedObject;





/**
 * Information retrieved from the {@link org.gwtgen.api.shared.UIClass} annotation
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
