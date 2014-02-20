package com.hiperf.common.ui.client;


public interface IRowFormatter<T extends INakedObject> {
	
	String getDefaultRowStyle();
	String getCustomRowStyle(T object);

}
