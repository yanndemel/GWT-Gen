package com.hiperf.common.ui.server.storage.impl;

import java.util.Map;

import com.hiperf.common.ui.shared.IConstants;

public class StorageHelper {

	public static void fillGetAllMap(Map<String, String> map, Object o) {
		if(o == null) {
			map.put(IConstants.NULL, null);
		} else {
			if(o instanceof Enum) {
				String name = ((Enum)o).name();
				map.put(name, ((Enum)o).getDeclaringClass().getName() + "." + name);
			}
			else
				map.put(o.toString(), o instanceof String ? null : o.toString());
		}
	}
	
}
