package com.hiperf.common.ui.client.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.model.Filter;
import com.hiperf.common.ui.shared.model.ScreenLabels;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.NakedObjectsList;
import com.hiperf.common.ui.shared.util.TableConfig;

public interface PersistenceServiceAsync {

	void persist(ObjectsToPersist toPersist, AsyncCallback<Map<Id, INakedObject>> callback);

	void loadAll(String className, String currentFilter, String sortAttribute, boolean asc, int page, int rowsPerPage,
			ObjectsToPersist toPersist, AsyncCallback<NakedObjectsList> callback);

	void loadAll(String className, String currentFilter, String sortAttribute, boolean distinct, boolean asc, int page, int rowsPerPage,
			ObjectsToPersist toPersist, AsyncCallback<NakedObjectsList> callback);

	void sort(String nakedObjectName, String currentFilter, String attribute, boolean distinct, boolean asc, int page, int nbRows,
			ObjectsToPersist toPersist, AsyncCallback<NakedObjectsList> callback);

	void reload(String nakedObjectName, List<String> idFields,
			List<List<Object>> idList, AsyncCallback<List<INakedObject>> callback);

	void get(String nakedObjectName, Id id, AsyncCallback<INakedObject> callback);

	void getCollection(String className, Id id, String attributeName, AsyncCallback<Collection<INakedObject>> callback);

	void getLinkedObject(String className, Id id, String attributeName, AsyncCallback<INakedObject> callback);

	void getAll(String rootClassName, String filter, String attPrefix, String childClassName, String childAttribute, AsyncCallback<Map<String, String>> callback);

	void saveFilter(Filter f, AsyncCallback<Long> callback);

	void getFilters(String viewName, String className, AsyncCallback<Map<Long, String>> callback);

	void getFilter(Long id, AsyncCallback<Filter> callback);

	void getSortedCollection(String nakedObjectName, Id id,
			String attribute, String sortAttribute, boolean asc, int page,
			int nbRows, ObjectsToPersist toPersist, AsyncCallback<NakedObjectsList> callback);

	void getSortedCollectionInverse(String wrappedClassName, String attribute,
			Id id, String sortAttribute, Boolean asc, int page, int nbRows,
			ObjectsToPersist allObjectsToPersist,
			AsyncCallback<NakedObjectsList> callback);

	void getCollection(String parentClassName, Id id, String attributeName,
			int page,
			int nbRows, ObjectsToPersist toPersist, AsyncCallback<NakedObjectsList> callback);

	void getCollectionInverse(String wrappedClassName, String attribute, Id id,
			int pageNb, int rowsNb, ObjectsToPersist allObjectsToPersist,
			AsyncCallback<NakedObjectsList> callback);

	void saveConfiguration(String viewName, String className, int nbRows, List<HeaderInfo> headers,
			ScreenLabels labels, String langCode, AsyncCallback<Void> callback);

	void getScreenConfigurations(String langCode, AsyncCallback<Map<String, TableConfig>> callback);

	void getFileName(String fileClass, String fileNameField, String id, AsyncCallback<String> callback);

	void checkExists(String className, String attribute, String value, AsyncCallback<String> callback);

	void checkExists(String className, Id id, String attribute, String value, AsyncCallback<String> callback);

	void getLazyCollection(String wrappedClassName, Id id, String attribute,
			AsyncCallback<CollectionInfo> callback);

	void removeFilter(Long id, AsyncCallback<Void> asyncCallback);

	void getAll(String rootClassName, String jpqlQuery, AsyncCallback<List<INakedObject>> asyncCallback);


}
