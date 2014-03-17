package com.hiperf.common.ui.client.event;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedTableEvent;
import com.hiperf.common.ui.client.IWrapper;

public class WrapperObjectAddedEvent extends
		GwtEvent<WrapperObjectAddedHandler> implements IWrappedTableEvent {

	private static final Type<WrapperObjectAddedHandler> TYPE = new Type<WrapperObjectAddedHandler>();

	private IWrapper wrapper;
	private String attribute;
	private INakedObject value;
	private boolean redraw;

	public WrapperObjectAddedEvent(IWrapper wrapper, String attribute,
			INakedObject newValue, boolean redraw) {
		super();
		this.wrapper = wrapper;
		this.attribute = attribute;
		this.value = newValue;
		this.redraw = redraw;
	}

	public IWrapper getWrapper() {
		return wrapper;
	}

	public String getAttribute() {
		return attribute;
	}

	public INakedObject getValue() {
		return value;
	}
	
	public boolean isRedraw() {
		return redraw;
	}

	public static Type<WrapperObjectAddedHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(WrapperObjectAddedHandler handler) {
		handler.onItemAdded(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<WrapperObjectAddedHandler> getAssociatedType() {
		return TYPE;
	}

}
