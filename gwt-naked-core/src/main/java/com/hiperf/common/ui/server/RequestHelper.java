package com.hiperf.common.ui.server;

import javax.servlet.http.HttpServletRequest;

public class RequestHelper {
	
	public static final String USER = "l";

	public static String getLoggedUser(HttpServletRequest req) {
		return req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : req.getSession() != null ? (String)req.getSession().getAttribute(USER) : null;
	}

}
