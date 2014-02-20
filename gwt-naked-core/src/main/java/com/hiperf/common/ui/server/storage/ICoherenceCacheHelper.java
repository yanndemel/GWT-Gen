package com.hiperf.common.ui.server.storage;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.server.util.sequence.IdGenerator;
import com.hiperf.common.ui.shared.annotation.UIManyToMany;
import com.hiperf.common.ui.shared.annotation.UIManyToOne;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.NakedObjectsList;

public interface ICoherenceCacheHelper {

	Map<String, List<Field>> getCollectionsByClass();

	Map<String, List<Field>> getMapsByClass();

	Map<String, Map<Field, OneToMany>> getOneToManiesByClass();

	Map<String, Map<Field, ManyToMany>> getManyToManiesByClass();

	Map<String, Map<Field, UIManyToMany>> getUiManyToManiesByClass();

	Map<String, Map<Field, UIManyToOne>> getManyToOnesByClass();

	Map<String, PropertyDescriptor[]> getPropertyDescriptorsByClassName();

	Map<String, Method> getGetIdByClass();

	void setCacheNameByClass(Map<String, String> cacheNameByClass);

	void setSequencesCacheName(String property);

	void setSequenceClassName(String property);

	Map<String, IdGenerator> getSequenceGeneratorByClass();

	String getSequencesCacheName();

	String getSequenceClassName();

	INakedObject get(String cacheName, com.hiperf.common.ui.shared.util.Id id) throws PersistenceException;

	Map<String, String> getAll(String cacheName, String rootClassName,
			String filter, String attPrefix, String childClassName,
			String childAttribute) throws PersistenceException;

	NakedObjectsList loadAll(String cacheName, String className,
			String currentFilter, int page, int rowsPerPage,
			String sortAttribute, boolean asc) throws PersistenceException;

	NakedObjectsList loadAll(String cacheName, String className,
			String currentFilter, int page, int rowsPerPage,
			String sortAttribute, boolean distinct, boolean asc) throws PersistenceException;

	Map<com.hiperf.common.ui.shared.util.Id, INakedObject> persist(
			List<INakedObject> toInsertCache,
			Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, Serializable>>> toUpdateCache,
			Map<String, Set<com.hiperf.common.ui.shared.util.Id>> toDeleteCache,
			Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> manyToManyAddedCache,
			Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> manyToManyRemovedCache,
			String userName) throws PersistenceException;

	List<INakedObject> reload(String cacheName, List<List<Object>> idList) throws PersistenceException;

	Collection<INakedObject> getCollection(String cacheName, String className,
			com.hiperf.common.ui.shared.util.Id id, String attributeName) throws PersistenceException;

	INakedObject getLinkedObject(String cacheName, String className,
			com.hiperf.common.ui.shared.util.Id id, String attributeName) throws PersistenceException;

	NakedObjectsList getSortedCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attribute,
			String sortAttribute, boolean asc, int page, int nbRows) throws PersistenceException;

	NakedObjectsList getCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attributeName,
			int page, int nbRows, ObjectsToPersist toPersist) throws PersistenceException;

	NakedObjectsList getCollectionInverse(String wrappedClassName,
			String attribute, com.hiperf.common.ui.shared.util.Id id, int page,
			int nbRows, ObjectsToPersist toPersist, String sortAttribute,
			Boolean asc) throws PersistenceException;

	void getExtractedData(HttpServletRequest req, HttpServletResponse resp,
			String cacheName, String className) throws ServletException;

	String checkExists(String className, String attribute, String value,
			Locale locale);

	String checkExists(String className,
			com.hiperf.common.ui.shared.util.Id id, String attribute,
			String value, Locale locale);

	Collection<INakedObject> getByAttribute(String className, String att,
			Object value) throws PersistenceException;

	CollectionInfo getLazyCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attribute);

	List<INakedObject> getAll(String jpqlQuery);

	IdGenerator getSequenceGenerator(SequenceGenerator gene);

	IdGenerator getUuidGenerator();

}
