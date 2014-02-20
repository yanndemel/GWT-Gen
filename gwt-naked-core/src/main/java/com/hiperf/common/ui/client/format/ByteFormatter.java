package com.hiperf.common.ui.client.format;

import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.exception.ParseException;

public class ByteFormatter implements IFormatter<Byte> {

	@Override
	public String format(Byte o) {
		if(o!=null)
			return o.toString();
		return null;
	}

	@Override
	public Byte parse(String s) throws ParseException {
		if(s!=null && s.length() > 0) {
			try {
				return Byte.parseByte(s);
			} catch (Exception e) {
				throw new ParseException("Bad value for Byte "+s, e);
			}
		}
		return null;
	}

}
