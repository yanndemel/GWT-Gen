package com.hiperf.common.ui.client;

import java.util.List;

import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.IsWidget;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.util.Id;

public interface IWrappedObjectForm extends IsWidget {

	IWrapper getWrapper();

	IPanelMediator getMediator();

	String getFormTitle();

	void setValueError(String attribute, String msg);

	boolean hasErrors();

	void clearErrors();

	List<HeaderInfo> getSortedFields();

	HTMLTable getFormGrid();
	
	void redrawRow(int row, Id id);

	void setWidth(String w);

	void setHeight(String h);

	IsWidget getCell(int row);

	IsWidget getCell(String attribute);

	void setFormTitle(String title);

}
