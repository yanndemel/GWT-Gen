package com.hiperf.common.ui.server.storage.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.UIManyToMany;
import org.gwtgen.api.shared.UIManyToOne;

import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.server.listener.GlobalParams;
import com.hiperf.common.ui.server.storage.CacheHelperFactory;
import com.hiperf.common.ui.server.storage.ICoherenceCacheHelper;
import com.hiperf.common.ui.server.storage.IPersistenceHelper;
import com.hiperf.common.ui.server.storage.IPersistenceHelper.TYPE;
import com.hiperf.common.ui.server.storage.IStorageService;
import com.hiperf.common.ui.server.tx.TransactionContext;
import com.hiperf.common.ui.server.util.IdHolder;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.IConstants;
import com.hiperf.common.ui.shared.model.Filter;
import com.hiperf.common.ui.shared.model.LanguageEnum;
import com.hiperf.common.ui.shared.model.ScreenLabels;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.NakedObjectsList;
import com.hiperf.common.ui.shared.util.TableConfig;

public final class StorageService implements IStorageService {

	private static final Logger logger = Logger.getLogger(StorageService.class
			.getName());

	private static final IStorageService INSTANCE =  new StorageService();

	private final Map<String, String> cacheNameByClass = new HashMap<String, String>();

	private IPersistenceHelper defaultPH = null;
	private ICoherenceCacheHelper cachePH = null;

	public static final Object[] emptyArg = new Object[0];

	private StorageService() {
		initPersistenceCache();
		initDefaultPersistence();
	}

	public static IStorageService getInstance() {
		return INSTANCE;
	}

	@Override
	public ICoherenceCacheHelper getCachePH() {
		return cachePH;
	}

	@Override
	public IPersistenceHelper getDefaultPH() {
		return defaultPH;
	}

	private void initDefaultPersistence() {
		GlobalParams p = GlobalParams.getInstance();
		if (p.getUnitName() != null) {
			if (TYPE.LOCAL.name().equalsIgnoreCase(p.getTransactionType())) {
				defaultPH = PersistenceHelper.createInstance(TYPE.LOCAL,
						p.getUnitName());
			} else {
				defaultPH = PersistenceHelper.createInstance(TYPE.JTA,
						p.getUnitName());
			}
		}
	}

	private OneToMany getOneToMany(String className, PropertyDescriptor[] pds,
			Field f) {
		if (f.isAnnotationPresent(OneToMany.class))
			return f.getAnnotation(OneToMany.class);
		for (PropertyDescriptor pd : pds) {
			if (pd.getName().equals(f.getName()) && pd.getReadMethod() != null)
				return pd.getReadMethod().getAnnotation(OneToMany.class);
		}
		return null;
	}

	private ManyToMany getManyToMany(String className,
			PropertyDescriptor[] pds, Field f) {
		if (f.isAnnotationPresent(ManyToMany.class))
			return f.getAnnotation(ManyToMany.class);
		for (PropertyDescriptor pd : pds) {
			if (pd.getName().equals(f.getName()) && pd.getReadMethod() != null)
				return pd.getReadMethod().getAnnotation(ManyToMany.class);
		}
		return null;
	}

	private UIManyToMany getUIManyToMany(String className,
			PropertyDescriptor[] pds, Field f) {
		if (f.isAnnotationPresent(UIManyToMany.class))
			return f.getAnnotation(UIManyToMany.class);
		for (PropertyDescriptor pd : pds) {
			if (pd.getName().equals(f.getName()) && pd.getReadMethod() != null)
				return pd.getReadMethod().getAnnotation(UIManyToMany.class);
		}
		return null;
	}

	private void initPersistenceCache() {
		try {
			Properties p = new Properties();
			InputStream mappingFileStream = StorageService.class
					.getClassLoader().getResourceAsStream(
							"coherence-cache-mapping.properties");
			if (mappingFileStream == null) {
				logger.info("No cache information provided.");
				return;
			}
			p.load(mappingFileStream);
			cachePH = CacheHelperFactory.getDefaultCacheHelper();
			for (String cacheName : p.stringPropertyNames()) {
				String className = p.getProperty(cacheName);
				Class<?> clazz = Class.forName(className);
				BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
				PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
				Method getIdMethod = findGetId(clazz);
				if (getIdMethod != null) {
					List<Field> collections = findCollections(clazz);
					List<Field> maps = findMaps(clazz);
					Map<Field, OneToMany> oneToManyMap = new HashMap<Field, OneToMany>();
					Map<Field, ManyToMany> manyToManyMap = new HashMap<Field, ManyToMany>();
					Map<Field, UIManyToMany> uiManyToManyMap = new HashMap<Field, UIManyToMany>();
					Map<Field, UIManyToOne> manyToOneMap = new HashMap<Field, UIManyToOne>();
					for (Field f : collections) {
						OneToMany ann = getOneToMany(className, pds, f);
						if (ann != null && ann.targetEntity() != null
								&& ann.mappedBy() != null) {
							oneToManyMap.put(f, ann);
						} else {
							ManyToMany ann2 = getManyToMany(className, pds, f);
							if (ann2 != null && ann2.targetEntity() != null
									&& ann2.mappedBy() != null) {
								manyToManyMap.put(f, ann2);
							} else {
								UIManyToMany ann3 = getUIManyToMany(className,
										pds, f);
								if (ann3 != null)
									uiManyToManyMap.put(f, ann3);
							}
						}
					}
					for (PropertyDescriptor pd : pds) {
						Method m = pd.getReadMethod();
						if (m != null
								&& m.isAnnotationPresent(UIManyToOne.class)) {
							manyToOneMap.put(
									clazz.getDeclaredField(pd.getName()),
									m.getAnnotation(UIManyToOne.class));
						}
					}

					cacheNameByClass.put(className, cacheName);
					if (!collections.isEmpty())
						cachePH.getCollectionsByClass().put(className,
								collections);
					if (!maps.isEmpty())
						cachePH.getMapsByClass().put(className, maps);
					if (!oneToManyMap.isEmpty())
						cachePH.getOneToManiesByClass().put(className,
								oneToManyMap);
					if (!manyToManyMap.isEmpty())
						cachePH.getManyToManiesByClass().put(className,
								manyToManyMap);
					if (!uiManyToManyMap.isEmpty())
						cachePH.getUiManyToManiesByClass().put(className,
								uiManyToManyMap);
					if (!manyToOneMap.isEmpty()) {
						cachePH.getManyToOnesByClass().put(className,
								manyToOneMap);
					}
					cachePH.getPropertyDescriptorsByClassName().put(className,
							pds);
					cachePH.getGetIdByClass().put(className, getIdMethod);

				} else {
					logger.severe("Class "
							+ className
							+ " does not define the @javax.persistence.Id annotation...Won't be taken into account");
				}
			}
			cachePH.setCacheNameByClass(cacheNameByClass);
			p.clear();
			p.load(StorageService.class.getClassLoader().getResourceAsStream(
					"coherence-cache-global.properties"));
			String property = p.getProperty("SequencesCache");
			if (property != null)
				cachePH.setSequencesCacheName(property);
			property = p.getProperty("SequenceClass");
			if (property != null)
				cachePH.setSequenceClassName(property);
			for (String className : cachePH.getGetIdByClass().keySet()) {
				Method getId = cachePH.getGetIdByClass().get(className);
				if (getId.isAnnotationPresent(SequenceGenerator.class)) {
					getSequenceGenerator(className, getId);
				} else {
					Class<?> clazz = Class.forName(className);
					Method method = null;
					try {
						method = clazz.getDeclaredMethod(getId.getName(),
								new Class[0]);
					} catch (Exception e) {
					}
					if (method != null
							&& method
									.isAnnotationPresent(SequenceGenerator.class)) {
						getSequenceGenerator(className, method);
					}
				}

			}
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"Exception while retrieving cache properties ...", e);
		}
	}

	private void getSequenceGenerator(String className, Method getId) {
		SequenceGenerator gene = getId.getAnnotation(SequenceGenerator.class);
		logger.info("gene for class " + className + " - method "
				+ getId.getName() + " = " + gene);
		if (gene != null) {
			Class<?> returnType = getId.getReturnType();
			if (returnType.equals(Long.class) || returnType.equals(long.class)) {
				cachePH.getSequenceGeneratorByClass()
						.put(className, cachePH.getSequenceGenerator(gene));
			} else if (returnType.equals(String.class)) {
				cachePH.getSequenceGeneratorByClass().put(className,
						cachePH.getUuidGenerator());
			}
		}
	}

	private List<Field> findMaps(Class<?> clazz) {
		List<Field> l = new ArrayList<Field>();
		do {
			for (Field f : clazz.getDeclaredFields()) {
				if (Map.class.isAssignableFrom(f.getType())) {
					l.add(f);
				}
			}
			clazz = clazz.getSuperclass();
		} while (clazz != null
				&& !clazz.getName().equals(Object.class.getName()));
		return l;
	}

	private List<Field> findCollections(Class<?> clazz) {
		List<Field> l = new ArrayList<Field>();
		do {
			for (Field f : clazz.getDeclaredFields()) {
				if (Collection.class.isAssignableFrom(f.getType())) {
					l.add(f);
				}
			}
			clazz = clazz.getSuperclass();
		} while (clazz != null
				&& !clazz.getName().equals(Object.class.getName()));
		return l;
	}

	private Method findGetId(Class<?> originalClazz)
			throws IntrospectionException {
		Class<?> clazz = originalClazz;
		Method getIdMethod = null;
		do {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class iClazz : interfaces) {
				getIdMethod = findGetId(iClazz);
				if (getIdMethod != null)
					break;
			}
			if (getIdMethod == null) {
				Method[] methods = clazz.getDeclaredMethods();
				for (Method m : methods) {
					if (m.isAnnotationPresent(Id.class)) {
						getIdMethod = m;
						break;
					}
				}
			}
			if (getIdMethod == null) {
				Field[] fields = clazz.getDeclaredFields();
				PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz)
						.getPropertyDescriptors();
				for (Field f : fields) {
					if (f.isAnnotationPresent(Id.class)) {
						for (PropertyDescriptor pd : pds) {
							if (pd.getName().equals(f.getName())) {
								getIdMethod = pd.getReadMethod();
								break;
							}
						}
					}
				}
			}
			clazz = clazz.getSuperclass();
		} while (clazz != null && getIdMethod == null
				&& !clazz.getName().equals(Object.class.getName()));
		return getIdMethod;
	}

	@Override
	public INakedObject get(String className, com.hiperf.common.ui.shared.util.Id id)
			throws PersistenceException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			if (id.getFieldNames().size() > 1)
				throw new PersistenceException(
						"The Id has to be on one field only for " + className);
			return cachePH.get(cacheName, id);
		}
		return defaultPH.get(className, id);
	}

	@Override
	public Map<String, String> getAll(String rootClassName, String filter, String attPrefix, String childClassName, String childAttribute)
			throws PersistenceException {
		String cacheName = cacheNameByClass.get(childClassName);
		if (cacheName != null) {
			return cachePH.getAll(cacheName, rootClassName, filter, attPrefix, childClassName, childAttribute);
		}
		return defaultPH.getAll(rootClassName, filter, attPrefix, childClassName, childAttribute);
	}

	@Override
	public Filter getFilter(Long id)
			throws PersistenceException {
		/*
		 * String cacheName = cacheNameByClass.get(Filter.class.getName());
		 * if(cacheName != null) { return cachePH.getFilter(className, name,
		 * userName); }
		 */
		return defaultPH.getFilter(id);
	}

	@Override
	public void removeFilter(Long id) throws PersistenceException {
		defaultPH.removeFilter(id);
	}

	@Override
	public Map<Long, String> getFilters(String viewName, String className, String userName)
			throws PersistenceException {
		/*
		 * String cacheName = cacheNameByClass.get(Filter.class.getName());
		 * if(cacheName != null) { return cachePH.getFilterNames(className,
		 * userName); }
		 */
		return defaultPH.getFilters(viewName, className, userName);
	}

	@Override
	public NakedObjectsList loadAll(String className, String currentFilter,
			int page, int rowsPerPage, String sortAttribute, boolean asc,
			ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException {
		return queryData(className, currentFilter, sortAttribute, asc,
					page, rowsPerPage, toPersist, locale);
	}

	@Override
	public NakedObjectsList loadAll(String className, String currentFilter,
			int page, int rowsPerPage, String sortAttribute, boolean distinct,
			boolean asc, ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException {
		return queryData(className, currentFilter, sortAttribute, distinct,
					asc, page, rowsPerPage, toPersist, locale);
	}

	private NakedObjectsList queryData(String className, String currentFilter,
			String sortAttribute, boolean asc, int page, int rowsPerPage,
			ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			return cachePH.loadAll(cacheName, className, currentFilter, page,
					rowsPerPage, sortAttribute, asc);
		}
		String orderBy = null;
		if (sortAttribute != null) {
			orderBy = "order by o." + sortAttribute;
			if (!asc)
				orderBy += " DESC";
		}
		return defaultPH.queryData(className, currentFilter, page, rowsPerPage,
				orderBy, toPersist, locale);
	}

	private NakedObjectsList queryData(String className, String currentFilter,
			String sortAttribute, boolean distinct, boolean asc, int page, int rowsPerPage,
			ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			return cachePH.loadAll(cacheName, className, currentFilter, page,
					rowsPerPage, sortAttribute, distinct, asc);
		}
		String orderBy = null;
		if (sortAttribute != null) {
			orderBy = defaultPH.getSortClause(sortAttribute);
			if (!asc)
				orderBy += " DESC";
		}
		return defaultPH.queryData(className, currentFilter, page, rowsPerPage,
				orderBy, toPersist, locale, distinct);

	}

	@Override
	public Map<com.hiperf.common.ui.shared.util.Id, INakedObject> persist(
			ObjectsToPersist toPersist, String userName, Locale locale)
			throws PersistenceException {
		List<INakedObject> toInsertCache = new ArrayList<INakedObject>();
		Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, Serializable>>> toUpdateCache = new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, Serializable>>>();
		Map<String, Set<com.hiperf.common.ui.shared.util.Id>> toDeleteCache = new HashMap<String, Set<com.hiperf.common.ui.shared.util.Id>>();
		Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> manyToManyAddedCache = new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>>();
		Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> manyToManyRemovedCache = new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>>();
		List<INakedObject> toInsert = toPersist.getInsertedObjects();
		if (toInsert != null) {
			Iterator<INakedObject> it = toInsert.iterator();
			while (it.hasNext()) {
				INakedObject no = it.next();
				if (cacheNameByClass.containsKey(no.getClass().getName())) {
					toInsertCache.add(no);
					it.remove();
				}
			}
		}
		Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, Serializable>>> toUpdate = toPersist
				.getUpdatedObjects();
		if (toUpdate != null) {
			Iterator<String> it2 = toUpdate.keySet().iterator();
			while (it2.hasNext()) {
				String key = it2.next();
				if (cacheNameByClass.containsKey(key)) {
					toUpdateCache.put(key, toUpdate.get(key));
					it2.remove();
				}
			}
		}
		Map<String, Set<com.hiperf.common.ui.shared.util.Id>> toDelete = toPersist
				.getRemovedObjectsIdsByClassName();
		if (toDelete != null) {
			Iterator<String> it3 = toDelete.keySet().iterator();
			while (it3.hasNext()) {
				String key = it3.next();
				if (cacheNameByClass.containsKey(key)) {
					toDeleteCache.put(key, toDelete.get(key));
					it3.remove();
				}
			}
		}
		Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> manyToManyAdded = toPersist
				.getManyToManyAddedByClassName();
		if (manyToManyAdded != null) {
			Iterator<Entry<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>>> it = manyToManyAdded
					.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> e = it
						.next();
				String className = e.getKey();
				if (cacheNameByClass.containsKey(className)) {
					manyToManyAddedCache.put(className, e.getValue());
					it.remove();
				}
			}
		}
		Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> manyToManyRemoved = toPersist
				.getManyToManyRemovedByClassName();
		if (manyToManyRemoved != null) {
			Iterator<Entry<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>>> it = manyToManyRemoved
					.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> e = it
						.next();
				String className = e.getKey();
				if (cacheNameByClass.containsKey(className)) {
					manyToManyRemovedCache.put(className, e.getValue());
					it.remove();
				}
			}
		}
		Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res = new HashMap<com.hiperf.common.ui.shared.util.Id, INakedObject>();
		if (!toInsertCache.isEmpty() || !toUpdateCache.isEmpty()
				|| !toDeleteCache.isEmpty() || !manyToManyAddedCache.isEmpty()
				|| !manyToManyRemovedCache.isEmpty()) {
			res = cachePH.persist(toInsertCache, toUpdateCache, toDeleteCache,
					manyToManyAddedCache, manyToManyRemovedCache, userName);
		}
		if ((toInsert != null && !toInsert.isEmpty())
				|| (toUpdate != null && !toUpdate.isEmpty())
				|| (toDelete != null && !toDelete.isEmpty())
				|| (manyToManyAdded != null && !manyToManyAdded.isEmpty())
				|| (toPersist.getManyToManyRemovedByClassName() != null && !toPersist
						.getManyToManyRemovedByClassName().isEmpty())) {
			res.putAll(defaultPH.persist(toPersist, userName, locale));
		}

		return res;
	}

	@Override
	public List<INakedObject> reload(String className, List<String> idFields,
			List<List<Object>> idList) throws PersistenceException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			return cachePH.reload(cacheName, idList);
		}
		return defaultPH.reload(className, idFields, idList);
	}

	@Override
	public Collection<INakedObject> getCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attributeName)
			throws PersistenceException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			return cachePH.getCollection(cacheName, className, id,
					attributeName);
		}
		return defaultPH.getCollection(className, id, attributeName);
	}

	@Override
	public INakedObject getLinkedObject(String className,
			com.hiperf.common.ui.shared.util.Id id, String attributeName)
			throws PersistenceException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			return cachePH.getLinkedObject(cacheName, className, id,
					attributeName);
		}
		return defaultPH.getLinkedObject(className, id, attributeName);
	}

	@Override
	public NakedObjectsList getSortedCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attribute,
			String sortAttribute, boolean asc, int page, int nbRows,
			ObjectsToPersist toPersist) throws PersistenceException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			return cachePH.getSortedCollection(className, id, attribute,
					sortAttribute, asc, page, nbRows);
		}
		return defaultPH.getSortedCollection(className, id, attribute,
				sortAttribute, asc, page, nbRows, toPersist);
	}

	@Override
	public NakedObjectsList getCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attributeName, int page, int nbRows,
			ObjectsToPersist toPersist) throws PersistenceException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			return cachePH.getCollection(className, id, attributeName, page, nbRows,toPersist);
		}
		return defaultPH.getCollection(className, id, attributeName, page, nbRows,toPersist);
	}

	@Override
	public NakedObjectsList getCollectionInverse(String wrappedClassName,
			String attribute, com.hiperf.common.ui.shared.util.Id id, int page, int nbRows,
			ObjectsToPersist toPersist) throws PersistenceException {
		String cacheName = cacheNameByClass.get(wrappedClassName);
		if (cacheName != null) {
			return cachePH.getCollectionInverse(wrappedClassName, attribute, id, page, nbRows,toPersist, null, null);
		}
		return defaultPH.getCollectionInverse(wrappedClassName, attribute, id, page, nbRows,toPersist, null, null);
	}

	@Override
	public NakedObjectsList getSortedCollectionInverse(String wrappedClassName,
			String attribute, com.hiperf.common.ui.shared.util.Id id, int page,
			int nbRows, ObjectsToPersist allObjectsToPersist, String sortAttribute, Boolean asc) throws PersistenceException {
		String cacheName = cacheNameByClass.get(wrappedClassName);
		if (cacheName != null) {
			return cachePH.getCollectionInverse(wrappedClassName, attribute, id, page, nbRows, allObjectsToPersist, sortAttribute, asc);
		}
		return defaultPH.getCollectionInverse(wrappedClassName, attribute, id, page, nbRows, allObjectsToPersist, sortAttribute, asc);
	}

	@Override
	public Long saveFilter(Filter f, String userName)
			throws PersistenceException {
		/*
		 * String cacheName = cacheNameByClass.get(Filter.class.getName());
		 * if(cacheName != null) { cachePH.saveFilter(f, userName); } else
		 */
		return defaultPH.saveFilter(f, userName);
	}

	@Override
	public NakedObjectsList sort(String className, String currentFilter,
			String attribute, boolean distinct, boolean asc, int page, int nbRows,
			ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			return cachePH.loadAll(cacheName, className, currentFilter, page,
					nbRows, attribute, distinct, asc);
		}
		return defaultPH.sort(className, currentFilter, attribute, distinct, asc, page,
				nbRows, toPersist, locale);
	}

	@Override
	public void getExtractedData(HttpServletRequest req,
			HttpServletResponse resp, String className) throws ServletException {
		String cacheName = cacheNameByClass.get(className);
		if (cacheName != null) {
			cachePH.getExtractedData(req, resp, cacheName, className);
		} else
			defaultPH.getExtractedData(req, resp, className);
	}

	@Override
	public EntityManager createEntityManager() {
		return defaultPH.getEntityManager();
	}

	@Override
	public TransactionContext createTransactionalContext() {
		return defaultPH.createTransactionalContext();
	}

	@Override
	public void deproxyNakedObject(INakedObject no, EntityManager em)
			throws PersistenceException {
		defaultPH
				.deproxyNakedObject(
						no,
						em,
						new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>>());
	}

	@Override
	public void saveConfiguration(String viewName, String className, int nbRows,
			List<HeaderInfo> headers, ScreenLabels labels, String connectedUser, LanguageEnum language) throws PersistenceException {
		defaultPH.saveConfiguration(viewName, className, nbRows, headers, labels,
				connectedUser, language);
	}

	@Override
	public Map<String, TableConfig> getScreenConfigurations(String connectedUser, LanguageEnum language)
			throws PersistenceException {

		return defaultPH.getScreenConfigurations(connectedUser, language);
	}

	@Override
	public void downloadFile(HttpServletResponse resp, String fileClass,
			String fileNameField, String fileStorageField, String fileId)
			throws PersistenceException {
		defaultPH.downloadFile(resp, fileClass, fileNameField,
				fileStorageField, fileId);

	}

	@Override
	public Object saveFile(String fileClass, String fileNameField,
			String fileStorageField, String fileName, FileItem item)
			throws PersistenceException {
		return defaultPH.saveFile(fileClass, fileNameField, fileStorageField,
				fileName, item);

	}

	@Override
	public String replaceFile(String fileClass, String fileNameField,
			String fileStorageField, String fileName, FileItem fileItem,
			String existingId) throws PersistenceException {
		return defaultPH.replaceFile(fileClass, fileNameField, fileStorageField,
				fileName, fileItem, existingId);

	}

	@Override
	public String getFileName(String fileClass, String fileNameField, String id)
			throws PersistenceException {
		return defaultPH.getFileName(fileClass, fileNameField,
				defaultPH.getFileId(id, fileClass));
	}

	@Override
	public String checkExists(String className, String attribute, String value,
			Locale locale) throws PersistenceException {
		String cacheName = (String) this.cacheNameByClass.get(className);
		if (cacheName != null) {
			return this.cachePH
					.checkExists(className, attribute, value, locale);
		}
		return this.defaultPH.checkExists(className, attribute, value, locale);
	}

	@Override
	public String checkExists(String className,
			com.hiperf.common.ui.shared.util.Id id, String attribute, String value,
			Locale locale) throws PersistenceException {
		String cacheName = (String) this.cacheNameByClass.get(className);
		if (cacheName != null) {
			return this.cachePH.checkExists(className, id, attribute, value,
					locale);
		}
		return this.defaultPH.checkExists(className, id, attribute, value,
				locale);
	}

	public static boolean isNumber(String attribute, PropertyDescriptor[] pds) {
		for (PropertyDescriptor pd : pds) {
			if (pd.getName().equals(attribute)) {
				Class<?> propertyType = pd.getPropertyType();
				if (propertyType.isPrimitive()
						|| Number.class.isAssignableFrom(propertyType)) {
					return true;
				}
				break;
			}
		}
		return false;
	}

	@Override
	public void processDownload(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		String fileClass = req.getParameter(IConstants.FILE_CLASS);
		String fileNameField = req.getParameter(IConstants.FILE_NAME);
		String fileStorageField = req
				.getParameter(IConstants.FILE_STORAGE_FIELD);
		String fileId = req.getParameter(IConstants.ID);
		if (fileClass != null && fileNameField != null
				&& fileStorageField != null && fileId != null) {
			try {
				downloadFile(resp, fileClass, fileNameField, fileStorageField,
						fileId);
			} catch (PersistenceException e) {
				throw new ServletException("Problem while downloading file", e);
			}
		}
	}

	@Override
	public void doPersist(
			ObjectsToPersist toPersist,
			Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res,
			Map<Object, IdHolder> newIdByOldId, EntityManager em, Locale locale)
			throws ClassNotFoundException, IntrospectionException,
			PersistenceException, IllegalAccessException,
			InvocationTargetException, InstantiationException {
		this.defaultPH.doPersist(toPersist, res, newIdByOldId, em, locale);
	}

	@Override
	public CollectionInfo getLazyCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attribute) throws PersistenceException {
		String cacheName = (String) this.cacheNameByClass.get(className);
		if (cacheName != null) {
			return this.cachePH.getLazyCollection(className, id, attribute);
		}
		return this.defaultPH.getLazyCollection(className, id, attribute);
	}


	@Override
	public Collection<INakedObject> getByAttribute(String className, String att, Object value, EntityManager em)
			throws PersistenceException {
		String cacheName = (String) this.cacheNameByClass.get(className);
		if (cacheName != null)
			return this.cachePH.getByAttribute(className, att, value);
		return this.defaultPH.getByAttribute(className, att, value, em);
	}

	@Override
	public void shutdown() {
		if(defaultPH != null)
			defaultPH.shutdown();
	}

	@Override
	public void close(TransactionContext tc) {
		if(defaultPH != null) {
			defaultPH.close(tc);
		} else
			tc.close();
	}

	@Override
	public void close(EntityManager em) {
		defaultPH.close(em);
	}

	@Override
	public List<INakedObject> getAll(String rootClassName, String jpqlQuery) throws PersistenceException {
		String cacheName = (String) this.cacheNameByClass.get(rootClassName);
		if (cacheName != null)
			return this.cachePH.getAll(jpqlQuery);
		return this.defaultPH.getAll(jpqlQuery);
	}

	@Override
	public void setTestPersistenceHelper(IPersistenceHelper ph) {
		defaultPH = ph;
	}


}
