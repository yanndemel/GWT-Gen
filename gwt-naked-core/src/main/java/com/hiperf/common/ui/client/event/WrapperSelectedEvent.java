package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedTableEvent;
import com.hiperf.common.ui.client.widget.WrappedFlexTable;

public class WrapperSelectedEvent extends GwtEvent<WrapperSelectedHandler> implements IWrappedTableEvent {
	private static final Type<WrapperSelectedHandler> TYPE = new Type<WrapperSelectedHandler>();

	private final WrappedFlexTable selectionTable;
	private final int selectedRow;


	public WrapperSelectedEvent(WrappedFlexTable table, int row) {
		this.selectionTable = table;
		this.selectedRow = row;
	}

	public static Type<WrapperSelectedHandler> getType() {
		return TYPE;
	}

	public WrappedFlexTable getSelectionTable() {
		return selectionTable;
	}

	public int getSelectedRow() {
		return selectedRow;
	}

	@Override
	protected void dispatch(WrapperSelectedHandler handler) {
		handler.onItemSelected(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<WrapperSelectedHandler> getAssociatedType() {
		return TYPE;
	}
}
