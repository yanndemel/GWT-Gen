package com.hiperf.common.ui.client.format;

import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class LongFormatter implements IFormatter<Long> {

	@Override
	public String format(Long o) {
		if(o!=null)
			return o.toString();
		return null;
	}

	@Override
	public Long parse(String s) throws ParseException {
		if(s != null && s.length() > 0) {
			try {
				return Long.parseLong(s);
			} catch (Exception e) {
				throw new ParseException(s+" is not a Long number", e);
			}
		}
		return null;
	}

}
