package com.hiperf.common.ui.client.widget;

import com.google.gwt.event.shared.HandlerRegistration;
import com.hiperf.common.ui.client.event.HideLoadingPopupEvent;
import com.hiperf.common.ui.client.event.HideLoadingPopupHandler;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.shared.WrapperContext;

public class PopupMediator implements HideLoadingPopupHandler {

	private ICloseablePopupPanel popup;
	private int savedStateIdx = -1;

	private HandlerRegistration hidePopupHandler;

	public PopupMediator(ICloseablePopupPanel popup) {
		super();
		this.popup = popup;

	}

	public void addNoDataFoundHideLoadingPopupHandler() {
		if(hidePopupHandler == null)
			hidePopupHandler = WrapperContext.getEventBus().addHandler(HideLoadingPopupEvent.getType(), this);
	}

	@Override
	public void onHideLoadingPopup(HideLoadingPopupEvent e) {
		if(e.isNoDataFound()) {
			popup.setForceClose(true);
			popup.hide();
			MessageBox.alert(NakedConstants.constants.noDataFound());
		} else if(popup != null && popup.isShowing())
			popup.center();
		if(hidePopupHandler != null) {
			hidePopupHandler.removeHandler();
			hidePopupHandler = null;
		}

	}

	public int getSavedStateIdx() {
		return savedStateIdx;
	}

	public void setSavedStateIdx(int savedStateIdx) {
		this.savedStateIdx = savedStateIdx;
	}



}
