package com.hiperf.common.ui.client.format;

import com.google.gwt.i18n.client.NumberFormat;
import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class DoubleFormatter implements IFormatter<Double> {

	private String pattern;
	
	public DoubleFormatter() {
		super();
	}

	public DoubleFormatter(String pattern) {
		super();
		this.pattern = pattern;
	}

	@Override
	public String format(Double d) {
		if(d != null) {
			if(pattern != null) {
				return NumberFormat.getFormat(pattern).format(d);
			} else {
				return NumberFormat.getDecimalFormat().format(d); 		
			}	
		}
		return null;
	}

	@Override
	public Double parse(String s) throws ParseException {
		if(s != null && s.length() > 0) {
			try {
				if(pattern != null) {
					return NumberFormat.getFormat(pattern).parse(s);
				} else {
					return NumberFormat.getDecimalFormat().parse(s); 		
				}
			} catch (Exception e) {
				throw new ParseException("Bad value for Double "+s, e);
			}
		}
		return null;
	}

}
