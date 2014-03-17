package com.hiperf.common.ui.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gwtgen.api.shared.INakedObject;

import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.shared.util.Id;

public class PersistenceManager {

	static final HashMap<String, Map<String, IFieldInfo>> idInfosByClass = new HashMap<String, Map<String, IFieldInfo>>();

	private static long lastLongSequence = 0;
	public static final String SEQ_PREFIX = "tmp-id";



	public static Map<String, IFieldInfo> getIdInfos(String wrappedClassName) {
		if(!idInfosByClass.containsKey(wrappedClassName)) {
			Map<String, IFieldInfo> l = new HashMap<String, IFieldInfo>();
			idInfosByClass.put(wrappedClassName, l);
			Map<String, IFieldInfo> map = WrapperContext.getFieldInfoByName().get(wrappedClassName);
			for(String att : map.keySet()) {
				IFieldInfo fi = map.get(att);
				if(fi.isId()) {
					l.put(att,fi);
				}
			}
		}
		return idInfosByClass.get(wrappedClassName);
	}

	public static Id getId(IWrapper wrapper) {
		List<String> idFields = new ArrayList<String>();
		List<Object> list = new ArrayList<Object>();
		Map<String, IFieldInfo> idInfos = getIdInfos(wrapper.getWrappedClassName());
		for(String att : idInfos.keySet()) {
			idFields.add(att);
		}
		for(String f : idFields) {
			Object att = wrapper.getNakedAttribute(f);
			if(att == null)
				return null;
			list.add(att);
		}
		return new Id(idFields, list);
	}

	public static Id getIdLocalPart(IWrapper wrapper) {
		List<String> idFields = new ArrayList<String>();
		List<Object> list = new ArrayList<Object>();
		Map<String, IFieldInfo> idInfos = getIdInfos(wrapper.getWrappedClassName());
		for(String att : idInfos.keySet()) {
			idFields.add(att);
		}
		Iterator<String> it = idFields.iterator();
		while(it.hasNext()) {
			String f = it.next();
			Object o = wrapper.getNakedAttribute(f);
			if(o != null && (o instanceof Long && ((Long)o).longValue() < 0) ||
					(o instanceof String && ((String)o).startsWith(PersistenceManager.SEQ_PREFIX))) {
				list.add(o);
			} else
				it.remove();
		}
		if(!idFields.isEmpty())
			return new Id(idFields, list);
		return null;
	}

	public static boolean isNull(Id id) {
		List<Object> fieldValues = id.getFieldValues();
		if(fieldValues.isEmpty())
			return true;
		for(Object o : fieldValues) {
			if(o==null)
				return true;
			if(o instanceof Integer && (Integer)o == 0)
				return true;
			if(o instanceof Long && (Long)o == 0L)
				return true;
		}
		return false;
	}

	public static Id getId(INakedObject no) {
		IWrapper w = WrapperContext.getEmptyWrappersMap().get(no.getClass().getName()).newWrapper();
		w.setContent(no);
		return getId(w);
	}

	public static Long nextLongId() {
		return --lastLongSequence;
	}

	public static String nextStringId() {
		return SEQ_PREFIX + nextLongId();
	}

	public static void reinitSequence() {
		lastLongSequence = 0;
	}

	public static boolean isLocal(Object o) {
		if(o instanceof Long && ((Long)o).longValue() < 0) {
			return true;
		} else if(o instanceof String && ((String)o).startsWith(PersistenceManager.SEQ_PREFIX)) {
			return true;
		}
		return false;
	}

	public static String appendFilters(String f1, String f2) {
		int i1 = f1.indexOf("where ");
		int s0 = f1.indexOf("select");
		if(i1 > 0 && s0 >= 0 && s0 < i1) {
			i1 = -1;
		}
		int j1 = f2.indexOf("where ");
		s0 = f2.indexOf("select");
		if(j1 > 0 && s0 >= 0 && s0 < j1) {
			j1 = -1;
		}
		if(i1 >=0 && j1 < 0)
			return f1 + " and (" + f2 + ")";
		else if(i1<0)
			return f2 + " and (" + f1 + ")";
		else {
			StringBuilder f = new StringBuilder();
			if(i1>0)
				f.append(f1.substring(0,i1)).append(" ");
			if(j1>0)
				f.append(f2.substring(0,j1)).append(" ");
			f.append("where ").append(f1.substring(i1+5)).append(" and (").append(f2.substring(j1+5)).append(" )");
			return f.toString();
		}
	}
	
}
