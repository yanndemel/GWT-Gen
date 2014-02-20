package com.hiperf.common.ui.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.event.HideLoadingPopupEvent;
import com.hiperf.common.ui.client.event.WrapperAddedEvent;
import com.hiperf.common.ui.client.event.WrapperRemovedEvent;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.widget.ViewHelper;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.Id;

public class WrapperListModel implements IWrapperListModel {
	protected final String nakedObjectName;
	protected final List<IWrapper> items;
	private boolean lazy;

	public WrapperListModel(String nakedObjectName) {
		this.nakedObjectName = nakedObjectName;
		this.items = new ArrayList<IWrapper>() {

			@Override
			public void clear() {
				super.clear();
			}

		};
		this.lazy = false;
	}

	public boolean isPersistent() {
		return false;
	}

	public void simpleAdd(IWrapper item) {
		items.add(item);
	}

	public void simpleAddAll(Collection<IWrapper> l) {
		items.addAll(l);
	}

	public void addItem(IWrapper item) {
		addItem(-1, item);
	}

	public void addItem(int beforeRow, IWrapper item) {
		if(beforeRow >= 0)
			items.add(beforeRow, item);
		else
			items.add(item);
		WrapperContext.getEventBus().fireEventFromSource(new WrapperAddedEvent(beforeRow, item), items);
	}

	public void removeItem(IWrapper item) {
		items.remove(item);
		WrappedObjectsRepository.getInstance().addRemovedObject(item);
		WrapperContext.getEventBus().fireEventFromSource(new WrapperRemovedEvent(item), items);
	}


	public List<IWrapper> getItems() {
		return items;
	}

	public String getNakedObjectName() {
		return nakedObjectName;
	}

	@Override
	public String getText() {
		return NakedConstants.messages.nbItems(getItems().size());
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}



	@Override
	public void setLazy(boolean b) {
		this.lazy = b;
	}

	@Override
	public boolean isLazy() {
		return lazy;
	}

	@Override
	public void sort(String attribute, boolean asc, int page, int nbRows, AsyncCallback<Void> cb) {}

	@Override
	public void addExcludedEntity(Id id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reload(int pageNb, int rowsNb, AsyncCallback<Void> cb) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getServerCount() {
		return items.size();
	}

	public void hideLoadingPopup() {
		WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(items.size() == 0), items);		
	}

	public void displayError(Throwable caught) {
		ViewHelper.displayError(caught, NakedConstants.constants.exceptionDataDB());
		WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), items);
	}

	@Override
	public String toString() {
		if(items != null && items.size() == 1) {
			IWrapper w = items.get(0);
			if(w != null && w.getContent() != null)
				return w.getContent().toString();
		}
		return super.toString();
	}

	@Override
	public String getDefaultSortAtt() {
		return null;
	}

	@Override
	public Boolean isDefaultSortAsc() {
		return null;
	}



}
