package com.hiperf.common.ui.shared.util;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.model.ScreenLabels;

public class TableConfig implements IsSerializable {

	private int nbRows;
	private List<HeaderInfo> headers;
	private ScreenLabels labels;

	public TableConfig() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TableConfig(int nbRows, ScreenLabels labels, List<HeaderInfo> headers) {
		super();
		this.nbRows = nbRows;
		this.labels = labels;
		this.headers = headers;
	}

	public int getNbRows() {
		return nbRows;
	}

	public void setNbRows(int nbRows) {
		this.nbRows = nbRows;
	}

	public List<HeaderInfo> getHeaders() {
		return headers;
	}

	public void setHeaders(List<HeaderInfo> headers) {
		this.headers = headers;
	}

	public String getTableLabel() {
		return labels != null ? labels.getTableLabel() : null;
	}

	public String getFormLabel() {
		return  labels != null ? labels.getFormLabel() : null;
	}

	public String getCreateLabel() {
		return  labels != null ? labels.getCreateLabel() : null;
	}

	public String getSelectLabel() {
		return  labels != null ? labels.getSelectLabel() : null;
	}

	public String getViewLabel() {
		return  labels != null ? labels.getViewLabel() : null;
	}

	public String getEditLabel() {
		return  labels != null ? labels.getEditLabel() : null;
	}

}
