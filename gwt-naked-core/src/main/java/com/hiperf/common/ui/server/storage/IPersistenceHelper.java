package com.hiperf.common.ui.server.storage;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
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
import com.hiperf.common.ui.shared.model.ScreenConfig;
import com.hiperf.common.ui.shared.model.ScreenLabels;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.NakedObjectsList;
import com.hiperf.common.ui.shared.util.TableConfig;

public interface IPersistenceHelper {

	public static enum TYPE{JTA,LOCAL}
	
	static final String SERVER_MESSAGES = "com.hiperf.common.ui.server.util.ServerMessages";

	public abstract CollectionInfo getLazyCollection(String className, com.hiperf.common.ui.shared.util.Id id,
			String attribute) throws PersistenceException;

	public abstract String checkExists(String className, com.hiperf.common.ui.shared.util.Id id, String attribute,
			String value, Locale locale) throws PersistenceException;

	public abstract String checkExists(String className, String attribute, String value,
			Locale locale) throws PersistenceException;

	public abstract Object getFileId(String existingId, String idField, PropertyDescriptor[] pds)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, PersistenceException;

	public abstract Object getFileId(String existingId, String fileClass)
			throws PersistenceException;

	public abstract String replaceFile(String fileClass, String fileNameField, String fileStorageField,
			String fileName, FileItem fileItem, String existingId) throws PersistenceException;

	public abstract Object saveFile(String fileClass, String fileNameField, String fileStorageField,
			String fileName, FileItem item) throws PersistenceException;

	public abstract void downloadFile(HttpServletResponse resp, String fileClass, String fileNameField,
			String fileStorageField, String fileId) throws PersistenceException;

	public abstract String getViewKey(ScreenConfig sc);

	public abstract TableConfig getTableConfig(LanguageEnum language, ScreenConfig sc, EntityManager em, Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext) throws PersistenceException;

	public abstract void addHeader(LanguageEnum language, Map<String, TableConfig> map, ScreenConfig sc, EntityManager em, Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext) throws PersistenceException;

	public abstract Map<String, TableConfig> getScreenConfigurations(String connectedUser, LanguageEnum language)
			throws PersistenceException;

	public abstract void saveConfiguration(String viewName, String className,
			int nbRows, List<HeaderInfo> headers, ScreenLabels sl, String connectedUser, LanguageEnum language)
			throws PersistenceException;

	public abstract void getExtractedData(HttpServletRequest req, HttpServletResponse resp,
			String className) throws ServletException;

	public abstract NakedObjectsList sort(String className, String currentFilter, String attribute,
			boolean distinct, boolean asc, int page, int nbRows, ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException;

	public abstract Long saveFilter(Filter f, String userName)
			throws PersistenceException;

	public abstract NakedObjectsList getSortedCollection(String nakedObjectName, com.hiperf.common.ui.shared.util.Id id,
			String attributeName, String sortAttribute, Boolean asc, int page, int rowsPerPage,
			ObjectsToPersist toPersist) throws PersistenceException;

	public abstract NakedObjectsList getCollection(String className, com.hiperf.common.ui.shared.util.Id id,
			String attributeName, int page, int nbRows, ObjectsToPersist toPersist)
			throws PersistenceException;

	public abstract NakedObjectsList getCollectionInverse(String wrappedClassName, String attribute,
			com.hiperf.common.ui.shared.util.Id id, int page, int nbRows, ObjectsToPersist toPersist, String sortAttribute,
			Boolean asc) throws PersistenceException;

	public abstract INakedObject getLinkedObject(String nakedObjectName, com.hiperf.common.ui.shared.util.Id id,
			String attributeName) throws PersistenceException;

	public abstract List<INakedObject> reload(String nakedObjectName, List<String> idFields, List<List<Object>> idList)
			throws PersistenceException;

	public abstract boolean doPersist(ObjectsToPersist toPersist, Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res, Map<Object, IdHolder> newIdByOldId,
			EntityManager em, Locale locale) throws ClassNotFoundException,
			IntrospectionException, PersistenceException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException;

	public abstract Map<com.hiperf.common.ui.shared.util.Id, INakedObject> persist(ObjectsToPersist toPersist, String userName, Locale locale)
			throws PersistenceException;

	public abstract NakedObjectsList queryData(String className, String currentFilter, int page,
			int rowsPerPage, String orderBy, ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException;

	public abstract NakedObjectsList queryData(String className, String currentFilter, int page,
			int rowsPerPage, String orderBy, ObjectsToPersist toPersist, Locale locale, boolean distinct)
			throws PersistenceException;

	public abstract Map<Long, String> getFilters(String viewName, String className, String userName)
			throws PersistenceException;

	public abstract Collection<INakedObject> getCollection(String nakedObjectName, com.hiperf.common.ui.shared.util.Id id, String attributeName)
			throws PersistenceException;

	public abstract Filter getFilter(Long id) throws PersistenceException;

	public abstract Map<String,String> getAll(String rootClassName, String filter, String attPrefix, String childClassName, String childAttribute)
			throws PersistenceException;

	public abstract INakedObject get(String nakedObjectName, com.hiperf.common.ui.shared.util.Id id)
			throws PersistenceException;

	public abstract void updateAttributeValue(String className, INakedObject original,
			String att, Object object) throws PersistenceException;

	public abstract Object getCompositeId(Class<?> c, List<String> idFieldNames, List myId)
			throws InstantiationException, IllegalAccessException;

	public abstract INakedObject deproxyLinkedObject(INakedObject no, String attributeName,
			EntityManager em) throws PersistenceException;

	public abstract Collection<INakedObject> deproxyCollection(INakedObject no, String attributeName,
			EntityManager em, Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext) throws PersistenceException;

	public abstract String getFileName(String fileClassName, String fileNameField, Object fileId)
			throws PersistenceException;

	public abstract CollectionInfo getCollectionInfo(EntityManager em, com.hiperf.common.ui.shared.util.Id id,
			String className, String attName);

	public abstract INakedObject deproxyNakedObject(INakedObject no, EntityManager em,
			Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext) throws PersistenceException;

	public abstract List<INakedObject> deproxyEntities(String className, List<INakedObject> list,
			boolean root, Map<String, Map<Object, Object>> oldIdByNewId) throws PersistenceException;

	public abstract EntityManager getEntityManager();

	public abstract TransactionContext createTransactionalContext();

	public abstract boolean hasManyToOne(String className);

	public abstract void removeFilter(Long id) throws PersistenceException;

	public abstract String getSortClause(String sortAttribute);

	public abstract Collection<INakedObject> getByAttribute(String className,
			String att, Object value, EntityManager em);

	public abstract void shutdown();

	public abstract Id getId(String string, String className);
	
	public abstract Id getId(INakedObject o) throws IllegalAccessException, InvocationTargetException;
	 
	void close(TransactionContext tc);

	public abstract void close(EntityManager em);

	void sendFile(HttpServletResponse resp, String name, byte[] b) throws IOException, ServletException;

	List<INakedObject> getAll(String jpqlQuery) throws PersistenceException;
	
	String getMessage(String key, Locale l);

}
