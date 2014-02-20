package com.hiperf.gwtjpa.test.table.basic.client;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.shared.annotation.UIAttribute;

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
