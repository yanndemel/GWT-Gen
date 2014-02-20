package com.hiperf.common.ui.client.format;

import com.google.gwt.i18n.client.NumberFormat;
import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class FloatFormatter implements IFormatter<Float> {

	private String pattern;
	
	public FloatFormatter() {
		super();
	}

	public FloatFormatter(String pattern) {
		super();
		this.pattern = pattern;
	}

	@Override
	public String format(Float d) {
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
	public Float parse(String s) throws ParseException {
		if(s != null && s.length() > 0) {
			try {
				if(pattern != null) {
					return (float)NumberFormat.getFormat(pattern).parse(s);
				} else {
					return (float)NumberFormat.getDecimalFormat().parse(s); 		
				}
			} catch (NumberFormatException e) {
				throw new ParseException("Bad value for Float "+s, e);
			}
		}
		return null;
	}

}
