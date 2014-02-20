package com.hiperf.gwtjpa.test.table.msgprovider.client;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.shared.annotation.UIAttribute;
import com.hiperf.common.ui.shared.annotation.UIClass;

@UIClass(tableLabelKey="myTable", formLabelKey="myForm", rowFormatter = "com.hiperf.gwtjpa.test.table.msgprovider.client.MyRowFormatter")
public class B implements INakedObject {

	public B() {
	}
	
	
	private Double price;

	
	
	public B(Double price) {
		super();
		this.price = price;
	}


	
	@UIAttribute(labelKey="myLabel")
	public Double getPrice() {
		return price;
	}




	@Override
	public String toString() {
		return price + " Euros";
	}
	
	

}
