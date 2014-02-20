package com.hiperf.common.rebind;

import java.util.Arrays;
import java.util.Set;

import com.google.gwt.dev.util.collect.HashSet;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IValidator;

public class AttributeInfo {

	public static enum Type {
		DEFAULT, COLLECTION, ENUM
	};

	public static enum CollectionType {
		LIST, SET
	};

	public class ValidatorInfo {
		private Class clazz;
		private Object[] params;

		public ValidatorInfo(Class clazz, Object[] params) {
			super();
			this.clazz = clazz;
			this.params = params;
		}

		public Class getClazz() {
			return clazz;
		}

		public Object[] getParams() {
			return params;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + Arrays.hashCode(params);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ValidatorInfo other = (ValidatorInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			if (!Arrays.equals(params, other.params))
				return false;
			return true;
		}

		
		
		public void setParams(Object[] params) {
			this.params = params;
		}

		private AttributeInfo getOuterType() {
			return AttributeInfo.this;
		}

	}

	private Type type;
	private CollectionType collectionType;
	private String name;
	private String labelKey = null;
	private boolean editable = false;
	private boolean canAddNew;
	private boolean canEdit;
	private boolean canSelect;
	private boolean canRemove;
	private boolean displayed = true;
	private boolean displayedInForm = true;
	private int index = 0;
	private String formatterKey = null;
	private String formatterClass = null;
	private String validatorKey;
	private Set<ValidatorInfo> validators;
	private String dataType = null;
	private String javaType = null;
	private String realJavaTypeName = null;
	private boolean id;
	private boolean notNull;
	private boolean notEmpty;
	private boolean unique;
	private boolean generatedId;
	private boolean manyToOne;
	private boolean oneToOne;
	private boolean oneToMany;
	private boolean manyToMany;
	private boolean jpaTransient;
	private Set<String> enumConstants;
	private String mappedBy;
	private boolean linkedFile = false;
	private boolean linkedFileDownload;
	private boolean linkedFileUpload;
	private String linkedFileClassName;
	private String linkedFileStorageFieldName;
	private String linkedFileName;
	private String linkedFileLocalKeyField;
	private String toStringAttribute;
	private String redrawOnUpdateLinkedObject;
	private String customCellKey;
	private String customCellClass;
	private String sortCriteria;
	private String helpTextKey;

	// for UIManyToMany
	private String returnClass;
	private String joinClass;
	private String targetIdField;
	private String joinField;
	private String targetJoinField;
	
	private String importAttribute;	
	
	private String orderBy;
	private boolean orderAsc = true;
	
	private boolean preview;
	private boolean url;
	private boolean hidden;
	private boolean forceImport;
	private boolean importable;
	
	
	public AttributeInfo(Type type) {
		this.type = type;
		if (type.equals(Type.ENUM)) {
			enumConstants = new HashSet<String>();
		}
	}

	public boolean isJpaTransient() {
		return jpaTransient;
	}

	public void setJpaTransient(boolean jpaTransient) {
		this.jpaTransient = jpaTransient;
	}

	public boolean isManyToOne() {
		return manyToOne;
	}

	public void setManyToOne(boolean manyToOne) {
		this.manyToOne = manyToOne;
	}

	public boolean isOneToOne() {
		return oneToOne;
	}

	public void setOneToOne(boolean oneToOne) {
		this.oneToOne = oneToOne;
	}

	public boolean isOneToMany() {
		return oneToMany;
	}

	public void setOneToMany(boolean oneToMany) {
		this.oneToMany = oneToMany;
	}

	public boolean isManyToMany() {
		return manyToMany;
	}

	public void setManyToMany(boolean manyToMany) {
		this.manyToMany = manyToMany;
	}

	public boolean isId() {
		return id;
	}

	public void setId(boolean id) {
		this.id = id;
	}

	public boolean isGeneratedId() {
		return generatedId;
	}

	public void setGeneratedId(boolean generatedId) {
		this.generatedId = generatedId;
	}

	public String getRealJavaTypeName() {
		return realJavaTypeName;
	}

	public void setRealJavaTypeName(String realJavaTypeName) {
		this.realJavaTypeName = realJavaTypeName;
	}

	public CollectionType getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(CollectionType collectionType) {
		this.collectionType = collectionType;
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public boolean isNakedObject() {
		return javaType.equals(INakedObject.class.getSimpleName());
	}

	public boolean isCollection() {
		return type.equals(Type.COLLECTION);
	}

	public boolean isEnum() {
		return type.equals(Type.ENUM);
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getFormatterClass() {
		return formatterClass;
	}

	public void setFormatterClass(String formatterClass) {
		this.formatterClass = formatterClass;
	}

	public String getFormatterKey() {
		return formatterKey;
	}

	public void setFormatterKey(String formatterKey) {
		this.formatterKey = formatterKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public String getLabelKey() {
		return labelKey;
	}

	public void setLabelKey(String label) {
		this.labelKey = label;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void addEnumConstant(String name) {
		enumConstants.add(name);
	}

	public Set<String> getEnumConstants() {
		return enumConstants;
	}

	public String getMappedBy() {
		return mappedBy;
	}

	public void setMappedBy(String mappedBy) {
		this.mappedBy = mappedBy;
	}

	public boolean isDisplayed() {
		return displayed;
	}

	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}

	public void addValidator(Class<? extends IValidator> clazz, Object[] params) {
		if (validators == null) {
			validators = new HashSet<ValidatorInfo>();
		}
		validators.add(new ValidatorInfo(clazz, params));
	}

	public Set<ValidatorInfo> getValidators() {
		return validators;
	}

	public String getValidatorKey() {
		return validatorKey;
	}

	public void setValidatorKey(String validatorKey) {
		this.validatorKey = validatorKey;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}

	public boolean isLinkedFile() {
		return linkedFile;
	}

	public void setLinkedFile(boolean linkedFile) {
		this.linkedFile = linkedFile;
	}

	public boolean isLinkedFileDownload() {
		return linkedFileDownload;
	}

	public void setLinkedFileDownload(boolean linkedFileDownload) {
		this.linkedFileDownload = linkedFileDownload;
	}

	public boolean isLinkedFileUpload() {
		return linkedFileUpload;
	}

	public void setLinkedFileUpload(boolean linkedFileUpload) {
		this.linkedFileUpload = linkedFileUpload;
	}

	public String getLinkedFileClassName() {
		return linkedFileClassName;
	}

	public void setLinkedFileClassName(String linkedFileClassName) {
		this.linkedFileClassName = linkedFileClassName;
	}

	public String getLinkedFileStorageFieldName() {
		return linkedFileStorageFieldName;
	}

	public void setLinkedFileStorageFieldName(String linkedFileStorageFieldName) {
		this.linkedFileStorageFieldName = linkedFileStorageFieldName;
	}

	public String getLinkedFileLocalKeyField() {
		return linkedFileLocalKeyField;
	}

	public void setLinkedFileLocalKeyField(String linkedFileLocalKeyField) {
		this.linkedFileLocalKeyField = linkedFileLocalKeyField;
	}

	public String getLinkedFileName() {
		return linkedFileName;
	}

	public void setLinkedFileName(String linkedFileName) {
		this.linkedFileName = linkedFileName;
	}

	public String getToStringAttribute() {
		return toStringAttribute;
	}

	public void setToStringAttribute(String toStringAtt) {
		this.toStringAttribute = toStringAtt;
	}

	public String getRedrawOnUpdateLinkedObject() {
		return redrawOnUpdateLinkedObject;
	}

	public void setRedrawOnUpdateLinkedObject(
			String redrawOnUpdateLinkedAttribute) {
		this.redrawOnUpdateLinkedObject = redrawOnUpdateLinkedAttribute;
	}

	public String getCustomCellClass() {
		return customCellClass;
	}

	public void setCustomCellClass(String customCellClass) {
		this.customCellClass = customCellClass;
	}

	public String getCustomCellKey() {
		return customCellKey;
	}

	public void setCustomCellKey(String customCellKey) {
		this.customCellKey = customCellKey;
	}

	public void setSortCriteria(String sortCriteria) {
		this.sortCriteria = sortCriteria;
	}

	public String getSortCriteria() {
		return sortCriteria;
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

	public void setNotEmpty(boolean notEmpty) {
		this.notEmpty = notEmpty;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public String getHelpTextKey() {
		return helpTextKey;
	}

	public void setHelpTextKey(String helpTextKey) {
		this.helpTextKey = helpTextKey;
	}

	public boolean canAddNew() {
		return canAddNew;
	}

	public void setCanAddNew(boolean canAddNew) {
		this.canAddNew = canAddNew;
	}

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

	public void setCanRemove(boolean canRemove) {
		this.canRemove = canRemove;
	}

	public String getImportAttribute() {
		return importAttribute;
	}

	public void setImportAttribute(String importAttribute) {
		this.importAttribute = importAttribute;
	}

	public boolean isDisplayedInForm() {
		return displayedInForm;
	}

	public void setDisplayedInForm(boolean displayedInForm) {
		this.displayedInForm = displayedInForm;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public boolean isOrderAsc() {
		return orderAsc;
	}

	public void setOrderAsc(boolean orderAsc) {
		this.orderAsc = orderAsc;
	}

	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	public boolean isUrl() {
		return url;
	}

	public void setUrl(boolean url) {
		this.url = url;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isForceImport() {
		return forceImport;
	}

	public void setForceImport(boolean forceImport) {
		this.forceImport = forceImport;
	}

	public boolean isImportable() {
		return importable;
	}

	public void setImportable(boolean importable) {
		this.importable = importable;
	}

	
	

}
