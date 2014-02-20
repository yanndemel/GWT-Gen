package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedTableEvent;
import com.hiperf.common.ui.client.IWrapper;

public class CollectionDataRemovedEvent extends GwtEvent<CollectionDataRemovedHandler> implements IWrappedTableEvent {

	private static final Type<CollectionDataRemovedHandler> TYPE = new Type<CollectionDataRemovedHandler>();

	private IWrapper wrapper;
	private String attribute;
	private Object value;

	public CollectionDataRemovedEvent(IWrapper wrapper, String attribute,
			Object newValue) {
		super();
		this.wrapper = wrapper;
		this.attribute = attribute;
		this.value = newValue;
	}

	public IWrapper getWrapper() {
		return wrapper;
	}

	public String getAttribute() {
		return attribute;
	}

	public Object getValue() {
		return value;
	}

	public static Type<CollectionDataRemovedHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(CollectionDataRemovedHandler handler) {
		handler.onItemRemoved(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<CollectionDataRemovedHandler> getAssociatedType() {
		return TYPE;
	}

}
