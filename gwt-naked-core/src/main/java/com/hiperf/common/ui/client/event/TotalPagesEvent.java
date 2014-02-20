package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.ITablePanelEvent;

public class TotalPagesEvent extends GwtEvent<TotalPagesHandler> implements ITablePanelEvent {
	private static final Type<TotalPagesHandler> TYPE = new Type<TotalPagesHandler>();

	private int page;
	private int total;

	public TotalPagesEvent(int page, int tot) {
		this.page = page;
		this.total = tot;
		
	}

	public static Type<TotalPagesHandler> getType() {
		return TYPE;
	}

	public int getTotal() {
		return total;
	}

	@Override
	protected void dispatch(TotalPagesHandler handler) {
		handler.onTotalPagesChanged(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<TotalPagesHandler> getAssociatedType() {
		return TYPE;
	}

	public int getPage() {
		return page;
	}
	
	

}
