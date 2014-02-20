package com.hiperf.common.ui.client.format;

import java.util.HashMap;
import java.util.Map;

import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.IFormatter;

public class FormatterFactory {
	private static final Map<DataType, IFormatter> DEFAULT_FORMATTERS = new HashMap<DataType, IFormatter>();
	private static final Map<String, IFormatter> CUSTOM_FORMATTERS = new HashMap<String, IFormatter>();
	static {		
		DEFAULT_FORMATTERS.put(DataType.INT, new IntegerFormatter());
		DEFAULT_FORMATTERS.put(DataType.LONG, new LongFormatter());
		DEFAULT_FORMATTERS.put(DataType.SHORT, new ShortFormatter());
		DEFAULT_FORMATTERS.put(DataType.BYTE, new ByteFormatter());
		DEFAULT_FORMATTERS.put(DataType.CHAR, new CharFormatter());
		DEFAULT_FORMATTERS.put(DataType.DOUBLE, new DoubleFormatter());
		DEFAULT_FORMATTERS.put(DataType.FLOAT, new FloatFormatter());
		DEFAULT_FORMATTERS.put(DataType.BOOLEAN, new BooleanFormatter());
		DEFAULT_FORMATTERS.put(DataType.DATE, new DateFormatter());
		DEFAULT_FORMATTERS.put(DataType.STRING, new StringFormatter());
		
	}
	public static Map<DataType, IFormatter> getDefaultFormatters() {
		return DEFAULT_FORMATTERS;
	}
	
	public static Map<String, IFormatter> getCustomFormatters() {
		return CUSTOM_FORMATTERS;
	}
	
}
