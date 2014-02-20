package com.hiperf.common.ui.client.model;

import java.util.List;

import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.ICustomCell;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.IValidator;
import com.hiperf.common.ui.client.format.FormatterFactory;
import com.hiperf.common.ui.client.validation.ValidatorFactory;
import com.hiperf.common.ui.shared.WidgetFactory;
import com.hiperf.common.ui.shared.util.LinkedFileInfo;

public class FieldInfo implements IFieldInfo {
	private String name;
	private String labelKey;
	private DataType dataType;
	private IFormatter formatter;
	private ICustomCell customCell;
	private String helpTextKey;
	private List<IValidator> validators;
	private boolean editable;
	private boolean canAddNew;
	private boolean canEdit;
	private boolean canSelect;
	private boolean canRemove;
	private int index;
	private boolean collection;
	private boolean id;
	private boolean generatedId;
	private boolean manyToOne;
	private boolean oneToOne;
	private boolean oneToMany;
	private String mappedBy;
	private boolean manyToMany;
	private boolean jpaTransient;
	private boolean isEnum;
	private String typeName;
	private boolean displayed;
	private boolean displayedInForm;
	private boolean notNull;
	private boolean notEmpty;
	private LinkedFileInfo linkedFileInfo;
	private String toStringAttribute;
	private String redrawOnUpdateLinkedObject;
	private String sortCriteria;

	// for UIManyToMany
	private String returnClass;
	private String joinClass;
	private String targetIdField;
	private String joinField;
	private String targetJoinField;
	
	private String importAttribute;

	private boolean preview;
	private boolean hidden;
	private boolean forceImport;
	private boolean importable;

	public FieldInfo(String name, boolean isCollection, String label, String toStringAtt,
			String sortCriteria, String redrawOnUpdateLinkedAttribute,
			DataType dataType, String formatterKey, String customCellKey,
			String helpTextInfoKey, String validatorKey, boolean editable, boolean isId,
			boolean isGeneratedId, boolean isManyToOne, boolean isOneToOne,
			boolean isOneToMany, String mappedBy, boolean isManyToMany,
			boolean isJpaTransient, boolean isEnum, boolean display, boolean displayInForm, int idx,
			String type, boolean isNotNull, boolean isNotEmpty, LinkedFileInfo lfi,
			boolean canAddNew, boolean canEdit, boolean canSelect, boolean canRemove,
			String importAttribute) {
		super();
		this.name = name;
		this.toStringAttribute = toStringAtt;
		this.sortCriteria = sortCriteria;
		this.redrawOnUpdateLinkedObject = redrawOnUpdateLinkedAttribute;
		this.collection = isCollection;
		this.dataType = dataType;
		this.editable = editable;
		this.id = isId;
		this.generatedId = isGeneratedId;
		this.manyToOne = isManyToOne;
		this.oneToOne = isOneToOne;
		this.oneToMany = isOneToMany;
		this.mappedBy = mappedBy != null && mappedBy.length() > 0 ? mappedBy : null;
		this.manyToMany = isManyToMany;
		this.jpaTransient = isJpaTransient;
		this.isEnum = isEnum;
		this.displayed = display;
		this.displayedInForm = displayInForm;
		if (formatterKey != null)
			this.formatter = FormatterFactory.getCustomFormatters().get(
					formatterKey);
		else
			this.formatter = FormatterFactory.getDefaultFormatters().get(
					dataType);
		if (customCellKey != null)
			this.customCell = WidgetFactory.getCustomCells().get(customCellKey);
		else
			this.customCell = null;
		this.helpTextKey = helpTextInfoKey;
		if (validatorKey != null)
			this.validators = ValidatorFactory.getValidators()
					.get(validatorKey);
		else
			this.validators = null;
		this.labelKey = label;
		this.index = idx;
		this.typeName = type;
		this.notNull = isNotNull;
		this.notEmpty = isNotEmpty;
		this.linkedFileInfo = lfi;
		this.canAddNew = canAddNew;
		this.canEdit = canEdit;
		this.canSelect = canSelect;
		this.canRemove = canRemove;
		this.importAttribute = importAttribute;
	}

	public boolean isId() {
		return id;
	}

	public boolean isGeneratedId() {
		return generatedId;
	}

	public boolean isManyToOne() {
		return manyToOne;
	}

	public boolean isOneToOne() {
		return oneToOne;
	}

	public boolean isOneToMany() {
		return oneToMany;
	}

	public boolean isManyToMany() {
		return manyToMany;
	}

	public boolean isJpaTransient() {
		return jpaTransient;
	}

	public boolean isCollection() {
		return collection;
	}

	public boolean isEnum() {
		return isEnum;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.hiperf.common.ui.IFieldInfo#getDataType()
	 */
	public DataType getDataType() {
		return dataType;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.hiperf.common.ui.IFieldInfo#isEditable()
	 */
	public boolean isEditable() {
		return editable && !generatedId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.hiperf.common.ui.IFieldInfo#getFormatter()
	 */
	public IFormatter getFormatter() {
		return formatter;
	}

	public String getLabel() {
		String l = WidgetFactory.getAttributesLabels().get(labelKey);
		if(l != null)
			return l;
		return name;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public String getMappedBy() {
		return mappedBy;
	}

	public boolean isDisplayed() {
		return displayed;
	}

	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}

	@Override
	public List<IValidator> getValidators() {
		return validators;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public boolean isLinkedFile() {
		return linkedFileInfo != null;
	}

	@Override
	public LinkedFileInfo getLinkedFileInfo() {
		return linkedFileInfo;
	}

	@Override
	public String getToStringAttribute() {
		return toStringAttribute;
	}

	public String getRedrawOnUpdateLinkedObject() {
		return redrawOnUpdateLinkedObject;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public ICustomCell getCustomCell() {
		return customCell;
	}

	public void setCustomCell(ICustomCell customCell) {
		this.customCell = customCell;
	}

	public String getSortCriteria() {
		return sortCriteria;
	}

	public void setSortCriteria(String sortCriteria) {
		this.sortCriteria = sortCriteria;
	}

	public String getReturnClass() {
		return returnClass;
	}

	public void setReturnClass(String returnClass) {
		this.returnClass = returnClass;
	}

	public String getJoinClass() {
		return joinClass;
	}

	public void setJoinClass(String joinClass) {
		this.joinClass = joinClass;
	}

	public String getTargetIdField() {
		return targetIdField;
	}

	public void setTargetIdField(String targetIdField) {
		this.targetIdField = targetIdField;
	}

	public String getJoinField() {
		return joinField;
	}

	public void setJoinField(String joinField) {
		this.joinField = joinField;
	}

	public String getTargetJoinField() {
		return targetJoinField;
	}

	public void setTargetJoinField(String targetJoinField) {
		this.targetJoinField = targetJoinField;
	}

	public boolean isNotEmpty() {
		return notEmpty;
	}

	public String getHelpTextKey() {
		return helpTextKey;
	}

	@Override
	public boolean canAddNew() {
		return canAddNew;
	}

	public void setCanAddNew(boolean canAddNew) {
		this.canAddNew = canAddNew;
	}

	@Override
	public boolean canEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	public boolean canSelect() {
		return canSelect;
	}

	public void setCanSelect(boolean canSelect) {
		this.canSelect = canSelect;
	}

	public boolean canRemove() {
		return canRemove;
	}

	@Override
	public String getImportAttribute() {
		return importAttribute;
	}

	@Override
	public boolean isDisplayedInForm() {
		return displayedInForm;
	}

	public void setDisplayedInForm(boolean displayedInForm) {
		this.displayedInForm = displayedInForm;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isForceImport() {
		return forceImport;
	}

	public void setForceImport(boolean forceImport) {
		this.forceImport = forceImport;
	}

	@Override
	public boolean isImportable() {
		return importable;
	}

	@Override
	public void setImportable(boolean importable) {
		this.importable = importable;
	}

	
	
	
}
