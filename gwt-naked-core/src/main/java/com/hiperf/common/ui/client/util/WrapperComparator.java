package com.hiperf.common.ui.client.util;

import java.util.Comparator;

import com.hiperf.common.ui.client.IWrapper;

public class WrapperComparator implements Comparator<IWrapper> {
	
	private String attribute;
	
	public WrapperComparator(String att) {
		super();
		this.attribute = att;
	}

	@Override
	public int compare(IWrapper o1, IWrapper o2) {
		if(o1.getNakedAttribute(attribute) == null)
			return o2.getNakedAttribute(attribute) == null ? 0 : -1;
		return o2.getNakedAttribute(attribute) == null ? 1 :
			o1.getNakedAttribute(attribute).toString().compareTo(o2.getNakedAttribute(attribute).toString());
	}
}