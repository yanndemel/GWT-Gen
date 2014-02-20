package com.hiperf.common.ui.shared.model;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.shared.CommonUtil;
import com.hiperf.common.ui.shared.annotation.UIAttribute;
import com.hiperf.common.ui.shared.annotation.UIClass;

@Entity
@Table(name = "HIP_SCI")
@UIClass(tableLabelKey = "screenHeaders", formLabelKey = "screenHeader", validator = "com.hiperf.common.ui.client.validation.ScreenHeaderInfoValidator")
@XmlRootElement
public class ScreenHeaderInfo implements INakedObject {

	@SequenceGenerator(name = "hip_shi_generator", sequenceName = "SEQ_HIP_SHI", allocationSize = 1)
	@Id
    @GeneratedValue(strategy = SEQUENCE, generator = "hip_shi_generator")
    @Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "ATTRIBUTE", nullable = false)
	private String attribute;

	@OneToMany(targetEntity = Label.class, fetch = FetchType.LAZY, cascade=CascadeType.ALL, mappedBy = "header")
	private List<Label> labels;

	@Column(name = "DISPLAYED", nullable = false)
	private boolean displayed;
	
	@Column(name = "EDITABLE", nullable = true)
	private Boolean editable;

	@Column(name = "IDX", nullable = false)
	private int index;

	@ManyToOne
	@JoinColumn(name = "HIP_SC_ID", nullable = false)
	private ScreenConfig screenConfig;		

	public ScreenHeaderInfo() {
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

	@UIAttribute(labelKey = "attribute", customCellClass = "com.hiperf.common.ui.client.widget.custom.ClassAttributesCombo")
	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}


	@UIAttribute(labelKey = "labels")
	public List<Label> getLabels() {
		return labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	@UIAttribute(labelKey = "index")
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@XmlTransient
	@UIAttribute(hidden = true)
	public ScreenConfig getScreenConfig() {
		return screenConfig;
	}

	public void setScreenConfig(ScreenConfig screenConfig) {
		this.screenConfig = screenConfig;
	}

	@UIAttribute(labelKey = "displayed")
	public boolean isDisplayed() {
		return displayed;
	}

	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}

	public String getLabel(String langCode) {
		LanguageEnum le = CommonUtil.getLanguage(langCode);
		if(labels != null && !labels.isEmpty()) {
			for(Label l : labels) {
				if(le.equals(l.getLanguage())) {
					return l.getLabel();
				}
			}
		}
		return null;
	}

	@UIAttribute(hidden = true)
	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}
	
	

}
