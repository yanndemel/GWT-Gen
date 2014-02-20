package com.hiperf.common.ui.client;

import com.hiperf.common.ui.client.exception.ParseException;



/**
 * Base interface for Formatters
 * 
 * If no formatter is provided in the {@link com.hiperf.common.ui.shared.annotation.UIAttribute} 
 * annotation of a getter of a {@link com.hiperf.common.ui.client.INakedObject} then the formatter 
 * corresponding to the data type will be used
 * <p>
 * <b>example:</b><br>
 * public class A implements INakedObject {<br>
 * 
 *&nbsp;&nbsp;&nbsp;&nbsp;public Integer getA() {&nbsp;&nbsp;//if no formatter info is present then the default formatter corresponding<br> 
 *&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return a;&nbsp;&nbsp;//	to the data type is chosen ({@link com.hiperf.common.ui.client.format.IntegerFormatter} in that case)<br> 
 *&nbsp;&nbsp;&nbsp;&nbsp;}<br>
 * 
 * }<br>
 * 
 *  @see com.hiperf.common.ui.shared.annotation.UIAttribute
 * */
public interface IFormatter<T> {
	
	String format(T o);
	
	T parse(String s) throws ParseException;
	
}
