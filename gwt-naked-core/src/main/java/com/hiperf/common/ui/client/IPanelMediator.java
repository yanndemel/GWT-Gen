package com.hiperf.common.ui.client;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.widget.ICloseablePopupPanel;

public interface IPanelMediator {

	void addWrapperUpdatedHandler(String att,
			final IWrapper iWrapper, IWrappedCollectionCell cell);

	void addWrapperObjectAddedHandler(IWrappedCollectionCell wrappedCollectionCell);

	void addCollectionDataAddedHandler(IListBoxCell listBoxCell);

	void addCollectionDataRemovedHandler(IListBoxCell listBoxCell);

	void addWrapperSelectedHandler(ILinkedCell linkedCell);

	void addWrapperUpdatedHandler(IWrappedObjectCell wrappedObjectCell);

	void addSpecialWrapperUpdatedHandler(INakedObject no,
			ICell cell);

	void showPopupPanel(Widget widget, Widget sourceWidget);

	void unRegisterAll(boolean close);

	void addValidateButton(IObjectPanel vp, IWrappedObjectCell cell, boolean insert);

	void addValidateButton(IObjectPanel vp, IWrappedCollectionCell cell, boolean insert);

	void addNoDataFoundHideLoadingPopupHandler();

	void hidePopup(boolean force);

	void replaceWrapperUpdatedHandler(INakedObject oldContent, IWrappedObjectCell cell);

	void addWrapperSelectedForDeleteHandler(IWrappedCollectionCell linkedCell);

	ICloseablePopupPanel newPopupPanel();
	
	ICloseablePopupPanel getPopupPanel();

	void registerAll();

	void addWrapperSelectedHandler(ILinkedCell cell, HandlerRegistration handler);

}
