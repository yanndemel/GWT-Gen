package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.WrapperUpdatedHandler;
import com.hiperf.common.ui.client.event.WrapperUpdatedEvent;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;

public class DefaultMediator extends PanelMediator implements WrapperUpdatedHandler {

	//Registered handlers
	private List<HandlerRegistration> handlerRegistry;


	private IWrappedObjectForm form;

	public DefaultMediator(IWrappedObjectForm form) {
		super();
		this.form = form;
		this.handlerRegistry = new ArrayList<HandlerRegistration>();
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(WrapperUpdatedEvent.getType(), this));
	}



	public DefaultMediator() {
		this(null);
	}



	@Override
	public void unRegisterAll(boolean close) {
		super.unRegisterAll(close);
		for(HandlerRegistration hr : handlerRegistry) {
			hr.removeHandler();
		}
		handlerRegistry.clear();
	}
	
	@Override
	public void onItemUpdated(WrapperUpdatedEvent event) {
		//GWT.log("FormMediator : onItemUpdated "+event.toString());
		String attribute = event.getAttribute();
		if(form != null) {
			int index = ViewHelper.getIndex(attribute, form.getSortedFields());
			if(index >= 0) {
				form.getFormGrid().getRowFormatter().setStyleName(index, "updatedRow");
				form.redrawRow(index, null);
			}			
		}
		IWrapper wrapper = event.getWrapper();
		WrappedObjectsRepository.getInstance().addUpdatedObject(wrapper, attribute, event.getOldValue(), event.getNewValue());	
	}
	
	
}
