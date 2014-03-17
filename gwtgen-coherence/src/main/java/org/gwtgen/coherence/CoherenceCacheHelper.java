package org.gwtgen.coherence;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.UIManyToMany;
import org.gwtgen.api.shared.UIManyToOne;
import org.gwtgen.coherence.sequence.UuidGenerator;

import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.server.UTF8Control;
import com.hiperf.common.ui.server.storage.ICoherenceCacheHelper;
import com.hiperf.common.ui.server.storage.impl.StorageHelper;
import com.hiperf.common.ui.server.storage.impl.StorageService;
import com.hiperf.common.ui.server.util.ExcelHelper;
import com.hiperf.common.ui.server.util.NakedObjectComparator;
import com.hiperf.common.ui.server.util.sequence.IdGenerator;
import com.hiperf.common.ui.shared.IConstants;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.NakedObjectsList;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.QueryHelper;
import com.tangosol.util.extractor.ReflectionUpdater;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.processor.UpdaterProcessor;

public class CoherenceCacheHelper implements ICoherenceCacheHelper{

	private static final Logger logger = Logger
			.getLogger(CoherenceCacheHelper.class.getName());

	private static final String DEFAULT_SEQUENCES_CACHE = "Sequences";

	private String sequencesCacheName;
	private String sequenceClassName;
	private Map<String, IdGenerator> sequenceGeneratorByClass = new HashMap<String, IdGenerator>();
	private Map<String, String> cacheNameByClass = null;
	private Map<String, PropertyDescriptor[]> propertyDescriptorsByClassName = new HashMap<String, PropertyDescriptor[]>();
	private Map<String, Method> getIdByClass = new HashMap<String, Method>();
	private Map<String, Method> setIdByClass = new HashMap<String, Method>();
	private Map<String, List<Field>> collectionsByClass = new HashMap<String, List<Field>>();
	private Map<String, List<Field>> mapsByClass = new HashMap<String, List<Field>>();
	private Map<String, Map<Field, OneToMany>> oneToManiesByClass = new HashMap<String, Map<Field, OneToMany>>();
	private Map<String, Map<Field, ManyToMany>> manyToManiesByClass = new HashMap<String, Map<Field, ManyToMany>>();
	private Map<String, Map<Field, UIManyToMany>> uiManyToManiesByClass = new HashMap<String, Map<Field, UIManyToMany>>();
	private Map<String, Map<Field, UIManyToOne>> manyToOnesByClass = new HashMap<String, Map<Field, UIManyToOne>>();

	public CoherenceCacheHelper() {
		sequencesCacheName = DEFAULT_SEQUENCES_CACHE;
		sequenceClassName = null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.hiperf.common.ui.server.util.IStorageService#get(java.lang.String,
	 * java.lang.Object)
	 */
	public INakedObject get(String cacheName, Id id)
			throws PersistenceException {
		NamedCache cache = CacheFactory.getCache(cacheName);
		return (INakedObject) cache.get(id.getFieldValues().get(0));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.hiperf.common.ui.server.util.IStorageService#getAll(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public Map<String, String> getAll(String cacheName, String rootClassName, String filter, String attPrefix, String childClassName, String childAttribute) throws PersistenceException {
		Map<String, String> map = new HashMap<String, String>();
		NamedCache cache = CacheFactory.getCache(cacheName);
		Method m = null;
		for (PropertyDescriptor pd : propertyDescriptorsByClassName
				.get(childClassName)) {
			if (childAttribute.equals(pd.getName())) {
				m = pd.getReadMethod();
				break;
			}
		}
		if (m != null) {
			try {
				for (Iterator iter = cache.entrySet(null).iterator(); iter
						.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					Object value = entry.getValue();
					Object attVal = m.invoke(value, StorageService.emptyArg);
					StorageHelper.fillGetAllMap(map, attVal);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception in getAll", e);
				throw new PersistenceException(e);
			}
		} else {
			throw new PersistenceException("Getter of " + childAttribute
					+ " on class " + rootClassName + " does not exist !!!");
		}
		return map;
	}

	public NakedObjectsList loadAll(String cacheName, String className,
			String currentFilter, int page, int rowsPerPage,
			String sortAttribute, boolean asc) throws PersistenceException {
		return loadAll(cacheName, className, currentFilter, page, rowsPerPage,
				sortAttribute, false, asc);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.hiperf.common.ui.server.util.IStorageService#loadAll(java.lang.String
	 * , java.lang.String, int, int, java.lang.String, boolean)
	 */
	public NakedObjectsList loadAll(String cacheName, String className,
			String currentFilter, int page, int rowsPerPage,
			String sortAttribute, boolean distinct, boolean asc)
			throws PersistenceException {
		if (currentFilter != null && currentFilter.contains(IConstants.DT_SEP))
			currentFilter = currentFilter.replaceAll(IConstants.DT_SEP, "'");
		NamedCache cache = CacheFactory.getCache(cacheName);
		List<INakedObject> list = new ArrayList<INakedObject>();
		com.tangosol.util.Filter curFilter = null;
		if (currentFilter != null && currentFilter.length() > 0
				&& !currentFilter.startsWith("left join")) {
			currentFilter = currentFilter.replaceAll("o[.]", "");
			curFilter = QueryHelper.createFilter(currentFilter);
		}
		if (sortAttribute == null) {
			Set allKeys = cache.keySet(curFilter);
			ArrayList keys = new ArrayList(allKeys);
			Collections.sort(keys);
			List filteredKeys = new ArrayList(rowsPerPage);
			for (int i = ((page - 1) * rowsPerPage); i < page * rowsPerPage
					&& i < keys.size(); i++) {
				filteredKeys.add(keys.get(i));
			}
			list.addAll((Collection<? extends INakedObject>) (cache
					.getAll(filteredKeys).values()));
		} else {
			Set entrySet = null;
			for (PropertyDescriptor pd : propertyDescriptorsByClassName
					.get(className)) {
				if (sortAttribute.equals(pd.getName())) {
					entrySet = cache.entrySet(curFilter,
							new NakedObjectComparator(pd.getReadMethod(), asc));
					break;
				}
			}
			int i = 0;
			for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				if (i >= page * rowsPerPage)
					break;
				if (i >= ((page - 1) * rowsPerPage))
					list.add((INakedObject) entry.getValue());
				i++;
			}
		}
		deproxyAndFetchCollections(className, list);
		fetchLinkedObjects(cacheName, className, list);
		int nbPages;
		int size;
		if (currentFilter == null)
			size = cache.size();
		else
			size = list.size();
		return new NakedObjectsList(list, size, rowsPerPage, currentFilter);
	}

	private void fetchLinkedObjects(String cacheName, String className,
			List<INakedObject> list) throws PersistenceException {
		Map<Field, UIManyToOne> map = manyToOnesByClass.get(className);
		if (map != null) {
			for (Field f : map.keySet()) {
				UIManyToOne uiManyToOne = map.get(f);
				String mappedBy = uiManyToOne.mappedBy();
				if (mappedBy.startsWith("this."))
					mappedBy = mappedBy.substring(5);
				boolean acc = false;
				Field field = null;
				boolean accessible = f.isAccessible();
				try {
					f.setAccessible(true);
					Class c = Class.forName(className);
					field = c.getDeclaredField(mappedBy);
					acc = field.isAccessible();
					String linkedCacheName = cacheNameByClass.get(f.getType()
							.getName());
					if (linkedCacheName != null) {
						NamedCache linkedCache = CacheFactory
								.getCache(linkedCacheName);
						try {
							field.setAccessible(true);
							Map<Object, List<INakedObject>> objByFk = new HashMap<Object, List<INakedObject>>();
							for (INakedObject o : list) {

								Object objId = field.get(o);
								if (objId != null) {
									List<INakedObject> myList = objByFk
											.get(objId);
									if (myList == null) {
										myList = new ArrayList<INakedObject>();
										objByFk.put(objId, myList);
									}
									myList.add(o);
								}
								// f.set(o, (INakedObject)
								// linkedCache.get(objId));

							}
							if (!objByFk.isEmpty()) {
								Map allFKs = linkedCache.getAll(objByFk
										.keySet());
								for (Object fkId : allFKs.keySet()) {
									List<INakedObject> noList = objByFk
											.get(fkId);
									for (INakedObject no : noList) {
										f.set(no, allFKs.get(fkId));
									}
								}
							}
						} catch (Exception e) {
							throw new PersistenceException(e);
						} finally {
							if (!acc && field != null)
								field.setAccessible(false);
						}
					} else {
						logger.warning("No cache defined for " + f.getType());
					}
				} catch (Exception e) {
					throw new PersistenceException(e);
				} finally {
					if (!accessible)
						f.setAccessible(false);
				}
			}

		}

	}

	private void deproxyAndFetchCollections(String className,
			List<INakedObject> list) throws PersistenceException {
		List<Field> colls = collectionsByClass.get(className);
		if (colls != null) {
			for (Field f : colls) {
				deproxyCollectionField(list, f);
			}
		}
		List<Field> maps = mapsByClass.get(className);
		if (maps != null) {
			for (Field f : maps) {
				deproxyMapField(list, f);
			}
		}
		Map<Field, OneToMany> oneToManies = oneToManiesByClass.get(className);
		if (oneToManies != null) {
			for (Field f : oneToManies.keySet()) {
				OneToMany oneToMany = oneToManies.get(f);
				String cacheName = cacheNameByClass.get(oneToMany
						.targetEntity().getName());
				String mappedBy = oneToMany.mappedBy();
				setLinkedObjects(list, colls, f, cacheName, mappedBy);
			}
		}
		Map<Field, ManyToMany> manyToManies = manyToManiesByClass
				.get(className);
		if (manyToManies != null) {
			for (Field f : manyToManies.keySet()) {
				ManyToMany manyToMany = manyToManies.get(f);
				String cacheName = cacheNameByClass.get(manyToMany
						.targetEntity().getName());
				String mappedBy = manyToMany.mappedBy();
				setLinkedObjects(list, colls, f, cacheName, mappedBy);
			}
		}
		Map<Field, UIManyToMany> uiManyToManies = uiManyToManiesByClass
				.get(className);
		if (uiManyToManies != null) {
			for (Field f : uiManyToManies.keySet()) {
				UIManyToMany manyToMany = uiManyToManies.get(f);
				setLinkedObjects(className, list, f, manyToMany);
			}
		}
	}

	private void deproxyMapField(List<INakedObject> list, Field f)
			throws PersistenceException {
		boolean accessible = f.isAccessible();
		try {
			if (!accessible)
				f.setAccessible(true);
			ParameterizedType genericType = (ParameterizedType) f
					.getGenericType();
			if (genericType != null) {
				int i = 0;
				for (Type t : genericType.getActualTypeArguments()) {
					if (t instanceof ParameterizedType) {
						Class rawType = (Class) ((ParameterizedType) t)
								.getRawType();

						// Class<? extends Type> clazz = rawType.getClass();
						if (Collection.class.isAssignableFrom(rawType)) {
							deproxyMapField(list, f, rawType, i);
						}
					}
					i++;
				}
			}
			/*
			 * if(List.class.isAssignableFrom(f.getType())) { for(INakedObject
			 * no : list) { Collection originalColl = (Collection) f.get(no);
			 * if(originalColl != null &&
			 * !ArrayList.class.getName().equals(originalColl
			 * .getClass().getName())) { List myList = new
			 * ArrayList(originalColl.size()); for(Object o : originalColl) {
			 * myList.add(o); } f.set(no, myList); } } } else
			 * if(Set.class.isAssignableFrom(f.getType())) { for(INakedObject no
			 * : list) { Collection originalColl = (Collection) f.get(no);
			 * if(originalColl != null&&
			 * !HashSet.class.getName().equals(originalColl
			 * .getClass().getName())) { Set myList = new
			 * HashSet(originalColl.size()); for(Object o : originalColl) {
			 * myList.add(o); } f.set(no, myList); } } } else { throw new
			 * Exception("Type "+f.getType()+" is not supported"); }
			 */

		} catch (Exception e) {
			throw new PersistenceException(e);
		} finally {
			if (!accessible)
				f.setAccessible(false);
		}
	}

	private void deproxyMapField(List<INakedObject> list, Field f, Class clazz,
			int i) throws Exception {
		for (INakedObject o : list) {
			Map map = (Map) f.get(o);
			if (map != null && !map.isEmpty()) {
				if (i == 0) {
					for (Object key : map.keySet()) {
						Collection c = (Collection) key;
						if (c != null && !c.isEmpty()) {
							if (List.class.isAssignableFrom(clazz)) {
								if (c != null
										&& !ArrayList.class.getName().equals(
												c.getClass().getName())) {
									List myList = new ArrayList(c.size());
									for (Object obj : c) {
										myList.add(obj);
									}
									map.put(c, map.remove(key));
								}
							} else if (Set.class.isAssignableFrom(f.getType())) {
								if (c != null
										&& !HashSet.class.getName().equals(
												c.getClass().getName())) {
									Set myList = new HashSet(c.size());
									for (Object obj : c) {
										myList.add(obj);
									}
									map.put(c, map.remove(key));
								}
							} else {
								throw new Exception("Type " + f.getType()
										+ " is not supported");
							}
						}
					}
				} else {
					for (Object key : map.keySet()) {
						Collection c = (Collection) map.get(key);
						if (c != null && !c.isEmpty()) {
							if (List.class.isAssignableFrom(clazz)) {
								if (c != null
										&& !ArrayList.class.getName().equals(
												c.getClass().getName())) {
									List myList = new ArrayList(c.size());
									for (Object obj : c) {
										myList.add(obj);
									}
									map.put(key, myList);
								}
							} else if (Set.class.isAssignableFrom(f.getType())) {
								if (c != null
										&& !HashSet.class.getName().equals(
												c.getClass().getName())) {
									Set myList = new HashSet(c.size());
									for (Object obj : c) {
										myList.add(obj);
									}
									map.put(key, myList);
								}
							} else {
								throw new Exception("Type " + f.getType()
										+ " is not supported");
							}
						}
					}
				}
			}
		}
	}

	private void deproxyCollectionField(List<INakedObject> list, Field f)
			throws PersistenceException {
		boolean accessible = f.isAccessible();
		try {
			if (!accessible)
				f.setAccessible(true);
			if (List.class.isAssignableFrom(f.getType())) {
				for (INakedObject no : list) {
					Collection originalColl = (Collection) f.get(no);
					if (originalColl != null
							&& !ArrayList.class.getName().equals(
									originalColl.getClass().getName())) {
						List myList = new ArrayList(originalColl.size());
						for (Object o : originalColl) {
							myList.add(o);
						}
						f.set(no, myList);
					}
				}
			} else if (Set.class.isAssignableFrom(f.getType())) {
				for (INakedObject no : list) {
					Collection originalColl = (Collection) f.get(no);
					if (originalColl != null
							&& !HashSet.class.getName().equals(
									originalColl.getClass().getName())) {
						Set myList = new HashSet(originalColl.size());
						for (Object o : originalColl) {
							myList.add(o);
						}
						f.set(no, myList);
					}
				}
			} else {
				throw new Exception("Type " + f.getType() + " is not supported");
			}

		} catch (Exception e) {
			throw new PersistenceException(e);
		} finally {
			if (!accessible)
				f.setAccessible(false);
		}
	}

	private void setLinkedObjects(String className, List<INakedObject> list,
			Field f, UIManyToMany manyToMany) throws PersistenceException {
		Class<? extends INakedObject> returnClass = manyToMany.returnClass();
		Class<? extends INakedObject> targetJoinClass = manyToMany
				.targetJoinClass();
		String returnClassName = cacheNameByClass.get(returnClass.getName());
		String joinCacheName = cacheNameByClass.get(targetJoinClass.getName());
		NamedCache joinCache = CacheFactory.getCache(joinCacheName);
		NamedCache returnCache = CacheFactory.getCache(returnClassName);
		String targetJoinField = manyToMany.targetJoinField();
		String targetIdField = manyToMany.targetIdField();
		String joinField = manyToMany.joinField();
		PropertyDescriptor joinFieldPd = null;
		PropertyDescriptor targetJoinFieldPd = null;
		PropertyDescriptor targetIdFieldPd = null;
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(Class
					.forName(className));
			for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
				if (pd.getName().equals(joinField)) {
					joinFieldPd = pd;
					break;
				}
			}
			beanInfo = Introspector.getBeanInfo(targetJoinClass);
			for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
				if (pd.getName().equals(targetJoinField)) {
					targetJoinFieldPd = pd;
				} else if (pd.getName().equals(targetIdField)) {
					targetIdFieldPd = pd;
				}
			}
		} catch (Exception e1) {
			throw new PersistenceException(e1);
		}
		if (joinFieldPd != null && targetJoinFieldPd != null
				&& targetIdFieldPd != null) {
			Filter filter;
			boolean accessible = f.isAccessible();
			try {
				f.setAccessible(true);
				for (INakedObject no : list) {
					filter = new EqualsFilter(targetJoinFieldPd.getReadMethod()
							.getName(), joinFieldPd.getReadMethod().invoke(no,
							StorageService.emptyArg));
					Set ids = new HashSet();
					for (Iterator iter = joinCache.entrySet(filter).iterator(); iter
							.hasNext();) {
						Map.Entry entry = (Map.Entry) iter.next();
						Object linkedObject = entry.getValue();
						ids.add(targetIdFieldPd.getReadMethod().invoke(
								linkedObject, StorageService.emptyArg));
					}
					if (!ids.isEmpty()) {
						Collection values = returnCache.getAll(ids).values();
						if (values != null && !values.isEmpty()) {
							if (Set.class.isAssignableFrom(f.getType())) {
								f.set(no, new HashSet(values));
							} else {
								f.set(no, new ArrayList(values));
							}
						}

					}
				}
			} catch (Exception e) {
				throw new PersistenceException(e);
			} finally {
				if (!accessible)
					f.setAccessible(false);
			}
		}
	}

	private void setLinkedObjects(List<INakedObject> list, List<Field> colls,
			Field f, String cacheName, String mappedBy)
			throws PersistenceException {
		if (cacheName != null && mappedBy.startsWith("this.")) {
			NamedCache cache = CacheFactory.getCache(cacheName);
			mappedBy = mappedBy.substring(5);
			boolean accessible = f.isAccessible();
			try {
				f.setAccessible(true);
				for (Field id : colls) {
					if (id.getName().equals(mappedBy)) {
						for (INakedObject no : list) {
							boolean idAcc = id.isAccessible();
							try {
								id.setAccessible(true);
								Collection coll = (Collection) id.get(no);
								if (coll != null && !coll.isEmpty()) {
									if (List.class
											.isAssignableFrom(f.getType())) {
										Collection values = cache.getAll(coll)
												.values();
										List myList = new ArrayList(
												values.size());
										for (Object obj : values) {
											myList.add(obj);
										}
										f.set(no, myList);
									} else if (Set.class.isAssignableFrom(f
											.getType())) {
										Collection values = cache.getAll(coll)
												.values();
										Set myList = new HashSet(values.size());
										for (Object obj : values) {
											myList.add(obj);
										}
										f.set(no, myList);
									} else {
										throw new Exception("Type "
												+ f.getType()
												+ " is not supported");
									}
								}
							} finally {
								if (!idAcc)
									id.setAccessible(false);
							}
						}
					}
				}
			} catch (Exception e) {
				throw new PersistenceException(e);
			} finally {
				if (!accessible)
					f.setAccessible(false);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.hiperf.common.ui.server.util.IStorageService#persist(java.lang.String
	 * , java.util.List, java.util.Map, java.util.Map, java.lang.String)
	 */
	public Map<Id, INakedObject> persist(List<INakedObject> toInsert,
			Map<String, Map<Id, Map<String, Serializable>>> toUpdate,
			Map<String, Set<Id>> toDelete,
			Map<String, Map<Id, Map<String, List<Id>>>> manyToManyAddedCache,
			Map<String, Map<Id, Map<String, List<Id>>>> manyToManyRemovedCache,
			String userName) throws PersistenceException {
		try {
			Map<Object, Object> newIdByOldId = new HashMap<Object, Object>();
			Map<Id, INakedObject> res = new HashMap<Id, INakedObject>();
			if (toInsert != null) {
				Map<String, Map<Object, INakedObject>> toInsertByCacheName = new HashMap<String, Map<Object, INakedObject>>();
				for (INakedObject o : toInsert) {
					String className = o.getClass().getName();
					String cacheName = cacheNameByClass.get(className);
					Map<Object, INakedObject> putAllMap = toInsertByCacheName
							.get(cacheName);
					if (putAllMap == null) {
						putAllMap = new HashMap<Object, INakedObject>();
						toInsertByCacheName.put(cacheName, putAllMap);
					}
					/*
					 * if(o instanceof IAuditable) { IAuditable aud =
					 * (IAuditable) o; aud.setCreateUser(userName);
					 * aud.setCreateDate(new Date()); }
					 */
					Method getId = getIdByClass.get(className);
					Object id = getId.invoke(o, StorageService.emptyArg);
					IdGenerator seqGen = sequenceGeneratorByClass
							.get(className);
					logger.info("seqGen = " + seqGen);
					if (seqGen != null) {
						Class<? extends INakedObject> clazz = o.getClass();
						Class<?> returnType;
						if (getId
								.isAnnotationPresent(javax.persistence.SequenceGenerator.class))
							returnType = getId.getReturnType();
						else
							returnType = clazz.getMethod(getId.getName(),
									new Class[0]).getReturnType();
						Method setId = clazz.getMethod("s"
								+ getId.getName().substring(1),
								new Class[] { returnType });
						if (setId != null) {
							Object newId = seqGen.generateIdentity();
							newIdByOldId.put(id, newId);
							setId.invoke(o, newId);
							putAllMap.put(newId, o);
						} else {
							throw new PersistenceException(
									"Cannot persist data : no setter for id !");
						}
					} else if (id != null) {
						putAllMap.put(id, o);
					} else {
						throw new PersistenceException(
								"Cannot persist data : id is null !");
					}
					List<Object> idVals = new ArrayList<Object>(1);
					idVals.add(id);
					res.put(new Id(idVals), o);
				}

				for (String cacheName : toInsertByCacheName.keySet()) {
					NamedCache cache = CacheFactory.getCache(cacheName);
					Map<Object, INakedObject> map2 = toInsertByCacheName
							.get(cacheName);
					cache.putAll(map2);
				}
			}
			if (toUpdate != null) {
				for (INakedObject o : toInsert) {
					replaceLinkedIds(newIdByOldId, o);
				}
				for (String className : toUpdate.keySet()) {
					String cacheName = cacheNameByClass.get(className);
					NamedCache cache = CacheFactory.getCache(cacheName);
					Map<Id, Map<String, Serializable>> map = toUpdate
							.get(className);

					for (Id id : map.keySet()) {
						Map<String, Serializable> modifs = map.get(id);
						for (String attName : modifs.keySet()) {
							boolean isMTO = false;
							Map<Field, UIManyToOne> manyToOnes = manyToOnesByClass
									.get(className);
							if (manyToOnes != null && !manyToOnes.isEmpty()) {
								for (Field f : manyToOnes.keySet()) {
									if (f.getName().equals(attName)) {
										isMTO = true;
										break;
									}
								}
							}
							ReflectionUpdater upd = null;
							for (PropertyDescriptor pd : propertyDescriptorsByClassName
									.get(className)) {
								if (attName.equals(pd.getName())) {
									upd = new ReflectionUpdater(pd
											.getWriteMethod().getName());
									break;
								}
							}
							if (upd == null) {
								throw new PersistenceException(
										"No setter found for " + attName);
							}
							UpdaterProcessor proc;
							Object value = modifs.get(attName);
							if (value != null && isMTO
									&& newIdByOldId.containsKey(value)) {
								proc = new UpdaterProcessor(upd,
										newIdByOldId.get(value));
							} else {
								proc = new UpdaterProcessor(upd, value);
							}
							cache.invoke(id.getFieldValues().get(0), proc);
						}
						res.put(id, (INakedObject) cache.get(id
								.getFieldValues().get(0)));
					}
				}
			}
			if (toDelete != null) {
				for (String className : toDelete.keySet()) {
					String cacheName = cacheNameByClass.get(className);
					NamedCache cache = CacheFactory.getCache(cacheName);
					Set<Id> set = toDelete.get(className);
					for (Id id : set) {
						cache.remove(id.getFieldValues().get(0));
					}
				}
			}
			processManyToMany(manyToManyAddedCache, true);
			processManyToMany(manyToManyRemovedCache, false);
			return res;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new PersistenceException(e);
		}
	}

	private void processManyToMany(
			Map<String, Map<Id, Map<String, List<Id>>>> mm, boolean toAdd)
			throws PersistenceException {
		if (mm != null) {
			for (Entry<String, Map<Id, Map<String, List<Id>>>> e : mm
					.entrySet()) {
				String className = e.getKey();
				Map<Id, Map<String, List<Id>>> addedMap = e.getValue();
				if (uiManyToManiesByClass.containsKey(className)) {
					Map<Field, UIManyToMany> map = uiManyToManiesByClass
							.get(className);
					for (Entry<Field, UIManyToMany> ee : map.entrySet()) {
						Field f = ee.getKey();
						UIManyToMany ann = ee.getValue();
						Class<? extends INakedObject> targetJoinClass = ann
								.targetJoinClass();
						NamedCache joinCache = CacheFactory
								.getCache(cacheNameByClass.get(targetJoinClass
										.getName()));
						boolean acc1 = false;
						boolean acc2 = false;
						Field field1 = null;
						Field field2 = null;
						try {
							field1 = targetJoinClass.getDeclaredField(ann
									.targetJoinField());
							field2 = targetJoinClass.getDeclaredField(ann
									.targetIdField());
							acc1 = field1.isAccessible();
							acc2 = field2.isAccessible();
							field1.setAccessible(true);
							field2.setAccessible(true);
							for (Entry<Id, Map<String, List<Id>>> eA : addedMap
									.entrySet()) {
								Id id1 = eA.getKey();
								List<Id> list = eA.getValue().get(f.getName());
								if (list != null) {
									for (Id id2 : list) {
										INakedObject o = targetJoinClass
												.newInstance();
										field1.set(o,
												id1.getFieldValues().get(0));
										field2.set(o,
												id2.getFieldValues().get(0));
										if (toAdd)
											joinCache
													.put(getIdByClass
															.get(targetJoinClass
																	.getName())
															.invoke(o,
																	StorageService.emptyArg),
															o);
										else
											joinCache
													.remove(getIdByClass
															.get(targetJoinClass
																	.getName())
															.invoke(o,
																	StorageService.emptyArg));
									}
								}
							}

						} catch (Exception ex) {
							throw new PersistenceException(ex);
						} finally {
							if (!acc1 && field1 != null)
								field1.setAccessible(false);
							if (!acc2 && field2 != null)
								field2.setAccessible(false);
						}
					}
				}
			}
		}
	}

	private void replaceLinkedIds(Map<Object, Object> newIdByOldId,
			INakedObject o) throws PersistenceException {
		Map<Field, UIManyToOne> manyToOnes = manyToOnesByClass.get(o.getClass()
				.getName());
		if (manyToOnes != null && !manyToOnes.isEmpty()) {
			for (Field f : manyToOnes.keySet()) {
				UIManyToOne uiManyToOne = manyToOnes.get(f);
				String mappedBy = uiManyToOne.mappedBy();
				if (mappedBy.startsWith("this."))
					mappedBy = mappedBy.substring(5);
				boolean acc = false;
				Field field = null;
				try {
					Class c = o.getClass();
					field = c.getDeclaredField(mappedBy);
					acc = field.isAccessible();
					field.setAccessible(true);
					Object objId = field.get(o);
					if (objId != null && newIdByOldId.containsKey(objId)) {
						field.set(o, newIdByOldId.get(objId));
					}
				} catch (Exception e) {
					throw new PersistenceException(e);
				} finally {
					if (!acc && field != null)
						field.setAccessible(false);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.hiperf.common.ui.server.util.IStorageService#reload(java.lang.String,
	 * java.util.List)
	 */
	public List<INakedObject> reload(String cacheName, List<List<Object>> idList)
			throws PersistenceException {
		NamedCache cache = CacheFactory.getCache(cacheName);
		List<INakedObject> l = new ArrayList<INakedObject>();
		List<Object> ids = new ArrayList<Object>(idList.size());
		for (List myId : idList) {
			ids.add(myId.get(0));
		}
		Map all = cache.getAll(ids);
		for (Iterator ie = all.entrySet().iterator(); ie.hasNext();) {
			Map.Entry e = (Map.Entry) ie.next();
			l.add((INakedObject) e.getValue());
		}
		return l;
	}

	public Map<String, PropertyDescriptor[]> getPropertyDescriptorsByClassName() {
		return propertyDescriptorsByClassName;
	}

	public Map<String, Method> getGetIdByClass() {
		return getIdByClass;
	}

	public Map<String, Method> getSetIdByClass() {
		return setIdByClass;
	}

	public void setCacheNameByClass(Map<String, String> cacheNameByClass) {
		this.cacheNameByClass = cacheNameByClass;
	}

	public Collection<INakedObject> getCollection(String rootCacheName,
			String className, Id id, String attributeName)
			throws PersistenceException {
		List<Field> colls = collectionsByClass.get(className);
		Map<Field, OneToMany> oneToManies = oneToManiesByClass.get(className);
		if (oneToManies != null) {
			for (Field f : oneToManies.keySet()) {
				if (f.getName().equals(attributeName)) {
					NamedCache cache = CacheFactory.getCache(rootCacheName);
					INakedObject o = (INakedObject) cache.get(id
							.getFieldValues().get(0));
					OneToMany oneToMany = oneToManies.get(f);
					String cacheName = cacheNameByClass.get(oneToMany
							.targetEntity().getName());
					String mappedBy = oneToMany.mappedBy();
					return getCollection(colls, f, cache, o, cacheName,
							mappedBy);
				}
			}
		}
		Map<Field, ManyToMany> manyToManies = manyToManiesByClass
				.get(className);
		if (manyToManies != null) {
			for (Field f : manyToManies.keySet()) {
				if (f.getName().equals(attributeName)) {
					NamedCache cache = CacheFactory.getCache(rootCacheName);
					INakedObject o = (INakedObject) cache.get(id
							.getFieldValues().get(0));
					ManyToMany manyToMany = manyToManies.get(f);
					String cacheName = cacheNameByClass.get(manyToMany
							.targetEntity().getName());
					String mappedBy = manyToMany.mappedBy();
					return getCollection(colls, f, cache, o, cacheName,
							mappedBy);
				}
			}
		}
		return null;
	}

	private Collection<INakedObject> getCollection(List<Field> colls, Field f,
			NamedCache cache, INakedObject o, String cacheName, String mappedBy)
			throws PersistenceException {
		if (cacheName != null && mappedBy.startsWith("this.")) {
			mappedBy = mappedBy.substring(5);
			for (Field idField : colls) {
				if (idField.getName().equals(mappedBy)) {
					NamedCache linkedCache = CacheFactory.getCache(cacheName);
					boolean idAcc = idField.isAccessible();
					try {
						idField.setAccessible(true);
						Collection coll = (Collection) idField.get(o);
						if (coll != null && !coll.isEmpty())
							return cache.getAll(coll).values();
					} catch (Exception e) {
						throw new PersistenceException(e);
					} finally {
						if (!idAcc)
							f.setAccessible(false);
					}
				}
			}
		}
		throw new PersistenceException("Not yet supported");
	}

	private static Collection<INakedObject> fillLinkedObjects(
			List<Field> colls, Object o, String collCacheName, String mappedBy)
			throws PersistenceException {
		if (mappedBy.startsWith("this.")) {
			String idsFieldName = mappedBy.substring(5);
			for (Field ff : colls) {
				if (ff.getName().equals(idsFieldName)) {
					NamedCache collCache = CacheFactory.getCache(collCacheName);
					boolean acc = ff.isAccessible();
					try {
						if (!acc)
							ff.setAccessible(true);
						Collection coll = (Collection) ff.get(o);
						if (coll != null && !coll.isEmpty())
							return collCache.getAll(coll).values();
						return null;
					} catch (Exception e) {
						throw new PersistenceException(e);
					} finally {
						if (!acc)
							ff.setAccessible(false);
					}
				}
			}
		}
		return null;
	}

	public INakedObject getLinkedObject(String cacheName, String className,
			Id id, String attributeName) throws PersistenceException {
		Map<Field, UIManyToOne> map = manyToOnesByClass.get(className);
		if (map != null) {
			for (Field f : map.keySet()) {
				if (f.getName().equals(attributeName)) {
					UIManyToOne uiManyToOne = map.get(f);
					String mappedBy = uiManyToOne.mappedBy();
					if (mappedBy.startsWith("this."))
						mappedBy = mappedBy.substring(5);
					boolean acc = false;
					Field field = null;
					NamedCache cache = CacheFactory.getCache(cacheName);
					INakedObject o = (INakedObject) cache.get(id
							.getFieldValues().get(0));
					try {
						Class c = o.getClass();
						field = c.getDeclaredField(mappedBy);
						acc = field.isAccessible();
						field.setAccessible(true);
						String linkedCacheName = cacheNameByClass.get(field
								.getType());
						NamedCache linkedCache = CacheFactory
								.getCache(linkedCacheName);
						Object objId = field.get(o);
						if (objId != null)
							return (INakedObject) linkedCache.get(objId);
						return null;
					} catch (Exception e) {
						throw new PersistenceException(e);
					} finally {
						if (!acc && field != null)
							field.setAccessible(false);
					}
				}
			}
		}
		return null;
	}

	public NakedObjectsList getSortedCollection(String className, Id id,
			String attribute, String sortAttribute, boolean asc, int page,
			int nbRows) throws PersistenceException {
		throw new PersistenceException("Not yet implemented");
	}

	public NakedObjectsList getCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attribute, int page,
			int nbRows, ObjectsToPersist toPersist) throws PersistenceException {
		throw new PersistenceException("Not yet implemented");
	}

	public Map<String, List<Field>> getCollectionsByClass() {
		return collectionsByClass;
	}

	public void getExtractedData(HttpServletRequest req,
			HttpServletResponse resp, String cacheName, String className)
			throws ServletException {
		// col index / Attribute, label
		Map<Integer, Object[]> map = new HashMap<Integer, Object[]>();
		logger.fine("Class = " + className);
		try {
			ExcelHelper.fillObjectMap(req, className, map);
			List res = new ArrayList();
			NamedCache cache = CacheFactory.getCache(cacheName);
			for (Iterator iter = cache.entrySet(null).iterator(); iter
					.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				res.add(entry.getValue());
			}
			ExcelHelper.sendResponse(resp, className, map, res);
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"Exception while querying data for export", e);
			throw new ServletException(
					"Exception while querying data for export", e);
		}
	}

	public Map<String, Map<Field, OneToMany>> getOneToManiesByClass() {
		return oneToManiesByClass;
	}

	public Map<String, Map<Field, ManyToMany>> getManyToManiesByClass() {
		return manyToManiesByClass;
	}

	public Map<String, Map<Field, UIManyToOne>> getManyToOnesByClass() {
		return manyToOnesByClass;
	}

	public Map<String, List<Field>> getMapsByClass() {
		return mapsByClass;
	}

	public String getSequencesCacheName() {
		return sequencesCacheName;
	}

	public void setSequencesCacheName(String sequencesCacheName) {
		this.sequencesCacheName = sequencesCacheName;
	}

	public String getSequenceClassName() {
		return sequenceClassName;
	}

	public void setSequenceClassName(String sequenceClassName) {
		this.sequenceClassName = sequenceClassName;
	}

	public Map<String, IdGenerator> getSequenceGeneratorByClass() {
		return sequenceGeneratorByClass;
	}

	public Map<String, Map<Field, UIManyToMany>> getUiManyToManiesByClass() {
		return uiManyToManiesByClass;
	}

	public String checkExists(String className, String attribute, String value,
			Locale locale) {
		String cacheName = (String) this.cacheNameByClass.get(className);
		NamedCache cache = CacheFactory.getCache(cacheName);
		if (value != null) {
			PropertyDescriptor[] pds = (PropertyDescriptor[]) this.propertyDescriptorsByClassName
					.get(className);
			boolean isNumber = false;
			for (PropertyDescriptor pd : pds) {
				if (pd.getName().equals(attribute)) {
					Class propertyType = pd.getPropertyType();
					if ((!(propertyType.isPrimitive()))
							&& (!(Number.class.isAssignableFrom(propertyType))))
						break;
					isNumber = true;
					break;
				}

			}

			StringBuilder q = new StringBuilder(attribute + " = ");
			if (!(isNumber))
				q.append("'");
			q.append(value);
			if (!(isNumber))
				q.append("'");
			Filter filter = QueryHelper.createFilter(q.toString());
			if (cache.keySet(filter).size() > 0) {
				ResourceBundle ressource = ResourceBundle.getBundle(
						"com.hiperf.common.ui.server.util.ServerMessages",
						locale, new UTF8Control());
				return MessageFormat.format(
						ressource.getString("uniqueConstraintViolated").replaceAll("'", "''"),
						new Object[] { value, attribute });
			}

			return null;
		}

		Filter filter = QueryHelper.createFilter(attribute + " is null");
		if (cache.keySet(filter).size() > 0) {
			ResourceBundle ressource = ResourceBundle.getBundle(
					"com.hiperf.common.ui.server.util.ServerMessages", locale, new UTF8Control());
			return MessageFormat.format(
					ressource.getString("uniqueConstraintViolated").replaceAll("'", "''"),
					new Object[] { value, attribute });
		}

		return null;
	}

	public String checkExists(String className, Id id, String attribute,
			String value, Locale locale) {
		String cacheName = (String) this.cacheNameByClass.get(className);
		NamedCache cache = CacheFactory.getCache(cacheName);
		String att = (String) id.getFieldNames().get(0);
		PropertyDescriptor[] pds = (PropertyDescriptor[]) this.propertyDescriptorsByClassName
				.get(className);
		StringBuilder q = new StringBuilder(attribute);
		if (value != null) {
			boolean isNumber = StorageService.isNumber(attribute, pds);
			q.append(" = ");
			if (!(isNumber))
				q.append("'");
			q.append(value);
			if (!(isNumber))
				q.append("'");
			appendIdData(id, att, pds, q);
			Filter filter = QueryHelper.createFilter(q.toString());
			if (cache.keySet(filter).size() > 0) {
				ResourceBundle ressource = ResourceBundle.getBundle(
						"com.hiperf.common.ui.server.util.ServerMessages",
						locale, new UTF8Control());
				return MessageFormat.format(
						ressource.getString("uniqueConstraintViolated").replaceAll("'", "''"),
						new Object[] { value, attribute });
			}

			return null;
		}

		q.append(" is null");
		appendIdData(id, att, pds, q);
		Filter filter = QueryHelper.createFilter(q.toString());
		appendIdData(id, att, pds, q);
		if (cache.keySet(filter).size() > 0) {
			ResourceBundle ressource = ResourceBundle.getBundle(
					"com.hiperf.common.ui.server.util.ServerMessages", locale, new UTF8Control());
			return MessageFormat.format(
					ressource.getString("uniqueConstraintViolated").replaceAll("'", "''"),
					new Object[] { value, attribute });
		}

		return null;
	}

	private void appendIdData(Id id, String att, PropertyDescriptor[] pds,
			StringBuilder q) {
		q.append(" and ").append(att).append(" = ");
		if (StorageService.isNumber(att, pds)) {
			q.append(id.getFieldValues().get(0));
		} else {
			q.append(" '");
			q.append(id.getFieldValues().get(0));
			q.append("'");
		}
	}

	public NakedObjectsList getCollectionInverse(String wrappedClassName,
			String attribute, com.hiperf.common.ui.shared.util.Id id, int page,
			int nbRows, ObjectsToPersist toPersist, String sortAttribute,
			Boolean asc) throws PersistenceException {
		return getCollection(wrappedClassName, id, sortAttribute, page, nbRows,
				toPersist);
	}

	public CollectionInfo getLazyCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<INakedObject> getByAttribute(String className,
			String att, Object value) throws PersistenceException {
		String cacheName = (String) this.cacheNameByClass.get(className);
		NamedCache cache = CacheFactory.getCache(cacheName);
		PropertyDescriptor[] pds = (PropertyDescriptor[]) this.propertyDescriptorsByClassName
				.get(className);
		boolean isNumber = false;
		for (PropertyDescriptor pd : pds) {
			if (pd.getName().equals(att)) {
				Class propertyType = pd.getPropertyType();
				if ((!(propertyType.isPrimitive()))
						&& (!(Number.class.isAssignableFrom(propertyType))))
					break;
				isNumber = true;
				break;
			}

		}

		StringBuilder q = new StringBuilder(att + " = ");
		if (!(isNumber))
			q.append("'");
		q.append(value);
		if (!(isNumber))
			q.append("'");
		Filter filter = QueryHelper.createFilter(q.toString());
		Set allKeys = cache.keySet(filter);
		if (allKeys != null && !allKeys.isEmpty())
			return ((Collection<INakedObject>) (cache.getAll(allKeys)
					.values()));

		return null;
	}

	public List<INakedObject> getAll(String jpqlQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	

	public IdGenerator getSequenceGenerator(SequenceGenerator gene) {
		return new org.gwtgen.coherence.sequence.SequenceGenerator(
				getSequencesCacheName(),
				getSequenceClassName(), gene
						.name(), gene.allocationSize());
	}

	public IdGenerator getUuidGenerator() {
		return UuidGenerator.INSTANCE;
	}

}
