package com.hiperf.common.ui.server.storage.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.server.storage.IImportService;
import com.hiperf.common.ui.server.storage.IImportValidator;
import com.hiperf.common.ui.server.storage.IStorageService;
import com.hiperf.common.ui.server.storage.ImportValidationException;
import com.hiperf.common.ui.server.tx.ITransaction;
import com.hiperf.common.ui.server.tx.TransactionContext;
import com.hiperf.common.ui.server.util.IOUtils;
import com.hiperf.common.ui.shared.annotation.UIAttribute;
import com.hiperf.common.ui.shared.annotation.UIClass;
import com.hiperf.common.ui.shared.util.Id;

public class ImportService implements IImportService {

	private static final Logger logger = Logger.getLogger(ImportService.class.getName());

	private static final ImportService INSTANCE = new ImportService();

	private ImportService() {
	}

	public static IImportService getInstance() {
		return INSTANCE;
	}

	public void getImportTemplate(String className, Map<Integer, String> attsMap, Map<Integer, String> labelsMap,
			HttpServletResponse resp, Locale locale) throws Exception {
		Class<?> clazz = Class.forName(className);
		BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
		PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
		Workbook wb = new HSSFWorkbook();
		CreationHelper factory = wb.getCreationHelper();
		Sheet sheet = wb.createSheet(getLabel(clazz));
		Drawing drawing = sheet.createDrawingPatriarch();
		Row header = sheet.createRow(0);
		List<Class> allClasses = new ArrayList<Class>();
		allClasses.add(clazz);
		for (int i = 0; i < labelsMap.size(); i++) {
			Cell cell = header.createCell(i);
			cell.setCellValue(factory.createRichTextString(labelsMap.get(i)));
			PropertyDescriptor pd = getDescriptor(attsMap.get(i), pds);
			UIAttribute uiAtt = pd.getReadMethod().getAnnotation(UIAttribute.class);
			if((!uiAtt.hidden() || uiAtt.forceImport()) && uiAtt.importable()) {
				Class realTargetClass = getRealTargetClass(clazz, pd);
				addComment(factory, drawing, cell, realTargetClass, locale, uiAtt.importAttribute(), uiAtt.importCreateMissing());

				if(uiAtt.importCreateMissing()) {
					createSheet(wb, factory, realTargetClass, uiAtt.importAttribute(), allClasses, locale);
				}				
			}
		}
		File f = File.createTempFile("Import_template_", ".xls");
		FileOutputStream fileOut = new FileOutputStream(f);
		// BufferedOutputStream bos = new BufferedOutputStream(fileOut,
		// IOUtils.BUFFER_SIZE);
		try {
			wb.write(fileOut);
		} finally {
			fileOut.flush();
			fileOut.close();
		}

		InputStream input = new FileInputStream(f);
		String fileName = f.getName();
		IOUtils.sendAttachment(resp, input, fileName);
	}

	private Class getRealTargetClass(Class<?> clazz, PropertyDescriptor pd) {
		Class target = null;
		if(Collection.class.isAssignableFrom(pd.getPropertyType())) {
			target = getTargetEntity(pd, clazz);
		} else
			target = pd.getPropertyType();
		return target;
	}

	private void addComment(CreationHelper factory, Drawing drawing, Cell cell,
			Class<?> type, Locale locale, String importAttribute, boolean createDataIfMissing) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Comment comment = null;
		if(type.isEnum()) {
			ClientAnchor anchor = factory.createClientAnchor();
		    anchor.setCol1(cell.getColumnIndex());
		    anchor.setCol2(cell.getColumnIndex()+5);
		    anchor.setRow1(0);
		    anchor.setRow2(5);

		    // Create the comment and set the text+author
		    comment = drawing.createCellComment(anchor);
		    StringBuilder sb = new StringBuilder();
		    int k = 1;
		    Object[] consts=type.getEnumConstants();
		    Method m = consts[0].getClass().getMethod("name");
			for(Object o : consts) {
				sb.append(m.invoke(o, new Object[0]));
		    	if(k<consts.length)
		    		sb.append(", ");
		    	k++;
		    }
		    RichTextString str = factory.createRichTextString(MessageFormat.format(PersistenceHelper.getInstance().getMessage("authValues", locale), sb.toString()));
		    comment.setString(str);
		    comment.setAuthor("PCM");
		} else if(Date.class.isAssignableFrom(type)) {
			ClientAnchor anchor = factory.createClientAnchor();
		    anchor.setCol1(cell.getColumnIndex());
		    anchor.setCol2(cell.getColumnIndex()+5);
		    anchor.setRow1(0);
		    anchor.setRow2(5);

		    // Create the comment and set the text+author
		    comment = drawing.createCellComment(anchor);
		    DateFormat df;
		    if(locale != null) {
				df = DateFormat.getDateTimeInstance(
						DateFormat.SHORT,
						DateFormat.SHORT, locale);
			} else
				df = DateFormat.getDateTimeInstance(
						DateFormat.SHORT,
						DateFormat.SHORT);
		    RichTextString str = factory.createRichTextString(MessageFormat.format(PersistenceHelper.getInstance().getMessage("excelDates", locale), df.format(new Date())));
		    comment.setString(str);
		    comment.setAuthor("PCM");
		} else if(int.class.equals(type) || Integer.class.equals(type) ||
				long.class.equals(type) || Long.class.equals(type)) {
			ClientAnchor anchor = factory.createClientAnchor();
		    anchor.setCol1(cell.getColumnIndex());
		    anchor.setCol2(cell.getColumnIndex()+5);
		    anchor.setRow1(0);
		    anchor.setRow2(5);

		    // Create the comment and set the text+author
		    comment = drawing.createCellComment(anchor);
		    RichTextString str = factory.createRichTextString(PersistenceHelper.getInstance().getMessage("intValues", locale));
		    comment.setString(str);
		    comment.setAuthor("PCM");
		} else if(boolean.class.equals(type) || Boolean.class.equals(type)) {
			ClientAnchor anchor = factory.createClientAnchor();
		    anchor.setCol1(cell.getColumnIndex());
		    anchor.setCol2(cell.getColumnIndex()+5);
		    anchor.setRow1(0);
		    anchor.setRow2(5);

		    // Create the comment and set the text+author
		    comment = drawing.createCellComment(anchor);
		    RichTextString str = factory.createRichTextString(PersistenceHelper.getInstance().getMessage("boolValues", locale));
		    comment.setString(str);
		    comment.setAuthor("PCM");
		} else if(importAttribute != null && importAttribute.length() > 0) {
			ClientAnchor anchor = factory.createClientAnchor();
		    anchor.setCol1(cell.getColumnIndex());
		    anchor.setCol2(cell.getColumnIndex()+5);
		    anchor.setRow1(0);
		    anchor.setRow2(5);

		    // Create the comment and set the text+author
		    comment = drawing.createCellComment(anchor);
		    String name = getLabel(type);
			String msg = MessageFormat.format(PersistenceHelper.getInstance().getMessage("refLink", locale), name, importAttribute);
		    if(createDataIfMissing) {
		    	msg += "\n"+MessageFormat.format(PersistenceHelper.getInstance().getMessage("createMissing", locale), name);
		    }
			RichTextString str = factory.createRichTextString(msg);

		    comment.setString(str);
		    comment.setAuthor("PCM");
		}
		if(comment != null)
			cell.setCellComment(comment);
	}

	private void createSheet(Workbook wb, CreationHelper factory, Class<?> clazz,
			String importAttribute, List<Class> allClasses, Locale locale) throws IntrospectionException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(!allClasses.contains(clazz)) {
			allClasses.add(clazz);
			Sheet sheet = wb.createSheet(getLabel(clazz));
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
			Drawing drawing = sheet.createDrawingPatriarch();
			Row header = sheet.createRow(0);
			Cell cell = header.createCell(0);
			cell.setCellValue(importAttribute);
			int i = 1;
			for(PropertyDescriptor pd : pds) {
				Method readMethod = pd.getReadMethod();
				Class<?> type = pd.getPropertyType();
				if(isImportable(type, pd) && readMethod != null && readMethod.isAnnotationPresent(UIAttribute.class)) {
					if(!importAttribute.equals(pd.getName())) {
						UIAttribute uiAtt = readMethod.getAnnotation(UIAttribute.class);
						if(!uiAtt.hidden() && uiAtt.importable()) {
							Cell c = header.createCell(i);
							c.setCellValue(pd.getName());
							addComment(factory, drawing, c, type, locale, uiAtt.importAttribute(), uiAtt.importCreateMissing());
							if(uiAtt.importCreateMissing()) {
								createSheet(wb, factory, pd.getPropertyType(), uiAtt.importAttribute(), allClasses, locale);
							}
							i++;							
						}
					}
				}
			}
		}

	}

	private boolean isImportable(Class<?> type, PropertyDescriptor pd) {
		UIAttribute uiAtt = null;
		if(pd.getReadMethod() != null)
			uiAtt = pd.getReadMethod().getAnnotation(UIAttribute.class);
		if(uiAtt != null && uiAtt.display() && uiAtt.importable() && (type.isPrimitive() ||
				Number.class.isAssignableFrom(type) ||
				boolean.class.equals(type) ||
				Boolean.class.equals(type) ||
				String.class.equals(type) ||
				Date.class.isAssignableFrom(type)))
			return true;
		Class realTargetClass = getRealTargetClass(type, pd);
		if(realTargetClass != null) {
			UIClass uiClass = (UIClass) realTargetClass.getAnnotation(UIClass.class);
			if(uiClass != null && uiClass.importable()) {
				return uiAtt.importAttribute() != null && uiAtt.importAttribute().length() > 0;
			}
		}
		return false;
	}

	private String getLabel(Class<?> clazz) {
		return clazz.getSimpleName();
	}

	private static PropertyDescriptor getDescriptor(String att,
			PropertyDescriptor[] pds) {
		for (PropertyDescriptor pd : pds) {
			if (pd.getName().equals(att)) {
				return pd;
			}
		}
		return null;
	}

	@Override
	public void upload(String className, Map<Integer, String> attsMap,
			Map<Integer, String> labelsMap,
			Map<Integer, String> importAttributes, FileItem fileItem, 
			Locale locale, String userName, HttpServletRequest req)
			throws Exception {
		Class<?> clazz = Class.forName(className);
		BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
		PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
		Workbook wb = new HSSFWorkbook(fileItem.getInputStream());
		Map<Integer, PropertyDescriptor> pdMap = new HashMap<Integer, PropertyDescriptor>();
		IStorageService storeSrv = StorageService.getInstance();
		EntityManager em = null;
		TransactionContext tc = null;
		try {
			tc = storeSrv.createTransactionalContext();
            em = tc.getEm();
            ITransaction tx = tc.getTx();
            tx.begin();
            String name = getLabel(clazz);
			Sheet sheet = wb.getSheet(name);
            if(sheet == null)
            	throw new Exception("No sheet with name "+name+" found in Workbook. Please download Excel upload template file");
            ObjectsToAdd l = new ObjectsToAdd();
			extractRows(wb, className, attsMap, locale, clazz, pds, pdMap,
					em, sheet, l, false, false);
			if(!l.isEmpty()) {
				List<INakedObject> allObjects = l.getAllObjects();
				if(!allObjects.isEmpty()) {
					List<INakedObject> toAddFirst = new ArrayList<INakedObject>();
					Iterator<INakedObject> it = allObjects.iterator();
					while(it.hasNext()) {
						INakedObject o = it.next();
						if(!PersistenceHelper.getInstance().hasManyToOne(o.getClass().getName())) {
							toAddFirst.add(o);
							it.remove();
						}
					}
					allObjects.addAll(0, toAddFirst);
				}
				while(!allObjects.isEmpty()) {
					allObjects = validateAndPersist(em, allObjects, req);
				}

				tx.commit();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in persist : "+e.getMessage(), e);
			try {
				if(tc!=null)
					tc.rollback();
	        } catch (Exception ee) {}
	        throw e;
		} finally {
			if(tc != null)
				storeSrv.close(tc);
		}
	}


	public List<INakedObject> validateAndPersist(EntityManager em,
			List<INakedObject> l, HttpServletRequest req) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			ImportValidationException {
		List<INakedObject> toAdd = new ArrayList<INakedObject>();
		List<INakedObject> ctx = new ArrayList<INakedObject>();
		//(Class<? extends IImportValidator>) Class.forName(importValidator).newIntance()
		Map<String, IImportValidator> impMap =  new HashMap<String, IImportValidator>();
		for(INakedObject o : l) {
			UIClass uiClass = o.getClass().getAnnotation(UIClass.class);
			if(uiClass != null) {
				String importValidator = uiClass.importValidator();
				if(importValidator != null && importValidator.length() > 0) {
					IImportValidator validator = impMap.get(importValidator);
					if(validator == null) {
						validator = ((Class<? extends IImportValidator>) Class.forName(importValidator)).newInstance();
						impMap.put(importValidator, validator);
					}
					List valRes = validator.validate(o, ctx, em, req);
					if(valRes != null  && !valRes.isEmpty())
						toAdd.addAll(valRes);
				}
			}
			Id id;
			try {
				id = PersistenceHelper.getInstance().getId(o);
			} catch (InvocationTargetException e) {
				throw new ImportValidationException(e);
			}
			if(id == null || id.getFieldValues().isEmpty() || id.getFieldValues().get(0) == null)
				em.persist(o);
		}
		return toAdd;
	}

	private void extractRows(Workbook wb, String className, Map<Integer, String> attsMap,
			Locale locale, Class<?> clazz, PropertyDescriptor[] pds,
			Map<Integer, PropertyDescriptor> pdMap, EntityManager em,
			Sheet sheet, ObjectsToAdd l, boolean insertFirst, boolean insertLast) throws InstantiationException,
			IllegalAccessException, InvocationTargetException, Exception,
			IntrospectionException, PersistenceException {
		UIClass uiClass = clazz.getAnnotation(UIClass.class);
		if(uiClass != null && uiClass.importable()) {
			int i = 1;
			while (true) {
				Row row = sheet.getRow(i);
				if (row == null)
					break;
				boolean notNull = false;
				INakedObject no = (INakedObject) clazz.newInstance();
				for (int j = 0; j < attsMap.size(); j++) {
					Cell cell = row.getCell(j);
					String v = null;
					if (cell != null) {
						PropertyDescriptor pd = pdMap.get(j);
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							v = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_NUMERIC:
							v = Double.toString(cell.getNumericCellValue());
							break;
						default:
							break;
						}
						if (v != null && v.length() > 0) {
							if(!l.contains(no)) {
								if(insertFirst)
									l.getFirstList().add(no);
								else if(insertLast)
									l.getLastList().add(no);
								else
									l.getMainList().add(no);
							}
							notNull = true;
							if (pd == null) {
								pd = getDescriptor(attsMap.get(j), pds);
								pdMap.put(j, pd);
							}
							if (pd != null) {
								Method writeMethod = pd.getWriteMethod();
								Class type = pd.getPropertyType();
								fillNakedObject(wb, className, locale, em, no,
										cell, v, pd, writeMethod, type, l);

							}

						}
					}
				}
				if(!notNull)
					break;
				i++;
			}
		} else {
			logger.warning("Class "+className+" is not importable !");
		}
	}

	private void fillNakedObject(Workbook wb, String className, Locale locale,
			EntityManager em, INakedObject no, Cell cell, String v,
			PropertyDescriptor pd, Method writeMethod, Class type, ObjectsToAdd l)
			throws IllegalAccessException, InvocationTargetException,
			Exception, IntrospectionException, PersistenceException {
		if (type.equals(String.class))
			writeMethod.invoke(no, v);
		else if (type.equals(Long.class)
				|| type.equals(long.class))
			writeMethod.invoke(no, Long.valueOf(v));
		else if (type.equals(Double.class)
				|| type.equals(double.class))
			writeMethod.invoke(no,
					Double.valueOf(v));
		else if (type.equals(Integer.class)
				|| type.equals(int.class))
			writeMethod.invoke(no,
					Integer.valueOf(v));
		else if (type.equals(Short.class)
				|| type.equals(short.class))
			writeMethod
					.invoke(no, Short.valueOf(v));
		else if (type.equals(Float.class)
				|| type.equals(float.class))
			writeMethod
					.invoke(no, Float.valueOf(v));
		else if (type.equals(Byte.class)
				|| type.equals(byte.class))
			writeMethod.invoke(no, Byte.valueOf(v));
		else if(type.equals(Date.class)) {
			DateFormat df = null;
			try {
				if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
					writeMethod.invoke(no, cell.getDateCellValue());
				else {
					if(locale != null) {
						df = DateFormat.getDateTimeInstance(
								DateFormat.SHORT,
								DateFormat.SHORT, locale);
					} else
						df = DateFormat.getDateTimeInstance(
								DateFormat.SHORT,
								DateFormat.SHORT);
					writeMethod.invoke(no, df.parse(v));
				}
			} catch (Exception e) {
				String msg = "Problem while setting date on attribute "+pd.getName()+".";
				if(df != null)
					msg += " Date format : "+df.format(new Date());
				throw new Exception(msg);
			}
		}
		else if (INakedObject.class
				.isAssignableFrom(type)) {
			importLinkedNakedObject(wb, className, locale, em, no, v, pd,
					writeMethod, type, l);

		} else if(type.isEnum()) {
			for(Object o : type.getEnumConstants()) {
				Method m = o.getClass().getMethod("name");
				if(v.equals((String)m.invoke(o, new Object[0]))) {
					writeMethod.invoke(no, Enum.valueOf(type, v));
					break;
				}
			}
		} else if(Collection.class.isAssignableFrom(type)) {
			Class<? extends INakedObject> clazz = no.getClass();
			Class targetClass = getTargetEntity(pd, clazz);
			if(targetClass != null) {
				Collection coll = null;
				if(Set.class.isAssignableFrom(type))
					coll = new HashSet();
				else
					coll = new ArrayList();
				writeMethod.invoke(no, coll);
				importLinkedCollection(wb, className, locale, em, v, pd,
						coll, l, targetClass, no);
			}
		}
	}

	public Class getTargetEntity(PropertyDescriptor pd,
			Class clazz) {
		for(Field f : clazz.getDeclaredFields()) {
			if(f.getName().equals(pd.getName())) {
				if(f.isAnnotationPresent(OneToMany.class)) {
					return f.getAnnotation(OneToMany.class).targetEntity();
				}
				break;
			}
		}
		if(pd.getReadMethod().isAnnotationPresent(OneToMany.class)) {
			return pd.getReadMethod().getAnnotation(OneToMany.class).targetEntity();
		}
		return null;
	}

	private void importLinkedCollection(Workbook wb, String className, Locale locale,
			EntityManager em, String v, PropertyDescriptor pd, Collection coll, ObjectsToAdd l,
			Class type, INakedObject collectionHolder) throws InstantiationException, IllegalAccessException, InvocationTargetException, IntrospectionException, PersistenceException, Exception {
		UIAttribute uiAtt = pd.getReadMethod().getAnnotation(UIAttribute.class);
		if(uiAtt.importable()) {
			String ia = uiAtt.importAttribute();
			if (ia == null) {
				throw new Exception(
						"No import attribute set on "
								+ pd.getName()
								+ " in class "
								+ className);
			}

			BeanInfo beanInfo2 = Introspector
					.getBeanInfo(type);
			PropertyDescriptor[] pds2 = beanInfo2
					.getPropertyDescriptors();
			PropertyDescriptor des = getDescriptor(ia, pds2);
			Object val = convertExcelValue(className, v, ia, des);
			if (val == null) {
				throw new Exception(
						"Import attribute "
								+ ia
								+ " on class "
								+ className
								+ " is not found...");
			}
			Collection<INakedObject> l2 = null;
			boolean found = false;
			for(INakedObject obj : l.getLastList()) {
				if(obj.getClass().getName().equals(type.getName())) {
					if(des.getReadMethod().invoke(obj, new Object[0]).equals(val)) {
						addToCollection(coll, collectionHolder, obj);
						found = true;
						break;
					}
				}
			}
			if(!found) {
				l2 = StorageService
					.getInstance().getByAttribute(
							type.getName(), ia,
							val, em);
				if (l2 == null || l2.isEmpty()) {
					if(!uiAtt.importCreateMissing())
						throw new Exception(
								"No data found for "
										+ className
										+ " with attribute "
										+ ia
										+ " value equals to "
										+ val);
					else {
						Sheet linkedSheet = wb.getSheet(getLabel(type));
						Row r = linkedSheet.getRow(0);
						Map<Integer, String> attsMap = new HashMap<Integer, String>();
						for(int i = 0; i<r.getLastCellNum(); i++) {
							Cell c = r.getCell(i);
							if(c != null)
								attsMap.put(i, c.getStringCellValue());
							else
								break;
						}
						extractRows(wb, type.getName(), attsMap, locale, type, pds2, new HashMap<Integer, PropertyDescriptor>(), em, linkedSheet, l, false, true);
						found = false;
						for(INakedObject o : l.getLastList()) {
							if(o.getClass().getName().equals(type.getName())) {
								 Object res = des.getReadMethod().invoke(o, new Object[0]);
								 if(res != null && res.equals(val)) {
									 addToCollection(coll, collectionHolder, o);
									 found = true;
									 break;
								 }
							}
						}
						if(!found)
							logger.warning("Linked object with import attribute "+ia+" = "+val+" was not founf in the "+getLabel(type)+" sheet !!");

					}
				}
				else if (l2.size() > 1)
					throw new Exception(
							"Too many rows found for "
									+ className
									+ " with attribute "
									+ ia
									+ " value equals to "
									+ val);
				else {
					addToCollection(coll, collectionHolder, l2.iterator().next());
				}
			}
		}
		
	}

	private void addToCollection(Collection coll,
			INakedObject collectionHolder, INakedObject o)
			throws IntrospectionException, IllegalAccessException,
			InvocationTargetException {
		String inverseAtt = null;
		 for(Field f : o.getClass().getDeclaredFields()) {
			 if(f.isAnnotationPresent(ManyToOne.class) && f.getType().equals(collectionHolder.getClass())) {
				 inverseAtt = f.getName();
			 }
		 }
		 BeanInfo beanInfo = Introspector.getBeanInfo(o.getClass());
		 PropertyDescriptor[] collPds = beanInfo.getPropertyDescriptors();
		 Method wm = null;
		 for(PropertyDescriptor p : collPds) {
			 if((inverseAtt != null && p.getName().equals(inverseAtt)) || (inverseAtt == null && p.getReadMethod().isAnnotationPresent(ManyToOne.class) && p.getPropertyType().equals(collectionHolder.getClass()))) {
				wm = p.getWriteMethod();
				break;
			 }
		 }
		 if(wm != null)
			 wm.invoke(o, collectionHolder);
		 coll.add(o);
	}

	public void importLinkedNakedObject(Workbook wb, String className,
			Locale locale, EntityManager em, INakedObject no, String v,
			PropertyDescriptor pd, Method writeMethod, Class type,
			ObjectsToAdd l) throws Exception, IntrospectionException,
			PersistenceException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		UIAttribute uiAtt = pd.getReadMethod().getAnnotation(UIAttribute.class);
		if(uiAtt.importable()) {
			String ia = uiAtt.importAttribute();
			if (ia == null) {
				throw new Exception(
						"No import attribute set on "
								+ pd.getName()
								+ " in class "
								+ className);
			}
			BeanInfo beanInfo2 = Introspector
					.getBeanInfo(type);
			PropertyDescriptor[] pds2 = beanInfo2
					.getPropertyDescriptors();
			PropertyDescriptor des = getDescriptor(ia, pds2);
			Object val = convertExcelValue(className, v, ia, des);
			/*
			 * create new att in UIAttribute : importType : default ImportType.FIND with ImportType : enum {FIND, FIND_OR_CREATE}
			 * if FIND_OR_CREATE => if object not found with importAttribute, go to Sheet att<j> at row i and create linked object with data
			 * For collections => add found or created object to collection
			 * + add in UIClass : importValidator & importable (default false) : if true : CN define :
			 * + in UIClass : importValidator default DefaultImportValidator implements IImportValidator
			 */
			if (val == null) {
				throw new Exception(
						"Import attribute "
								+ ia
								+ " on class "
								+ className
								+ " is not found...");
			}

			Collection<INakedObject> l2 = null;
			boolean found = false;
			for(INakedObject obj : l.getFirstList()) {
				if(obj.getClass().getName().equals(type.getName())) {
					if(des.getReadMethod().invoke(obj, new Object[0]).equals(val)) {
						writeMethod.invoke(no, obj);
						found = true;
						break;
					}
				}
			}
			if(!found) {
				l2 = StorageService.getInstance().getByAttribute(
								type.getName(), ia,
								val, em);
				if (l2 == null || l2.isEmpty()) {
					if(!uiAtt.importCreateMissing())
						throw new Exception(
								"No data found for "
										+ className
										+ " with attribute "
										+ ia
										+ " value equals to "
										+ val);
					else {
						Sheet linkedSheet = wb.getSheet(getLabel(type));
						Row r = linkedSheet.getRow(0);
						Map<Integer, String> attsMap = new HashMap<Integer, String>();
						for(int i = 0; i<r.getLastCellNum(); i++) {
							Cell c = r.getCell(i);
							if(c != null)
								attsMap.put(i, c.getStringCellValue());
							else
								break;
						}
						extractRows(wb, type.getName(), attsMap, locale, type, pds2, new HashMap<Integer, PropertyDescriptor>(), em, linkedSheet, l, true, false);
						found = false;
						for(INakedObject o : l.getFirstList()) {

							if(o.getClass().getName().equals(type.getName())) {
								 Object res = des.getReadMethod().invoke(o, new Object[0]);
								 if(res != null && res.equals(val)) {
									 writeMethod.invoke(no, o);
									 found = true;
									 break;
								 }
							}
						}
						if(!found)
							logger.warning("Linked object with import attribute "+ia+" = "+val+" was not found in the "+getLabel(type)+" sheet !!");

					}
				}
				else if (l2.size() > 1)
					throw new Exception(
							"Too many rows found for "
									+ className
									+ " with attribute "
									+ ia
									+ " value equals to "
									+ val);
				else {
					writeMethod.invoke(no, l2
							.iterator().next());
				}
			}
		}
	}

	private Object convertExcelValue(String className, String excelVal, String att,
			PropertyDescriptor d) throws Exception {
		Object val = null;
		Class<?> cType = d
				.getPropertyType();
		if (cType.equals(String.class))
			val = excelVal;
		else if (cType
				.equals(Long.class)
				|| cType.equals(long.class))
			val = Long.valueOf(excelVal);
		else if (cType
				.equals(Double.class)
				|| cType.equals(double.class))
			val = Double.valueOf(excelVal);
		else if (cType
				.equals(Integer.class)
				|| cType.equals(int.class))
			val = Integer.valueOf(excelVal);
		else if (cType
				.equals(Short.class)
				|| cType.equals(short.class))
			val = Short.valueOf(excelVal);
		else if (cType
				.equals(Float.class)
				|| cType.equals(float.class))
			val = Float.valueOf(excelVal);
		else if (cType
				.equals(Byte.class)
				|| cType.equals(byte.class))
			val = Byte.valueOf(excelVal);
		else
			throw new Exception(
					"Import attribute "
							+ att
							+ " on class "
							+ className
							+ " is not managed...("
							+ cType
							+ "). Must be String, primitive, number, date or boolean");
		return val;
	}

}
