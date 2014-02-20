package com.hiperf.common.ui.shared.model;

import static javax.persistence.GenerationType.SEQUENCE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.google.gwt.user.client.rpc.IsSerializable;

@Entity
@Table(name = "HIP_HF")
public class HFilter implements IsSerializable {
	
	@SequenceGenerator(name = "hip_hf_generator", sequenceName = "SEQ_HIP_HF", allocationSize = 1)
	@Id
    @GeneratedValue(strategy = SEQUENCE, generator = "hip_hf_generator")
    @Column(name = "ID", nullable = false)
	private Long id;
	
	@Column(name = "NAME", nullable = false)
	private String name;
	
	@Column(name = "L_DT", nullable = false)
	private long dt;
	
	public HFilter() {
		super();
	}

	public HFilter(String name) {
		super();
		this.name = name;
		this.dt = System.currentTimeMillis();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDt(long dt) {
		this.dt = dt;
	}

	public String getName() {
		return name;
	}
	public long getDt() {
		return dt;
	}
	
	
	
	
}
