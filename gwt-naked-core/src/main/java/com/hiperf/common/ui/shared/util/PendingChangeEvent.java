package com.hiperf.common.ui.shared.util;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.ITablePanelEvent;
import com.hiperf.common.ui.client.PendingChangeHandler;

public class PendingChangeEvent extends GwtEvent<PendingChangeHandler> implements ITablePanelEvent {
	private static final Type<PendingChangeHandler> TYPE = new Type<PendingChangeHandler>();

	public PendingChangeEvent() {
	}

	public static Type<PendingChangeHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PendingChangeHandler handler) {
		handler.onChange(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<PendingChangeHandler> getAssociatedType() {
		return TYPE;
	}

}
