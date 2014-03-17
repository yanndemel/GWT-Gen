package com.hiperf.common.ui.server.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.gwtgen.api.shared.INakedObject;

import com.hiperf.common.ui.server.storage.impl.PersistenceHelper;
import com.hiperf.common.ui.shared.CommonUtil;
import com.hiperf.common.ui.shared.IConstants;
import com.hiperf.common.ui.shared.util.Id;

public class ExcelHelper {

	private static final Logger logger = Logger.getLogger(ExcelHelper.class.getName());

	public static void sendResponse(HttpServletResponse resp, String className,
			Map<Integer, Object[]> map, List res)
			throws IllegalAccessException, InvocationTargetException,
			IOException, FileNotFoundException, ServletException {
		if(res != null && !res.isEmpty()) {

			Workbook wb = new HSSFWorkbook();
			CreationHelper createHelper = wb.getCreationHelper();
			Sheet sheet = wb.createSheet(className);
			Row header = sheet.createRow(0);
			
			Font defaultFont= wb.createFont();
		    defaultFont.setFontHeightInPoints((short)10);
		    defaultFont.setFontName("Arial");
		    defaultFont.setColor(IndexedColors.BLACK.getIndex());
		    defaultFont.setBoldweight( Font.BOLDWEIGHT_NORMAL);
		    defaultFont.setItalic(false);
		    
		   

		    Font font= wb.createFont();
		    font.setFontHeightInPoints((short)10);
		    font.setFontName("Arial");
		    font.setColor(IndexedColors.BLACK.getIndex());
		    font.setBoldweight( Font.BOLDWEIGHT_BOLD);
		    font.setItalic(false);

		    CellStyle style= wb.createCellStyle();
		    style.setAlignment(CellStyle.ALIGN_CENTER);
		    style.setBorderRight(CellStyle.BORDER_THIN);
		    style.setFont(font);
		    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		    style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
		    //style.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
		    
		    for(int i = 0; i<map.size(); i++) {
				Cell cell = header.createCell(i);
				Object[] key = map.get(i);
				if(key != null) {
					cell.setCellValue(createHelper.createRichTextString((String)(key[1])));
					cell.setCellStyle(style);
				}
			}

			
			int i = 1;

			for(Object obj : res) {
				Row row= sheet.createRow(i);

				for(int j = 0; j<map.size(); j++) {
					Cell cell = row.createCell(j);
					Object[] objects = map.get(j);
					if(objects != null) {
						Object value = ((Method)objects[0]).invoke(obj, new Object[]{});
						if(value != null) {
							String s;
							if(value instanceof Enum)
								s = ((Enum)value).name();
							else if(value instanceof Collection)
								s = CommonUtil.collectionToString((Collection) value);
							else
								s = value.toString();
							if(s != null) {
								if(s.length() > 32767)
									s = s.substring(0, 32767);
								cell.setCellValue(createHelper.createRichTextString(s));								
							}
						}
					}
				}
				i++;
			}
			File f = File.createTempFile("Export_", ".xls");
			FileOutputStream fileOut = new FileOutputStream(f);
			//BufferedOutputStream bos = new BufferedOutputStream(fileOut, IOUtils.BUFFER_SIZE);
		    try {
				wb.write(fileOut);
			} finally {
				fileOut.flush();
				fileOut.close();
			}

		    InputStream input = new FileInputStream(f);
		    String fileName = f.getName();
		    IOUtils.sendAttachment(resp, input, fileName);


		} else {
			throw new ServletException("No data found for class "+className);
		}
	}

	public static Method getReadMethod(String att, PropertyDescriptor[] pds) {
		for(PropertyDescriptor pd : pds) {
			if(pd.getName().equals(att)) {
				return pd.getReadMethod();
			}
		}
		return null;
	}

	public HSSFColor setColor(HSSFWorkbook workbook, byte r,byte g, byte b){
		HSSFPalette palette = workbook.getCustomPalette();
		HSSFColor hssfColor = null;
		try {
		hssfColor= palette.findColor(r, g, b); 
		if (hssfColor == null ){
		    palette.setColorAtIndex(HSSFColor.LAVENDER.index, r, g,b);
		    hssfColor = palette.getColor(HSSFColor.LAVENDER.index);
		}
		 } catch (Exception e) {}

		 return hssfColor;
		}
	
	public static void fillObjectMap(HttpServletRequest req, String className,
			Map<Integer, Object[]> map) throws Exception {
		FileItem fileItem = null;
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		Class<?> clazz = Class.forName(className);
		try {
			List<FileItem> items = upload.parseRequest(req);
			Map<Integer, Integer> idxMap = new HashMap<Integer, Integer>();
			Map<Integer, String> attsMap = new HashMap<Integer, String>();
			Map<Integer, String> lbMap = new HashMap<Integer, String>();
			String att = null;
			for (FileItem item : items) {
				// process only file upload - discard other form item types
				if (item.isFormField()) {
					String name = item.getFieldName();
					if(name.startsWith(IConstants.IDX_COL)) {
						int id = Integer.valueOf(name.substring(IConstants.IDX_COL.length()));
						idxMap.put(id, Integer.valueOf( item.getString()));
						/*map.put(Integer.valueOf( item.getString()),
								new Object[] {getReadMethod(req.getParameter(IConstants.ATT_COL+id), pds), req.getParameter(IConstants.LB_COL+id)});*/
					} else if(name.startsWith(IConstants.ATT_COL)) {
						int id = Integer.valueOf(name.substring(IConstants.ATT_COL.length()));
						attsMap.put(id, item.getString());
					} else if(name.startsWith(IConstants.LB_COL)) {
						int id = Integer.valueOf(name.substring(IConstants.LB_COL.length()));
						lbMap.put(id, item.getString("UTF-8"));
					} else if(name.equals(IConstants.FILTER)) {
						map.put(IConstants.KEY_FILTER, new String[] {item.getString()});
					} else if(name.equals(IConstants.LAZY)) {
						map.put(IConstants.KEY_LAZY, new Boolean[] {Boolean.valueOf(item.getString())});
					} else if(name.equals(IConstants.ID)) {
						map.put(IConstants.KEY_ID, new Id[] {PersistenceHelper.getInstance().getId(item.getString(), className)});
					} else if(name.equals(IConstants.MAPPED_BY)) {
						map.put(IConstants.KEY_MAPPED_BY, new String[] {item.getString()});
					}  else if(name.equals(IConstants.ATTRIBUTE)) {
						att = item.getString();
						map.put(IConstants.KEY_ATTRIBUTE, new String[] {att});
					}
					
				}
			}
			BeanInfo beanInfo = null;
			PropertyDescriptor[] pds;
			if(att == null)
				beanInfo = Introspector.getBeanInfo(clazz);
			else {
				ParameterizedType genericType = (ParameterizedType) clazz.getDeclaredField(att).getGenericType();
				if(genericType != null) {
					for(Type t : genericType.getActualTypeArguments()) {

						if(t instanceof Class && INakedObject.class.isAssignableFrom((Class)t)) {
							clazz = (Class) t;
							break;
						}
					}
				}
				beanInfo = Introspector.getBeanInfo(clazz);
			}
			if(beanInfo != null) {
				pds = beanInfo.getPropertyDescriptors();
				for(Entry<Integer, Integer> e : idxMap.entrySet()) {
					map.put(e.getValue(), new Object[] {getReadMethod(attsMap.get(e.getKey()), pds), lbMap.get(e.getKey())});
				}
			}
		} catch (FileUploadException e) {
			logger.log(Level.SEVERE,
					"Exception while generating template for import", e);
			throw new Exception(
					"Exception while generating template for import", e);
		}
	}

}
