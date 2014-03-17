package com.hiperf.gwtjpa.test.table.basic.client;

import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.UIAttribute;

public class B implements INakedObject {

	public B() {
	}
	
	
	private Double price;

	
	
	public B(Double price) {
		super();
		this.price = price;
	}


	
	@UIAttribute
	public Double getPrice() {
		return price;
	}




	@Override
	public String toString() {
		return price + " Euros";
	}
	
	

}
