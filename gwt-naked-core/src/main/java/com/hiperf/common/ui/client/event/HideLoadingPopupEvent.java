package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.hiperf.common.ui.client.IWrappedTableEvent;

public class HideLoadingPopupEvent extends GwtEvent<HideLoadingPopupHandler> implements IWrappedTableEvent {
	private static final Type<HideLoadingPopupHandler> TYPE = new Type<HideLoadingPopupHandler>();

	private boolean noDataFound;

	public HideLoadingPopupEvent() {
		this.noDataFound = false;
	}
	
	public HideLoadingPopupEvent(boolean noDataFound) {
		this.noDataFound = noDataFound;
	}

	public static Type<HideLoadingPopupHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(HideLoadingPopupHandler handler) {
		handler.onHideLoadingPopup(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<HideLoadingPopupHandler> getAssociatedType() {
		return TYPE;
	}

	public boolean isNoDataFound() {
		return noDataFound;
	}

	public void cancel() {
		kill();
	}

	public boolean alive() {
		return isLive();
	}


}
