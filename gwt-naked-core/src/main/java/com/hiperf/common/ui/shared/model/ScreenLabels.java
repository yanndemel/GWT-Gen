package com.hiperf.common.ui.shared.model;

import static javax.persistence.GenerationType.SEQUENCE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.UIAttribute;
import org.gwtgen.api.shared.UIClass;

@Entity
@Table(name = "HIP_SL")
@UIClass(tableLabelKey = "allScreenLabels", formLabelKey = "screenLabels")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement
public class ScreenLabels implements INakedObject {

	@SequenceGenerator(name = "hip_sl_generator", sequenceName = "SEQ_HIP_SL", allocationSize = 1)
	@Id
    @GeneratedValue(strategy = SEQUENCE, generator = "hip_sl_generator")
    @Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "LANG", nullable = false)
	@Enumerated(EnumType.ORDINAL)
	private LanguageEnum language;

	@Column(name = "FORM_LABEL")
	private String formLabel;

	@Column(name = "TABLE_LABEL")
	private String tableLabel;

	@Column(name = "CREATE_LABEL")
	private String createLabel;

	@Column(name = "SELECT_LABEL")
	private String selectLabel;

	@Column(name = "VIEW_LABEL")
	private String viewLabel;

	@Column(name = "EDIT_LABEL")
	private String editLabel;

	@ManyToOne
	@JoinColumn(name = "HIP_SC_ID", nullable = false)
	private ScreenConfig screenConfig;

	public ScreenLabels() {
		super();
	}
	
	@XmlTransient
	@UIAttribute(hidden = true)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@UIAttribute(index = 1, labelKey = "language")
	public LanguageEnum getLanguage() {
		return language;
	}

	public void setLanguage(LanguageEnum language) {
		this.language = language;
	}
	@UIAttribute(index = 2, labelKey = "formLabel")
	public String getFormLabel() {
		return formLabel;
	}

	public void setFormLabel(String formLabel) {
		this.formLabel = formLabel;
	}

	@UIAttribute(index = 3, labelKey = "tableLabel")
	public String getTableLabel() {
		return tableLabel;
	}

	public void setTableLabel(String tableLabel) {
		this.tableLabel = tableLabel;
	}

	@UIAttribute(index = 4, labelKey = "createLabel")
	public String getCreateLabel() {
		return createLabel;
	}

	public void setCreateLabel(String createLabel) {
		this.createLabel = createLabel;
	}

	@UIAttribute(index = 5, labelKey = "selectLabel")
	public String getSelectLabel() {
		return selectLabel;
	}

	public void setSelectLabel(String selectLabel) {
		this.selectLabel = selectLabel;
	}

	@UIAttribute(index = 6, labelKey = "viewLabel")
	public String getViewLabel() {
		return viewLabel;
	}

	public void setViewLabel(String viewLabel) {
		this.viewLabel = viewLabel;
	}

	@UIAttribute(index = 7, labelKey = "editLabel")
	public String getEditLabel() {
		return editLabel;
	}

	public void setEditLabel(String editLabel) {
		this.editLabel = editLabel;
	}

	@XmlTransient
	@UIAttribute(hidden = true)
	public ScreenConfig getScreenConfig() {
		return screenConfig;
	}

	public void setScreenConfig(ScreenConfig screenConfig) {
		this.screenConfig = screenConfig;
	}

	@Override
	public String toString() {
		return language.ordinal() + " - " + (formLabel != null ? formLabel : tableLabel != null ? tableLabel : createLabel != null ? createLabel : selectLabel != null ? selectLabel : viewLabel != null ? viewLabel : editLabel != null ? editLabel : "");
	}


}
