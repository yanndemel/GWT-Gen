package org.agoncal.application.petstore.client;

import org.gwtgen.api.shared.MessagesProvider;

import com.google.gwt.core.client.GWT;
import com.hiperf.common.ui.client.INakedLabels;

@MessagesProvider(name = "org.agoncal.application.petstore.client.Constants.constants")
public class Constants implements INakedLabels {
	public static final IConstants constants = GWT.create(IConstants.class);
}