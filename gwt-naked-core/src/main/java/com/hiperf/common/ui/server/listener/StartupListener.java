package com.hiperf.common.ui.server.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hiperf.common.ui.server.storage.IPersistenceHelper;
import com.hiperf.common.ui.server.storage.impl.StorageService;

public class StartupListener implements ServletContextListener {

	

	private static final String INIT_PARAM_TX_TYPE = "transactionType";
	private static final String INIT_PARAM_NAME = "unitName";

	@Override
	public void contextDestroyed(ServletContextEvent e) {
		StorageService.getInstance().shutdown();
	}

	@Override
	public void contextInitialized(ServletContextEvent e) {
		ServletContext ctx = e.getServletContext();
		String param = ctx.getInitParameter(INIT_PARAM_TX_TYPE);
		GlobalParams p = GlobalParams.getInstance();
		if(param != null) {
			p.setTransactionType(param);
		} else {
			p.setTransactionType(IPersistenceHelper.TYPE.JTA.name());
		}
		param = ctx.getInitParameter(INIT_PARAM_NAME);
		if(param != null) {
			p.setUnitName(param);
		}
		StorageService.getInstance();
	}

}
