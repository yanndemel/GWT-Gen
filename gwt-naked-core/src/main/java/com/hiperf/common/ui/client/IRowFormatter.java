package com.hiperf.common.ui.client;

import org.gwtgen.api.shared.INakedObject;


public interface IRowFormatter<T extends INakedObject> {
	
	String getDefaultRowStyle();
	String getCustomRowStyle(T object);

}
