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


@Entity
@Table(name = "HIP_FV")
public class FilterValue extends AbstractEntity {

	@SequenceGenerator(name = "hip_fv_generator", sequenceName = "SEQ_HIP_FV", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "hip_fv_generator")
    @Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "IDX")
	private int idx;

	@Column(name="PREV_AND")
	private boolean previousAnd;

	@Column(name = "ATTRIBUTE")
	private String attribute;

	@Column(name = "TYPE")
	@Enumerated(EnumType.ORDINAL)
	private FilterType type;

	@Column(name = "OPE")
	private String operator;

	@Column(name = "VALUE1")
	private String value1;

	@Column(name = "VALUE2")
	private String value2;

	@ManyToOne
	@JoinColumn(name = "HIP_F_ID", nullable = false)
	private Filter filter;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String value1) {
		this.value1 = value1;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public boolean isPreviousAnd() {
		return previousAnd;
	}

	public void setPreviousAnd(boolean previousAnd) {
		this.previousAnd = previousAnd;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public FilterType getType() {
		return type;
	}

	public void setType(FilterType type) {
		this.type = type;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}


}
