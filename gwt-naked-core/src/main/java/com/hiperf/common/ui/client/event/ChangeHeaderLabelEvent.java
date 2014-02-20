package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedFlexTableEvent;

public class ChangeHeaderLabelEvent extends GwtEvent<ChangeHeaderLabelHandler> implements IWrappedFlexTableEvent {
	private static final Type<ChangeHeaderLabelHandler> TYPE = new Type<ChangeHeaderLabelHandler>();

	private String label;
	private int index;

	public ChangeHeaderLabelEvent(int colIndex, String label) {
		this.label = label;
		this.index = colIndex;
	}

	public static Type<ChangeHeaderLabelHandler> getType() {
		return TYPE;
	}


	public String getLabel() {
		return label;
	}


	public int getIndex() {
		return index;
	}

	@Override
	protected void dispatch(ChangeHeaderLabelHandler handler) {
		handler.onChangeHeaderLabel(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ChangeHeaderLabelHandler> getAssociatedType() {
		return TYPE;
	}
}
