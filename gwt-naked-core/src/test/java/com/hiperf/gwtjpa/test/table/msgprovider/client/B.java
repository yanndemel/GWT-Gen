package com.hiperf.gwtjpa.test.table.msgprovider.client;

import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.UIAttribute;
import org.gwtgen.api.shared.UIClass;

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
