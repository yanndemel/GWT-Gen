package com.hiperf.common.ui.client.format;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class DateFormatter implements IFormatter<Date> {

	public static final PredefinedFormat DEFAULT_DATE_FORMAT = PredefinedFormat.DATE_MEDIUM;
	private DateTimeFormat formatter;
	
	public DateFormatter() {
		super();
		formatter = DateTimeFormat.getFormat(DEFAULT_DATE_FORMAT);
	}

	public DateFormatter(String dateFormat) {
		super();
		formatter = DateTimeFormat.getFormat(dateFormat);
	}

	@Override
	public String format(Date o) {		
		if(o != null)
			return formatter.format(o);
		return null;
	}

	
	
	public DateTimeFormat getFormatter() {
		return formatter;
	}

	@Override
	public Date parse(String s) throws ParseException {
		if(s != null && s.length() > 0) {
			try {
				return formatter.parse(s);
			} catch (Exception e) {
				throw new ParseException("Cannot parse date "+s+" with DateTimeFormatter (pattern = "+formatter.getPattern()+")");
			}	
		}
		return null;
	}

	
}
