package com.hiperf.common.ui.client.widget;

import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.IWrapper;

public interface IFilePanel {

	void setFilename(String s);

	String getFilename();

	IWrapper getWrapper();

	String getAttribute();

	void setId(String id);

	Widget getLabel();

}
