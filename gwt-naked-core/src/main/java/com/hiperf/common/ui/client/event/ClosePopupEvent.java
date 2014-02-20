package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class ClosePopupEvent extends GwtEvent<ClosePopupHandler> {
	private static final Type<ClosePopupHandler> TYPE = new Type<ClosePopupHandler>();

	private int savedStateIndex;

	public ClosePopupEvent(int savedStateIndex) {
		this.savedStateIndex = savedStateIndex;
	}

	public static Type<ClosePopupHandler> getType() {
		return TYPE;
	}

	public int getSavedStateIndex() {
		return savedStateIndex;
	}

	@Override
	protected void dispatch(ClosePopupHandler handler) {
		handler.onClose(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ClosePopupHandler> getAssociatedType() {
		return TYPE;
	}
}
