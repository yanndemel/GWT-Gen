package com.hiperf.gwtjpa.test.table.msgprovider.client;

import com.google.gwt.core.client.GWT;
import com.hiperf.common.ui.client.INakedLabels;
import com.hiperf.common.ui.shared.annotation.MessagesProvider;

@MessagesProvider(name = "com.hiperf.gwtjpa.test.table.msgprovider.client.Constants.constants")
public class Constants implements INakedLabels {
	public static final IConstants constants = GWT.create(IConstants.class);


}