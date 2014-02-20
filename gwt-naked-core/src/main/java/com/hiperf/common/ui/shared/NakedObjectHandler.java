package com.hiperf.common.ui.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.hiperf.common.ui.shared.util.Id;

public class NakedObjectHandler implements Serializable, IsSerializable {

	private String className;
	private Id id;

	public NakedObjectHandler() {
		className = null;
		id = null;
	}


	public NakedObjectHandler(String className, Id id) {
		super();
		this.className = className;
		this.id = id;
	}
	public String getClassName() {
		return className;
	}
	public Id getId() {
		return id;
	}


	@Override
	public String toString() {
		return className+" ("+id+")";
	}








}
