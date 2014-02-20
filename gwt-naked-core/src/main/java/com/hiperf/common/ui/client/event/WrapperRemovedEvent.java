package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedFlexTableEvent;
import com.hiperf.common.ui.client.IWrapper;

public class WrapperRemovedEvent extends GwtEvent<WrapperRemovedHandler> implements IWrappedFlexTableEvent {
	private static final Type<WrapperRemovedHandler> TYPE = new Type<WrapperRemovedHandler>();

	final private IWrapper object;

	public WrapperRemovedEvent(IWrapper o) {
		this.object = o;
	}

	public static Type<WrapperRemovedHandler> getType() {
		return TYPE;
	}

	public IWrapper getObject() {
		return object;
	}

	@Override
	protected void dispatch(WrapperRemovedHandler handler) {
		handler.onItemRemoved(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<WrapperRemovedHandler> getAssociatedType() {
		return TYPE;
	}
}
