package com.hiperf.common.ui.client;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Base interface for all widgets displayed in each cell of a {@link com.hiperf.common.ui.client.widget.WrappedFlexTable}
 * */
public interface ICell extends IsWidget {

	IWrapper getWrapper();

	String getAttribute();

	void redraw();

	int getAbsoluteTop();

	int getAbsoluteLeft();

	void setTabIndex(int idx);

}
