package com.hiperf.common.ui.server;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.server.storage.impl.StorageService;
import com.hiperf.common.ui.shared.CommonUtil;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.model.Filter;
import com.hiperf.common.ui.shared.model.LanguageEnum;
import com.hiperf.common.ui.shared.model.ScreenLabels;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.NakedObjectsList;
import com.hiperf.common.ui.shared.util.TableConfig;

public class PersistenceServiceImpl extends RemoteServiceServlet implements
		PersistenceService {

	public NakedObjectsList loadAll(String className, String currentFilter,
			String sortAttribute, boolean asc, int page, int rowsPerPage,
			ObjectsToPersist toPersist) throws PersistenceException {
		return StorageService.getInstance().loadAll(className, currentFilter,
				page, rowsPerPage, sortAttribute, asc, toPersist,
				getLocale());
	}

	protected Locale getLocale() {
		HttpSession session = getThreadLocalRequest().getSession(false);
		if(session != null) {
			Object o = session.getAttribute("locale");
			if(o != null && o instanceof Locale)
				return (Locale)o;
		}
		return getThreadLocalRequest().getLocale();
	}

	public NakedObjectsList loadAll(String className, String currentFilter,
			String sortAttribute, boolean distinct, boolean asc, int page, int rowsPerPage,
			ObjectsToPersist toPersist) throws PersistenceException {
		return StorageService.getInstance().loadAll(className, currentFilter,
				page, rowsPerPage, sortAttribute, distinct,  asc, toPersist,
				getLocale());
	}

	public Long saveFilter(Filter f) throws PersistenceException {
		return StorageService.getInstance().saveFilter(f, getConnectedUser());
	}

	private String getConnectedUser() {
		String usr = RequestHelper.getLoggedUser(getThreadLocalRequest());
		return usr == null ? "<anonymous>" : usr;
	}

	public NakedObjectsList sort(String className, String currentFilter,
			String attribute, boolean distinct, boolean asc, int page, int nbRows,
			ObjectsToPersist toPersist) throws PersistenceException {
		return StorageService.getInstance().sort(className, currentFilter,
				attribute, distinct, asc, page, nbRows, toPersist,
				getLocale());
	}

	public Map<Id, INakedObject> persist(ObjectsToPersist toPersist)
			throws PersistenceException {
		return StorageService.getInstance().persist(toPersist,
				getConnectedUser(), getLocale());
	}

	public List<INakedObject> reload(String nakedObjectName,
			List<String> idFields, List<List<Object>> idList)
			throws PersistenceException {
		return StorageService.getInstance().reload(nakedObjectName, idFields,
				idList);
	}

	public INakedObject get(String className, Id id)
			throws PersistenceException {
		return StorageService.getInstance().get(className, id);
	}

	public Map<String, String> getAll(String rootClassName, String filter, String attPrefix, String childClassName, String childAttribute)
			throws PersistenceException {
		return StorageService.getInstance().getAll(rootClassName, filter, attPrefix, childClassName, childAttribute);
	}

	public Collection<INakedObject> getCollection(String nakedObjectName,
			Id id, String attributeName) throws PersistenceException {
		return StorageService.getInstance().getCollection(nakedObjectName, id,
				attributeName);
	}

	public NakedObjectsList getSortedCollection(String nakedObjectName, Id id,
			String attributeName, String sortAttribute, boolean asc, int page,
			int rowsPerPage, ObjectsToPersist toPersist)
			throws PersistenceException {
		return StorageService.getInstance().getSortedCollection(
				nakedObjectName, id, attributeName, sortAttribute, asc, page,
				rowsPerPage, toPersist);
	}

	public NakedObjectsList getCollection(String nakedObjectName, Id id,
			String attributeName, int page,
			int rowsPerPage, ObjectsToPersist toPersist)
			throws PersistenceException {
		return StorageService.getInstance().getCollection(
				nakedObjectName, id, attributeName, page,
				rowsPerPage, toPersist);
	}

	public NakedObjectsList getCollectionInverse(String wrappedClassName, String attribute, Id id,
			int page,
			int rowsPerPage, ObjectsToPersist toPersist)
			throws PersistenceException {
		return StorageService.getInstance().getCollectionInverse(wrappedClassName,
				attribute, id, page,
				rowsPerPage, toPersist);
	}

	@Override
	public NakedObjectsList getSortedCollectionInverse(String wrappedClassName,
			String attribute, Id id, String sortAttribute, Boolean asc,
			int page, int nbRows, ObjectsToPersist allObjectsToPersist)
			throws PersistenceException {
		return StorageService.getInstance().getSortedCollectionInverse(wrappedClassName,
				attribute, id, page,
				nbRows, allObjectsToPersist, sortAttribute, asc);
	}


	public INakedObject getLinkedObject(String nakedObjectName, Id id,
			String attributeName) throws PersistenceException {
		return StorageService.getInstance().getLinkedObject(nakedObjectName,
				id, attributeName);
	}

	public Map<Long, String> getFilters(String viewName, String className)
			throws PersistenceException {
		return StorageService.getInstance().getFilters(viewName, className,
				getConnectedUser());
	}

	public Filter getFilter(Long id)
			throws PersistenceException {
		return StorageService.getInstance().getFilter(id);
	}

	public void saveConfiguration(String viewName, String className, int nbRows,
			List<HeaderInfo> headers, ScreenLabels labels, String langCode) throws PersistenceException {
		StorageService.getInstance().saveConfiguration(viewName, className, nbRows,
				headers, labels, getConnectedUser(), getLanguage(langCode));
	}

	private LanguageEnum getLanguage(String langCode) {
		return CommonUtil.getLanguage(langCode);
	}

	public Map<String, TableConfig> getScreenConfigurations(String langCode)
			throws PersistenceException {
		return StorageService.getInstance().getScreenConfigurations(
				getConnectedUser(), getLanguage(langCode));
	}

	public String getFileName(String fileClass, String fileNameField, String id)
			throws PersistenceException {
		return StorageService.getInstance().getFileName(fileClass,
				fileNameField, id);
	}

	public String checkExists(String className, String attribute, String value)
			throws PersistenceException {
		return StorageService.getInstance().checkExists(className, attribute,
				value, getLocale());
	}

	public String checkExists(String className, Id id, String attribute,
			String value) throws PersistenceException {
		return StorageService.getInstance().checkExists(className, id,
				attribute, value, getLocale());
	}

	@Override
	public CollectionInfo getLazyCollection(String wrappedClassName, Id id,
			String attribute) throws PersistenceException {
		return StorageService.getInstance().getLazyCollection(wrappedClassName, id, attribute);
	}

	@Override
	public void removeFilter(Long id) throws PersistenceException {
		StorageService.getInstance().removeFilter(id);
	}

	@Override
	public List<INakedObject> getAll(String rootClassName, String jpqlQuery) throws PersistenceException {
		return StorageService.getInstance().getAll(rootClassName, jpqlQuery);
	}

}
