package com.hiperf.common.ui.client.format;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class LongToDateTimeFormatter implements IFormatter<Long> {

	private DateTimeFormat formatter;
	
	public LongToDateTimeFormatter() {
		super();
		formatter = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
	}

	public LongToDateTimeFormatter(String dateFormat) {
		super();
		formatter = DateTimeFormat.getFormat(dateFormat);
	}

	@Override
	public String format(Long o) {		
		if(o != null && o > 0L) {
			Date d = new Date(o);
			return formatter.format(d);
		}
		return null;
	}

	
	
	public DateTimeFormat getFormatter() {
		return formatter;
	}

	@Override
	public Long parse(String s) throws ParseException {
		if(s != null && s.trim().length() > 0) {
			try {
				return formatter.parse(s.trim()).getTime();
			} catch (Exception e) {
				throw new ParseException("Cannot parse date "+s+" with DateTimeFormatter (pattern = "+formatter.getPattern()+")");
			}	
		}
		return null;
	}

	
}
