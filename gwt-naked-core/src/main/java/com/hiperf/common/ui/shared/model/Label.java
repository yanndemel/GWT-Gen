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
import javax.xml.bind.annotation.XmlTransient;

import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.UIAttribute;
import org.gwtgen.api.shared.UIClass;

@Entity
@Table(name = "HIP_LABELS")
@UIClass(tableLabelKey = "labels", formLabelKey = "label")
public class Label implements INakedObject {

	@SequenceGenerator(name = "hip_lbl_generator", sequenceName = "SEQ_HIP_LBL", allocationSize = 1)
	@Id
    @GeneratedValue(strategy = SEQUENCE, generator = "hip_lbl_generator")
    @Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "LANG", nullable = false)
	@Enumerated(EnumType.ORDINAL)
	private LanguageEnum language;

	@Column(name = "LABEL")
	private String label;

	@ManyToOne
	@JoinColumn(name = "HIP_SHI_ID", nullable = false)
	private ScreenHeaderInfo header;

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

	@UIAttribute(index = 2, labelKey = "label")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return language.name() + " - " + label;
	}

	@XmlTransient
	@UIAttribute(hidden = true)
	public ScreenHeaderInfo getHeader() {
		return header;
	}

	public void setHeader(ScreenHeaderInfo h) {
		this.header = h;
	}


}
