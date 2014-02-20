package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedFlexTableEvent;
import com.hiperf.common.ui.client.IWrapper;

public class WrapperAddedEvent extends GwtEvent<WrapperAddedHandler> implements IWrappedFlexTableEvent {
	private static final Type<WrapperAddedHandler> TYPE = new Type<WrapperAddedHandler>();

	private int row;
	private final IWrapper object;

	public WrapperAddedEvent(int beforeRow, IWrapper o) {
		this.row = beforeRow;
		this.object = o;
	}

	public static Type<WrapperAddedHandler> getType() {
		return TYPE;
	}

	/** @return The item added to the model */
	public IWrapper getObject() {
		return object;
	}



	public int getRow() {
		return row;
	}

	@Override
	protected void dispatch(WrapperAddedHandler handler) {
		handler.onItemAdded(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<WrapperAddedHandler> getAssociatedType() {
		return TYPE;
	}
}
