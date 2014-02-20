package com.hiperf.common.ui.server.storage;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

public interface IImportService {

	void getImportTemplate(String className, Map<Integer, String> attsMap, Map<Integer, String> labelsMap, HttpServletResponse resp, Locale locale) throws Exception;

	void upload(String className, Map<Integer, String> attsMap,
			Map<Integer, String> labelsMap,
			Map<Integer, String> importAttributes, FileItem fileItem, Locale locale, String userName, HttpServletRequest req) throws Exception;

}
