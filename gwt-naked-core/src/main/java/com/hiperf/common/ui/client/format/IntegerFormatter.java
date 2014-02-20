package com.hiperf.common.ui.client.format;

import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class IntegerFormatter implements IFormatter<Integer> {

	@Override
	public String format(Integer o) {
		if(o != null)
			return o.toString();
		return null;
	}

	@Override
	public Integer parse(String s) throws ParseException {
		if(s != null && s.length() > 0) {
			try {
				return Integer.parseInt(s);
			} catch (Exception e) {
				throw new ParseException("Bad value for Integer "+s, e);
			}
		}
		return null;
	}

}
