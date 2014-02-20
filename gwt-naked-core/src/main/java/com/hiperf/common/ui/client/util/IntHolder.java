package com.hiperf.common.ui.client.util;

public class IntHolder {

	private int i = 0;
	
	public IntHolder(int i) {
		super();
		this.i = i;
	}

	public void increment() {
		i++;
	}
	
	public int get() {
		return i;
	}
	
}
