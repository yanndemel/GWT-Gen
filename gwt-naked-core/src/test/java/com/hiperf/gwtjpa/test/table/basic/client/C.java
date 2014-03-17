package com.hiperf.gwtjpa.test.table.basic.client;

import java.util.List;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.NotEmpty;
import org.gwtgen.api.shared.UIAttribute;

import com.hiperf.common.ui.client.i18n.INakedConstants;

public class C implements INakedObject {

	@Min(value = 5)
	private int min;
	
	@Max(value = 100)
	private long max;
	
	@DecimalMin(value = "5.02")
	@DecimalMax(value = "100.75")
	private double minMax;
	
	@Pattern(regexp = INakedConstants.REGEX_EMAIL)
	private String mail;
	
	@NotNull
	private Float price;
	
	@NotEmpty
	private List<String> list;
	
	@Size(min=2, max=5)
	private String size;
	
	@AssertTrue
	private Boolean isTrue;
	
	public C() {
		super();
		// TODO Auto-generated constructor stub
	}

	@UIAttribute(index = 1)
	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	@UIAttribute(index = 2)
	public long getMax() {
		return max;
	}

	public void setMax(long max) {
		this.max = max;
	}

	@UIAttribute(index = 3)
	public double getMinMax() {
		return minMax;
	}

	public void setMinMax(double minMax) {
		this.minMax = minMax;
	}

	@UIAttribute(index = 4)
	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	@UIAttribute(index = 5)
	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	@UIAttribute(index = 6)
	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

	@UIAttribute(index = 7)
	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	@UIAttribute(index = 8)
	public Boolean getIsTrue() {
		return isTrue;
	}

	public void setIsTrue(Boolean isTrue) {
		this.isTrue = isTrue;
	}


	
	
}
