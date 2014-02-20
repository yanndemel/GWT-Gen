package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedFlexTableEvent;

public class DisplayColumnEvent extends GwtEvent<DisplayColumnHandler> implements IWrappedFlexTableEvent {
	private static final Type<DisplayColumnHandler> TYPE = new Type<DisplayColumnHandler>();

	private String attribute;
	private boolean insert;

	public DisplayColumnEvent(String attribute) {
		this(attribute, false);
	}

	public DisplayColumnEvent(String attribute, boolean insert) {
		this.attribute = attribute;
		this.insert = insert;
	}

	public static Type<DisplayColumnHandler> getType() {
		return TYPE;
	}

	public String getAttribute() {
		return attribute;
	}

	public boolean isInsert() {
		return insert;
	}

	@Override
	protected void dispatch(DisplayColumnHandler handler) {
		handler.onDisplayColumn(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<DisplayColumnHandler> getAssociatedType() {
		return TYPE;
	}
}
