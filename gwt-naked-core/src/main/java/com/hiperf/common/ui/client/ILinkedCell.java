package com.hiperf.common.ui.client;

import com.google.gwt.user.client.ui.Label;


/**
 * Interface implemented by the widgets wrapping a {@link com.hiperf.common.ui.client.INakedObject}
 * */
public interface ILinkedCell extends ICell {
	IWrapper getParentWrapper();
	void showDetail(boolean editable);
	String getLinkedObjectClass();
	boolean isEditable();
	boolean isPersistent();
	boolean isNull();
	IPanelMediator getMediator();
	Label getLabel();
	public abstract int getStateIdx();
}
