package com.hiperf.common.ui.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.hiperf.common.ui.server.storage.impl.ImportService;
import com.hiperf.common.ui.server.util.IOUtils;
import com.hiperf.common.ui.shared.IConstants;

public class ImportServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(ImportServlet.class
			.getName());

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}

	private void process(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html; charset=UTF-8"); 
		String className = null;
		String action = req.getParameter(IConstants.ACTION);
		if (ServletFileUpload.isMultipartContent(req)) {
			FileItem fileItem = null;
			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			Map<Integer, String> attsMap = new HashMap<Integer, String>();
			Map<Integer, String> labelsMap = new HashMap<Integer, String>();

			if (action != null && IConstants.ACTION_VIEW.equals(action)) {
				try {
					List<FileItem> items = upload.parseRequest(req);
					for (FileItem item : items) {
						// process only file upload - discard other form item types
						if (item.isFormField()) {
							String name = item.getFieldName();
							if (name.startsWith(IConstants.ATT_COL)) {
								int id = Integer.valueOf(name
										.substring(IConstants.ATT_COL.length()));
								attsMap.put(id, item.getString());
							} else if (name.startsWith(IConstants.LB_COL)) {
								int id = Integer.valueOf(name.substring(IConstants.LB_COL
										.length()));
								labelsMap.put(id, item.getString("UTF-8"));
							} else if(name.equals(IConstants.CLASS))
								className = item.getString();
						}
					}
					ImportService.getInstance().getImportTemplate(className, attsMap, labelsMap, resp, req.getLocale());
				} catch (ServletException e) {
					logger.log(Level.SEVERE,
							"Exception while generating template for import", e);
					throw e;
				} catch (IOException e) {
					logger.log(Level.SEVERE,
							"Exception while generating template for import", e);
					throw e;
				} catch (Exception e) {
					logger.log(Level.SEVERE,
							"Exception while generating template for import", e);
					throw new ServletException(e);
				}
			} else {
				Map<Integer, String> importAttributes = new HashMap<Integer, String>();
				String fileName = null;
				// Parse the request
				try {
					List<FileItem> items = upload.parseRequest(req);
					for (FileItem item : items) {
						// process only file upload - discard other form item types
						if (item.isFormField()) {
							String name = item.getFieldName();
							if (name.startsWith(IConstants.LB_COL)) {
								int id = Integer.valueOf(name.substring(IConstants.LB_COL
										.length()));
								labelsMap.put(id, item.getString("UTF-8"));
							} else if (name.startsWith(IConstants.ATT_COL)) {
								int id = Integer.valueOf(name
										.substring(IConstants.ATT_COL.length()));
								attsMap.put(id, item.getString());
							} else if (name.startsWith(IConstants.IMP_ATT_COL)) {
								int id = Integer
										.valueOf(name
												.substring(IConstants.IMP_ATT_COL
														.length()));
								importAttributes.put(id, item.getString());
							} else if(name.equals(IConstants.CLASS))
								className = item.getString();
						} else {
							fileItem = item;
							fileName = item.getName();
							// get only the file name not whole path
							if (fileName != null) {
								fileName = FilenameUtils.getName(fileName);
							}
						}
					}
					if (fileItem != null && className != null && !labelsMap.isEmpty()
							&& !attsMap.isEmpty()) {
						try {
							ImportService.getInstance().upload(className, attsMap, labelsMap,
									importAttributes, fileItem, req.getLocale(), 
									getCurrentLoggedUser(req), 
									req);
							resp.getWriter().print(
										IConstants.RESPONSE_TOKEN + IConstants.OK
												+ IConstants.RESPONSE_TOKEN);
						} catch (Exception e) {
							resp.getWriter().print(
									IConstants.RESPONSE_TOKEN + IOUtils.htmlEncode(e.getMessage())
											+ IConstants.RESPONSE_TOKEN);
						}
					} else {
						resp.getWriter().print(
								IConstants.RESPONSE_TOKEN + "No import file provided"
										+ IConstants.RESPONSE_TOKEN);
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error in FileServlet", e);
					resp.getWriter().print(
							IConstants.RESPONSE_TOKEN + "An error occurred while importing the file : "
							+ IOUtils.htmlEncode(e.getMessage())
									+ IConstants.RESPONSE_TOKEN);
				}

			}
		} else {
			resp.getWriter().print(
					IConstants.RESPONSE_TOKEN + "Request contents type is not supported by the servlet."
							+ IConstants.RESPONSE_TOKEN);
		}
		resp.setStatus(HttpServletResponse.SC_CREATED);
		resp.flushBuffer();
	}

	protected String getCurrentLoggedUser(HttpServletRequest req) {
		return RequestHelper.getLoggedUser(req);
	}
}
