package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedTableEvent;
import com.hiperf.common.ui.client.IWrapper;

public class CollectionDataAddedEvent extends
		GwtEvent<CollectionDataAddedHandler> implements IWrappedTableEvent {

	private static final Type<CollectionDataAddedHandler> TYPE = new Type<CollectionDataAddedHandler>();

	private IWrapper wrapper;
	private String attribute;
	private Object value;

	public CollectionDataAddedEvent(IWrapper wrapper, String attribute,
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

	public static Type<CollectionDataAddedHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(CollectionDataAddedHandler handler) {
		handler.onItemAdded(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<CollectionDataAddedHandler> getAssociatedType() {
		return TYPE;
	}

}
