package com.hiperf.common.ui.client.format;

import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class StringFormatter implements IFormatter<String> {
	
	@Override
	public String format(String o) {
		return o;			
	}

	@Override
	public String parse(String s) throws ParseException {
		return s;
	}
	
	

}
