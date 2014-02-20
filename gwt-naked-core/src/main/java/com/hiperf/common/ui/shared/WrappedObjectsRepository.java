package com.hiperf.common.ui.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.event.WrapperObjectAddedEvent;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.widget.ViewHelper;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.PendingChangeEvent;

public class WrappedObjectsRepository {

	private static final WrappedObjectsRepository instance = new WrappedObjectsRepository();

	private boolean pauseModifListener;

	private List<INakedObject> insertedObjects;

	private List<INakedObject> pendingValidations;

	private Map<String, Map<Id, Map<String, Serializable>>> updatedObjects;

	private Map<String, Map<Id, IWrapper>> updatedWrappersByClassName;

	private Map<String, Set<Id>> removedObjectsIdsByClassName;

	private Map<String, Map<Id, IWrapper>> objectsToRefreshByClassName;

	private Map<String, Map<Id, Map<String, List<Id>>>> manyToManyAddedByClassName;

	private Map<String, Map<Id, Map<String, List<Id>>>> manyToManyRemovedByClassName;

	private long lastCommitDate;

	private boolean pauseFireEvents;

	private List<SavedState> savedStates = new ArrayList<SavedState>();



	public class SavedState {
		private List<INakedObject> insertedObjects;
		private Map<String, Map<Id, Map<String, Serializable>>> updatedObjects;
		private Map<String, Set<Id>> removedObjectsIdsByClassName;
		private Map<IWrapper, Map<String, Object>> oldValues;
		private Map<IWrapper, Map<String, INakedObject>> objectsAddedToCollections;
		private Map<IWrapper, Map<String, INakedObject>> objectsRemovedFromCollections;
		private Map<String, Map<Id, Map<String, List<Id>>>> manyToManyAddedByClassName;
		private Map<String, Map<Id, Map<String, List<Id>>>> manyToManyRemovedByClassName;
		private INakedObject insertedObject;
		private Map<String, Map<Id, IWrapper>> objectsToRefreshByClassName;

		public SavedState() {
			super();
			this.insertedObjects = new ArrayList<INakedObject>(getInsertedObjects());
			Map<String, Map<Id, Map<String, Serializable>>> updMap = getUpdatedObjects();
			this.updatedObjects = new HashMap<String, Map<Id,Map<String,Serializable>>>(updMap.size());
			for(String clazz : updMap.keySet()) {
				Map<Id, Map<String, Serializable>> keyValMap = new HashMap<Id, Map<String,Serializable>>();
				Map<Id, Map<String, Serializable>> origKeyValMap = updMap.get(clazz);
				for(Id id : origKeyValMap.keySet()) {
					Map<String, Serializable> m = origKeyValMap.get(id);
					Map<String, Serializable> valMap = new HashMap<String, Serializable>(m.size());
					valMap.putAll(m);
					keyValMap.put(id, valMap);
				}
				this.updatedObjects.put(clazz, keyValMap);
			}
			Map<String, Set<Id>> removedObjects = getRemovedObjectsIdsByClassName();
			this.removedObjectsIdsByClassName = new HashMap<String, Set<Id>>(removedObjects.size());
			for(String clazz : removedObjects.keySet()) {
				Set<Id> set = removedObjects.get(clazz);
				removedObjectsIdsByClassName.put(clazz, new HashSet<Id>(set));
			}
			this.oldValues = new HashMap<IWrapper,Map<String,Object>>();
			this.objectsAddedToCollections = new HashMap<IWrapper, Map<String,INakedObject>>();
			this.objectsRemovedFromCollections = new HashMap<IWrapper, Map<String,INakedObject>>();
			this.objectsToRefreshByClassName = new HashMap<String, Map<Id,IWrapper>>();
			for(Entry<String, Map<Id, IWrapper>> entry : getObjectsToRefreshByClassName().entrySet()) {
				Map<Id, IWrapper> map = new HashMap<Id, IWrapper>();
				for(Entry<Id, IWrapper> e : entry.getValue().entrySet()) {
					map.put(e.getKey(), e.getValue());
				}
				this.objectsToRefreshByClassName.put(entry.getKey(), map);
			}
			this.manyToManyAddedByClassName = new HashMap<String, Map<Id, Map<String, List<Id>>>>();
			copyMap(getManyToManyAddedByClassName(), this.manyToManyAddedByClassName);
			this.manyToManyRemovedByClassName = new HashMap<String, Map<Id, Map<String, List<Id>>>>();
			copyMap(getManyToManyRemovedByClassName(), this.manyToManyRemovedByClassName);

		}

		private void copyMap(
				Map<String, Map<Id, Map<String, List<Id>>>> source,
				Map<String, Map<Id, Map<String, List<Id>>>> target) {
			for(Entry<String, Map<Id, Map<String, List<Id>>>> entry : source.entrySet()) {
				String className = entry.getKey();
				Map<Id, Map<String, List<Id>>> v = entry.getValue();
				if(v != null && !v.isEmpty()) {
					Map<Id, Map<String, List<Id>>> m2 = new HashMap<Id, Map<String, List<Id>>>(v.size());

					target.put(className, m2);
					for(Entry<Id, Map<String, List<Id>>> e : v.entrySet()) {
						Map<String, List<Id>> v2 = e.getValue();
						if(v2 != null && !v2.isEmpty()) {
							Map<String, List<Id>> v3 = new HashMap<String, List<Id>>(v2.size());
							m2.put(e.getKey(), v3);
							for(Entry<String, List<Id>> ee : v2.entrySet()) {
								List<Id> v4 = ee.getValue();
								if(v4 != null && !v4.isEmpty())
									v3.put(ee.getKey(), new ArrayList<Id>(v4));
							}
						}
					}
				}
			}
		}

		public void restore() {
			getInsertedObjects().clear();
			getInsertedObjects().addAll(this.insertedObjects);
			getUpdatedObjects().clear();
			getUpdatedObjects().putAll(this.updatedObjects);
			getRemovedObjectsIdsByClassName().clear();
			getRemovedObjectsIdsByClassName().putAll(this.removedObjectsIdsByClassName);
			getObjectsToRefreshByClassName().clear();
			getObjectsToRefreshByClassName().putAll(this.objectsToRefreshByClassName);
			getManyToManyAddedByClassName().clear();
			getManyToManyAddedByClassName().putAll(this.manyToManyAddedByClassName);
			getManyToManyRemovedByClassName().clear();
			getManyToManyRemovedByClassName().putAll(this.manyToManyRemovedByClassName);
		}



		public void recordOldValue(IWrapper w, String attribute,
				Object oldValue) {
			Map<String, Object> map = oldValues.get(w);
			if(map == null) {
				map = new HashMap<String, Object>();
				oldValues.put(w, map);
			}
			map.put(attribute, oldValue);
		}

		public Map<IWrapper, Map<String, Object>> getOldValues() {
			return oldValues;
		}

		public Map<IWrapper, Map<String, INakedObject>> getObjectsAddedToCollections() {
			return objectsAddedToCollections;
		}

		public Map<IWrapper, Map<String, INakedObject>> getObjectsRemovedFromCollections() {
			return objectsRemovedFromCollections;
		}

		public void setInsertedObject(INakedObject content) {
			insertedObject = content;
		}

		public INakedObject getInsertedObject() {
			return insertedObject;
		}

		public Map<String, Map<Id, Map<String, List<Id>>>> getManyToManyAdded() {
			return manyToManyAddedByClassName;
		}

		public Map<String, Map<Id, Map<String, List<Id>>>> getManyToManyRemoved() {
			return manyToManyRemovedByClassName;
		}



	}

	private WrappedObjectsRepository() {
		pauseModifListener = false;
		insertedObjects = new ArrayList<INakedObject>();
		pendingValidations = new ArrayList<INakedObject>();
		updatedObjects = new HashMap<String, Map<Id,Map<String,Serializable>>>();
		updatedWrappersByClassName = new HashMap<String, Map<Id,IWrapper>>();
		removedObjectsIdsByClassName = new HashMap<String, Set<Id>>();
		objectsToRefreshByClassName = new HashMap<String, Map<Id,IWrapper>>();
		manyToManyAddedByClassName = new HashMap<String, Map<Id, Map<String, List<Id>>>>();
		manyToManyRemovedByClassName = new HashMap<String, Map<Id, Map<String, List<Id>>>>();
		pauseFireEvents = false;
		lastCommitDate = 0L;
	}

	public static WrappedObjectsRepository getInstance() {
		return instance;
	}

	public void setPauseModifListener(boolean pauseModifListener) {
		this.pauseModifListener = pauseModifListener;
	}

	public List<INakedObject> getInsertedObjects() {
		return insertedObjects;
	}

	public Map<String, Map<Id, Map<String, Serializable>>> getUpdatedObjects() {
		return updatedObjects;
	}

	public Map<String, Set<Id>> getRemovedObjectsIdsByClassName() {
		return removedObjectsIdsByClassName;
	}

	public void addInsertedObject(INakedObject content) {
		if(!pauseModifListener) {
			insertedObjects.add(content);
			getLastSavedState().setInsertedObject(content);
			WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
		}
	}

	public void addUpdatedObject(IWrapper w, String attribute, Object oldValue, Object newValue) {
		String className = w.getWrappedClassName();
		Id id = PersistenceManager.getId(w);
		if(!pauseModifListener) {
			if(id != null && !id.isLocal()) {
				if(id.getFieldNames().size() > 1) {
					for(INakedObject o : insertedObjects) {
						if(o.getClass().getName().equals(className)) {
							IWrapper tmp = WrapperContext.getEmptyWrappersMap().get(className).newWrapper();
							tmp.setContent(o);
							Id newId = PersistenceManager.getId(tmp);
							if(newId != null && newId.equalsExcludeField(id, attribute)) {
								try {
									IFieldInfo fi = WrapperContext.getFieldInfoByName().get(className).get(attribute);
									if(fi.isManyToOne() || fi.isOneToOne())
										tmp.setNakedObjectAttribute(attribute, (INakedObject)newValue, false);
									else if(fi.isEnum())
										tmp.setEnumAttribute(attribute, newValue==null?null:((Enum)newValue).name(), false);
									else
										tmp.setAttribute(attribute, newValue==null?null:newValue.toString(), false);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return;
							}
						}
					}
				} else {
					for(INakedObject o : insertedObjects) {
						if(o.getClass().getName().equals(className) && PersistenceManager.getId(o).equals(id)) {
							return;
						}
					}
				}
				Map<Id, Map<String, Serializable>> classMap = updatedObjects.get(className);
				Map<Id, IWrapper> wrappersMap = updatedWrappersByClassName.get(className);
				if(classMap == null) {
					classMap = new HashMap<Id, Map<String,Serializable>>();
					updatedObjects.put(className, classMap);
					wrappersMap = new HashMap<Id, IWrapper>();
					updatedWrappersByClassName.put(className, wrappersMap);
				}
				wrappersMap.put(id, w);
				Map<String, Serializable> map = classMap.get(id);
				if(map == null) {
					map = new HashMap<String, Serializable>();
					classMap.put(id, map);
				}
				if(newValue != null && newValue instanceof INakedObject) {
					INakedObject no = (INakedObject) newValue;
					Id myId = PersistenceManager.getId(no);
					if(myId != null) {
						map.put(attribute, new NakedObjectHandler(no.getClass().getName(), myId));
					} else
						map.put(attribute, (Serializable)newValue);
				}
				else
					map.put(attribute, (Serializable)newValue);
				WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
			}
			if(!savedStates.isEmpty()) {
				SavedState state = savedStates.get(savedStates.size()-1);
				state.recordOldValue(w, attribute, oldValue);
			}

		}
	}

	public void addRemovedObject(IWrapper wrapper) {
		INakedObject content = wrapper.getContent();
		if((!pauseModifListener  || !savedStates.isEmpty()) && !insertedObjects.remove(content)) {
			String className = wrapper.getWrappedClassName();
			Set<Id> set = removedObjectsIdsByClassName.get(className);
			if(set == null) {
				set = new HashSet<Id>();
				removedObjectsIdsByClassName.put(className, set);
			}
			Id id = PersistenceManager.getId(wrapper);
			set.add(id);
			Map<Id, Map<String, Serializable>> map = updatedObjects.get(className);
			if(map != null) {
				map.remove(id);
				updatedWrappersByClassName.get(className).remove(id);
			}
			WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
		}
	}

	public void removeObjectFromLists(String className) {
		Iterator<INakedObject> it = insertedObjects.iterator();
		while(it.hasNext()) {
			INakedObject o = it.next();
			if(o.getClass().getName().equals(className)) {
				it.remove();
			}
		}
		updatedObjects.remove(className);
		updatedWrappersByClassName.remove(className);
		removedObjectsIdsByClassName.remove(className);
		objectsToRefreshByClassName.remove(className);
		manyToManyAddedByClassName.remove(className);
		manyToManyRemovedByClassName.remove(className);
		WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
	}



	public void removeObjectFromLists(IWrapper w) {
		INakedObject content = w.getContent();
		String className = w.getWrappedClassName();
		Id id = PersistenceManager.getId(w);
		insertedObjects.remove(content);
		Map<Id, Map<String, Serializable>> map = updatedObjects.get(className);
		if(map != null) {
			map.remove(id);
			updatedWrappersByClassName.get(className).remove(id);
		}
		Set<Id> set = removedObjectsIdsByClassName.get(className);
		if(set!=null) {
			set.remove(id);
		}
		WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
	}

	public boolean hasToCommit() {
		return !insertedObjects.isEmpty() || !updatedObjects.isEmpty() || !removedObjectsIdsByClassName.isEmpty() || !manyToManyAddedByClassName.isEmpty() || !manyToManyRemovedByClassName.isEmpty();
	}

	public List<INakedObject> getPendingValidations() {
		return pendingValidations;
	}

	public void setInsertedObjects(List<INakedObject> insertedObjects) {
		this.insertedObjects = insertedObjects;
	}

	public void clear() {
		clear(true);
	}

	public void clear(boolean reinitLocalSequence) {
		if(reinitLocalSequence)
			PersistenceManager.reinitSequence();
		insertedObjects.clear();
		pendingValidations.clear();
		updatedObjects.clear();
		updatedWrappersByClassName.clear();
		removedObjectsIdsByClassName.clear();
		objectsToRefreshByClassName.clear();
		manyToManyAddedByClassName.clear();
		manyToManyRemovedByClassName.clear();
		WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
	}

	public Map<Id, IWrapper> getObjectsToRefresh(String className) {
		return objectsToRefreshByClassName.get(className);
	}

	public Map<String, Map<Id, IWrapper>> getObjectsToRefreshByClassName() {
		return objectsToRefreshByClassName;
	}

	public void addWrapperToRefresh(IWrapper w) {
		String className = w.getWrappedClassName();
		Map<Id, IWrapper> map = objectsToRefreshByClassName.get(className);
		if(map == null) {
			map = new HashMap<Id, IWrapper>();
			objectsToRefreshByClassName.put(className, map);
		}
		map.put(PersistenceManager.getId(w), w);
	}

	public void setLastCommitDate(long timeInMillis) {
		this.lastCommitDate = timeInMillis;
	}

	public long getLastCommitDate() {
		return lastCommitDate;
	}

	public void setPauseFireEvents(boolean b) {
		pauseFireEvents = b;
	}

	public boolean isPauseFireEvents() {
		return pauseFireEvents;
	}

	public boolean isPauseModifListener() {
		return pauseModifListener;
	}

	public int saveState() {
		SavedState s = new SavedState();
		savedStates.add(s);
		return savedStates.indexOf(s);
	}

	public boolean hasPendingChanges(int idx) {
		if(idx >= 0) {
			for(int i = idx; i<savedStates.size(); i++) {
				SavedState s = savedStates.get(i);
				if(!s.getOldValues().isEmpty() || !s.getObjectsAddedToCollections().isEmpty()
						|| !s.getObjectsRemovedFromCollections().isEmpty() || s.getInsertedObject() != null)
					return true;
			}
		}
		return false;
	}

	public void restoreSavedState(int idx) {
		savedStates.get(idx).restore();
		setPauseModifListener(true);
		while(savedStates.size() > idx) {
			SavedState state = savedStates.remove(savedStates.size() - 1);
			Map<IWrapper, Map<String, Object>> oldValues = state.getOldValues();
			for(IWrapper w : oldValues.keySet()) {
				Map<String, Object> oldVals = oldValues.get(w);
				Map<String, IFieldInfo> fiMap = WrapperContext.getFieldInfoByName().get(w.getWrappedClassName());
				for(String att : oldVals.keySet()) {
					IFieldInfo fi = fiMap.get(att);
					Object value = oldVals.get(att);
					if(fi.isManyToOne())
						w.setNakedObjectAttribute(att, (INakedObject) value);
					else if(fi.isEnum())
						w.setEnumAttribute(att, (String) value);
					else if(!(fi.isId() && value == null))
						w.setObjectAttribute(att, value);
				}
				oldVals.clear();
			}
			Map<IWrapper, Map<String, INakedObject>> added = state.getObjectsAddedToCollections();
			for(IWrapper w : added.keySet()) {
				Map<String, INakedObject> map = added.get(w);
				for(String att : map.keySet()) {
					w.removeObjectFromCollection(att, map.get(att));
					Map<Id, Map<String, List<Id>>> map2 = manyToManyAddedByClassName.get(w.getWrappedClassName());
					if(map2 != null) {
						Id id = PersistenceManager.getId(w);
						if(id != null) {
							Map<String, List<Id>> map3 = map2.get(id);
							if(map3 != null) {
								map3.remove(att);
								if(map3.isEmpty()) {
									map2.remove(id);
									if(map2.isEmpty())
										manyToManyAddedByClassName.remove(w.getWrappedClassName());
								}
							}

						}

					}
				}
				map.clear();
			}
			Map<IWrapper, Map<String, INakedObject>> removed = state.getObjectsRemovedFromCollections();
			for(IWrapper w : removed.keySet()) {
				Map<String, INakedObject> map = removed.get(w);
				for(String att : map.keySet()) {
					w.addObjectToCollection(att, map.get(att));
				}
				map.clear();
			}
			INakedObject insertedObject = state.getInsertedObject();
			if(insertedObject != null) {
				insertedObjects.remove(insertedObject);
			}

		}
		WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
		setPauseModifListener(false);
	}

	public void clearSavedStatesIfNeeded(int idx) {
		if(idx == 0) {
			savedStates.clear();
		}
	}

	public boolean isSavedStateListEmpty() {
		return savedStates.isEmpty();
	}

	public SavedState getLastSavedState() {
		return savedStates.get(savedStates.size() - 1);
	}

	public void checkNewID(Id originalId, IWrapper w) {
		Id newId = PersistenceManager.getId(w);
		if(originalId != null && newId != null && !newId.equals(originalId)) {
			String className = w.getWrappedClassName();
			Map<Id, Map<String, Serializable>> map = updatedObjects.get(className);
			if(map != null) {
				map.remove(originalId);
				Map<Id, IWrapper> map2 = updatedWrappersByClassName.get(className);
				map2.remove(originalId);
				map.remove(newId);
				map2.remove(newId);
				if(map.isEmpty())
					updatedObjects.remove(className);
				if(map2.isEmpty())
					updatedWrappersByClassName.remove(className);
			}
			Set<Id> set = removedObjectsIdsByClassName.get(className);
			if(set == null) {
				set = new HashSet<Id>();
				removedObjectsIdsByClassName.put(className, set);
			}
			set.add(originalId);
			insertedObjects.add(w.getContent());
		}
		WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
	}

	public Map<String, Map<Id, IWrapper>> getUpdatedWrappersByClassName() {
		return updatedWrappersByClassName;
	}


	public void addManyToMany(String parentWrapperClassName, Id parentWrapperId, String attribute, Id addedObjectId) {
		if(removeFromMap(parentWrapperClassName, parentWrapperId, attribute,
				addedObjectId, manyToManyRemovedByClassName)) {
			if(!isSavedStateListEmpty()) {
				removeFromMap(parentWrapperClassName, parentWrapperId, attribute,
						addedObjectId, getLastSavedState().getManyToManyRemoved());
			}
		} else {
			addToMap(parentWrapperClassName, parentWrapperId, attribute, addedObjectId, manyToManyAddedByClassName);
			if(!isSavedStateListEmpty()) {
				addToMap(parentWrapperClassName, parentWrapperId, attribute, addedObjectId, getLastSavedState().getManyToManyAdded());
			}
		}
	}

	private void addToMap(String parentWrapperClassName, Id parentOjectId,
			String attribute, Id objectId,
			Map<String, Map<Id, Map<String, List<Id>>>> m) {
		Map<Id, Map<String, List<Id>>> map = m.get(parentWrapperClassName);
		if(map == null) {
			map = new HashMap<Id, Map<String, List<Id>>>();
			m.put(parentWrapperClassName, map);
		}
		Map<String, List<Id>> map2 = map.get(parentOjectId);
		if(map2 == null) {
			map2 = new HashMap<String, List<Id>>();
			map.put(parentOjectId, map2);
		}
		List<Id> list = map2.get(attribute);
		if(list == null) {
			list = new ArrayList<Id>();
			map2.put(attribute, list);
		}
		list.add(objectId);
	}


	public void removeManyToMany(String parentWrapperClassName, Id parentWrapperId, String attribute, Id removedObjectId) {
		if(removeFromMap(parentWrapperClassName, parentWrapperId, attribute,
				removedObjectId, manyToManyAddedByClassName)) {
			if(!isSavedStateListEmpty()) {
				removeFromMap(parentWrapperClassName, parentWrapperId, attribute,
						removedObjectId, getLastSavedState().getManyToManyAdded());
			}
		} else {
			addToMap(parentWrapperClassName, parentWrapperId, attribute, removedObjectId, manyToManyRemovedByClassName);
			if(!isSavedStateListEmpty()) {
				addToMap(parentWrapperClassName, parentWrapperId, attribute, removedObjectId, getLastSavedState().getManyToManyRemoved());
			}
		}
	}

	private boolean removeFromMap(String parentWrapperClassName,
			Id parentWrapperId, String attribute, Id removedObjectId,
			Map<String, Map<Id, Map<String, List<Id>>>> mm) {
		Map<Id, Map<String, List<Id>>> map = mm.get(parentWrapperClassName);
		if(map != null) {
			Map<String, List<Id>> map2 = map.get(parentWrapperId);
			if(map2 != null) {
				List<Id> list = map2.get(attribute);
				if(list != null) {
					return list.remove(removedObjectId);
				}
			}
		}
		return false;
	}

	public Map<String, Map<Id, Map<String, List<Id>>>> getManyToManyAddedByClassName() {
		return manyToManyAddedByClassName;
	}

	public Map<String, Map<Id, Map<String, List<Id>>>> getManyToManyRemovedByClassName() {
		return manyToManyRemovedByClassName;
	}

	public ObjectsToPersist getAllObjectsToPersist() {
		return new ObjectsToPersist(getValidInsertedObjects(), getUpdatedObjects(), getRemovedObjectsIdsByClassName(),
				getManyToManyAddedByClassName(), getManyToManyRemovedByClassName());
	}

	private List<INakedObject> getValidInsertedObjects() {
		List<INakedObject> l = new ArrayList<INakedObject>();
		for(INakedObject no : insertedObjects) {
			if(!pendingValidations.contains(no))
				l.add(no);
		}
		return l;
	}

	public void saveAll(final AsyncCallback<Void> cb) {
		ViewHelper.showWaitPopup(NakedConstants.constants.uploading());
		PersistenceServiceAsync service = PersistenceService.Util.getInstance();
		AsyncCallback<Map<Id, INakedObject>> callback = new AsyncCallback<Map<Id, INakedObject>>() {

			public void onFailure(Throwable caught) {
				cb.onFailure(caught);
				ViewHelper.hideWaitPopup();
			}

			public void onSuccess(Map<Id, INakedObject> result) {
				setLastCommitDate(new Date().getTime());
				clear();
				ViewHelper.hideWaitPopup();
				cb.onSuccess(null);
			}

		};
		service.persist(getAllObjectsToPersist(), callback);
	}

	public void processObjectAddedEvent(WrapperObjectAddedEvent event) {
		if(!isSavedStateListEmpty()) {
		    Map<IWrapper, Map<String, INakedObject>> added = getLastSavedState().getObjectsAddedToCollections();
		    Map<String, INakedObject> map = added.get(event.getWrapper());
		    if(map == null) {
		    	map = new HashMap<String, INakedObject>();
		        added.put(event.getWrapper(), map);
		    }
		    map.put(event.getAttribute(), event.getValue());
		}
	}

	public void processObjectRemovedEvent(IWrapper parentWrapper, String attribute, INakedObject no) {
		if(!isSavedStateListEmpty()) {
            Map<IWrapper, Map<String, INakedObject>> added = getLastSavedState().getObjectsRemovedFromCollections();
            Map<String, INakedObject> map = added.get(parentWrapper);
            if(map == null) {
                map = new HashMap<String, INakedObject>();
                added.put(parentWrapper, map);
            }
            map.put(attribute, no);
        }
	}

}
