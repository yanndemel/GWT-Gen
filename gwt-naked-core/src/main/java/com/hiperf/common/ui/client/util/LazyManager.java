package com.hiperf.common.ui.client.util;

import java.util.Map;

import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.shared.ILazyId;
import com.hiperf.common.ui.shared.PersistenceManager;

public class LazyManager {

	public static boolean isLazy(IWrapper w) {		
		String wrappedClassName = w.getWrappedClassName();
		Map<String, IFieldInfo> fis = PersistenceManager.getIdInfos(wrappedClassName);
		boolean b = true;
		if(!w.isLazy()) {
			for(String att : fis.keySet()) {
				IFieldInfo fi = fis.get(att);
				DataType type = fi.getDataType();
				if(!type.equals(DataType.NAKED_OBJECT)) {
					b = b && isLazy(type, w.getAttribute(att));
				}
			}			
		}
		return b;
	}



	private static boolean isLazy(DataType type, String s) {
		if(s == null)
			return false;
		switch(type) {
			case STRING:
				return s.equals(ILazyId.STRING);
			case LONG:
				return Long.parseLong(s) == ILazyId.LONG;
			case INT:
				return Integer.parseInt(s) == ILazyId.INT;
			default:
				return false;
		}
	}
	
	public static void setLazy(IWrapper w) {
		String wrappedClassName = w.getWrappedClassName();
		Map<String, IFieldInfo> fis = PersistenceManager.getIdInfos(wrappedClassName);
		for(String att : fis.keySet()) {
			IFieldInfo fi = fis.get(att);
			DataType type = fi.getDataType();	
			switch(type) {
				case STRING:
					w.setObjectAttribute(att, ILazyId.STRING, false);
					break;
				case LONG:
					w.setObjectAttribute(att, ILazyId.LONG, false);
					break;
				case INT:
					w.setObjectAttribute(att, ILazyId.INT, false);
					break;
				default:
					break;
			}
		}
	}
}
