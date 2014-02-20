package com.hiperf.common.ui.client;


public interface ICustomCell extends ICell {

	ICustomCell newInstance(IWrapper wrapper, String attribute, boolean editable, IPanelMediator mediator);

	boolean isCustomStyle(boolean table);

	void setCellStyle(IWrappedTable table, int row, int idx);

}
