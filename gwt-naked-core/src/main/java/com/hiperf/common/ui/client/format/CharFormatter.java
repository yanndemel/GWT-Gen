package com.hiperf.common.ui.client.format;

import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class CharFormatter implements IFormatter<Character> {

	@Override
	public String format(Character o) {
		if(o != null)
			return o.toString();
		return null;
	}

	@Override
	public Character parse(String s) throws ParseException {
		if(s != null && s.length() > 0) {
			try {
				return s.charAt(0);
			} catch (Exception e) {
				throw new ParseException("Bad value for Char "+s, e);
			}	
		}
		return null;
	}

}
