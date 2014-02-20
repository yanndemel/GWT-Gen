package com.hiperf.common.ui.client;

import java.util.Iterator;

/**
 * Interface use to represent Lazy collections sent by the server.
 * */
public interface ILazy {

	int size();
	boolean isInitialized();
	void setInitialized(boolean b);
	void setDescription(String description);
	void setOriginalSize(int originalSize);

	static final Iterator EMPTY_IT = new Iterator() {
		@Override
		public boolean hasNext() {
			return false;
		}
		@Override
		public Object next() {
			return null;
		}
		@Override
		public void remove() {
		}};


}
