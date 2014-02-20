package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedFlexTableEvent;

public class HideColumnEvent extends GwtEvent<HideColumnHandler> implements IWrappedFlexTableEvent  {
	private static final Type<HideColumnHandler> TYPE = new Type<HideColumnHandler>();

	private int index;

	public HideColumnEvent(int idx) {
		this.index = idx;
	}

	public static Type<HideColumnHandler> getType() {
		return TYPE;
	}

	public int getIndex() {
		return index;
	}

	@Override
	protected void dispatch(HideColumnHandler handler) {
		handler.onHideColumn(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<HideColumnHandler> getAssociatedType() {
		return TYPE;
	}
}
