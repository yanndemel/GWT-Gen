package com.hiperf.common.ui.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.event.HideLoadingPopupEvent;
import com.hiperf.common.ui.client.event.TotalPagesEvent;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.widget.ViewHelper;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.NakedObjectsList;

public class LazyListModel extends LinkedWrapperListModel {

	private int size;
	private String description;
	private boolean init;
	private String orderBy;
	private boolean asc;

	public LazyListModel(String nakedObjectClassName, IWrapper w, String attribute,
			String mappedByAttribute, String orderByAttribute, boolean isAsc, int size, String desc, boolean init) {
		super(nakedObjectClassName, w, attribute, mappedByAttribute);
		this.size = size;
		this.description = desc;
		this.init = init;
		this.orderBy = orderByAttribute;
		this.asc = isAsc;
	}

	@Override
	public void setLazy(boolean b) {
	}

	@Override
	public boolean isLazy() {
		return true;
	}

	public boolean isInit() {
		return init;
	}


	@Override
	public boolean isEmpty() {
		return size == 0 && super.isEmpty();
	}


	@Override
	public List<IWrapper> getItems() {
		return super.getItems();
	}



	@Override
	public void reload(int pageNb, int rowsNb, AsyncCallback<Void> cb)
	{
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		clearItems(wor);
		AsyncCallback<NakedObjectsList> callback;
		Id id = PersistenceManager.getId(parentWrapper);
		if(isMappedCollection()) {
			callback = getReloadCallback(pageNb, getIdClause(id, this.mappedBy), cb);
			if(orderBy != null)
				PersistenceService.Util.getInstance().getSortedCollection(nakedObjectName, id, this.mappedBy, this.orderBy, this.asc, pageNb, rowsNb, wor.getAllObjectsToPersist(), callback);
			else
				PersistenceService.Util.getInstance().getCollection(nakedObjectName, id, this.mappedBy, pageNb, rowsNb, wor.getAllObjectsToPersist(), callback);
		}
		else {
			callback = getReloadCallback(pageNb, null, cb);
			PersistenceService.Util.getInstance().getCollectionInverse(parentWrapper.getWrappedClassName(), attribute, id, pageNb, rowsNb, wor.getAllObjectsToPersist(), callback);
		}
	}

	public boolean isMappedCollection() {
		return this.mappedBy != null && !WrapperContext.getFieldInfoByName().get(nakedObjectName).get(this.mappedBy).isManyToMany();
	}

	private String getIdClause(com.hiperf.common.ui.shared.util.Id id,
			String attributeName) {
		String currentFilter = "";
		int i = 0;
		for(String name : id.getFieldNames()) {
			Object val = id.getFieldValues().get(i);
			if(String.class.equals(val.getClass()))
				currentFilter += "o."+attributeName+"."+name+" = '"+val+"'";
			else
				currentFilter += "o."+attributeName+"."+name+" = "+val;
			if(i<id.getFieldNames().size() - 1)
				currentFilter += " and ";
		}
		return currentFilter;
	}


	private AsyncCallback<NakedObjectsList> getReloadCallback(final int page, final String cstFilter, final AsyncCallback<Void> cb) {
		ViewHelper.showWaitPopup();
		AsyncCallback<NakedObjectsList> callback = new AsyncCallback<NakedObjectsList>() {

			public void onFailure(Throwable caught) {
				ViewHelper.displayError(caught, NakedConstants.constants.exceptionDataDB());
				WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), items);
				if(cb != null)
					cb.onFailure(null);
			}

			public void onSuccess(NakedObjectsList result) {
				if (result != null) {
					WrappedObjectsRepository.getInstance().setPauseModifListener(true);
					IWrapper emptyWrapper = WrapperContext.getEmptyWrappersMap().get(nakedObjectName);
					for (INakedObject o : result.getList()) {
						IWrapper w = emptyWrapper.newWrapper();
						w.setContent(o);
						addItem(w);
					}
					currentFilter = result.getFilter();
					if(cstFilter != null)
						constantFilter = cstFilter;
					if(result.getConstantFilterFields() != null) {
						constantFilterFields = new ArrayList<String>();
						for(String r : result.getConstantFilterFields()) {
							constantFilterFields.add(r);
						}
					}

					WrapperContext.getEventBus().fireEventFromSource(new TotalPagesEvent(page, result.getTotalPages()), items);
					WrappedObjectsRepository.getInstance().setPauseModifListener(false);
				} else {
					ViewHelper.displayError(NakedConstants.constants.exceptionDataDB());
				}
				if(cb != null)
					cb.onSuccess(null);
				WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), items);
			}
		};
		return callback;
	}


	public String toString() {
		return description;
	}

	@Override
	public int getServerCount() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setDescription(String description) {
		this.description = description;
	}



}
