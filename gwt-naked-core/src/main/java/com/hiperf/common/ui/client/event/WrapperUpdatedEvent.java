package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedTableEvent;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.WrapperUpdatedHandler;

public class WrapperUpdatedEvent extends GwtEvent<WrapperUpdatedHandler> implements IWrappedTableEvent {
	private static final Type<WrapperUpdatedHandler> TYPE = new Type<WrapperUpdatedHandler>();

	private IWrapper wrapper;
	private String attribute;
	private Object oldValue;
	private Object newValue;

	public WrapperUpdatedEvent(IWrapper o, String attribute, Object oldValue, Object newValue) {
		super();
		this.wrapper = o;
		this.attribute = attribute;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public IWrapper getWrapper() {
		return wrapper;
	}

	public String getAttribute() {
		return attribute;
	}

	public Object getNewValue() {
		return newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public static Type<WrapperUpdatedHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(WrapperUpdatedHandler handler) {
		handler.onItemUpdated(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<WrapperUpdatedHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return "WrapperUpdatedEvent [wrapper=" + wrapper.getWrappedClassName() + ", attribute="
				+ attribute + ", oldValue=" + oldValue + ", newValue="
				+ newValue + "]";
	}



}
