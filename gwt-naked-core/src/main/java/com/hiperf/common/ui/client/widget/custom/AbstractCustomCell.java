package com.hiperf.common.ui.client.widget.custom;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.ICustomCell;
import com.hiperf.common.ui.client.IWrappedTable;
import com.hiperf.common.ui.client.IWrapper;

public abstract class AbstractCustomCell extends Composite implements ICustomCell {

	protected IWrapper w;
	protected String att;

	
	
	public AbstractCustomCell(IWrapper w, String att) {
		this();
		this.w = w;
		this.att = att;
	}

	public AbstractCustomCell() {
		super();
	}

	@Override
	public IWrapper getWrapper() {
		return w;
	}

	@Override
	public String getAttribute() {
		return att;
	}

	@Override
	public void setTabIndex(int idx) {
	}

	@Override
	public boolean isCustomStyle(boolean table) {
		return false;
	}

	@Override
	public void setCellStyle(IWrappedTable table, int row, int idx) {
	}

	public Widget getInnerWidget() {
		return getWidget();
	}
	
}
