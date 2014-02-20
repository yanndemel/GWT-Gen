package com.hiperf.common.ui.shared.model;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "HIP_F")
public class Filter extends AbstractEntity {

	@SequenceGenerator(name = "hip_f_generator", sequenceName = "SEQ_HIP_F", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "hip_f_generator")
    @Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "CLASS_NAME")
	private String className;

	@Column(name = "VIEW_NAME")
	private String viewName;

	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "SORT_ATT")
	private String sortAttribute;

	@Column(name = "SORT_ASC")
	private Boolean sortAsc;
	
	@Column(name = "CASE_SENS")
	private Boolean caseSensitive;

	@OneToMany(targetEntity = FilterValue.class, fetch = FetchType.LAZY, cascade=CascadeType.ALL, mappedBy = "filter")
	@OrderBy(value="idx")
	private List<FilterValue> values = new ArrayList<FilterValue>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<FilterValue> getValues() {
		return values;
	}

	public void setValues(List<FilterValue> values) {
		this.values = values;
	}


	@Override
	public String toString() {
		return name;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public String getSortAttribute() {
		return sortAttribute;
	}

	public void setSortAttribute(String sortAttribute) {
		this.sortAttribute = sortAttribute;
	}

	public Boolean getSortAsc() {
		return sortAsc;
	}

	public void setSortAsc(Boolean sortAsc) {
		this.sortAsc = sortAsc;
	}

	public Boolean getCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(Boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}



}
