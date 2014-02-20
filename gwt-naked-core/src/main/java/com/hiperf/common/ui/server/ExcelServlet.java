package com.hiperf.common.ui.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.hiperf.common.ui.server.storage.impl.StorageService;
import com.hiperf.common.ui.shared.IConstants;

public class ExcelServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(ExcelServlet.class.getName());
	
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
			throws ServletException {
		String className = req.getParameter(IConstants.CLASS);
		if(className != null && ServletFileUpload.isMultipartContent(req)) {
			StorageService.getInstance().getExtractedData(req, resp, className);
		}
	}

	
}
