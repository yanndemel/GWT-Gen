package com.hiperf.common.ui.server;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.server.storage.IPersistenceHelper;
import com.hiperf.common.ui.server.storage.impl.StorageService;
import com.hiperf.common.ui.shared.IConstants;

public class FileServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(FileServlet.class.getName());

	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		process(req, resp);
}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);

	}

	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter(IConstants.ACTION);
		if(IConstants.DOWNLOAD_ACTION.equals(action)) {
			processDownload(req, resp);
		} else {
			String fileClass = null;
			String fileNameField = null;
			String fileStorageField = null;
			String fileName = null;
			String existingId = null;
			FileItem fileItem = null;
			 // process only multipart requests
            if (ServletFileUpload.isMultipartContent(req)) {

               // Create a factory for disk-based file items
               FileItemFactory factory = new DiskFileItemFactory();

               // Create a new file upload handler
               ServletFileUpload upload = new ServletFileUpload(factory);

               // Parse the request
               try {
                   List<FileItem> items = upload.parseRequest(req);
                   for (FileItem item : items) {
                       // process only file upload - discard other form item types
                       if (item.isFormField()) {
                    	   if(IConstants.FILE_CLASS.equals(item.getFieldName()))
                    		   fileClass = item.getString();
                    	   else if(IConstants.FILE_NAME.equals(item.getFieldName()))
                    		   fileNameField = item.getString();
                    	   else if(IConstants.FILE_STORAGE_FIELD.equals(item.getFieldName()))
                    		   fileStorageField= item.getString();
                    	   else if(IConstants.ID.equals(item.getFieldName()))
                    		   existingId = item.getString();

                       } else {
                    	   fileItem = item;
	                       fileName = item.getName();
	                       // get only the file name not whole path
	                       if (fileName != null) {
	                           fileName = FilenameUtils.getName(fileName);
	                       }
                       }
                   }
                   if(fileItem != null && fileClass != null && fileNameField != null && fileStorageField != null) {
                	   if(fileItem.getSize() <= 0) {
                		   ResourceBundle ressource = ResourceBundle.getBundle(IPersistenceHelper.SERVER_MESSAGES, req.getLocale(), IPersistenceHelper.class.getClassLoader(), new UTF8Control());
                		   resp.getWriter().print(IConstants.ERROR_TOKEN+ressource.getString("errorEmptyFile")+IConstants.ERROR_TOKEN);
                		   return;
                	   }
                	   persistDocument(req, resp, fileClass, fileNameField,
							fileStorageField, fileName, existingId, fileItem);
	                   resp.setStatus(HttpServletResponse.SC_CREATED);
	                   resp.flushBuffer();
	               } else {
	            	   resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	                           "No file provided");
	               }
               } catch (RuntimeException e) {
            	   processRuntimeException(req, resp, e);
               } catch (Exception e) {
            	   processException(resp, e);
               } finally {
            	   if(fileItem != null && !fileItem.isInMemory()) {
            		   fileItem.delete();
            	   }
               }

            } else {
               resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                               "Request contents type is not supported by the servlet.");
            }

		}




	}

	protected void processDownload(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException {
		String fileClass = req.getParameter(IConstants.FILE_CLASS);
		String fileNameField = req.getParameter(IConstants.FILE_NAME);
		String fileStorageField = req
				.getParameter(IConstants.FILE_STORAGE_FIELD);
		String fileId = req.getParameter(IConstants.ID);
		if (fileClass != null && fileNameField != null
				&& fileStorageField != null && fileId != null) {
			try {
				StorageService.getInstance().downloadFile(resp, fileClass, fileNameField, fileStorageField,
						fileId);
			} catch (PersistenceException e) {
				throw new ServletException("Problem while downloading file", e);
			}
		}
		StorageService.getInstance().processDownload(req, resp);
	}

	protected void processException(HttpServletResponse resp, Throwable e)
			throws IOException {
		logger.log(Level.SEVERE, "Error in FileServlet", e);
		   resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		           "An error occurred while creating the file : " + e.getMessage());
	}

	protected void processRuntimeException(HttpServletRequest req,
			HttpServletResponse resp, RuntimeException e) throws IOException {		
		processException(resp, e);
	}

	protected void persistDocument(HttpServletRequest req, HttpServletResponse resp, String fileClass,
			String fileNameField, String fileStorageField, String fileName,
			String existingId, FileItem fileItem) throws PersistenceException,
			IOException {
		resp.setContentType("text/html; charset=UTF-8"); 
		if(existingId != null) {
			   existingId = StorageService.getInstance().replaceFile(fileClass, fileNameField, fileStorageField, fileName, fileItem, existingId);
			   resp.getWriter().print(IConstants.RESPONSE_TOKEN+existingId.toString()+IConstants.RESPONSE_TOKEN);
		   }
		   else {
			   Object id = StorageService.getInstance().saveFile(fileClass, fileNameField, fileStorageField, fileName, fileItem);
			   resp.getWriter().print(IConstants.RESPONSE_TOKEN+id.toString()+IConstants.RESPONSE_TOKEN);
		   }
	}

}
