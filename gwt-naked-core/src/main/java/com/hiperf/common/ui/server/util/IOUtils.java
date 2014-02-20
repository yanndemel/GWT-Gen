package com.hiperf.common.ui.server.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class IOUtils {

	private static final Logger logger = Logger.getLogger(ExcelHelper.class.getName());

	public static int BUFFER_SIZE = 2048;

	public static void sendAttachment(HttpServletResponse resp,
			InputStream input, String fileName) throws IOException,
			ServletException {
		BufferedInputStream bis = new BufferedInputStream(input, BUFFER_SIZE);
		int length = input.available();
		resp.reset();
		resp.setContentType("application/octet-stream");
		resp.setContentLength(length);
		resp.setHeader("Content-disposition", "attachment; filename=\"" + fileName
				+ "\"");
		BufferedOutputStream output = new BufferedOutputStream(resp.getOutputStream(), BUFFER_SIZE);
		try {
			for(int data; (data=bis.read()) != -1;) {
				output.write(data);
			}
			output.flush();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception while sending attachment", e);
			throw new ServletException("Exception while sending attachment", e);
		} finally {
			output.close();
			input.close();
			bis.close();
		}
	}

	public static void sendAttachment(HttpServletResponse resp,
			byte[] b, String fileName) throws IOException,
			ServletException {
		int length = b.length;
		resp.reset();
		if(fileName.toLowerCase().endsWith(".html") || fileName.toLowerCase().endsWith(".htm")) {
			resp.setContentType("text/html; charset="+System.getProperty("file.encoding"));
		}
		else
			resp.setContentType("application/octet-stream");
		resp.setContentLength(length);
		resp.setHeader("Connection", "keep-alive");
		resp.setHeader("Content-disposition", "attachment; filename=\"" + fileName
				+ "\"");
		try {
			resp.getOutputStream().write(b);
			resp.getOutputStream().flush();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception while sending attachment", e);
			//throw new ServletException("Exception while sending attachment", e);
		} finally {
			resp.getOutputStream().close();
		}
	}

	/**

	* Takes UTF-8 strings and encodes non-ASCII as

	* ampersand-octothorpe-digits-semicolon

	* HTML-encoded characters

	*

	* @param string

	* @return HTML-encoded String

	*/

	public static String htmlEncode(final String string) {

	  final StringBuffer stringBuffer = new StringBuffer();

	  for (int i = 0; i < string.length(); i++) {

	    final Character character = string.charAt(i);

	    if (CharUtils.isAscii(character)) {

	      // Encode common HTML equivalent characters

	      stringBuffer.append(

	          StringEscapeUtils.escapeHtml4(character.toString()));

	    } else {

	      // Why isn't this done in escapeHtml4()?

	      stringBuffer.append(

	          String.format("&#x%x;",

	              Character.codePointAt(string, i)));

	    }

	  }

	  return stringBuffer.toString();

	}



}
