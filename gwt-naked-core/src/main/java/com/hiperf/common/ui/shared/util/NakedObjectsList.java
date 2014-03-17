package com.hiperf.common.ui.shared.util;

import java.util.List;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NakedObjectsList implements IsSerializable {

	private List<INakedObject> list;
	private int totalPages;
	private int count;
	private String filter;
	private String[] constantFilterFields;

	public NakedObjectsList() {}

	public NakedObjectsList(List<INakedObject> list, int count, int rowsPerPage, String currentFilter) {
		super();
		this.list = list;
		this.count = count;
		if(count % rowsPerPage == 0)
			totalPages = count / rowsPerPage;
		else
			totalPages = count / rowsPerPage + 1;
	}

	public List<INakedObject> getList() {
		return list;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public int getCount() {
		return count;
	}

	public String getFilter() {
		return filter;
	}

	public void setConstantFilterFields(String[] fields) {
		this.constantFilterFields = fields;
	}

	public String[] getConstantFilterFields() {
		return constantFilterFields;
	}



}
