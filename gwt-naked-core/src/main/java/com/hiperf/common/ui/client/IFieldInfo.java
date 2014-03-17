package com.hiperf.common.ui.client;

import java.util.List;

import com.hiperf.common.ui.shared.util.LinkedFileInfo;



/**
 * Information retrieved from the {@link org.gwtgen.api.shared.UIAttribute} annotation (if present),
 * from the JPA mapping (if present) and from the fields/getters/setters data types
 *
 * Accessible for each class via {@link com.hiperf.common.ui.shared.WrapperContext#getFieldInfoByName()}
 *
 * */
public interface IFieldInfo {

	boolean isCollection();

	DataType getDataType();

	boolean isEditable();

	void setEditable(boolean b);

	List<IValidator> getValidators();

	ICustomCell getCustomCell();

	void setCustomCell(ICustomCell customCell);

	IFormatter getFormatter();

	int getIndex();

	String getLabel();

	boolean isId();

	boolean isGeneratedId();

	boolean isManyToOne();

	boolean isOneToOne();

	boolean isOneToMany();

	boolean isManyToMany();

	boolean isJpaTransient();

	boolean isEnum();

	String getTypeName();

	String getMappedBy();

	boolean isDisplayed();

	void setDisplayed(boolean b);

	boolean isNotNull();

	boolean isLinkedFile();

	LinkedFileInfo getLinkedFileInfo();

	String getToStringAttribute();

	String getRedrawOnUpdateLinkedObject();

	String getSortCriteria();

	void setSortCriteria(String s);

	String getReturnClass();

	void setReturnClass(String returnClass);

	String getJoinClass();

	void setJoinClass(String joinClass);

	String getTargetIdField();

	void setTargetIdField(String targetIdField);

	void setJoinField(String joinField);

	void setTargetJoinField(String targetJoinField);

	boolean isNotEmpty();

	String getHelpTextKey();

	boolean canEdit();

	boolean canAddNew();

	boolean canSelect();

	boolean canRemove();

	public abstract String getImportAttribute();

	public abstract boolean isDisplayedInForm();

	void setIndex(int i);
	
	boolean isPreview();
	
	void setPreview(boolean b);

	public abstract void setHidden(boolean hidden);

	public abstract boolean isHidden();

	boolean isForceImport();
	
	void setForceImport(boolean b);

	public abstract void setImportable(boolean importable);

	public abstract boolean isImportable();

}