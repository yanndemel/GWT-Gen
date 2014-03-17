package com.hiperf.common.ui.server.storage.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FetchType;
import javax.persistence.FlushModeType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.gwtgen.api.shared.INakedObject;
import org.hibernate.exception.SQLGrammarException;

import com.hiperf.common.ui.client.IAuditable;
import com.hiperf.common.ui.client.ILazy;
import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.server.UTF8Control;
import com.hiperf.common.ui.server.listener.LicenseProvider;
import com.hiperf.common.ui.server.storage.AbstractPersistenceHelper;
import com.hiperf.common.ui.server.storage.IPersistenceHelper;
import com.hiperf.common.ui.server.tx.ITransaction;
import com.hiperf.common.ui.server.tx.JtaTransaction;
import com.hiperf.common.ui.server.tx.LocalTransaction;
import com.hiperf.common.ui.server.tx.TransactionContext;
import com.hiperf.common.ui.server.tx.TransactionException;
import com.hiperf.common.ui.server.util.ExcelHelper;
import com.hiperf.common.ui.server.util.IOUtils;
import com.hiperf.common.ui.server.util.IdHolder;
import com.hiperf.common.ui.server.util.LinkFileInfo;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.IConstants;
import com.hiperf.common.ui.shared.ILazyId;
import com.hiperf.common.ui.shared.LazyList;
import com.hiperf.common.ui.shared.LazySet;
import com.hiperf.common.ui.shared.NakedObjectHandler;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.model.Filter;
import com.hiperf.common.ui.shared.model.FilterValue;
import com.hiperf.common.ui.shared.model.Label;
import com.hiperf.common.ui.shared.model.LanguageEnum;
import com.hiperf.common.ui.shared.model.ScreenConfig;
import com.hiperf.common.ui.shared.model.ScreenHeaderInfo;
import com.hiperf.common.ui.shared.model.ScreenLabels;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.NakedObjectsList;
import com.hiperf.common.ui.shared.util.TableConfig;

public final class PersistenceHelper extends AbstractPersistenceHelper {

	private static final Logger logger = Logger.getLogger(PersistenceHelper.class.getName());

	private static final Object[] nullArg = new Object[1];

	static {
		nullArg[0] = null;
	}

	private TYPE type;
	private ITransaction tx = null;
	private EntityManagerFactory emf;
	private ThreadLocal<EntityManager> emByThread;

	


	private static final Map<String, String> tableByClassName = new HashMap<String, String>();
	private static final Map<String, PropertyDescriptor[]> propertyDescriptorsByClassName = new HashMap<String, PropertyDescriptor[]>();
	private static final Map<String, Set<PropertyDescriptor>> idsByClassName = new HashMap<String, Set<PropertyDescriptor>>();
	private static final Set<String> generatedIdClasses = new HashSet<String>();
	private static final Map<String, Set<PropertyDescriptor>> collectionsByClassName = new Hashtable<String, Set<PropertyDescriptor>>();
	private static final Map<String, Set<PropertyDescriptor>> lazysByClassName = new Hashtable<String, Set<PropertyDescriptor>>();
	private static final Map<String, Set<PropertyDescriptor>> eagerObjectsByClassName = new Hashtable<String, Set<PropertyDescriptor>>();
	private static final Map<String, Set<LinkFileInfo>> linkedFilesByClassName = new HashMap<String, Set<LinkFileInfo>>();
	private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

	private PersistenceHelper(TYPE t, String unitName) {
		startLicenseCheck();
		
		this.type = t;
		try {
			emf = Persistence.createEntityManagerFactory(unitName);
			emByThread = new ThreadLocal<EntityManager>();
		} catch (Exception ee) {
			logger.log(Level.SEVERE, "Exception while init Persistence.xml...", ee);
			throw new RuntimeException(ee);
		}
		if(this.type.equals(TYPE.JTA)) {
			try {
				Context c = new InitialContext();
				tx = new JtaTransaction((UserTransaction)c.lookup("java:comp/UserTransaction"));
			} catch (NamingException e) {
				logger.log(Level.SEVERE, "Exception while initializing PersistenceHelper in JTA...", e);
				type = TYPE.LOCAL;
			}
		}
		if(emf.getMetamodel().getEntities() != null && !emf.getMetamodel().getEntities().isEmpty()) {
			for(EntityType e : emf.getMetamodel().getEntities()) {
				try {
					initClassMapping(e.getBindableJavaType().getName());
				} catch (Exception e1) {
					logger.log(Level.SEVERE, "Exception", e1);
				}
			}
		}
		endLicenseCheck();
		
	}

	private void endLicenseCheck() {
		LicenseProvider lp = LicenseProvider.getInstance();
		if(lp != null)
			lp.endLicenseCheck();
	}

	private void startLicenseCheck() {
		LicenseProvider lp = LicenseProvider.getInstance();
		if(lp != null)
			lp.startLicenseCheck();
	}

	public static IPersistenceHelper createInstance(TYPE t, String unitName) {
		if(instance == null) {
			instance = new PersistenceHelper(t, unitName);
		}
		return instance;
	}

	@Override
	public TransactionContext createTransactionalContext() {
		EntityManager em = getEntityManager();
		switch(this.type) {
			case LOCAL:
				return new TransactionContext(new LocalTransaction(em.getTransaction()) , em);
			default:
				return new TransactionContext(tx , em);
		}
	}

	@Override
	public EntityManager getEntityManager() {
		EntityManager em = emByThread.get();

		if (em == null || !em.isOpen()) {
			em = emf.createEntityManager();
			em.setFlushMode(FlushModeType.COMMIT);
			emByThread.set(em);
		}
		return em;
	}


	@Override
	public List<INakedObject> deproxyEntities(String className, List<INakedObject> list, boolean root, Map<String, Map<Object, Object>> oldIdByNewId) throws PersistenceException {
		try {
			Set<PropertyDescriptor> idPds = idsByClassName.get(className);
			EntityManager em = getEntityManager();
			Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext = new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id,INakedObject>>();
			List<INakedObject> l2 = new ArrayList<INakedObject>(list.size());
			for(INakedObject no : list) {
				l2.add(deproxyNakedObject(root, collectionsByClassName.get(className), lazysByClassName.get(className), eagerObjectsByClassName.get(className), linkedFilesByClassName.get(className), no, idPds, oldIdByNewId, em, deproxyContext));
			}
			return l2;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in deproxyEntities", e);
			throw new PersistenceException("Exception in deproxyEntities...", e);
		}
	}

	@Override
	public INakedObject deproxyNakedObject(INakedObject no, EntityManager em, Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext) throws PersistenceException {
		if(no != null) {
			try {
				String className = getClassName(no);
				Set<PropertyDescriptor> idsPd = idsByClassName.get(className);
				return deproxyNakedObject(true, collectionsByClassName.get(className), lazysByClassName.get(className), eagerObjectsByClassName.get(className), linkedFilesByClassName.get(className), no, idsPd, null, em, deproxyContext);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception in deproxyNakedObject", e);
				throw new PersistenceException("Exception in deproxyNakedObject...", e);
			}
		}
		return null;
	}

	private INakedObject deproxyNakedObject(boolean root,
			Set<PropertyDescriptor> collections, Set<PropertyDescriptor> lazys,
			Set<PropertyDescriptor> eagers, Set<LinkFileInfo> linkedFiles,
			INakedObject no, Set<PropertyDescriptor> idPds, Map<String, Map<Object, Object>> oldIdByNewId,
			EntityManager em, Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext) throws IllegalAccessException,
			InvocationTargetException, InstantiationException,
			IntrospectionException, ClassNotFoundException, PersistenceException {
		String name = getClassName(no);
		if(isProxy(no, name))
			no = clone(no, name);
		Map<com.hiperf.common.ui.shared.util.Id, INakedObject> map = deproxyContext.get(name);
		if(map == null) {
			map = new HashMap<com.hiperf.common.ui.shared.util.Id, INakedObject>();
			deproxyContext.put(name, map);
		}
		com.hiperf.common.ui.shared.util.Id noId = getId(no, idsByClassName.get(name));
		if(map.containsKey(noId))
			return no;
		map.put(noId, no);
		
		if(root && linkedFiles != null && !linkedFiles.isEmpty()) {
			for(LinkFileInfo a : linkedFiles) {
				Object fileId = a.getLocalFileIdGetter().invoke(no, new Object[0]);
				if(fileId != null)
					a.getLocalFileNameSetter().invoke(no, getFileName(a.getFileClassName(), a.getFileNameField(), fileId, getEntityManager()));
			}
		}

		if(collections != null) {
			for(PropertyDescriptor pd : collections) {
				Method readMethod = pd.getReadMethod();
				Method writeMethod = pd.getWriteMethod();
				Object o = readMethod.invoke(no, new Object[0]);
				if(o != null) {
					if(root) {
						Collection orig = (Collection)o;
						boolean processed = false;
						Type type = readMethod.getGenericReturnType();
						Class classInCollection = null;
						if(type instanceof ParameterizedType) {
							classInCollection = (Class)((ParameterizedType)type).getActualTypeArguments()[0];
						}
						if(classInCollection != null) {
							if(Number.class.isAssignableFrom(classInCollection) || String.class.equals(classInCollection) ||
									Boolean.class.equals(classInCollection)) {
								Collection targetColl;
								int size = orig.size();
								if(List.class.isAssignableFrom(readMethod.getReturnType())) {
									targetColl = new ArrayList(size);
								} else {
									targetColl = new HashSet(size);
								}
								if(size > 0)
									targetColl.addAll(orig);
								writeMethod.invoke(no, targetColl);
								processed = true;
							}
						}
						if(!processed) {
							//deproxyCollection(no, readMethod, writeMethod, orig, oldIdByNewId, em, deproxyContext);
							com.hiperf.common.ui.shared.util.Id id = getId(no, idPds);
							CollectionInfo ci = null;
							if(!id.isLocal()) {
								String className = getClassName(no);
								String attName = pd.getName();
								ci = getCollectionInfo(em, id, className, attName);
							}
							if(List.class.isAssignableFrom(readMethod.getReturnType())) {
								if(ci != null)
									writeMethod.invoke(no, new LazyList<INakedObject>(ci.getSize(), ci.getDescription()));
								else
									writeMethod.invoke(no, new LazyList<INakedObject>());
							} else {
								if(ci != null)
									writeMethod.invoke(no, new LazySet<INakedObject>(ci.getSize(), ci.getDescription()));
								else
									writeMethod.invoke(no, new LazySet<INakedObject>());
							}
						}
					} else {
						if(List.class.isAssignableFrom(readMethod.getReturnType())) {
							writeMethod.invoke(no, new LazyList<INakedObject>());
						} else {
							writeMethod.invoke(no, new LazySet<INakedObject>());
						}

					}

				}
			}
		}
		if(lazys != null) {
			for(PropertyDescriptor pd : lazys) {
				Method readMethod = pd.getReadMethod();
				Object o = readMethod.invoke(no, new Object[0]);
				if(o != null) {
					Class<?> targetClass = pd.getPropertyType();
					if(root) {
						String targetClassName = targetClass.getName();
						if(isProxy(o, targetClassName)) {
							o = deproxyObject(targetClass, o);
						}
						Set<PropertyDescriptor> ids = idsByClassName.get(targetClassName);
						o = deproxyNakedObject(root, collectionsByClassName.get(targetClassName), lazysByClassName.get(targetClassName), eagerObjectsByClassName.get(targetClassName), linkedFilesByClassName.get(targetClassName), (INakedObject)o, ids, oldIdByNewId, em, deproxyContext);
						pd.getWriteMethod().invoke(no, o);
					} else {
						Object lazyObj = newLazyObject(targetClass);

						pd.getWriteMethod().invoke(no, lazyObj);
					}
				}
			}
		}
		if(eagers != null) {
			for(PropertyDescriptor pd : eagers) {
				String targetClassName = pd.getPropertyType().getName();
				Method readMethod = pd.getReadMethod();
				Object o = readMethod.invoke(no, new Object[0]);
				if(o != null) {
					Set<PropertyDescriptor> ids = idsByClassName.get(targetClassName);
					deproxyNakedObject(root, collectionsByClassName.get(targetClassName), lazysByClassName.get(targetClassName), eagerObjectsByClassName.get(targetClassName), linkedFilesByClassName.get(targetClassName), (INakedObject)o, ids, oldIdByNewId, em, deproxyContext);
				}
			}
		}



		if(oldIdByNewId != null && idPds != null) {
			Map<Object, Object> map2 = oldIdByNewId.get(no.getClass().getName());
			if(map2 != null && !map2.isEmpty()) {
				for(PropertyDescriptor pd : idPds) {
					Object id = pd.getReadMethod().invoke(no, StorageService.emptyArg);
					Object oldId = map2.get(id);
					if(oldId != null) {
						pd.getWriteMethod().invoke(no, new Object[] {oldId});
					}
				}	
			}
		}

		try {
			em.remove(no);
		} catch (Exception e) {}
		return no;
	}

	private INakedObject clone(INakedObject no, String name) {
		try {
			Class<?> cc = Class.forName(name);
			return (INakedObject) newLazyObject(cc);
			
//			PropertyDescriptor[] pds = propertyDescriptorsByClassName.get(name);
//			for(PropertyDescriptor pd : pds) {
//				if(pd.getReadMethod() != null && pd.getWriteMethod() != null) {
//					pd.getWriteMethod().invoke(o, pd.getReadMethod().invoke(no, new String[0]));
//				}
//			}
//			return o;
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | IllegalArgumentException | InvocationTargetException | IntrospectionException | PersistenceException e) {
			logger.log(Level.SEVERE, "Exception while cloning...", e);
		}
		return no;
	}

	private String getClassName(INakedObject no) {
		String className = no.getClass().getName();
		int idx= className.indexOf("_$$_");
		if(idx > 0)
			className = className.substring(0, idx);
		return className;
	}

	@Override
	public CollectionInfo getCollectionInfo(EntityManager em,
			com.hiperf.common.ui.shared.util.Id id, String className, String attName) {
		CollectionInfo ci;
		String currentFilter;
		String jpql;
		int count = 0;
		String desc = null;
		jpql = "select size(o."+attName+") from "+className+" o";
		currentFilter = getIdClause(id);
		jpql += " where " + currentFilter;
		Query q = em.createQuery(jpql);
		count = getSize(id, q);

		if(count == 1) {
			jpql = "select o."+attName+" from "+className+" o where "+currentFilter;
			q = em.createQuery(jpql);
			int i = 0;
			for(Object idObj : id.getFieldValues()) {
				q.setParameter("id"+i, idObj);
			}
			desc = q.getResultList().get(0).toString();
		}
		ci = new CollectionInfo(count, desc);
		return ci;
	}


	@Override
	public String getFileName(String fileClassName,
			String fileNameField, Object fileId) throws PersistenceException {
		EntityManager em = null;
		try {
			em = getEntityManager();
			return getFileName(fileClassName, fileNameField, fileId, em);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getFileName", e);
			throw new PersistenceException("Exception in getFileName", e);
		} finally {
			closeEntityManager(em);
		}
	}

	private String getFileName(String fileClassName, String fileNameField,
			Object fileId, EntityManager em) throws PersistenceException {
		String idFieldName = getIdFieldFromFileStorageClass(fileClassName);
		List<String> l = em.createQuery("select o."+fileNameField+" from "+fileClassName+" o where o."+idFieldName+" = :id").setParameter("id", fileId).getResultList();
		if(l!=null&&!l.isEmpty()) {
			return l.get(0);
		}
		return null;
	}


	private static boolean isProxy(Object o, String realClassName) {
		return !o.getClass().getName().equals(realClassName);
	}


	private static Object deproxyObject(Class<?> targetClass, Object proxy)
			throws InstantiationException, IllegalAccessException,
			IntrospectionException, InvocationTargetException, PersistenceException, ClassNotFoundException {
		Object target = targetClass.newInstance();
		PropertyDescriptor[] targetPds = Introspector.getBeanInfo(targetClass).getPropertyDescriptors();
		for(PropertyDescriptor targetPd : targetPds)  {
			if(targetPd.getReadMethod() != null && targetPd.getWriteMethod() != null) {
				Object o = targetPd.getReadMethod().invoke(proxy, new Object[0]);
				if(o!=null) {
					Class<?> propertyType = targetPd.getPropertyType();
					String className = propertyType.getName();
					if(!propertyType.isPrimitive() && !o.getClass().isPrimitive() && !(o instanceof Date) && isProxy(o, className)) {
						if(Set.class.isAssignableFrom(propertyType)) {
							o = new LazySet();
						} else if(List.class.isAssignableFrom(propertyType)) {
							o = new LazyList();
						} else
							o = newLazyObject(propertyType);
					}
					targetPd.getWriteMethod().invoke(target, o);
				}
			}
		}

		return target;
	}


	private void deproxyCollection(INakedObject no, Method readMethod,
			Method writeMethod, Collection originalColl, Map<String, Map<Object, Object>> oldIdByNewId, EntityManager em, Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext)
			throws InstantiationException, IllegalAccessException,
			IntrospectionException, InvocationTargetException,
			ClassNotFoundException, PersistenceException {
		if(!(originalColl instanceof ILazy)) {
			Collection targetColl;
			if(List.class.isAssignableFrom(readMethod.getReturnType())) {
				targetColl = new ArrayList(originalColl.size());
			} else {
				targetColl = new HashSet(originalColl.size());
			}
			Type type = readMethod.getGenericReturnType();
			Class classInCollection = null;
			if(type instanceof ParameterizedType) {
				classInCollection = (Class)((ParameterizedType)type).getActualTypeArguments()[0];
			}
			if(classInCollection != null) {
				if(Number.class.isAssignableFrom(classInCollection) || String.class.equals(classInCollection) ||
						Boolean.class.equals(classInCollection)) {
					targetColl.addAll((Collection)readMethod.invoke(no, new Object[0]));
				} else {
					for(Object obj : originalColl) {
						String targetClassName = classInCollection.getName();

						if(isProxy(obj, targetClassName)) {
							obj = deproxyObject(classInCollection, obj);
						}
						Set<PropertyDescriptor> idPds = idsByClassName.get(targetClassName);
						targetColl.add(deproxyNakedObject(false, collectionsByClassName.get(targetClassName), lazysByClassName.get(targetClassName), eagerObjectsByClassName.get(targetClassName), linkedFilesByClassName.get(targetClassName), (INakedObject)obj, idPds, oldIdByNewId, em, deproxyContext));
					}
				}

				writeMethod.invoke(no, targetColl);
			}
		}

	}


	private static final Object newLazyObject(Class<?> targetClass)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, IntrospectionException, IllegalArgumentException, InvocationTargetException, PersistenceException {
		Object lazyObj = targetClass.newInstance();
		String name = targetClass.getName();
		Set<PropertyDescriptor> pds = idsByClassName.get(name);
		for(PropertyDescriptor pd : pds) {
			pd.getWriteMethod().invoke(lazyObj, getLazyValue(pd.getPropertyType()));
		}
		return lazyObj;
	}

	private static final Object getLazyValue(Class<?> c) {
		if(c.equals(Integer.class) || c.equals(int.class)) {
			return ILazyId.INT;
		}
		if(c.equals(Long.class) || c.equals(long.class)) {
			return ILazyId.LONG;
		}
		if(c.equals(String.class)) {
			return ILazyId.STRING;
		}
		//TODO : Object class Type ....
		return null;
	}


	private static final Set<PropertyDescriptor> initClassMapping(String className) throws ClassNotFoundException, IntrospectionException, PersistenceException {
		Set<PropertyDescriptor> ids = new HashSet<PropertyDescriptor>();
		Set<PropertyDescriptor> collections = new HashSet<PropertyDescriptor>();
		Set<PropertyDescriptor> lazys = new HashSet<PropertyDescriptor>();
		Set<PropertyDescriptor> eagers = new HashSet<PropertyDescriptor>();
		Set<LinkFileInfo> linkedFiles = new HashSet<LinkFileInfo>();
		idsByClassName.put(className, ids);
		collectionsByClassName.put(className, collections);
		lazysByClassName.put(className, lazys);
		eagerObjectsByClassName.put(className, eagers);
		linkedFilesByClassName.put(className, linkedFiles);
		List<String> idsAttributes = new ArrayList<String>();
		Class<?> c = Class.forName(className);
		Table tableAnn = c.getAnnotation(Table.class);
		if(tableAnn != null) {
			tableByClassName.put(className, tableAnn.name());
		} else {
			Entity entityAnn = c.getAnnotation(Entity.class);
			if(entityAnn.name() != null) {
				tableByClassName.put(className, entityAnn.name());
			} else {
				tableByClassName.put(className, className.substring(className.lastIndexOf(".") + 1));
			}
		}
		BeanInfo beanInfo = Introspector.getBeanInfo(c);
		PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
		propertyDescriptorsByClassName.put(className, pds);
		IdClass idClass = c.getAnnotation(IdClass.class);

		for(Field f : c.getDeclaredFields()) {
			Id id = f.getAnnotation(Id.class);
			if(id != null) {
				idsAttributes.add(f.getName());
				if(f.isAnnotationPresent(GeneratedValue.class)) {
					generatedIdClasses.add(className);
				}
			}
		}
		if(!idsAttributes.isEmpty()) {
			for(Field f : c.getDeclaredFields()) {
				if(!Modifier.isStatic(f.getModifiers())) {
					PropertyDescriptor pd = getPropertyDescriptor(pds, f);
					processField(className, pd, ids, collections, lazys, eagers, f);
				}
			}
			if(idClass != null) {
				Class clazz = idClass.value();
				for(Field f : clazz.getDeclaredFields()) {
					if(!Modifier.isStatic(f.getModifiers())) {
						PropertyDescriptor pd = getPropertyDescriptor(pds, f);
						processField(clazz.getName(), pd, ids, collections, lazys, eagers, f);
					}
				}
			}
			/*for(PropertyDescriptor pd : pds) {
				processLinkedFiles(pds, linkedFiles, pd);
			}*/
		} else {
			for(PropertyDescriptor pd : pds) {
				processMethod(className, pds, ids, collections, lazys, eagers, linkedFiles, pd);
			}
			if(idClass != null) {
				Class clazz = idClass.value();
				for(PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
					processMethod(clazz.getName(), pds, ids, collections, lazys, eagers, linkedFiles, pd);
				}
			}
		}
		return ids;

	}


	private static PropertyDescriptor getPropertyDescriptor(
			PropertyDescriptor[] pds, Field f) throws PersistenceException {
		PropertyDescriptor pd = null;
		for(PropertyDescriptor p : pds) {
			if(f.getName().equals(p.getName())) {
				pd = p;
				break;
			}
		}
		if(pd == null)
			throw new PersistenceException("PropertyDescriptor not found for "+f.getName()+" in class "+f.getDeclaringClass().getName());
		return pd;
	}


	private static void processMethod(String className, PropertyDescriptor[] pds, Set<PropertyDescriptor> ids, Set<PropertyDescriptor> collections,
			Set<PropertyDescriptor> lazys, Set<PropertyDescriptor> eagers,
			Set<LinkFileInfo> linkedFiles, PropertyDescriptor pd) throws PersistenceException {
		Method readMethod = pd.getReadMethod();
		if(readMethod != null) {
			if(readMethod.isAnnotationPresent(Id.class)) {
				ids.add(pd);
				if(readMethod.isAnnotationPresent(GeneratedValue.class)) {
					generatedIdClasses.add(className);
				}
			}
			OneToMany oneToMany = readMethod.getAnnotation(OneToMany.class);
			if(oneToMany != null) {
				collections.add(pd);
			} else {
				ManyToMany manyToMany = readMethod.getAnnotation(ManyToMany.class);
				if(manyToMany != null) {
					collections.add(pd);
				} else if(INakedObject.class.isAssignableFrom(pd.getPropertyType())) {
					OneToOne oneToOne = readMethod.getAnnotation(OneToOne.class);
					if(oneToOne != null) {
						if(oneToOne.fetch().equals(FetchType.LAZY))
							lazys.add(pd);
						else
							eagers.add(pd);
					} else {
						ManyToOne manyToOne = readMethod.getAnnotation(ManyToOne.class);
						if(manyToOne != null) {
							if(manyToOne.fetch().equals(FetchType.LAZY))
								lazys.add(pd);
							else
								eagers.add(pd);
						} else {
							Basic basic = readMethod.getAnnotation(Basic.class);
							if(basic != null) {
								if(basic.fetch().equals(FetchType.LAZY))
									lazys.add(pd);
								else
									eagers.add(pd);
							}
						}
					}
				}
			}
			//processLinkedFiles(pds, linkedFiles, pd);
		}
	}


	/*private static void processLinkedFiles(PropertyDescriptor[] pds,
			Set<LinkFileInfo> linkedFiles, PropertyDescriptor pd) throws PersistenceException {
		Method readMethod = pd.getReadMethod();
		if(readMethod != null) {
			UILinkedFile lfAnn = readMethod.getAnnotation(UILinkedFile.class);
			if(lfAnn != null) {
				if(pd.getWriteMethod() == null) {
					throw new PersistenceException("Missing setter for property "+pd.getName()+" with annotation "+UILinkedFile.class.getName());
				}
				Method localFileIdGetter = null;
				for(PropertyDescriptor p : pds) {
					if(lfAnn.localKeyField().equals(p.getName())) {
						localFileIdGetter = p.getReadMethod();
						break;
					}
				}
				if(localFileIdGetter == null) {
					throw new PersistenceException("Missing getter for property "+lfAnn.localKeyField()+" with annotation "+UILinkedFile.class.getName());
				}
				linkedFiles.add(new LinkFileInfo(pd.getWriteMethod(), localFileIdGetter, lfAnn.fileClassName(), lfAnn.fileFieldName()));
			}
		}
	}*/


	private static void processField(String className, PropertyDescriptor pd, Set<PropertyDescriptor> ids, Set<PropertyDescriptor> collections, Set<PropertyDescriptor> lazys, Set<PropertyDescriptor> eagers, Field f) {
		if(f.isAnnotationPresent(Id.class)) {
			ids.add(pd);
			if(f.isAnnotationPresent(GeneratedValue.class)) {
				generatedIdClasses.add(className);
			}
		}
		OneToMany oneToMany = f.getAnnotation(OneToMany.class);
		CollectionTable collTable = f.getAnnotation(CollectionTable.class);
		if(oneToMany != null || collTable != null) {
			collections.add(pd);
		} else {
			ManyToMany manyToMany = f.getAnnotation(ManyToMany.class);
			if(manyToMany != null) {
				collections.add(pd);
			} else if(INakedObject.class.isAssignableFrom(f.getType())) {
				OneToOne oneToOne = f.getAnnotation(OneToOne.class);
				if(oneToOne != null) {
					if(oneToOne.fetch().equals(FetchType.LAZY)) {
						lazys.add(pd);
					}
					else {
						eagers.add(pd);
					}
				} else {
					ManyToOne manyToOne = f.getAnnotation(ManyToOne.class);
					if(manyToOne != null) {
						if(manyToOne.fetch().equals(FetchType.LAZY)) {
							lazys.add(pd);
						}
						else {
							eagers.add(pd);
						}
					} else {
						Basic basic = f.getAnnotation(Basic.class);
						if(basic != null) {
							if(basic.fetch().equals(FetchType.LAZY)) {
								lazys.add(pd);
							}
							else  {
								eagers.add(pd);
							}
						}
					}
				}
			}
		}
	}


	@Override
	public Collection<INakedObject> deproxyCollection(INakedObject no, String attributeName, EntityManager em, Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext) throws PersistenceException {
		try {
			String name = no.getClass().getName();
			Set<PropertyDescriptor> colls = collectionsByClassName.get(name);
			if(colls != null && !colls.isEmpty()) {
				for(PropertyDescriptor pd : colls) {
					if(attributeName.equals(pd.getName())) {
						Method readMethod = pd.getReadMethod();
						deproxyCollection(no, readMethod, pd.getWriteMethod(), (Collection)readMethod.invoke(no, new Object[0]), null, em, deproxyContext);
						return (Collection<INakedObject>)readMethod.invoke(no, new Object[0]);
					}
				}
			}
			return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in deproxyCollection", e);
			throw new PersistenceException("Exception in deproxyCollection...", e);
		}
	}


	@Override
	public INakedObject deproxyLinkedObject(INakedObject no, String attributeName, EntityManager em)  throws PersistenceException {
		try {
			String name = no.getClass().getName();
			Set<PropertyDescriptor> colls = lazysByClassName.get(name);
			if(colls != null && !colls.isEmpty()) {
				INakedObject lo = deproxyLinkedObject(no, attributeName, colls, em);
				if(lo != null)
					return lo;
			}
			colls = eagerObjectsByClassName.get(name);
			if(colls != null && !colls.isEmpty()) {
				INakedObject lo = deproxyLinkedObject(no, attributeName, colls, em);
				if(lo != null)
					return lo;
			}
			return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in deproxyLinkedObject", e);
			throw new PersistenceException("Exception in deproxyLinkedObject...", e);
		}
	}


	private INakedObject deproxyLinkedObject(INakedObject no,
			String attributeName, Set<PropertyDescriptor> colls, EntityManager em)
			throws IllegalAccessException, InvocationTargetException,
			InstantiationException, IntrospectionException,
			PersistenceException, ClassNotFoundException {

		for(PropertyDescriptor pd : colls) {
			if(attributeName.equals(pd.getName())) {
				Method readMethod = pd.getReadMethod();
				INakedObject linkedObj = (INakedObject)readMethod.invoke(no, new Object[0]);
				return deproxyNakedObject(linkedObj, em, new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>>());
			}
		}
		return null;
	}


	@Override
	public Object getCompositeId(Class<?> c, List<String> idFieldNames, List myId) throws InstantiationException, IllegalAccessException {
		IdClass idClass = c.getAnnotation(IdClass.class);
		Class clazz = idClass.value();
		Object pk = clazz.newInstance();
		for(Field f : clazz.getDeclaredFields()) {
			int idx = idFieldNames.indexOf(f.getName());
			if(idx >= 0) {
				boolean b = false;
				if(!f.isAccessible()) {
					f.setAccessible(true);
					b = true;
				}
				f.set(pk, myId.get(idx));
				if(b)
					f.setAccessible(false);
			}
		}
		return pk;
	}


	public static boolean isComposite(String className) throws PersistenceException {
		return idsByClassName.get(className).size() > 1;
	}


	public static String getTable(String className) throws PersistenceException {
		return tableByClassName.get(className);
	}


	@Override
	public void updateAttributeValue(String className, INakedObject original,
			String att, Object object) throws PersistenceException {
		for(PropertyDescriptor pd : propertyDescriptorsByClassName.get(className)) {
			if(pd.getName() != null && pd.getName().equals(att) && pd.getWriteMethod() != null) {
				try {
					pd.getWriteMethod().invoke(original, object);
					return;
				} catch (Exception e) {
					throw new PersistenceException(e);
				}
			}
		}
	}


	@Override
	public INakedObject get(String nakedObjectName, com.hiperf.common.ui.shared.util.Id id) throws PersistenceException {
		INakedObject o = null;
		EntityManager em= null;
		try {
			em = getEntityManager();
			o = getObject(Class.forName(nakedObjectName), id, em);
			return deproxyNakedObject(o, em, new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id,INakedObject>>());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in get : "+e.getMessage(), e);
			throw new PersistenceException("Exception in get : "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}


	private INakedObject getObject(Class<?> clazz, com.hiperf.common.ui.shared.util.Id id,
			EntityManager em)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		INakedObject o;
		if(id.getFieldValues().size() == 1) {
			o = (INakedObject) em.find(clazz, id.getFieldValues().get(0));
		} else {
			o = (INakedObject) em.find(clazz, getCompositeId(clazz, id.getFieldNames(), id.getFieldValues()));
		}
		return o;
	}


	@Override
	public Map<String,String> getAll(String rootClassName, String filter, String attPrefix, String childClassName, String childAttribute) throws PersistenceException {
		EntityManager em= null;

		if(attPrefix != null && !attPrefix.isEmpty()) {
			String[] s = attPrefix.split("\\.");
			if(s == null || s.length == 0) 
				s = new String[] {attPrefix};
			StringBuilder join = new StringBuilder();
			StringBuilder select = new StringBuilder();
			String lastClass = rootClassName;
			String lastJoinPrefix = "o.";
			int i = 0;
			for(int j = 0; j<s.length; j++) {
				String ss = s[j];
				PropertyDescriptor[] pds = propertyDescriptorsByClassName.get(lastClass);
				for(PropertyDescriptor pd : pds) {
					if(pd.getName().equals(ss)) {
						Class<?> pt = pd.getPropertyType();
						if(Collection.class.isAssignableFrom(pt) || INakedObject.class.isAssignableFrom(pt)) { 
							
							join.append(" inner join ").append(lastJoinPrefix);
							if(!lastJoinPrefix.endsWith(".")) {
								join.append(".");
							}
							join.append(ss).append(" y").append(i).append(" ");
							lastJoinPrefix = "y"+i+".";
							i++;
							if(Collection.class.isAssignableFrom(pt)) {
								Class<?> clazz;
								try {
									clazz = Class.forName(lastClass);
									ParameterizedType genericType = (ParameterizedType) clazz.getDeclaredField(ss).getGenericType();
									if(genericType != null) {
										for(Type t : genericType.getActualTypeArguments()) {
		
											if(t instanceof Class && INakedObject.class.isAssignableFrom((Class)t)) {
												lastClass = ((Class) t).getName();
												break;
											}
										}
									}
								} catch (Exception e) {
									logger.log(Level.SEVERE, "Error", e);
								}	
							} else {
								lastClass = pt.getName();
							}
							
						} else if(childClassName.equals(pt.getName())) {
							lastJoinPrefix += ss + ".";
							if(i == s.length - 1) {
								select.append("select ").append(lastJoinPrefix).append(childAttribute).append(" from ").append(rootClassName).append(" o ");
							}
							lastClass = pt.getName();
						} else {
							lastJoinPrefix += ss + ".";
							lastClass = pt.getName();
						}
						break;
					}
				}
			}
			HashMap<String, String> joinMap = null;
			if(join.length() > 0) {
				if(filter != null && !filter.isEmpty()) {
					int idx = filter.indexOf("inner join ");
					if(idx >= 0) {
						joinMap = new HashMap<>();
						String[] ff = filter.substring(idx + 11, filter.indexOf("where")).trim().split("inner join ");
						for(String fj : ff) {
							fj = fj.trim();
							String[] fjj = fj.split(" ");
							if(fjj.length == 2) {
								joinMap.put(fjj[0].trim(), fjj[1].trim());
							}
						}
					}
				}
			}
			if(joinMap != null && !joinMap.isEmpty() && join.length() > 0) {
				String jj = join.toString();
				for(Entry<String, String> e : joinMap.entrySet()) {
					String tmp = "inner join " + e.getKey();
					int k = jj.indexOf(tmp);
					if(k >= 0) {
						String ss = join.substring(k + tmp.length()).trim();
						int m = ss.indexOf(" ");
						if(m > 0) {
							ss = ss.substring(0, m);
						}
						jj = jj.replace(ss, e.getValue());
						/*lastJoinPrefix = lastJoinPrefix.replaceAll(ss, e.getValue());
						join.replace(k, k + tmp.length() + 1 + ss.length(), "");*/
					}
				}
			}
			if(select.length() == 0) {
				select.append("select ").append(lastJoinPrefix).append(childAttribute).append(" from ").append(rootClassName).append(" o ");
			}
			if(join.length() > 0) {				
				select.append(join);
			}
			if(filter != null && !filter.isEmpty()) {
				if(!filter.toLowerCase().contains("where"))
					select.append(" where ");
				select.append(filter);
			}
			select.append(" order by ").append(lastJoinPrefix).append(childAttribute).append(" asc");
			String jpql = select.toString();
			List<Date> dtParams = new ArrayList<Date>();
			try {
				em = getEntityManager();
				jpql = replaceDateParameters(jpql, dtParams);
				Query q = em.createQuery(jpql);
				List<Object> list = getResults(dtParams, q);
				if(list != null && !list.isEmpty()) {
					Map<String, String> map = new LinkedHashMap<String, String>();
					for(Object o : list) {
						StorageHelper.fillGetAllMap(map, o);
					}
					return map;
				} else
					return null;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception in getAll "+e.getMessage(), e);
				throw new PersistenceException("Exception in getAll "+e.getMessage(), e);
			} finally {
				closeEntityManager(em);
			}
		} else {
			try {
				em = getEntityManager();
				String jql = "select distinct o.";
				jql += childAttribute+" from "+rootClassName+" o ";
				if(filter != null && !filter.isEmpty()) {
					jql += filter;
				}
				jql += " order by o."+childAttribute+" asc";
				List<Date> dtParams = new ArrayList<Date>();
				jql = replaceDateParameters(jql, dtParams);
				Query q = em.createQuery(jql);
				List<Object> list = getResults(dtParams, q);
				
				
				if(list != null && !list.isEmpty()) {
					Map<String, String> map = new LinkedHashMap<String, String>();
					for(Object o : list) {
						StorageHelper.fillGetAllMap(map, o);
					}
					return map;
				} else
					return null;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception in getAll "+e.getMessage(), e);
				throw new PersistenceException("Exception in getAll "+e.getMessage(), e);
			} finally {
				closeEntityManager(em);
			}				
		}
	}

	private List<Object> getResults(List<Date> dtParams, Query q) {
		if(!dtParams.isEmpty()) {
			for(int k = 0; k<dtParams.size(); k++) {
				q.setParameter("dt"+k, dtParams.get(k));
			}
		}
		List<Object> list = q.getResultList();
		return list;
	}

	@Override
	public Filter getFilter(Long id) throws PersistenceException {
		Filter f = null;
		EntityManager em= null;
		try {
			em = getEntityManager();
			f = em.find(Filter.class, id);
			if(f != null && f.getValues() != null && !f.getValues().isEmpty()) {
				List<FilterValue> l = new ArrayList<FilterValue>(f.getValues().size());
				for(FilterValue fv : f.getValues())
					l.add(fv);
				f.setValues(l);
			}
			return f;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getFilter "+e.getMessage(), e);
			throw new PersistenceException("Exception in getFilter "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}

	@Override
	public Collection<INakedObject> getCollection(String nakedObjectName, com.hiperf.common.ui.shared.util.Id id, String attributeName) throws PersistenceException {
		INakedObject o = null;
		EntityManager em= null;
		try {
			em = getEntityManager();
			Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext = new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id,INakedObject>>();
			if(id.getFieldNames().size() == 1) {
				o = (INakedObject) em.find(Class.forName(nakedObjectName), id.getFieldValues().get(0));
			} else {
				Class c = Class.forName(nakedObjectName);
				o = (INakedObject) em.find(c, getCompositeId(c, id.getFieldNames(), id.getFieldValues()));
			}
			if(o != null) {
				return deproxyCollection(o, attributeName, em, deproxyContext);
			}
			return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getCollection "+e.getMessage(), e);
			throw new PersistenceException("Exception in getCollection "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}

	@Override
	public Map<Long, String> getFilters(String viewName, final String className, String userName)
			throws PersistenceException {
		EntityManager em= null;
		try {
			em = getEntityManager();
			String hql = "select o.id, o.name from Filter o where o.userName = :usr and o.className = :clazz";
			if(viewName != null)
				hql += " and o.viewName = :view";
			Query q = em.createQuery(hql);
			q.setParameter("usr", userName).setParameter("clazz", className);
			if(viewName != null)
				q.setParameter("view", viewName);
			List<Object[]> l = q.getResultList();
			if(l!=null && !l.isEmpty()) {
				Map<Long, String> map=  new HashMap<Long, String>(l.size());
				for(Object[] o : l) {
					map.put((Long)o[0], (String)o[1]);
				}
				return map;
			}
			return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getCollection "+e.getMessage(), e);
			throw new PersistenceException("Exception in getCollection "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}

	@Override
	public List<INakedObject> getAll(String jpqlQuery) throws PersistenceException {
		EntityManager em= null;
		List<INakedObject> res = null;
		try {
			em = getEntityManager();
			Query q = em.createQuery(jpqlQuery);
			setQueryTimeout(q);
			List<INakedObject> l = q.getResultList();
			if(l!=null && !l.isEmpty()) {
				Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext = new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id,INakedObject>>();
				res = new ArrayList<>(l.size());
				for(INakedObject no : l) {
					res.add(deproxyNakedObject(no, em, deproxyContext));
				}
			}
			return res;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getAll "+e.getMessage(), e);
			throw new PersistenceException("Exception in getAll "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}

	
	@Override
	public NakedObjectsList queryData(String className, String currentFilter, int page,
			int rowsPerPage, String orderBy, ObjectsToPersist toPersist, Locale locale, boolean distinct) throws PersistenceException {
		TransactionContext tc = null;
		Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res= new HashMap<com.hiperf.common.ui.shared.util.Id, INakedObject>();
		try {
			Map<Object, IdHolder> newIdByOldId = new HashMap<Object, IdHolder>();
			tc = createTransactionalContext();
			EntityManager em = tc.getEm();
			ITransaction tx = tc.getTx();
			tx.begin();
			if(toPersist != null)
				doPersist(toPersist, null, res,
						newIdByOldId, em, true, locale);
			Long count = null;
			int lastTotal = -1;
			StringBuilder jpqlSb;
			String jpql;
			List<Date> dtParams;
			if(rowsPerPage > 0) {
				jpqlSb = new StringBuilder("select count(");
				if(distinct && !isComposite(className))
					jpqlSb.append("distinct ");
				jpqlSb.append("o) from ").append(className);
				jpql = appendWhereClause(currentFilter, jpqlSb.toString());
				dtParams = new ArrayList<Date>();
				jpql = replaceDateParameters(jpql, dtParams);
				Query countQuery = em.createQuery(jpql);
				if(!dtParams.isEmpty()) {
					for(int i = 0; i<dtParams.size(); i++) {
						countQuery.setParameter("dt"+i, dtParams.get(i));
					}
				}
				count = (Long)countQuery.getResultList().get(0);
				lastTotal = rowsPerPage * (page - 1);
			}
			
			if(rowsPerPage <= 0 || (count > 0 && count > lastTotal)) {
				jpqlSb = new StringBuilder("select ");
				if(distinct)
					jpqlSb.append("distinct ");
				Set<PropertyDescriptor> ids = idsByClassName.get(className);
				Iterator<PropertyDescriptor> it = ids.iterator();
				List<String> idFieldNames = new ArrayList<>(ids.size());
				boolean hasOrder = orderBy != null && orderBy.length() > 0;
				while(it.hasNext()) {
					PropertyDescriptor n = it.next();
					jpqlSb.append("o.").append(n.getName());
					if(it.hasNext())
						jpqlSb.append(",");
					if(!it.hasNext() && hasOrder) {
						jpqlSb.append(",").append(getOrderByClause(orderBy));	
					}
					
					idFieldNames.add(n.getName());
				}
				jpqlSb.append(" from ").append(className);
				jpql = appendWhereClause(currentFilter, jpqlSb.toString());
				dtParams = new ArrayList<Date>();
				jpql = replaceDateParameters(jpql, dtParams);
				Query q = getSelectQuery(rowsPerPage, orderBy, em, jpql,
						dtParams, lastTotal, idFieldNames);
				List<INakedObject> list = null;	
				try {
					list = fillResultList(className, em, ids, idFieldNames, q, hasOrder);
				} catch (Exception e) {
					e.printStackTrace();
					if(e.getMessage().contains("ORA-01791")) {
						int idx = orderBy.indexOf("@");
						if(idx > 0) {							
							String s = orderBy.substring("left outer join ".length()).trim();	
							String clause = orderBy.substring(idx + 1);
							orderBy = " order by " + s.substring(0, s.indexOf(" "));
							StringTokenizer st = new StringTokenizer(clause);
							boolean asc = true;
							while(st.hasMoreTokens()) {
								String tok = st.nextToken();
								if(tok.equals(","))
									break;
								if(tok.equalsIgnoreCase("desc")) {
									asc = false;
									break;
								}
							}
							if(!asc) {
								orderBy += " DESC";
							}
						} else {
							idx = orderBy.indexOf(",");
							if(idx > 0)
								orderBy = orderBy.substring(0,idx).trim();
							idx = orderBy.indexOf("left outer join ");
							
							idx = orderBy.indexOf(".");
							if(idx > 0)
								orderBy = orderBy.substring(0,idx).trim();
						}
						q = getSelectQuery(rowsPerPage, orderBy, em, jpql,
								dtParams, lastTotal, idFieldNames);
						list = fillResultList(className, em, ids, idFieldNames, q, hasOrder);
					} else
						throw e;
				}
				Map<String, Map<Object, Object>> oldIdByNewId = buildInverseIdMap(newIdByOldId);
				if(list != null && !list.isEmpty()) {
					list = deproxyEntities(className, list, true, oldIdByNewId);
				}
				return new NakedObjectsList(list, count != null ? count.intValue() : list != null ? list.size() : 0, rowsPerPage, currentFilter);
			} else
				return null;

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in queryData : "+e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		} finally {
			finalizeTx(tc);
		}
	}

	public String getOrderByClause(String orderBy) {
		String l = orderBy.toLowerCase();
		int i = l.indexOf("by");
		if(i >=0)
			orderBy = orderBy.substring(i+2);
		if(l.endsWith("desc")) {
			orderBy = orderBy.substring(0, orderBy.length() - 4);
		} else if(l.endsWith("asc"))
			orderBy = orderBy.substring(0, orderBy.length() - 3);
		return orderBy;
	}

	public List<INakedObject> fillResultList(String className, EntityManager em, Set<PropertyDescriptor> ids,
			List<String> idFieldNames, Query q, boolean hasOrder)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		q.setHint("org.hibernate.readOnly", Boolean.TRUE);
		List<Object[]> ll = q.getResultList();
		if(ll != null && !ll.isEmpty()) {
			boolean single = ids.size() == 1;
			Object pk = null;
			Class<?> c = Class.forName(className);
			Class clazz = null;
			if(!single) {
				IdClass idClass = c.getAnnotation(IdClass.class);
				clazz = idClass.value();
				pk = clazz.newInstance();
			}
			List<INakedObject> list = new ArrayList<>(ll.size());
			for(Object o : ll) {
				if(single) {
					list.add((INakedObject) em.find(c, hasOrder ? ((Object[])o)[0] : o));
				} else {
					for(Field f : clazz.getDeclaredFields()) {
						int idx = idFieldNames.indexOf(f.getName());
						if(idx >= 0) {
							boolean b = false;
							if(!f.isAccessible()) {
								f.setAccessible(true);
								b = true;
							}
							f.set(pk, ((Object[])o)[idx]);
							if(b)
								f.setAccessible(false);
						}
					}
					list.add((INakedObject) em.find(c, pk));
				}
			}
			return list;	
		}
		return null;
	}

	public Query getSelectQuery(int rowsPerPage, String orderBy,
			EntityManager em, String jpql, List<Date> dtParams, int lastTotal, List<String> idFieldNames) {
		if(orderBy != null && orderBy.length() > 0) {
			int join = orderBy.indexOf("@");
			if(join > 0) {
				int k = jpql.indexOf("where");
				if(k>0) {
					jpql = jpql.substring(0, k) + orderBy.substring(0, join) + jpql.substring(k) + orderBy.substring(join + 1);
				} else {
					jpql = jpql + " " + orderBy.substring(0, join) + orderBy.substring(join + 1);
				}
			} else
				jpql = jpql + " " + orderBy;
			jpql = jpql + ", " + toString(idFieldNames);
		}
		Query q = em.createQuery(jpql);
		if(!dtParams.isEmpty()) {
			for(int i = 0; i<dtParams.size(); i++) {
				q.setParameter("dt"+i, dtParams.get(i));
			}
		}		
		setQueryTimeout(q);
		if(lastTotal >= 0 && rowsPerPage > 0) {
			q.setFirstResult(lastTotal);
			q.setMaxResults(rowsPerPage);			
		}
		return q;
	}

	private void setQueryTimeout(Query q) {
		q.setHint("org.hibernate.timeout", new Integer(60));
	}
	
	public static String toString(Collection collection) {
	    StringBuilder sb = new StringBuilder("o.");
	    Iterator it = collection.iterator();
	    while(it.hasNext()) {
	    	sb.append(it.next());
	    	if(it.hasNext())
	    		sb.append(", o.");
	    }
	    sb.append(" ");
	    return sb.toString();
	}

	private String replaceDateParameters(String jpql, List<Date> dtParams)
			throws ParseException {
		if(jpql.contains(IConstants.DT_SEP)) {
			String[] split = jpql.split(IConstants.DT_SEP);
			int j=0;
			SimpleDateFormat sdf = new SimpleDateFormat(IConstants.DT_YYYY_MM_DD);
			for(int i=1; i<split.length; i++) {
				if(split[i] != null && split[i].length() > 0 && !split[i].startsWith(" ")) {
					try {
						Date dt = sdf.parse(split[i]);
						String last = split[i-1].trim();
						if(last.endsWith("<") || last.endsWith("<=")) {
							dt.setHours(23);
							dt.setMinutes(59);
							dt.setSeconds(59);
						} else if(last.endsWith(">") || last.endsWith(">=")) {
							dt.setHours(0);
							dt.setMinutes(0);
							dt.setSeconds(0);
						}
						dtParams.add(dt);
						split[i] = ":dt" + j;
						j++;
					} catch (Exception e) {}
				}
			}
			jpql = "";
			for(int i=0; i<split.length; i++) {
				jpql = jpql + split[i];
			}
		}
		return jpql;
	}

	@Override
	public NakedObjectsList queryData(String className, String currentFilter, int page,
			int rowsPerPage, String orderBy, ObjectsToPersist toPersist, Locale locale) throws PersistenceException {
		return queryData(className, currentFilter, page, rowsPerPage, orderBy, toPersist, locale, false);
	}


	private String appendWhereClause(String currentFilter, String jpql) {
		if(currentFilter != null && currentFilter.length() > 0) {
			if(currentFilter.startsWith("o.") || currentFilter.startsWith("(o.") || currentFilter.startsWith("UPPER(o."))  {
				jpql += " o where " + currentFilter;
			} else {
				jpql += " o " + currentFilter;
			}
		} else {
			jpql +=" o";
		}
		return jpql;
	}

	@Override
	public Map<com.hiperf.common.ui.shared.util.Id, INakedObject> persist(ObjectsToPersist toPersist, String userName, Locale locale)
			throws PersistenceException {
		TransactionContext tc = null;
		Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res= new HashMap<com.hiperf.common.ui.shared.util.Id, INakedObject>();
		try {
			Map<Object, IdHolder> newIdByOldId = new HashMap<Object, IdHolder>();

			tc = createTransactionalContext();
			EntityManager em = tc.getEm();
			ITransaction tx = tc.getTx();
			tx.begin();
			if(doPersist(toPersist, userName, res,
					newIdByOldId, em, false, locale))
				tx.commit();
			else
				throw new PersistenceException("a technical problem occured during data insertion");

		} catch (Exception e) {
			catchPersistException(tc, e);
	        processDbExceptions(locale, e);
		} finally {
			if(tc != null)
				close(tc);
		}
		Map<String, Map<com.hiperf.common.ui.shared.util.Id,INakedObject>> deproxyCtx = new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id,INakedObject>>();
		EntityManager em = null;
		try {
			for(Entry<com.hiperf.common.ui.shared.util.Id, INakedObject> entry : res.entrySet()) {
				em = getEntityManager();
				INakedObject no = entry.getValue();
				String nakedObjectName = no.getClass().getName();
				com.hiperf.common.ui.shared.util.Id id = getId(no, idsByClassName.get(nakedObjectName));
				Map<com.hiperf.common.ui.shared.util.Id, INakedObject> map = deproxyCtx.get(nakedObjectName);
				if(map == null || !map.containsKey(id)) {
					if(id.getFieldNames().size() == 1) {
						no = (INakedObject) em.find(Class.forName(nakedObjectName), id.getFieldValues().get(0));
					} else {
						Class c = Class.forName(nakedObjectName);
						no = (INakedObject) em.find(c, getCompositeId(c, id.getFieldNames(), id.getFieldValues()));
					}
					entry.setValue(deproxyNakedObject(no, em, deproxyCtx));
				} else {
					entry.setValue(map.get(id));
				}
			}
			return res;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in persist : "+e.getMessage(), e);
			throw new PersistenceException("Exception in persist : "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}

	private void closeEntityManager(EntityManager em) {
		if(em != null && em.isOpen())
			close(em);
	}

	@Override
	public boolean doPersist(
			ObjectsToPersist toPersist,
			Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res,
			Map<Object, IdHolder> newIdByOldId, EntityManager em, Locale locale)
			throws ClassNotFoundException, IntrospectionException,
			PersistenceException, IllegalAccessException,
			InvocationTargetException, InstantiationException {
		return doPersist(toPersist, null, res, newIdByOldId, em, true, locale, false);
	}

	private boolean doPersist(
			ObjectsToPersist toPersist,
			String userName,
			Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res,
			Map<Object, IdHolder> newIdByOldId, EntityManager em, boolean validateBefore, Locale locale)
			throws ClassNotFoundException, IntrospectionException,
			PersistenceException, IllegalAccessException,
			InvocationTargetException, InstantiationException {
		return doPersist(toPersist, userName, res, newIdByOldId, em, validateBefore, locale, true);
	}

	private boolean doPersist(
			ObjectsToPersist toPersist,
			String userName,
			Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res,
			Map<Object, IdHolder> newIdByOldId, EntityManager em, boolean validateBefore, Locale locale,
			boolean processExceptions)
			throws ClassNotFoundException, IntrospectionException,
			PersistenceException, IllegalAccessException,
			InvocationTargetException, InstantiationException {
		try {
			Validator validator = validatorFactory.getValidator();
			List<INakedObject> toInsert = toPersist.getInsertedObjects();
			if(toInsert != null) {
				int max = 100 * toInsert.size();
				int idx = -1;
				int k = -1;
				int s = toInsert.size();
				int prevSize = s;
				while(!toInsert.isEmpty()) {

					if(s == toInsert.size()) {
						k++;
					} else
						k = 0;
					if(k == 1) {
						logger.log(Level.FINE, "Impossible to persist data : one linked object not found in toInsert list");
						return false;
					}
					
					if(prevSize == toInsert.size()) {
						idx++;
					} else {
						idx = 0;
						prevSize = toInsert.size();
					}
					if(idx > max) {
						logger.log(Level.FINE, "Impossible to persist data : one linked object not found in toInsert list...");
						return false;
					}
					Iterator<INakedObject> it = toInsert.iterator();
					while(it.hasNext()) {
						INakedObject o = (INakedObject)it.next();
						String className = o.getClass().getName();
						if(o instanceof IAuditable) {
							IAuditable aud = (IAuditable) o;
							aud.setCreateUser(userName);
							aud.setCreateDate(new Date());
						}

						Set<PropertyDescriptor> ids = idsByClassName.get(className);

						processLinkedCollectionsBeforePersist(
								o, collectionsByClassName.get(className));

						if(!processLinkedObjectsBeforePersist(
								newIdByOldId, o, lazysByClassName.get(className), toPersist))
							continue;
						if(!processLinkedObjectsBeforePersist(
								newIdByOldId, o, eagerObjectsByClassName.get(className), toPersist))
							continue;

						if(generatedIdClasses.contains(className)) {
							PropertyDescriptor idPd = ids.iterator().next();
							Object oldId = idPd.getReadMethod().invoke(o, StorageService.emptyArg);
							Object[] args = new Object[1];
							if(!idPd.getPropertyType().isPrimitive())
								args[0] = null;
							else
								args[0] = 0L;
							idPd.getWriteMethod().invoke(o, args);

							if(validateBefore) {
								Set<ConstraintViolation<INakedObject>> errors = validator.validate(o);
								if(errors != null && !errors.isEmpty()) {
									it.remove();
									continue;
								}
								try {
									em.persist(o);
								} catch (Exception e) {
									it.remove();
									continue;
								}
							} else
								em.persist(o);
							Object newId = idPd.getReadMethod().invoke(o, StorageService.emptyArg);
							newIdByOldId.put(oldId, new IdHolder(newId, className));
							List<Object> idVals = new ArrayList<Object>(1);
							idVals.add(oldId);
							List<String> idFields = new ArrayList<String>(1);
							idFields.add(idPd.getName());
							res.put(new com.hiperf.common.ui.shared.util.Id(idFields, idVals), o);

							it.remove();
						}  else {
							com.hiperf.common.ui.shared.util.Id id = getId(o, ids);
							int i = 0;
							boolean toProcess = true;
							for(Object idVal : id.getFieldValues()) {
								if((idVal instanceof Long && ((Long)idVal).longValue() < 0) ||
										(idVal instanceof String && ((String)idVal).startsWith(PersistenceManager.SEQ_PREFIX))) {
									IdHolder newIds = newIdByOldId.get(idVal);
									if(newIds != null) {
										String att = id.getFieldNames().get(i);
										for(PropertyDescriptor idPd : ids) {
											if(idPd.getName().equals(att)) {
												Object[] args = new Object[1];
												args[0] = newIds.getId();
												idPd.getWriteMethod().invoke(o, args);
												break;
											}
										}
									} else {
										toProcess = false;
										break;
									}
								}
								i++;
							}
							if(toProcess) {
								if(validateBefore) {
									Set<ConstraintViolation<INakedObject>> errors = validator.validate(o);
									if(errors != null && !errors.isEmpty()) {
										it.remove();
										continue;
									}
									try {
										refreshManyToOneLinkedWithId(o, id, em);
										em.persist(o);
									} catch (Exception e) {
										it.remove();
										continue;
									}
								} else {
									refreshManyToOneLinkedWithId(o, id, em);
									em.persist(o);
								}
								id = getId(o, ids);
								res.put(id, o);
								it.remove();
							}
						}
					}
				}
			}
			Map<String, Set<com.hiperf.common.ui.shared.util.Id>> toDelete = toPersist.getRemovedObjectsIdsByClassName();
			if(toDelete != null) {
				for(String className : toDelete.keySet()) {
					Set<com.hiperf.common.ui.shared.util.Id> ids = toDelete.get(className);
					Class<?> clazz = Class.forName(className);
					Map<Field,Field> toRemove = null;
					if(ids != null && !ids.isEmpty()) {
						com.hiperf.common.ui.shared.util.Id id = ids.iterator().next();
						if(id.getFieldValues().size() > 1) {
							toRemove = new HashMap<Field, Field>();
							Field[] fields = clazz.getDeclaredFields();
							for(Field f : fields) {
								if(f.isAnnotationPresent(ManyToOne.class)) {
									Field[] ff = f.getType().getDeclaredFields();
									for(Field lf : ff) {
										OneToMany ann = lf.getAnnotation(OneToMany.class);
										if(ann != null && ann.targetEntity() != null && ann.targetEntity().equals(clazz)) {
											toRemove.put(f, lf);
										}
									}
								}
							}
							// TODO : manage annotations on the getters...
						}
					}
					for(com.hiperf.common.ui.shared.util.Id id : ids) {
						INakedObject no = getObject(clazz,id, em);
						if(no != null) {
							if(toRemove != null && !toRemove.isEmpty()) {
								for(Entry<Field,Field> e : toRemove.entrySet()) {
									Field f = e.getKey();
									Field ff = e.getValue();
									boolean b1 = false;
									boolean b2 = false;
									if(!f.isAccessible()) {
										f.setAccessible(true);
										b1 = true;
									}
									if(!ff.isAccessible()) {
										ff.setAccessible(true);
										b2 = true;
									}
									((Collection)ff.get(f.get(no))).remove(no);
									if(b1)
										f.setAccessible(false);
									if(b2)
										ff.setAccessible(false);
								}
							} else {
								// TODO : manage annotations on the getters...
							}
							em.remove(no);
						}
					}
				}
			}
			Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, Serializable>>> toUpdate = toPersist.getUpdatedObjects();
			if(toUpdate != null) {
				for(String className: toUpdate.keySet()) {
					Map<com.hiperf.common.ui.shared.util.Id, Map<String, Serializable>> map = toUpdate.get(className);
					Class<?> clazz = Class.forName(className);
					Iterator<Entry<com.hiperf.common.ui.shared.util.Id, Map<String, Serializable>>> iterator = map.entrySet().iterator();
					while(iterator.hasNext()) {
						Entry<com.hiperf.common.ui.shared.util.Id, Map<String, Serializable>> entry = iterator.next();
						com.hiperf.common.ui.shared.util.Id id = entry.getKey();
						INakedObject original = getObject(clazz, id, em);
						Map<String, Serializable> updateMap = entry.getValue();
						for(String att : updateMap.keySet()) {
							Object object = updateMap.get(att);
							if(object != null && object instanceof NakedObjectHandler) {
								NakedObjectHandler oo = (NakedObjectHandler) object;
								com.hiperf.common.ui.shared.util.Id objId = oo.getId();
								if(generatedIdClasses.contains(oo.getClassName()) && newIdByOldId.containsKey(objId.getFieldValues().get(0))) {
									IdHolder newIds = newIdByOldId.get(objId.getFieldValues().get(0));
									List<Object> idVals = new ArrayList<Object>(1);
									idVals.add(newIds.getId());
									List<String> idFields = new ArrayList<String>(1);
									idFields.add(idsByClassName.get(oo.getClassName()).iterator().next().getName());
									com.hiperf.common.ui.shared.util.Id newObjId = new com.hiperf.common.ui.shared.util.Id(idFields, idVals);
									object = getObject(Class.forName(oo.getClassName()), newObjId, em);
								} else {
									object = getObject(Class.forName(oo.getClassName()), oo.getId(), em);
								}
							}
							updateAttributeValue(className, original, att, object);
						}
						if(original instanceof IAuditable) {
							IAuditable aud = (IAuditable) original;
							aud.setModifyUser(userName);
							aud.setModifyDate(new Date());
						}
						INakedObject o = null;
						if(validateBefore) {
							Set<ConstraintViolation<INakedObject>> errors = validator.validate(original);
							if(errors != null && !errors.isEmpty()) {
								iterator.remove();
								continue;
							}
							try {
								o = em.merge(original);
								em.flush();
							} catch (Exception e) {
								iterator.remove();
								continue;
							}
						} else
							o = em.merge(original);

						res.put(id, o);
					}
				}
			}
			processAddedManyToMany(toPersist, res, newIdByOldId, em);
			processRemovedManyToMany(toPersist, res, newIdByOldId, em);
			em.flush();
			return true;
		} catch(Exception e) {
			logger.log(Level.WARNING, "Exception", e);
			if(processExceptions) {
				processDbExceptions(locale, e);
				return false;
			}
			else
				throw new PersistenceException(e);
		}

	}

	private void refreshManyToOneLinkedWithId(INakedObject o,
			com.hiperf.common.ui.shared.util.Id id, EntityManager em) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<? extends INakedObject> clazz = o.getClass();
		Field f = clazz.getDeclaredField(id.getFieldNames().get(0));
		if(f.isAnnotationPresent(Id.class)) {
			int i = 0;
			for(String name : id.getFieldNames()) {
				f = clazz.getDeclaredField(id.getFieldNames().get(i));
				String columnName = name;
				if(f.isAnnotationPresent(Column.class)) {
					columnName = f.getAnnotation(Column.class).name();
				}
				for(Field field : clazz.getDeclaredFields()) {
					if(field.isAnnotationPresent(ManyToOne.class) && field.isAnnotationPresent(JoinColumn.class)
							&& field.getAnnotation(JoinColumn.class).name().equalsIgnoreCase(columnName)) {
						boolean acc = field.isAccessible();
						try {
							if(!acc)
								field.setAccessible(true);
							Object pk = id.getFieldValues().get(i);
							if(PersistenceManager.isLocal(pk)) {
								boolean acc2 = f.isAccessible();
								try {
									if(!acc2) {
										f.setAccessible(true);
									}
									pk = f.get(o);
								} finally {
									if(!acc2) {
										f.setAccessible(false);
									}
								}
								if(!PersistenceManager.isLocal(pk))
									field.set(o, em.find(field.getType(), pk));		
							} else
								field.set(o, em.find(field.getType(), pk));
						} finally {
							field.setAccessible(acc);
						}
						break;
					}

				}
				i++;
			}
		} else {
			//TODO
		}



	}

	private void processRemovedManyToMany(ObjectsToPersist toPersist,
			Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res,
			Map<Object, IdHolder> newIdByOldId, EntityManager em)
			throws ClassNotFoundException, IntrospectionException,
			PersistenceException, IllegalAccessException,
			InvocationTargetException, NoSuchFieldException {
		Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> manyToManyRemoved = toPersist.getManyToManyRemovedByClassName();
		if(manyToManyRemoved != null && !manyToManyRemoved.isEmpty()) {
			for(Entry<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> e : manyToManyRemoved.entrySet()) {
				String className = e.getKey();
				Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>> map = e.getValue();
				if(map != null && !map.isEmpty()) {
					Class<?> clazz = Class.forName(className);
					for(Entry<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>> entry : map.entrySet()) {
						com.hiperf.common.ui.shared.util.Id id = entry.getKey();
						Map<String, List<com.hiperf.common.ui.shared.util.Id>> m = entry.getValue();
						if(m != null && !m.isEmpty()) {
							Object objId = id.getFieldValues().get(0);
							if(newIdByOldId.containsKey(objId))
								objId = newIdByOldId.get(objId).getId();
							Object o = em.find(clazz, objId);
							if(o != null) {
								PropertyDescriptor[] pds = propertyDescriptorsByClassName.get(className);
								for(Entry<String, List<com.hiperf.common.ui.shared.util.Id>> ee : m.entrySet()) {
									String attr = ee.getKey();
									List<com.hiperf.common.ui.shared.util.Id> ll = ee.getValue();
									if(ll != null && !ll.isEmpty()) {
										Collection coll = null;
										Class classInColl = null;
										PropertyDescriptor myPd = null;
										for(PropertyDescriptor pd : pds) {
											if(pd.getName().equals(attr)) {
												myPd = pd;
												coll = (Collection) pd.getReadMethod().invoke(o, StorageService.emptyArg);
												break;
											}
										}
										if(coll != null) {
											ParameterizedType genericType = (ParameterizedType) clazz.getDeclaredField(myPd.getName()).getGenericType();
											if(genericType != null) {
												for(Type t : genericType.getActualTypeArguments()) {

													if(t instanceof Class && INakedObject.class.isAssignableFrom((Class)t)) {
														classInColl = (Class) t;
														break;
													}
												}
											}
											for(com.hiperf.common.ui.shared.util.Id i : ll) {
												Object idObj = i.getFieldValues().get(0);
												if(newIdByOldId.containsKey(idObj))
													idObj = newIdByOldId.get(idObj);
												Object linkedObj = em.find(classInColl, idObj);
												coll.remove(linkedObj);
											}
										}
									}
								}
								res.put(id, (INakedObject)em.merge(o));
							}
						}

					}
				}
			}
		}
	}

	private void processAddedManyToMany(ObjectsToPersist toPersist,
			Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res,
			Map<Object, IdHolder> newIdByOldId, EntityManager em)
			throws ClassNotFoundException, IntrospectionException,
			PersistenceException, IllegalAccessException,
			InvocationTargetException, NoSuchFieldException {
		Map<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> manyToManyAdded = toPersist.getManyToManyAddedByClassName();
		if(manyToManyAdded != null && !manyToManyAdded.isEmpty()) {
			for(Entry<String, Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>>> e : manyToManyAdded.entrySet()) {
				String className = e.getKey();
				Map<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>> map = e.getValue();
				if(map != null && !map.isEmpty()) {
					Class<?> clazz = Class.forName(className);
					for(Entry<com.hiperf.common.ui.shared.util.Id, Map<String, List<com.hiperf.common.ui.shared.util.Id>>> entry : map.entrySet()) {
						com.hiperf.common.ui.shared.util.Id id = entry.getKey();
						Map<String, List<com.hiperf.common.ui.shared.util.Id>> m = entry.getValue();
						if(m != null && !m.isEmpty()) {
							Object objId = id.getFieldValues().get(0);
							if(newIdByOldId.containsKey(objId))
								objId = newIdByOldId.get(objId).getId();
							Object o = em.find(clazz, objId);
							if(o != null) {
								PropertyDescriptor[] pds = propertyDescriptorsByClassName.get(className);
								for(Entry<String, List<com.hiperf.common.ui.shared.util.Id>> ee : m.entrySet()) {
									String attr = ee.getKey();
									List<com.hiperf.common.ui.shared.util.Id> ll = ee.getValue();
									if(ll != null && !ll.isEmpty()) {
										Collection coll = null;
										Class classInColl = null;
										PropertyDescriptor myPd = null;
										String mappedBy = null;
										for(PropertyDescriptor pd : pds) {
											if(pd.getName().equals(attr)) {
												myPd = pd;
												coll = (Collection) pd.getReadMethod().invoke(o, StorageService.emptyArg);
												if(coll == null) {
													if(List.class.isAssignableFrom(pd.getPropertyType()))
														coll = new ArrayList();
													else
														coll = new HashSet();
													pd.getWriteMethod().invoke(o, coll);
												}
												ManyToMany ann = pd.getReadMethod().getAnnotation(ManyToMany.class);
												if(ann == null) {
													ann = clazz.getDeclaredField(pd.getName()).getAnnotation(ManyToMany.class);
												}
												if(ann != null) {
													mappedBy = ann.mappedBy();
												}
												break;
											}
										}
										if(coll != null) {
											ParameterizedType genericType = (ParameterizedType) clazz.getDeclaredField(myPd.getName()).getGenericType();
											if(genericType != null) {
												for(Type t : genericType.getActualTypeArguments()) {

													if(t instanceof Class && INakedObject.class.isAssignableFrom((Class)t)) {
														classInColl = (Class) t;
														break;
													}
												}
											}
											for(com.hiperf.common.ui.shared.util.Id i : ll) {
												Object idObj = i.getFieldValues().get(0);
												if(newIdByOldId.containsKey(idObj))
													idObj = newIdByOldId.get(idObj).getId();
												Object linkedObj = em.find(classInColl, idObj);
												if(mappedBy == null || mappedBy.length() == 0)
													coll.add(linkedObj);
												else {
													PropertyDescriptor[] pds2 = propertyDescriptorsByClassName.get(classInColl.getName());
													if(pds2 == null) {
														pds2 = propertyDescriptorsByClassName.get(classInColl.getName());
													}
													for(PropertyDescriptor pd : collectionsByClassName.get(classInColl.getName())) {
														if(pd.getName().equals(mappedBy)) {
															Collection coll2 = (Collection) pd.getReadMethod().invoke(linkedObj, StorageService.emptyArg);
															if(coll2 == null) {
																if(List.class.isAssignableFrom(pd.getPropertyType()))
																	coll2 = new ArrayList();
																else
																	coll2 = new HashSet();
																pd.getWriteMethod().invoke(linkedObj, coll2);
															}
															coll2.add(o);
														}
													}
													em.merge(linkedObj);
												}
												if(linkedObj instanceof INakedObject)
													res.put(i, (INakedObject)linkedObj);
											}
										}
									}
								}
								res.put(id, (INakedObject)em.merge(o));
							}
						}

					}
				}
			}
		}
	}


	private void processDbExceptions(Locale locale, Throwable e)
			throws PersistenceException {
		Throwable t = e;
		while(t != null && !t.getClass().getName().contains("ConstraintViolationException")) {
			t = t.getCause();
		}
		if(t != null) {
			ResourceBundle ressource = ResourceBundle.getBundle(SERVER_MESSAGES, locale, PersistenceHelper.class.getClassLoader(), new UTF8Control());
			throw new PersistenceException(ressource.getString("errorConstraintViolation"), t);
		} else {
			logger.log(Level.SEVERE, "DB error", e);
			throw new PersistenceException(e);
		}
	}


	private void processLinkedCollectionsBeforePersist(
			INakedObject o, Set<PropertyDescriptor> linkedObjects)
			throws IllegalAccessException, InvocationTargetException, ClassNotFoundException, IntrospectionException, PersistenceException {
		if(linkedObjects != null && !linkedObjects.isEmpty()) {
			for(PropertyDescriptor pd : linkedObjects) {
				pd.getWriteMethod().invoke(o, nullArg);
			}
		}
	}


	private boolean processLinkedObjectsBeforePersist(
			Map<Object, IdHolder> newIdByOldId,
			INakedObject o, Set<PropertyDescriptor> linkedObjects, ObjectsToPersist toPersist)
			throws IllegalAccessException, InvocationTargetException, ClassNotFoundException, IntrospectionException, PersistenceException, InstantiationException {
		if(linkedObjects != null && !linkedObjects.isEmpty()) {
			for(PropertyDescriptor pd : linkedObjects) {
				INakedObject no = (INakedObject)pd.getReadMethod().invoke(o, StorageService.emptyArg);
				if(no != null) {
					Class<? extends INakedObject> linkedClass = no.getClass();
					String linkedClassName = linkedClass.getName();
					Set<PropertyDescriptor> loIds = idsByClassName.get(linkedClassName);
					com.hiperf.common.ui.shared.util.Id loId = getId(no, loIds);
					if(loId.isLocal()) {
						INakedObject newObj = linkedClass.newInstance();
						int i = 0;
						for(Object id : loId.getFieldValues()) {
							if((id instanceof Long && ((Long)id).longValue() < 0)
									|| (id instanceof String && ((String)id).startsWith(PersistenceManager.SEQ_PREFIX))) {
								IdHolder ids = newIdByOldId.get(id);
								if(ids == null)
									return false;
								else
									id = ids.getId();
							}
							setIdField(loIds, loId, newObj, i, id);
							i++;
						}
						setObject(o, pd, newObj);
					} else if(!newIdByOldId.containsValue(new IdHolder(loId.getFieldValues().get(0), linkedClassName ))
							&& toPersist.getUpdatedObjects() != null && toPersist.getUpdatedObjects().containsKey(linkedClassName)
							&& toPersist.getUpdatedObjects().get(linkedClassName).get(loId) != null) {
						INakedObject newObj = linkedClass.newInstance();
						int i = 0;
						for(Object id : loId.getFieldValues()) {
							setIdField(loIds, loId, newObj, i, id);
							i++;
						}
						setObject(o, pd, newObj);
					}
				}
			}
		}
		return true;
	}


	private void setObject(INakedObject o, PropertyDescriptor pd,
			INakedObject newObj) throws IllegalAccessException,
			InvocationTargetException {
		Object[] args = new Object[1];
		args[0] = newObj;
		pd.getWriteMethod().invoke(o, args);
	}


	private void setIdField(Set<PropertyDescriptor> loIds,
			com.hiperf.common.ui.shared.util.Id loId, INakedObject newObj, int i,
			Object id) throws IllegalAccessException, InvocationTargetException {
		for(PropertyDescriptor pdId : loIds) {
			if(pdId.getName().equals(loId.getFieldNames().get(i))) {
				Object[] aa = new Object[1];
				aa[0] = id;
				pdId.getWriteMethod().invoke(newObj, aa);
				break;
			}
		}
	}

	public com.hiperf.common.ui.shared.util.Id getId(INakedObject o) throws IllegalAccessException, InvocationTargetException {
		return getId(o, idsByClassName.get(o.getClass().getName()));
	}

	private com.hiperf.common.ui.shared.util.Id getId(INakedObject o,
			Set<PropertyDescriptor> ids) throws IllegalAccessException,
			InvocationTargetException {
		com.hiperf.common.ui.shared.util.Id myOldId;
		List<Object> idList = new ArrayList<Object>();
		List<String> idFields = new ArrayList<String>();
		for(PropertyDescriptor pd : ids) {
			idFields.add(pd.getName());
			idList.add(pd.getReadMethod().invoke(o, StorageService.emptyArg));
		}
		myOldId = new com.hiperf.common.ui.shared.util.Id(idFields, idList);
		return myOldId;
	}
	
	public com.hiperf.common.ui.shared.util.Id getId(String s, String className) {
		if(s != null && s.length() > 0) {
			String[] ss = s.split(",");
			int l = ss.length;
			PropertyDescriptor[] pds = propertyDescriptorsByClassName.get(className);
			List<String> fieldNames = new ArrayList<String>(l);
			List<Object> fieldValues = new ArrayList<Object>(l);
			int j = 0;
			for(int i = 0; i<l; i++) {
				String[] ids = ss[i].split(":");
				String att = null;
				if(ids.length == 2) {
					j = 1;
					att = ids[0];
					fieldNames.add(att);
				} else if(l == 1) {
					j = 0;
					att = idsByClassName.get(className).iterator().next().getName();
					fieldNames.add(att);
				}
				if(att != null) {
					for(PropertyDescriptor pd : pds) {
						if(pd.getName().equals(att)) {
							if(pd.getPropertyType().equals(Long.class) || pd.getPropertyType().equals(long.class)) {
								fieldValues.add(Long.valueOf(ids[j]));			
							} else if(pd.getPropertyType().equals(Integer.class) || pd.getPropertyType().equals(int.class)) {
								fieldValues.add(Long.valueOf(ids[j]));			
							} else
								fieldValues.add(ids[j].toString());
						}
					}
				}
			}
			return new com.hiperf.common.ui.shared.util.Id(fieldNames, fieldValues);
		}
		return null;
	}

	@Override
	public List<INakedObject> reload(String nakedObjectName,
			List<String> idFields, List<List<Object>> idList)
			throws PersistenceException {
		TransactionContext tc = null;
		List<INakedObject> l = new ArrayList<INakedObject>(idList.size());
		try {
			Class<?> c = Class.forName(nakedObjectName);
			tc = createTransactionalContext();
			EntityManager em = tc.getEm();
			ITransaction tx = tc.getTx();
			tx.begin();
			if(idFields.size() == 1) {
				for(List myId : idList) {
					l.add((INakedObject) em.find(c, myId.get(0)));
				}
				l = deproxyEntities(nakedObjectName, l, true, null);
			} else {
				for(List myId : idList) {
					l.add((INakedObject) em.find(c, getCompositeId(c, idFields, myId)));
				}
				l = deproxyEntities(nakedObjectName, l, true, null);
			}
			return l;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in reload : "+e.getMessage(), e);
			throw new PersistenceException("Exception in reload : "+e.getMessage(), e);
		} finally {
			finalizeTx(tc);
		}
	}

	private void finalizeTx(TransactionContext tc) {
		if(tc != null) {
			try {
				tc.rollback();
			} catch (TransactionException e) {
				logger.log(Level.SEVERE, "Exception in rollback...", e);
			}
			close(tc);
		}
	}


	@Override
	public INakedObject getLinkedObject(String nakedObjectName, com.hiperf.common.ui.shared.util.Id id,
			String attributeName) throws PersistenceException {
		INakedObject o = null;
		EntityManager em= null;
		try {
			em = getEntityManager();
			if(id.getFieldNames().size() == 1) {
				o = (INakedObject) em.find(Class.forName(nakedObjectName), id.getFieldValues().get(0));
			} else {
				Class c = Class.forName(nakedObjectName);
				o = (INakedObject) em.find(c, getCompositeId(c, id.getFieldNames(), id.getFieldValues()));
			}
			if(o != null) {
				return deproxyLinkedObject(o, attributeName, em);
			}
			return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getLinkedObject "+e.getMessage(), e);
			throw new PersistenceException("Exception in getLinkedObject "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}

	@Override
	public NakedObjectsList getCollectionInverse(String wrappedClassName, String attribute, com.hiperf.common.ui.shared.util.Id id, int page,
			int nbRows, ObjectsToPersist toPersist, String sortAttribute, Boolean asc)  throws PersistenceException {
		INakedObject o = null;
		EntityManager em= null;
		TransactionContext tc = null;
		Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res= new HashMap<com.hiperf.common.ui.shared.util.Id, INakedObject>();
		try {
			Map<Object, IdHolder> newIdByOldId = new HashMap<Object, IdHolder>();
			tc = createTransactionalContext();
			em = tc.getEm();
			ITransaction tx = tc.getTx();
			tx.begin();
			doPersist(toPersist, null, res,
					newIdByOldId, em, true, null);
			String jpql = "select size(o."+attribute+") from "+wrappedClassName+" o";
			String currentFilter = getIdClause(id);
			jpql += " where " + currentFilter;
			Query q = em.createQuery(jpql);
			Integer count = getSize(id, newIdByOldId, q);
			int lastTotal = nbRows * (page - 1);
			if(count > 0 && count > lastTotal) {
				jpql = "select j from "+wrappedClassName+" o inner join o."+attribute+" j ";
				jpql += "where " + currentFilter;
				if(asc != null && sortAttribute != null) {
					if(asc) {
						jpql += " order by j."+sortAttribute+" asc";
					} else {
						jpql += " order by j."+sortAttribute+" desc";
					}
				}
				q = em.createQuery(jpql);
				int i = 0;
				for(Object idObj : id.getFieldValues()) {
					if(id.isLocal() && newIdByOldId.containsKey(idObj))
						q.setParameter("id"+i, newIdByOldId.get(idObj).getId());
					else
						q.setParameter("id"+i, idObj);
				}
				q.setFirstResult(lastTotal);
				q.setMaxResults(nbRows);

				List<INakedObject> list = q.getResultList();
				if(list != null && !list.isEmpty()) {
					String name = list.get(0).getClass().getName();
					Map<String, Map<Object, Object>> oldIdByNewMap = buildInverseIdMap(newIdByOldId);
					list = deproxyEntities(name, list, true, oldIdByNewMap);
				}
				return new NakedObjectsList(list, count.intValue(), nbRows, currentFilter);
			} else
				return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getCollection "+e.getMessage(), e);
			throw new PersistenceException("Exception in getCollection "+e.getMessage(), e);
		} finally {
			finalizeTx(tc);
		}
	}

	public Map<String, Map<Object, Object>> buildInverseIdMap(
			Map<Object, IdHolder> newIdByOldId) {
		Map<String, Map<Object, Object>> oldIdByNewMap = null;
		if(!newIdByOldId.isEmpty()) {
			oldIdByNewMap = new HashMap<String, Map<Object, Object>>();
			for(Entry<Object, IdHolder> e : newIdByOldId.entrySet()) {
				IdHolder id = e.getValue();
				Map<Object, Object> map = oldIdByNewMap.get(id.getClassName());
				if(map == null) {
					map = new HashMap<Object, Object>();
					oldIdByNewMap.put(id.getClassName(), map);
				}
				map.put(id.getId(), e.getKey());
			}
		}
		return oldIdByNewMap;
	}

	@Override
	public NakedObjectsList getCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attributeName, int page,
			int nbRows, ObjectsToPersist toPersist)  throws PersistenceException {
		return getSortedCollection(className, id, attributeName, null, null, page, nbRows, toPersist);
	}

	@Override
	public NakedObjectsList getSortedCollection(String nakedObjectName, com.hiperf.common.ui.shared.util.Id id,
			String attributeName, String sortAttribute, Boolean asc, int page,
			int rowsPerPage, ObjectsToPersist toPersist) throws PersistenceException {
		EntityManager em= null;
		TransactionContext tc = null;
		Map<com.hiperf.common.ui.shared.util.Id, INakedObject> res= new HashMap<com.hiperf.common.ui.shared.util.Id, INakedObject>();
		try {
			Map<Object, IdHolder> newIdByOldId = new HashMap<Object, IdHolder>();
			tc = createTransactionalContext();
			em = tc.getEm();
			ITransaction tx = tc.getTx();
			tx.begin();
			doPersist(toPersist, null, res,
					newIdByOldId, em, true, null);
			String jpql = "select count(*) from "+nakedObjectName+" o";
			String currentFilter = getIdClause(id, attributeName);
			jpql += " where " + currentFilter;
			Query q = em.createQuery(jpql);
			Long count = getCount(id, newIdByOldId, q);
			int lastTotal = rowsPerPage * (page - 1);
			if(count > 0 && count > lastTotal) {
				jpql = "select o from "+nakedObjectName+" o ";
				jpql += "where " + currentFilter;
				if(asc != null) {
					if(asc) {
						jpql += " order by o."+sortAttribute+" asc";
					} else {
						jpql += " order by o."+sortAttribute+" desc";
					}
				}
				q = em.createQuery(jpql);
				int i = 0;
				for(Object idObj : id.getFieldValues()) {
					if(id.isLocal() && newIdByOldId.containsKey(idObj))
						q.setParameter("id"+i, newIdByOldId.get(idObj).getId());
					else
						q.setParameter("id"+i, idObj);
				}
				q.setFirstResult(lastTotal);
				q.setMaxResults(rowsPerPage);

				List<INakedObject> list = q.getResultList();
				if(list != null && !list.isEmpty()) {
					String name = list.get(0).getClass().getName();
					Map<String, Map<Object, Object>> oldIdByNewIdMap = buildInverseIdMap(newIdByOldId);
					list = deproxyEntities(name, list, true, oldIdByNewIdMap);
				}
				NakedObjectsList l = new NakedObjectsList(list, count.intValue(), rowsPerPage, currentFilter);
				l.setConstantFilterFields(new String[] {attributeName});
				return l;
			} else
				return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getCollection "+e.getMessage(), e);
			throw new PersistenceException("Exception in getCollection "+e.getMessage(), e);
		} finally {
			finalizeTx(tc);
		}
	}

	private Integer getSize(com.hiperf.common.ui.shared.util.Id id,
			Query q) {
		int i = 0;
		for(Object idObj : id.getFieldValues()) {
			q.setParameter("id"+i, idObj);
		}
		return (Integer)q.getResultList().get(0);
	}

	private Long getCount(com.hiperf.common.ui.shared.util.Id id,
			Map<Object, IdHolder> newIdByOldId, Query q) {
		int i = 0;
		for(Object idObj : id.getFieldValues()) {
			if(id.isLocal() && newIdByOldId.containsKey(idObj))
				q.setParameter("id"+i, newIdByOldId.get(idObj).getId());
			else
				q.setParameter("id"+i, idObj);
		}
		Long count = (Long)q.getResultList().get(0);
		return count;
	}

	private Integer getSize(com.hiperf.common.ui.shared.util.Id id,
			Map<Object, IdHolder> newIdByOldId, Query q) {
		int i = 0;
		for(Object idObj : id.getFieldValues()) {
			if(id.isLocal() && newIdByOldId.containsKey(idObj))
				q.setParameter("id"+i, newIdByOldId.get(idObj).getId());
			else
				q.setParameter("id"+i, idObj);
		}
		Integer count = (Integer)q.getResultList().get(0);
		return count;
	}

	private String getIdClause(com.hiperf.common.ui.shared.util.Id id) {
		String currentFilter = "";
		int i = 0;
		for(String name : id.getFieldNames()) {
			currentFilter += "o."+name+" = :id"+i;
			if(i<id.getFieldNames().size() - 1)
				currentFilter += " and ";
		}
		return currentFilter;
	}

	private String getIdClause(com.hiperf.common.ui.shared.util.Id id,
			String attributeName) {
		String currentFilter = "";
		int i = 0;
		for(String name : id.getFieldNames()) {
			currentFilter += "o."+attributeName+"."+name+" = :id"+i;
			if(i<id.getFieldNames().size() - 1)
				currentFilter += " and ";
		}
		return currentFilter;
	}

	public void removeFilter(Long id) throws PersistenceException {
		TransactionContext tc = null;
		try {
			tc = createTransactionalContext();
			EntityManager em = tc.getEm();
			ITransaction tx = tc.getTx();
			Filter f = em.find(Filter.class, id);
			List<FilterValue> values = f.getValues();
			if(values != null && !values.isEmpty()) {
				for(FilterValue fv : values) {
					em.remove(fv);
				}
			}
			values.clear();
			em.remove(f);
			tx.begin();
			tx.commit();
		} catch (Exception e) {
			catchPersistException(tc, e);
			throw new PersistenceException(e.getMessage(), e);
		} finally {
			if(tc != null)
				close(tc);
		}
	}

	private void catchPersistException(TransactionContext tc, Exception e) {
		logger.log(Level.SEVERE, "Exception in persist : "+e.getMessage(), e);
		try {
			if(tc!=null)
				tc.rollback();
		} catch (Exception ee) {}
	}

	@Override
	public Long saveFilter(Filter f, String userName) throws PersistenceException {
		TransactionContext tc = null;
		try {
			tc = createTransactionalContext();
			EntityManager em = tc.getEm();
			ITransaction tx = tc.getTx();
			tx.begin();
			Long id = null;
			Filter orig = null;
			if(f.getId() != null)
				orig = em.find(Filter.class, f.getId());
			if(orig != null) {
				if(orig.getValues() != null && !orig.getValues().isEmpty()) {
					for(FilterValue fv : orig.getValues()) {
						em.remove(fv);
					}
					orig.getValues().clear();
				}
				orig.setName(f.getName());
				orig.setClassName(f.getClassName());
				orig.setViewName(f.getViewName());
				orig.setUserName(userName);
				ArrayList<FilterValue> l = new ArrayList<FilterValue>();
				orig.setValues(l);
				for(FilterValue fv : f.getValues()) {
					fv.setId(null);
					fv.setFilter(orig);
					em.persist(fv);
					l.add(fv);
				}
				if(f.getSortAttribute() != null) {
					orig.setSortAttribute(f.getSortAttribute());
					orig.setSortAsc(f.getSortAsc());
				}

				em.merge(orig);
				id = orig.getId();
			} else {
				f.setId(null);
				f.setCreateUser(userName);
				f.setUserName(userName);
				for(FilterValue fv : f.getValues())
					fv.setId(null);
				em.persist(f);
				id = f.getId();
			}

			tx.commit();
			return id;
		} catch (Exception e) {
			catchPersistException(tc, e);
			throw new PersistenceException(e.getMessage(), e);
		} finally {
			if(tc != null)
				close(tc);
		}
	}

	@Override
	public NakedObjectsList sort(String className, String currentFilter, String attribute, boolean distinct, boolean asc, int page, int nbRows,
			ObjectsToPersist toPersist, Locale locale)
			throws PersistenceException {
		String order = getSortClause(attribute);
		if(!asc)
			order += " DESC";
		return queryData(className, currentFilter, page, nbRows, order, toPersist, locale, distinct);
	}

	public String getSortClause(String attribute) {
		List<String> joins = new ArrayList<String>();
		String[] atts = attribute.split(",");
		String order = " order by ";
		int i = 0;
		for(String att : atts) {
			att = att.trim();
			int j = att.indexOf(".");
			if(j>0) {
				joins.add(att.substring(0, j));
				order += "jjjs"+joins.size()+ att.substring(j);
			} else {
				order += "o." + att;	
			}
			
			if(i<atts.length - 1)
				order += ", ";
			i++;
		}
		if(!joins.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			int j = 1;
			for(String s : joins) {
				sb.append("left outer join o.").append(s).append(" jjjs").append(j).append(" ");
				j++;
			}
			order = sb.toString() + "@" + order;
			
		}
		return order;
	}

	@Override
	public void getExtractedData(HttpServletRequest req,
			HttpServletResponse resp, String className) throws ServletException {
		//col index / Attribute, label
		Map<Integer,Object[]> map = new HashMap<Integer, Object[]>();
		logger.fine("Class = "+className);
		EntityManager em = null;
		try {
			ExcelHelper.fillObjectMap(req, className, map);
			em = getEntityManager();
			StringBuilder jpqlSb = new StringBuilder("select distinct o from ").append(className);
			String filter = null;
			Object[] o = map.get(IConstants.KEY_FILTER);
			String jpql;
			List res = null;
			Query q = null;
			if(o != null) {
				if(o.length == 1)
					filter = (String) o[0];
				jpql = appendWhereClause(filter, jpqlSb.toString());	
				List<Date> dtParams = new ArrayList<Date>();
				jpql = replaceDateParameters(jpql, dtParams);
				q = em.createQuery(jpql);
				if(!dtParams.isEmpty()) {
					for(int i = 0; i<dtParams.size(); i++) {
						q.setParameter("dt"+i, dtParams.get(i));
					}
				}
			} else {
				o = map.get(IConstants.KEY_LAZY);
				if(o != null && o.length == 1 && (Boolean)o[0]) {
					com.hiperf.common.ui.shared.util.Id id = (com.hiperf.common.ui.shared.util.Id) map.get(IConstants.KEY_ID)[0]; 
					String mappedBy = null;
					o = map.get(IConstants.KEY_MAPPED_BY);
					if(o != null && o.length == 1) {
						mappedBy = (String) o[0];
					}
					o = map.get(IConstants.KEY_ATTRIBUTE);
					String att  = null;
					if(o != null && o.length == 1) {
						att = (String) o[0];
					}
					if(mappedBy != null) {
						String currentFilter = getIdClause(id, mappedBy);
						jpql = "select o from "+className+" o where " + currentFilter;
					} else {
						String currentFilter = getIdClause(id);
						jpql = "select j from "+className+" o inner join o."+att+" j where "+currentFilter;
					}
					q = em.createQuery(jpql);
					int i = 0;
					for(Object idObj : id.getFieldValues()) {
						q.setParameter("id"+i, idObj);
						i++;
					}
				} else {
					q = em.createQuery(jpqlSb.append(" o").toString());
				}
			}
			if(q != null) {
				setQueryTimeout(q);
				res = q.getResultList();	
			}
			
			ExcelHelper.sendResponse(resp, className, map, res);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception while querying data for export", e);
			throw new ServletException("Exception while querying data for export", e);
		} finally {
			closeEntityManager(em);
		}
	}

	@Override
	public void saveConfiguration(String viewName, String className, int nbRows, List<HeaderInfo> headers,
			ScreenLabels sl, String connectedUser, LanguageEnum language) throws PersistenceException {
		TransactionContext tc = null;
		try {
			tc = createTransactionalContext();
			EntityManager em = tc.getEm();
			ITransaction tx = tc.getTx();
			tx.begin();
			List<ScreenConfig> l = em.createQuery("select o from ScreenConfig o where o.viewName = :vN and o.className = :clazz and o.createUser = :user")
				.setParameter("vN", viewName)
				.setParameter("clazz", className)
				.setParameter("user", connectedUser)
				.getResultList();
			ScreenConfig sc = null;
			Map<String, ScreenHeaderInfo> headerByAttribute = new HashMap<String, ScreenHeaderInfo>();
			if(l!=null&&!l.isEmpty()) {
				sc = l.get(0);
				if(l.size()>1) {
					for(int i=1; i<l.size(); i++) {
						em.remove(l.get(i));
					}
				}
				for(ScreenHeaderInfo hi : sc.getHeaders()) {
					headerByAttribute.put(hi.getAttribute(), hi);
				}
			} else {
				sc = new ScreenConfig();
				sc.setViewName(viewName);
				sc.setClassName(className);
				sc.setCreateUser(connectedUser);
				em.persist(sc);
			}
			sc.setNbRows(nbRows);

			if(sl != null) {
				boolean found = false;
				if(sc.getLabels() != null && !sc.getLabels().isEmpty()) {
					for(ScreenLabels lb : sc.getLabels()) {
						if(lb.getLanguage().equals(language)) {
							if(sl.getCreateLabel() != null)
								lb.setCreateLabel(sl.getCreateLabel());
							if(sl.getEditLabel() != null)
								lb.setEditLabel(sl.getEditLabel());
							if(sl.getFormLabel() != null)
								lb.setFormLabel(sl.getFormLabel());
							if(sl.getSelectLabel() != null)
								lb.setSelectLabel(sl.getSelectLabel());
							if(sl.getTableLabel() != null)
								lb.setTableLabel(sl.getTableLabel());
							if(sl.getViewLabel() != null)
								lb.setViewLabel(sl.getViewLabel());
							em.merge(lb);
							found = true;
							break;
						}
					}
				}
				if(!found) {
					sl.setScreenConfig(sc);
					em.persist(sl);
				}
			}
			for(HeaderInfo hi : headers) {
				ScreenHeaderInfo shi = headerByAttribute.get(hi.getAttribute());
				if(shi == null) {
					shi = new ScreenHeaderInfo();
					shi.setAttribute(hi.getAttribute());
					shi.setDisplayed(hi.isDisplayed());
					shi.setScreenConfig(sc);
					shi.setIndex(hi.getIndex());
					em.persist(shi);
				} else {
					shi.setDisplayed(hi.isDisplayed());
					shi.setScreenConfig(sc);
					shi.setIndex(hi.getIndex());
					em.merge(shi);
				}
				if(hi.getLabel() != null) {
					boolean found = false;
					List<Label> labels = shi.getLabels();
					if(labels != null && !labels.isEmpty()) {
						Iterator<Label> it = labels.iterator();
						while(it.hasNext()) {
							Label lbl = it.next();
							if(lbl.getLanguage().equals(language)) {
								if(hi.getLabel().length() > 0) {
									lbl.setLabel(hi.getLabel());
									em.merge(lbl);
								} else {
									em.remove(lbl);
									it.remove();
								}
								found = true;
								break;
							}
						}
					}
					if(!found) {
						Label lbl = new Label();
						lbl.setLanguage(language);
						lbl.setLabel(hi.getLabel());
						lbl.setHeader(shi);
						em.persist(lbl);
					}
				}
			}
			tx.commit();
		} catch (Exception e) {
			catchPersistException(tc, e);
			throw new PersistenceException(e.getMessage(), e);
		} finally {
			if(tc != null)
				close(tc);
		}
	}


	@Override
	public Map<String, TableConfig> getScreenConfigurations(
			String connectedUser, LanguageEnum language) throws PersistenceException  {
		EntityManager em= null;
		try {
			em = getEntityManager();
			Map<String, TableConfig> map = new HashMap<String, TableConfig>();
			List<ScreenConfig> l = em.createQuery("select o from ScreenConfig o where o.createUser = :user")
			.setParameter("user", connectedUser)
				.getResultList();
			Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext = new HashMap<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>>();
			if(l!=null&&!l.isEmpty()) {
				for(ScreenConfig sc : l) {
					addHeader(language, map, sc, em, deproxyContext);
				}
			}
			String hql = "select o from ScreenConfig o where o.defaultConfig = true";
			Query q = em.createQuery(hql);
			l = q.getResultList();
			if(l!=null&&!l.isEmpty()) {
				for(ScreenConfig sc : l) {
					String key = getViewKey(sc);
					if(!map.containsKey(key)) {
						addHeader(language, map, sc, em, deproxyContext);
					}
				}
			}
			return map;
		} catch(javax.persistence.PersistenceException pe) {
			Throwable cause = pe.getCause();
			if(cause != null && cause instanceof SQLGrammarException) {
				logger.log(Level.SEVERE, "SQLGrammarException "+cause.getMessage(), cause);
				throw new PersistenceException("Exception in getScreenConfigurations "+cause.getMessage(), cause, true);
			} else {
				logger.log(Level.SEVERE, "Exception in getScreenConfigurations "+pe.getMessage(), pe);
				throw new PersistenceException("Exception in getScreenConfigurations "+pe.getMessage(), pe);
			}
		} 
		catch(SQLGrammarException e) {
			logger.log(Level.SEVERE, "SQLGrammarException "+e.getMessage(), e);
			throw new PersistenceException("Exception in getScreenConfigurations "+e.getMessage(), e, true);
		} 
		catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getScreenConfigurations "+e.getMessage(), e);
			throw new PersistenceException("Exception in getScreenConfigurations "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}

	@Override
	public void addHeader(LanguageEnum language, Map<String, TableConfig> map,
			ScreenConfig sc, EntityManager em, Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext) throws PersistenceException {
		TableConfig tc = getTableConfig(language, sc, em, deproxyContext);
		String key = getViewKey(sc);
		map.put(key, tc);
	}

	@Override
	public TableConfig getTableConfig(LanguageEnum language, ScreenConfig sc, EntityManager em, Map<String, Map<com.hiperf.common.ui.shared.util.Id, INakedObject>> deproxyContext) throws PersistenceException {
		List<HeaderInfo> list = null;
		List<ScreenHeaderInfo> headers = sc.getHeaders();
		if(headers != null && !headers.isEmpty()) {
			list = new ArrayList<HeaderInfo>();
			Class cl = null;
			try {
				cl = Class.forName(sc.getClassName());
			
				for(ScreenHeaderInfo shi : headers) {
					String att = shi.getAttribute();
					try {
						cl.getDeclaredField(att);
					} catch (NoSuchFieldException e) {
						logger.info("Field "+att+" not found");
						String name = att.substring(0, 1).toUpperCase() + att.substring(1);
						String meth = "get" + name;
						try {
							cl.getMethod(meth);
						} catch (NoSuchMethodException e1) {
							logger.info("Method "+meth+" not found");
							 meth = "is" + name;
							 try {
								cl.getMethod(meth);
							} catch (NoSuchMethodException e2) {
								logger.log(Level.SEVERE, "Field "+att+" not Found !!!", e2);
								continue;
							}
						}
					}
						String label = null;
						List<Label> labels = shi.getLabels();
						if(labels != null && !labels.isEmpty()) {
							for(Label lbl : labels) {
								if(lbl.getLanguage().equals(language)) {
									label = lbl.getLabel();
									break;
								}
							}
						}
						list.add(new HeaderInfo(att, label, shi.getIndex(), shi.isDisplayed(), shi.getEditable()));
					
				}
			} catch (ClassNotFoundException | SecurityException e) {
				logger.log(Level.SEVERE, "Field not Found !!!", e);
				throw new PersistenceException(e.getMessage());
			}
		}
		List<ScreenLabels> lbls = sc.getLabels();
		ScreenLabels scLbls = null;
		if(lbls != null && !lbls.isEmpty()) {
			for(ScreenLabels lbl : lbls) {
				if(lbl.getLanguage().equals(language)) {
					scLbls = lbl;
					break;
				}
			}
		}
		scLbls = (ScreenLabels) deproxyNakedObject(scLbls, em, deproxyContext);
		return new TableConfig(sc.getNbRows(), scLbls, list);
	}

	@Override
	public String getViewKey(ScreenConfig sc) {
		return sc.getViewName() == null ? sc.getClassName() : sc.getViewName() + "#" + sc.getClassName();
	}


	/*public TableConfig getScreenConfiguration(String className,
			String connectedUser) throws PersistenceException {
		EntityManager em= null;
		try {
			em = getEntityManager();
			List<HeaderInfo> list = new ArrayList<HeaderInfo>();
			List<ScreenConfig> l = em.createQuery("select o from ScreenConfig o where o.className = :clazz and o.userName = :user")
			.setParameter("clazz", className)
			.setParameter("user", connectedUser)
				.getResultList();
			if(l!=null&&!l.isEmpty()) {
				ScreenConfig sc = l.get(0);
				return getTableConfig(get, sc);
			}
			return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in getScreenConfigurations "+e.getMessage(), e);
			throw new PersistenceException("Exception in getScreenConfigurations "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}*/


	@Override
	public void downloadFile(HttpServletResponse resp, String fileClass, String fileNameField,
			String fileStorageField, String fileId) throws PersistenceException {
		String id = getIdFieldFromFileStorageClass(fileClass);
		EntityManager em= null;
		try {
			em = getEntityManager();
			List<Object[]> l = em.createQuery("select o."+fileNameField+", o."+fileStorageField+" from "+fileClass+" o where o."+id+" = "+fileId).getResultList();
			if(l != null && !l.isEmpty()) {
				Object[] o = l.get(0);
				String name = (String)o[0];
				byte[] b = (byte[])o[1];
				if(b != null && b.length > 0) {
					sendFile(resp, name, b);					
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in downloadFile "+e.getMessage(), e);
			throw new PersistenceException("Exception in downloadFile "+e.getMessage(), e);
		} finally {
			closeEntityManager(em);
		}
	}

	@Override
	public void sendFile(HttpServletResponse resp, String name, byte[] b) throws IOException, ServletException {
		IOUtils.sendAttachment(resp, b, name);
	}


	private String getIdFieldFromFileStorageClass(String fileClass)
			throws PersistenceException {
		Set<PropertyDescriptor> ids = idsByClassName.get(fileClass);
		if(ids == null) {
			String msg = "Exception in downloadFile : class "+fileClass+" is not a persistent entity";
			logger.log(Level.SEVERE, msg);
			throw new PersistenceException(msg);
		}
		if(ids.size() != 1)
			throw new PersistenceException("File storage class "+fileClass+" must have one field as Id");
		String id = ids.iterator().next().getName();
		return id;
	}


	@Override
	public Object saveFile(String fileClass, String fileNameField,
			String fileStorageField, String fileName, FileItem item)throws PersistenceException {
		getIdFieldFromFileStorageClass(fileClass);
		TransactionContext tc = null;
		try {
			tc = createTransactionalContext();
			EntityManager em = tc.getEm();
			ITransaction tx = tc.getTx();
			tx.begin();
			Class clazz = Class.forName(fileClass);
			Object o = clazz.newInstance();
			for(PropertyDescriptor pd : propertyDescriptorsByClassName.get(fileClass)) {
				if(fileNameField.equals(pd.getName())) {
					pd.getWriteMethod().invoke(o, fileName);
				} else if(fileStorageField.equals(pd.getName())) {
					pd.getWriteMethod().invoke(o, item.get());
				}
			}
			em.persist(o);
			tx.commit();
			return idsByClassName.get(fileClass).iterator().next().getReadMethod().invoke(o, new Object[0]);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in saveFile : "+e.getMessage(), e);
			try {
				if(tc!=null)
					tc.rollback();
	        } catch (Exception ee) {}
			throw new PersistenceException(e.getMessage(), e);
		} finally {
			if(tc != null)
				close(tc);
		}

	}


	@Override
	public String replaceFile(String fileClass, String fileNameField,
			String fileStorageField, String fileName, FileItem fileItem,
			String existingId) throws PersistenceException {
		String idField = getIdFieldFromFileStorageClass(fileClass);
		boolean error = false;
		TransactionContext tc = null;
		Object id = null;
		PropertyDescriptor[] pds = propertyDescriptorsByClassName.get(fileClass);

		try {
			id = getFileId(existingId, idField, pds);
			tc = createTransactionalContext();
			EntityManager em = tc.getEm();
			ITransaction tx = tc.getTx();
			tx.begin();
			Object o;
			Class<?> clazz = Class.forName(fileClass);
			o = em.find(clazz, id);
			for(PropertyDescriptor pd : pds) {
				if(fileNameField.equals(pd.getName())) {
					pd.getWriteMethod().invoke(o, fileName);
				} else if(fileStorageField.equals(pd.getName())) {
					pd.getWriteMethod().invoke(o, fileItem.get());
				}
			}
			em.merge(o);

			tx.commit();
			//idsByClassName.get(fileClass).iterator().next().getReadMethod().invoke(o, new Object[0]);
		} catch (Exception e) {
			error = true;
			logger.log(Level.SEVERE, "Exception in saveFile : "+e.getMessage(), e);
			try {
				if(tc!=null)
					tc.rollback();
	        } catch (Exception ee) {}
		} finally {
			if(tc != null) {
				if(!error)
					close(tc);
				else
					tc.close();
			}
		}
		if(error) {
			try {
				tc = createTransactionalContext();
				EntityManager em = tc.getEm();
				ITransaction tx = tc.getTx();
				tx.begin();
				Class<?> clazz = Class.forName(fileClass);
				em.createQuery(
						"delete from " + fileClass + " o where o." + idField
								+ " = :id").setParameter("id", id)
						.executeUpdate();
				Object newDoc = clazz.newInstance();
				for (PropertyDescriptor pd : pds) {
					if (fileNameField.equals(pd.getName())) {
						pd.getWriteMethod().invoke(newDoc, fileName);
					} else if (fileStorageField.equals(pd.getName())) {
						pd.getWriteMethod().invoke(newDoc, fileItem.get());
					}
				}
				em.persist(newDoc);
				tx.commit();
				for (PropertyDescriptor pd : pds) {
					if (idField.equals(pd.getName())) {
						existingId = pd.getReadMethod()
								.invoke(newDoc, new String[0]).toString();
						break;
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE,
						"Exception in saveFile : " + e.getMessage(), e);
				try {
					if (tc != null)
						tc.rollback();
				} catch (Exception ee) {
				}
				throw new PersistenceException(e.getMessage(), e);
			} finally {
				if (tc != null)
					close(tc);
			}
		}

		return existingId;
	}


	@Override
	public Object getFileId(String existingId, String fileClass) throws PersistenceException {
		String idField = getIdFieldFromFileStorageClass(fileClass);
		try {
			return getFileId(existingId, idField, propertyDescriptorsByClassName.get(fileClass));
		} catch (IllegalAccessException e) {
			throw new PersistenceException(e);
		} catch (InvocationTargetException e) {
			throw new PersistenceException(e);
		} catch (NoSuchMethodException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public Object getFileId(String existingId, String idField, PropertyDescriptor[] pds) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			PersistenceException {
		Object id = null;
		for(PropertyDescriptor pd : pds) {
			if(pd.getName().equals(idField)) {
				Class<?> propertyType = pd.getPropertyType();
				if(Number.class.isAssignableFrom(propertyType))
					id = propertyType.getMethod("valueOf", String.class).invoke(propertyType, existingId);
				else if(propertyType.equals(String.class))
					id = existingId;
				else
					throw new PersistenceException("Type "+propertyType+" not supported");
			}
		}
		return id;
	}


	@Override
	public String checkExists(String className, String attribute, String value, Locale locale) throws PersistenceException
	  {
	    EntityManager em = null;
	    try {
	      em = getEntityManager();
	      boolean isString;
	      if (value != null) {
	        PropertyDescriptor[] pds = (PropertyDescriptor[])propertyDescriptorsByClassName.get(className);
	        boolean isNumber = false;
	        isString = false;
	        for (PropertyDescriptor pd  : pds) {
	          if (pd.getName().equals(attribute)) {
	            Class propertyType = pd.getPropertyType();
	            if ((propertyType.isPrimitive()) || (Number.class.isAssignableFrom(propertyType))) {
	              isNumber = true; break; }
	            if (!(String.class.isAssignableFrom(propertyType))) break;
	            isString = true; break;
	          }
	        }

	        StringBuilder q = getCheckExistsQuery(className, attribute, value, isNumber, isString);

	        Query q1 = em.createQuery(q.toString());
	        if(isString)
	        	q1.setParameter(1, value.toLowerCase());
			List l = q1.getResultList();
	        ResourceBundle ressource;
	        if ((l != null) && (l.size() > 0)) {
	          return getUniqueErrorMsg(attribute, value, locale);
	        }

	        return null;
	      }

	      List l = em.createQuery("select 1 from " + className + " o where o." + attribute + " is null").getResultList();
	      ResourceBundle ressource;
	      if ((l != null) && (l.size() > 0)) {
	    	return getUniqueErrorMsg(attribute, value, locale);	        
	      }

	      return null;
	    }
	    catch (Exception e) {
	      logger.log(Level.SEVERE, "Exception in checkExists " + e.getMessage(), e);
	      throw new PersistenceException("Exception in checkExists " + e.getMessage(), e);
	    } finally {
	      closeEntityManager(em);
	    }
	  }

	public String getMessage(String key, Locale l) {
		ResourceBundle ressource = ResourceBundle.getBundle(SERVER_MESSAGES, l, PersistenceHelper.class.getClassLoader(), new UTF8Control());
		return ressource.getString(key);
	}
	
	private String getUniqueErrorMsg(String attribute, String value,
			Locale locale) {
		ResourceBundle ressource = ResourceBundle.getBundle(SERVER_MESSAGES, locale, PersistenceHelper.class.getClassLoader(), new UTF8Control());
		String lbl = null;
		try {
			lbl = ressource.getString(attribute);
		} catch (Exception e) {}
		  if(lbl == null)
			  lbl = attribute;
		  return MessageFormat.format(ressource.getString("uniqueConstraintViolated").replaceAll("'", "''"), new Object[] { value, lbl });
	}

	  private StringBuilder getCheckExistsQuery(String className, String attribute, String value, boolean isNumber, boolean isString)
	  {
	    StringBuilder q = new StringBuilder("select 1 from " + className + " o where ");
	    if (isString) {
	    	q.append("lower(o.").append(attribute).append(") = ?1");
	    }
	    else if (isNumber)
	      q.append("o.").append(attribute).append(" = ").append(value);
	    else
	      q.append("o.").append(attribute).append(" = '").append( StringUtils.replace(value, "'", "''")).append("'");
	    return q;
	  }

	  @Override
	public String checkExists(String className, com.hiperf.common.ui.shared.util.Id id, String attribute, String value, Locale locale)
	    throws PersistenceException
	  {
	    EntityManager em = null;
	    try {
	      Set<PropertyDescriptor> ids = idsByClassName.get(className);
	      PropertyDescriptor[] idsArr = (PropertyDescriptor[])ids.toArray(new PropertyDescriptor[0]);
	      em = getEntityManager();
	      int i;
	      if (value != null) {
	        boolean isNumber = false;
	        boolean isString = false;
	        PropertyDescriptor[] pds = (PropertyDescriptor[])propertyDescriptorsByClassName.get(className);
	        Class<?> propertyType;
	        for (PropertyDescriptor pd : pds) {
	          if (pd.getName().equals(attribute)) {
	            propertyType = pd.getPropertyType();
	            if ((propertyType.isPrimitive()) || (Number.class.isAssignableFrom(propertyType))) {
	              isNumber = true; break; }
	            if (!(String.class.isAssignableFrom(propertyType))) break;
	            isString = true; break;
	          }
	        }

	        StringBuilder q = getCheckExistsQuery(className, attribute, value, isNumber, isString);

	        q.append(" and o.");
	        i = 0;
	        for (String att : id.getFieldNames())
	        {
	          q.append(att);
	          q.append(" != ");
	          if (StorageService.isNumber(att, idsArr)) {
	            q.append(id.getFieldValues().get(i));
	          } else {
	            q.append(" '").append(StringUtils.replace(id.getFieldValues().get(i).toString(), "'", "''")).append("'");
	          }
	          ++i;
	          if (i < id.getFieldNames().size())
	            q.append(" and o.");
	        }
	        Query q1 = em.createQuery(q.toString());
	        if(isString)
	        	q1.setParameter(1,  value.toLowerCase());
			List l = q1.getResultList();
	        if ((l != null) && (l.size() > 0)) {
	        	return getUniqueErrorMsg(attribute, value, locale);	          
	        }

	        return null;
	      }
	      
	      StringBuilder q = new StringBuilder("select 1 from " + className + " o where o." + attribute + " is null and o.");
	      i = 0;
	      for (String att : id.getFieldNames()) {
	        q.append(att);
	        q.append(" != ");
	        if (StorageService.isNumber(att, idsArr)) {
	          q.append(id.getFieldValues().get(i));
	        } else {
	          q.append(" '");
	          q.append(StringUtils.replace(id.getFieldValues().get(i).toString(), "'", "''"));
	          q.append("'");
	        }
	        ++i;
	        if (i < id.getFieldNames().size())
	          q.append(" and o.");
	      }
	      List l = em.createQuery(q.toString()).getResultList();
	      if ((l != null) && (l.size() > 0)) {
	    	  return getUniqueErrorMsg(attribute, value, locale);	       
	      }

	      return null;
	    }
	    catch (Exception e) {
	      logger.log(Level.SEVERE, "Exception in checkExists " + e.getMessage(), e);
	      throw new PersistenceException("Exception in checkExists " + e.getMessage(), e);
	    } finally {
	      closeEntityManager(em);
	    }
	  }

	@Override
	public CollectionInfo getLazyCollection(String className,
			com.hiperf.common.ui.shared.util.Id id, String attribute) throws PersistenceException {
		EntityManager em = null;
	    try {
	    	em = getEntityManager();
	    	return getCollectionInfo(em, id, className, attribute);
	    }
	    catch (Exception e) {
	      logger.log(Level.SEVERE, "Exception in getLazyCollection " + e.getMessage(), e);
	      throw new PersistenceException("Exception in getLazyCollection " + e.getMessage(), e);
	    } finally {
	      closeEntityManager(em);
	    }
	}

	public Collection<INakedObject> getByAttribute(String className,
			String att, Object value, EntityManager em) {
		return em.createQuery("select o from "+className+" o where o."+att+" = :val").setParameter("val", value).getResultList();
	}

	@Override
	public boolean hasManyToOne(String className) {
		Set<PropertyDescriptor> set = eagerObjectsByClassName.get(className);
		if(set == null || set.isEmpty()) {
			set = lazysByClassName.get(className);
			return set != null && !set.isEmpty();
		}
		return true;

	}

	@Override
	public void shutdown() {
		try {
			emf.close();
			instance = null;
			emf = null;
			emByThread = null;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception in shutdown", e);
		}		
	}

	@Override
	public void close(TransactionContext tc) {
		emByThread.remove();
		tc.close();
	}
	
	@Override
	public void close(EntityManager em) {
		emByThread.remove();
		em.close();
	}

}
