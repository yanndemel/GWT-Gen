package com.hiperf.common.ui.client.format;

import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class BooleanFormatter implements IFormatter<Boolean> {

	@Override
	public String format(Boolean o) {
		if(o != null)
			return o.toString();
		return null;
	}

	@Override
	public Boolean parse(String s) throws ParseException {
		if(s!=null && s.length() > 0) {
			try {
				return Boolean.parseBoolean(s);
			} catch (Exception e) {
				throw new ParseException("Bad value for Boolean "+s, e);
			}			
		}
		return null;
	}

}
