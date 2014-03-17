package com.hiperf.common.ui.client.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.event.HideLoadingPopupEvent;
import com.hiperf.common.ui.client.event.TotalPagesEvent;
import com.hiperf.common.ui.client.event.WrapperRemovedEvent;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.widget.ViewHelper;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.NakedObjectsList;
import com.hiperf.common.ui.shared.util.PendingChangeEvent;

public class LinkedWrapperListModel extends PersistentWrapperListModel {

	protected IWrapper parentWrapper = null;
	protected String attribute;
	protected String mappedBy;

	public LinkedWrapperListModel(String nakedObjectClassName, IWrapper w, String attribute, String mappedByAttribute) {
		super(nakedObjectClassName);
		this.parentWrapper = w;
		this.attribute = attribute;
		if(mappedByAttribute != null && mappedByAttribute.length() > 0)
			this.mappedBy = mappedByAttribute;
	}

	public LinkedWrapperListModel(String nakedObjectClassName) {
		super(nakedObjectClassName);
	}

	@Override
	public void sort(String sortAttribute, boolean asc, final int page, int nbRows, AsyncCallback<Void> cb)  {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		clearItems(wor);
		AsyncCallback<NakedObjectsList> callback = getSortCallback(page, cb);
		if(this.mappedBy != null && !WrapperContext.getFieldInfoByName().get(nakedObjectName).get(this.mappedBy).isManyToMany())
			PersistenceService.Util.getInstance().getSortedCollection(nakedObjectName, PersistenceManager.getId(parentWrapper), this.mappedBy, sortAttribute, asc, page, nbRows, wor.getAllObjectsToPersist(), callback);
		else
			PersistenceService.Util.getInstance().getSortedCollectionInverse(parentWrapper.getWrappedClassName(), attribute, PersistenceManager.getId(parentWrapper), sortAttribute, asc, page, nbRows, wor.getAllObjectsToPersist(), callback);

	}

	protected AsyncCallback<NakedObjectsList> getSortCallback(final int page, final AsyncCallback<Void> cb) {
		AsyncCallback<NakedObjectsList> callback = new AsyncCallback<NakedObjectsList>() {

			public void onFailure(Throwable caught) {
				ViewHelper.displayError(caught, NakedConstants.constants.exceptionDataDB());
				WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), items);
				if(cb != null)
					cb.onFailure(null);
			}

			public void onSuccess(NakedObjectsList result) {
				lastReloadDate = new Date().getTime();
				if (result != null) {
					WrappedObjectsRepository.getInstance().setPauseModifListener(true);
					IWrapper emptyWrapper = WrapperContext.getEmptyWrappersMap().get(nakedObjectName);
					for (INakedObject o : result.getList()) {
						IWrapper w = emptyWrapper.newWrapper();
						w.setContent(o);
						addItem(w);
					}
					serverCount = result.getCount();
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

	public void reload() {
		PersistenceServiceAsync service = PersistenceService.Util.getInstance();
		WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		for (IWrapper item : items) {
			wor.removeObjectFromLists(item);
			WrapperContext.getEventBus().fireEventFromSource(new WrapperRemovedEvent(item), items);
		}
		List<String> idFields = new ArrayList<String>();
		List<List<Object>> idList = new ArrayList<List<Object>>();
		Map<String, IFieldInfo> idInfos = PersistenceManager.getIdInfos(nakedObjectName);
		for(String att : idInfos.keySet()) {
			idFields.add(att);
		}
		for(IWrapper w : items) {
			List<Object> list = new ArrayList<Object>();
			idList.add(list);
			for(String f : idFields) {
				Object att = w.getNakedAttribute(f);
				list.add(att);
			}
		}
		items.clear();
		AsyncCallback<List<INakedObject>> callback = new AsyncCallback<List<INakedObject>>() {

			public void onFailure(Throwable caught) {
				ViewHelper.displayError(caught, NakedConstants.constants.exceptionDataDB());
			}

			public void onSuccess(List<INakedObject> result) {

				if (result != null) {
					WrappedObjectsRepository.getInstance().setPauseModifListener(true);
					IWrapper emptyWrapper = WrapperContext.getEmptyWrappersMap().get(nakedObjectName);
					for (INakedObject o : result) {
						IWrapper w = emptyWrapper.newWrapper();
						w.setContent(o);
						addItem(w);
					}
					serverCount = result.size();
					WrappedObjectsRepository.getInstance().setPauseModifListener(false);
				} else {
					ViewHelper.displayError(NakedConstants.constants.exceptionDataDB());
				}
			}
		};
		service.reload(nakedObjectName, idFields, idList, callback);
	}

	public void removeItem(IWrapper item) {
		WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		items.remove(item);
		if(parentWrapper != null) {
			try {
				wor.setPauseModifListener(true);
				parentWrapper.removeObjectFromCollection(attribute, item.getContent());
				wor.addWrapperToRefresh(parentWrapper);
			} finally {
				wor.setPauseModifListener(false);
			}

		}
		wor.addRemovedObject(item);
		WrapperContext.getEventBus().fireEventFromSource(new WrapperRemovedEvent(item), items);
		WrapperContext.getEventBus().fireEventFromSource(new PendingChangeEvent(), items);

	}

	public IWrapper getParentWrapper() {
		return parentWrapper;
	}

	public String getMappedBy() {
		return mappedBy;
	}

	public String getAttribute() {
		return attribute;
	}

	

}
