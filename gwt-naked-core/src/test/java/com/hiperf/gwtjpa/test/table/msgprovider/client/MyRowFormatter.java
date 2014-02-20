package com.hiperf.gwtjpa.test.table.msgprovider.client;

import com.hiperf.common.ui.client.IRowFormatter;

public class MyRowFormatter implements IRowFormatter<B> {

	
	
	public MyRowFormatter() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getDefaultRowStyle() {
		return "myStyle";
	}

	@Override
	public String getCustomRowStyle(B b) {
		if(b.getPrice() > 0)
			return "green";
		return null;
	}

}
