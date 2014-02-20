package com.hiperf.common.ui.client.widget;

import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.user.client.ui.PositionCallback;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public interface ICloseablePopupPanel extends HasCloseHandlers<ICloseablePopupPanel> {

	void setForceClose(boolean b);

	void hide();

	void center();

	boolean isShowing();

	void setPopupPosition(int absoluteLeft, int absoluteTop);

	void add(Widget w);

	void show();

	Widget getMainWidget();

	boolean isForceClose();

	PopupMediator getMediator();

	void clearMainPanel();

	void setPopupPositionAndShow(PositionCallback pc);

	void clear();

	void showRelativeTo(UIObject w);

	void setWidth(String string);
	
	 void setPopupTitle(String s);

	void setHeight(String string);

	void initPopup(boolean hideOnEscape);

	int getOffsetHeight();

	int getOffsetWidth();

}
