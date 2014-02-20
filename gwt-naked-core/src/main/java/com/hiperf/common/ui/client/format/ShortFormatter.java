package com.hiperf.common.ui.client.format;

import com.google.gwt.i18n.client.NumberFormat;
import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class ShortFormatter implements IFormatter<Short> {

	private String pattern;
	
	public ShortFormatter() {
		super();
	}

	public ShortFormatter(String pattern) {
		super();
		this.pattern = pattern;
	}

	@Override
	public String format(Short d) {
		if(d!=null) {
			if(pattern != null) {
				return NumberFormat.getFormat(pattern).format(d);
			} else {
				return NumberFormat.getDecimalFormat().format(d); 		
			}	
		}
		return null;
	}

	@Override
	public Short parse(String s) throws ParseException {	
		if(s != null && s.length() > 0) {
			try {
				if(pattern != null) {
					return (short)NumberFormat.getFormat(pattern).parse(s);
				} else {
					return (short)NumberFormat.getDecimalFormat().parse(s); 		
				}
			} catch (Exception e) {
				throw new ParseException("Bad value for Short "+s, e);
			}
		}
		return null;
	}

}
