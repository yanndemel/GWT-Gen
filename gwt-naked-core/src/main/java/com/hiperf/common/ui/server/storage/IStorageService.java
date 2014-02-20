package com.hiperf.common.ui.server.storage;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.server.tx.TransactionContext;
import com.hiperf.common.ui.server.util.IdHolder;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.model.Filter;
import com.hiperf.common.ui.shared.model.LanguageEnum;
import com.hiperf.common.ui.shared.model.ScreenLabels;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.NakedObjectsList;
import com.hiperf.common.ui.shared.util.TableConfig;

public interface IStorageService {

	public abstract CollectionInfo getLazyCollection(String className, com.hiperf.common.ui.shared.util.Id id,
			String attribute) throws PersistenceException;

	public abstract void doPersist(ObjectsToPersist toPersist, Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res, 
			Map<Object, IdHolder> newIdByOldId, EntityManager em, Locale locale) throws ClassNotFoundException,
			IntrospectionException, PersistenceException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException;

	public abstract void processDownload(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException;

	public abstract String checkExists(String className, com.hiperf.common.ui.shared.util.Id id, String attribute,
			String value, Locale locale) throws PersistenceException;

	public abstract String checkExists(String className, String attribute, String value,
			Locale locale) throws PersistenceException;

	public abstract String getFileName(String fileClass, String fileNameField, String id)
			throws PersistenceException;

	public abstract String replaceFile(String fileClass, String fileNameField, String fileStorageField,
			String fileName, FileItem fileItem, String existingId) throws PersistenceException;

	public abstract Object saveFile(String fileClass, String fileNameField, String fileStorageField,
			String fileName, FileItem item) throws PersistenceException;

	public abstract Map<String, TableConfig> getScreenConfigurations(String connectedUser, LanguageEnum language)
			throws PersistenceException;

	public abstract void saveConfiguration(String viewName, String className,
			int nbRows, List<HeaderInfo> headers, ScreenLabels labels, String connectedUser, LanguageEnum language)
			throws PersistenceException;

	public abstract void deproxyNakedObject(INakedObject no, EntityManager em)
			throws PersistenceException;

	public abstract TransactionContext createTransactionalContext();

	public abstract EntityManager createEntityManager();

	public abstract void getExtractedData(HttpServletRequest req, HttpServletResponse resp,
			String className) throws ServletException;

	public abstract NakedObjectsList sort(String className, String currentFilter, String attribute,
			boolean distinct, boolean asc, int page, int nbRows, ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException;

	public abstract Long saveFilter(Filter f, String userName)
			throws PersistenceException;

	public abstract NakedObjectsList getCollection(String className, com.hiperf.common.ui.shared.util.Id id,
			String attributeName, int page, int nbRows, ObjectsToPersist toPersist)
			throws PersistenceException;

	public abstract NakedObjectsList getSortedCollection(String className, com.hiperf.common.ui.shared.util.Id id,
			String attribute, String sortAttribute, boolean asc, int page, int nbRows,
			ObjectsToPersist toPersist) throws PersistenceException;

	public abstract INakedObject getLinkedObject(String className, com.hiperf.common.ui.shared.util.Id id,
			String attributeName) throws PersistenceException;

	public abstract Collection<INakedObject> getCollection(String className, com.hiperf.common.ui.shared.util.Id id, String attributeName)
			throws PersistenceException;

	public abstract List<INakedObject> reload(String className, List<String> idFields, List<List<Object>> idList)
			throws PersistenceException;

	public abstract Map<com.hiperf.common.ui.shared.util.Id, INakedObject> persist(ObjectsToPersist toPersist, String userName, Locale locale)
			throws PersistenceException;

	public abstract NakedObjectsList loadAll(String className, String currentFilter, int page,
			int rowsPerPage, String sortAttribute, boolean distinct, boolean asc, ObjectsToPersist toPersist,
			Locale locale) throws PersistenceException;

	public abstract NakedObjectsList loadAll(String className, String currentFilter, int page,
			int rowsPerPage, String sortAttribute, boolean asc, ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException;

	public abstract Map<Long, String> getFilters(String viewName, String className, String userName)
			throws PersistenceException;

	public abstract Filter getFilter(Long id) throws PersistenceException;

	public abstract Map<String, String> getAll(String rootClassName, String filter, String attPrefix, String childClassName, String childAttribute)
			throws PersistenceException;

	public abstract INakedObject get(String className, com.hiperf.common.ui.shared.util.Id id)
			throws PersistenceException;

	public abstract IPersistenceHelper getDefaultPH();

	public abstract ICoherenceCacheHelper getCachePH();

	public abstract NakedObjectsList getSortedCollectionInverse(String wrappedClassName,
			String attribute, com.hiperf.common.ui.shared.util.Id id, int page, int nbRows, ObjectsToPersist allObjectsToPersist,
			String sortAttribute, Boolean asc) throws PersistenceException;

	public abstract NakedObjectsList getCollectionInverse(String wrappedClassName, String attribute,
			com.hiperf.common.ui.shared.util.Id id, int page, int nbRows, ObjectsToPersist toPersist)
			throws PersistenceException;

	Collection<INakedObject> getByAttribute(String className, String att, Object value, EntityManager em) throws PersistenceException;

	void removeFilter(Long id) throws PersistenceException;

	void shutdown();

	void close(TransactionContext tc);

	void close(EntityManager em);

	List<INakedObject> getAll(String rootClassName, String jpqlQuery) throws PersistenceException;

	public abstract void downloadFile(HttpServletResponse resp, String fileClass, String fileNameField,
			String fileStorageField, String fileId) throws PersistenceException;

	public abstract void setTestPersistenceHelper(IPersistenceHelper ph);

}
