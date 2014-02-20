package com.hiperf.common.ui.shared.util;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CollectionInfo implements IsSerializable {

	private int size;
	private String description;

	public CollectionInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CollectionInfo(int size, String description) {
		super();
		this.size = size;
		this.description = description;
	}

	public int getSize() {
		return size;
	}

	public String getDescription() {
		return description;
	}



}
