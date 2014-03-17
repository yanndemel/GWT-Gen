package com.hiperf.common.ui.client.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.event.TotalPagesEvent;
import com.hiperf.common.ui.client.event.WrapperRemovedEvent;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.widget.ViewHelper;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.NakedObjectsList;

public class PersistentWrapperListModel extends WrapperListModel {

	protected long lastReloadDate;
	protected String currentFilter;
	protected int serverCount;
	private boolean keepObjectsToPersist;
	private boolean keepInsertedObjects;
	private boolean queryDistinctEntities;
	protected Set<Id> excludedEntityIds = null;
	protected List<String> constantFilterFields = null;
	protected String constantFilter;
	private List<String> enabledFilterFields = null;
	private String defaultSortAtt = null;
	private Boolean defaultSortAsc = null;

	public PersistentWrapperListModel(String nakedObjectClassName) {
		super(nakedObjectClassName);
		this.lastReloadDate = -1;
		this.keepObjectsToPersist = false;
		this.keepInsertedObjects = false;
		this.queryDistinctEntities = false;
	}

	public boolean isPersistent() {
		return true;
	}

	public void reload(String sortAttribute, Boolean way, int pageNb, int rowsNb, AsyncCallback<Void> cb) {
		WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		clearItems(wor);

		AsyncCallback<NakedObjectsList> callback = getServerCallback(wor, pageNb, rowsNb, cb);
		PersistenceServiceAsync service = PersistenceService.Util.getInstance();
		boolean asc = way != null ? way : true;
		if(!keepObjectsToPersist) {
			ObjectsToPersist toPersist = getObjectsToPersist(wor);
			service.loadAll(nakedObjectName, currentFilter, sortAttribute, queryDistinctEntities, asc, pageNb, rowsNb, toPersist, callback);
		}
		else
			service.loadAll(nakedObjectName, currentFilter, sortAttribute, queryDistinctEntities, asc, pageNb, rowsNb, null, callback);
	}

	public ObjectsToPersist getObjectsToPersist(WrappedObjectsRepository wor) {
		ObjectsToPersist toPersist;
		if(keepInsertedObjects)
			toPersist = new ObjectsToPersist(null, wor.getUpdatedObjects(), wor.getRemovedObjectsIdsByClassName(), wor.getManyToManyAddedByClassName(), wor.getManyToManyRemovedByClassName());
		else
			toPersist = wor.getAllObjectsToPersist();
		return toPersist;
	}

	@Override
	public void reload(int pageNb, int rowsNb, AsyncCallback<Void> cb) {
		reload(defaultSortAtt, defaultSortAsc, pageNb, rowsNb, cb);
	}



	private AsyncCallback<NakedObjectsList> getServerCallback(final WrappedObjectsRepository wor, final int pageNb, final int rowsNb, final AsyncCallback<Void> cb) {
		ViewHelper.showWaitPopup();
		AsyncCallback<NakedObjectsList> callback = new AsyncCallback<NakedObjectsList>() {

			public void onFailure(Throwable caught) {
				displayError(caught);
				//throw new RuntimeException(caught);
				if(cb != null)
					cb.onFailure(null);
			}

			public void onSuccess(NakedObjectsList result) {
				processResultList(pageNb, rowsNb, result, cb);
			}
		};
		return callback;
	}

	protected void clearItems(WrappedObjectsRepository wor) {
		boolean pauseModifListener = wor.isPauseModifListener();
		if(!pauseModifListener)
			wor.setPauseModifListener(true);
		for(IWrapper item : items) {
			WrapperContext.getEventBus().fireEventFromSource(new WrapperRemovedEvent(item), items);
		}
		items.clear();
		wor.setPauseModifListener(pauseModifListener);
	}

	@Override
	public void sort(String attribute, boolean asc, int pageNb, int rowsNb, AsyncCallback<Void> cb) {
		WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		clearItems(wor);
		AsyncCallback<NakedObjectsList> callback = getServerCallback(wor, pageNb, rowsNb, cb);
		PersistenceServiceAsync service = PersistenceService.Util.getInstance();
		if(!keepObjectsToPersist) {
			ObjectsToPersist toPersist = getObjectsToPersist(wor);
			service.sort(nakedObjectName, currentFilter, attribute, queryDistinctEntities, asc, pageNb, rowsNb, toPersist, callback);
		}
		else
			service.sort(nakedObjectName, currentFilter, attribute, queryDistinctEntities, asc, pageNb, rowsNb, null, callback);
	}


	public long getLastReloadDate() {
		return lastReloadDate;
	}

	public void filter(String jpql, final String sortAttribute, final Boolean way, final int page, final int rowNb, boolean caseSensitive, AsyncCallback<Void> cb) {
		//final String old = currentFilter;
		currentFilter = jpql;
		queryDistinctEntities = true;
		WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		clearItems(wor);
		reload(sortAttribute, way, page, rowNb, cb);

		/*final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		if(wor.hasToCommit()) {
			AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

				@Override
				public void onFailure(Throwable caught) {}

				@Override
				public void onSuccess(Boolean b) {
					if(b!=null && b.booleanValue()) {
						wor.clear();
						reload(sortAttribute, way, page, rowNb);
					} else {
						WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), items);
						currentFilter = old;
					}
				}
			};
			LoseChangesDialogBox.getInstance().show(callback);
		} else {
			reload(sortAttribute, way, page, rowNb);
		}*/

	}

	public String getCurrentFilter() {
		return currentFilter;
	}

	public void setCurrentFilter(String currentFilter) {
		this.currentFilter = currentFilter;
	}

	private void processResultList(final int pageNb, final int rowsNb, NakedObjectsList result, AsyncCallback<Void> cb) {
		lastReloadDate = new Date().getTime();
		WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		if (result != null && result.getList() != null) {
			serverCount = result.getCount();
			Map<Id, IWrapper> objectsToRefresh = wor.getObjectsToRefresh(nakedObjectName);
			boolean checkRefresh = false;
			if(objectsToRefresh != null && !objectsToRefresh.isEmpty())
				checkRefresh = true;
			wor.setPauseModifListener(true);
			List<INakedObject> insertedObjects = wor.getInsertedObjects();
			boolean checkInsert = !insertedObjects.isEmpty();
			Map<Id, INakedObject> objToInsert = null;
			if(checkInsert) {
				objToInsert = new HashMap<Id, INakedObject>();
				for(INakedObject no : insertedObjects) {
					if(no.getClass().getName().equals(nakedObjectName))
						objToInsert.put(PersistenceManager.getId(no), no);
				}
				if(objToInsert.isEmpty())
					checkInsert = false;
			}
			IWrapper emptyWrapper = WrapperContext.getEmptyWrappersMap().get(nakedObjectName);
			for (INakedObject o : result.getList()) {
				boolean found = false;
				Id id = PersistenceManager.getId(o);
				if(excludedEntityIds != null && excludedEntityIds.contains(id))
					continue;
				if(checkRefresh) {
					IWrapper w = objectsToRefresh.get(id);
					if(w != null) {
						addItem(w);
						found = true;
					}
				}
				if(!found) {
					IWrapper w = emptyWrapper.newWrapper();
					if(checkInsert && objToInsert.containsKey(id)) {
						w.setContent(objToInsert.get(id));
					} else {
						w.setContent(o);
					}
					addItem(w);
				}
			}
			WrapperContext.getEventBus().fireEventFromSource(new TotalPagesEvent(pageNb, result.getTotalPages()), items);
			wor.setPauseModifListener(false);
		} else {
			WrapperContext.getEventBus().fireEventFromSource(new TotalPagesEvent(1, 1), items);
		}
		if(cb != null)
			cb.onSuccess(null);
		hideLoadingPopup();
	}

	public int getServerCount() {
		return serverCount;
	}

	public void setKeepObjectsToPersist(boolean keepObjectsToPersist) {
		this.keepObjectsToPersist = keepObjectsToPersist;
	}

	public void setKeepInsertedObjects(boolean keepInsertedObjects) {
		this.keepInsertedObjects = keepInsertedObjects;
	}

	public boolean isQueryDistinctEntities() {
		return queryDistinctEntities;
	}

	public void setQueryDistinctEntities(boolean queryDistinctEntities) {
		this.queryDistinctEntities = queryDistinctEntities;
	}

	@Override
	public void addExcludedEntity(Id id) {
		if(excludedEntityIds == null)
			excludedEntityIds = new HashSet<Id>();
		excludedEntityIds.add(id);
	}

	public void setServerCount(int serverCount) {
		this.serverCount = serverCount;
	}

	public void setConstantFilterFields(List<String> constantFilterFields) {
		this.constantFilterFields  = constantFilterFields;
	}

	public List<String> getConstantFilterFields() {
		return constantFilterFields;
	}

	public void setConstantFilter(String filter) {
		this.constantFilter = filter;
	}

	public String getConstantFilter() {
		return constantFilter;
	}

	public void resetCurrentFilter() {
		currentFilter = constantFilter;
	}

	public List<String> getEnabledFilterFields() {
		return enabledFilterFields;
	}

	public void setEnabledFilterFields(List<String> specialFilterFields) {
		this.enabledFilterFields = specialFilterFields;
	}

	public void setDefaultSort(String attribute, boolean asc) {
		defaultSortAtt = attribute;
		defaultSortAsc = asc;
	}

	public String getDefaultSortAtt() {
		return defaultSortAtt;
	}

	@Override
	public Boolean isDefaultSortAsc() {
		return defaultSortAsc;
	}



}
