package com.hiperf.common.ui.shared.model;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.hiperf.common.ui.client.IAuditable;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.shared.annotation.UIAttribute;
import com.hiperf.common.ui.shared.annotation.UIClass;


/**
 * @author Yann
 *
 */
@Entity
@Table(name = "HIP_SC")
@UIClass(tableLabelKey = "screenConfigs", formLabelKey = "screenConfig", onUpdate="com.hiperf.common.ui.client.event.ScreenConfigHandler")
@XmlRootElement
public class ScreenConfig implements INakedObject, IAuditable {

	@SequenceGenerator(name = "hip_sc_generator", sequenceName = "SEQ_HIP_SC", allocationSize = 1)
	@Id
    @GeneratedValue(strategy = SEQUENCE, generator = "hip_sc_generator")
    @Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "CLASS_NAME")
	private String className;

	@Column(name = "VIEW_NAME")
	private String viewName;

	@Column(name = "USER_NAME")
	private String createUser;

	@Column(name = "NB_ROWS")
	private int nbRows;

	@Column(name = "DEFAULT_CONF")
	private boolean defaultConfig;

	@OneToMany(targetEntity = ScreenHeaderInfo.class, fetch = FetchType.LAZY, cascade=CascadeType.ALL, mappedBy = "screenConfig")
	private List<ScreenHeaderInfo> headers;

	@OneToMany(targetEntity = ScreenLabels.class, fetch = FetchType.LAZY, cascade=CascadeType.ALL, mappedBy = "screenConfig")
	private List<ScreenLabels> labels;

	public ScreenConfig() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@XmlTransient
	@UIAttribute(hidden = true)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@UIAttribute(index = 1, labelKey = "viewName", customCellClass = "com.hiperf.common.ui.client.widget.custom.ViewNamesCombo")
	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	@UIAttribute(display = false, labelKey = "className")
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@UIAttribute(labelKey = "userName", editable = false)
	public String getCreateUser() {
		return createUser;
	}

	@XmlElementWrapper(name="headers")
	@XmlElementRef()
	@UIAttribute(index = 2, labelKey = "headers", canAddNew = false, canRemove = false)
	public List<ScreenHeaderInfo> getHeaders() {
		return headers;
	}

	public void setHeaders(List<ScreenHeaderInfo> headers) {
		this.headers = headers;
	}

	@UIAttribute(index = 3, labelKey = "nbRows")
	public int getNbRows() {
		return nbRows;
	}

	public void setNbRows(int nbRows) {
		this.nbRows = nbRows;
	}

	@UIAttribute(index = 5, labelKey = "defaultConfig", hidden = true)
	public boolean isDefaultConfig() {
		return defaultConfig;
	}

	public void setDefaultConfig(boolean defaultConfig) {
		this.defaultConfig = defaultConfig;
	}

	@XmlElementWrapper
	@XmlElement(name="label")
	@UIAttribute(index = 4, labelKey = "labels")
	public List<ScreenLabels> getLabels() {
		return labels;
	}

	public void setLabels(List<ScreenLabels> labels) {
		this.labels = labels;
	}

	@Override
	public void setCreateDate(Date date) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setModifyDate(Date date) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setModifyUser(String modifyUser) {
		this.createUser = modifyUser;
	}

	@Override
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}


}
