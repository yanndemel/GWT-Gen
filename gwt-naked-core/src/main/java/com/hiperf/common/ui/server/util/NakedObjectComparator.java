package com.hiperf.common.ui.server.util;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.logging.Logger;

import org.gwtgen.api.shared.INakedObject;

public class NakedObjectComparator implements Comparator<INakedObject> {

	private static final Logger logger = Logger.getLogger(NakedObjectComparator.class.getName());
	
	private Method method;
	private boolean asc;
	
	
	
	public NakedObjectComparator() {
		super();
	}


	public NakedObjectComparator(Method method, boolean asc) {
		super();
		this.method = method;
		this.asc = asc;
	}


	@Override
	public int compare(INakedObject o1, INakedObject o2) {
		try {
			Object v1 = method.invoke(o1, new Object[0]);
			Object v2 = method.invoke(o2, new Object[0]);
			if(v1 == null && v2 != null) {
				return asc?-1:1;
			}
			if(v1 != null && v2 == null) {
				return asc?1:-1;
			}
			if(v1 != null && v2 != null) {
				if(v1 instanceof Comparable) {
					return asc ? ((Comparable)v1).compareTo((Comparable)v2) : ((Comparable)v2).compareTo((Comparable)v1);
				} else
					return asc ? (v1.toString().compareTo(v2.toString())) : (v2.toString()).compareTo(v1.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return 0;
		
	}

}
