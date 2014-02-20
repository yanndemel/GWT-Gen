package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedFlexTableEvent;

public class SwitchColumnsEvent extends GwtEvent<SwitchColumnsHandler> implements IWrappedFlexTableEvent {
	private static final Type<SwitchColumnsHandler> TYPE = new Type<SwitchColumnsHandler>();

	private int index1;
	private String att1;
	private int index2;
	private String att2;

	public SwitchColumnsEvent(int i1, String att1, int i2, String att2) {
		this.index1 = i1;
		this.att1 = att1;
		this.index2 = i2;
		this.att2 = att2;
	}

	public static Type<SwitchColumnsHandler> getType() {
		return TYPE;
	}

	public int getIndex1() {
		return index1;
	}

	public int getIndex2() {
		return index2;
	}

	public String getAtt1() {
		return att1;
	}

	public String getAtt2() {
		return att2;
	}

	@Override
	protected void dispatch(SwitchColumnsHandler handler) {
		handler.onSwitchColumns(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SwitchColumnsHandler> getAssociatedType() {
		return TYPE;
	}
}
