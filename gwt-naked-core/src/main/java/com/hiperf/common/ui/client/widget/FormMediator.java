package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.event.shared.HandlerRegistration;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.WrapperUpdatedHandler;
import com.hiperf.common.ui.client.event.WrapperUpdatedEvent;
import com.hiperf.common.ui.shared.WrapperContext;

public class FormMediator extends PanelMediator implements WrapperUpdatedHandler {

	//Registered handlers
	private List<HandlerRegistration> handlerRegistry;


	private IWrappedObjectForm form;


	public FormMediator(IWrappedObjectForm form) {
		super();
		this.form = form;
		this.handlerRegistry = new ArrayList<HandlerRegistration>();
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(WrapperUpdatedEvent.getType(), this));
	}


	protected IWrappedObjectForm getForm() {
		return form;
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
		int index = ViewHelper.getIndex(event.getAttribute(), form.getSortedFields());
		if(index >= 0) {
			form.getFormGrid().getRowFormatter().setStyleName(index, "updatedRow");
			form.redrawRow(index, null);
		}
		IWrapper wrapper = event.getWrapper();
		List<WrapperUpdatedHandler> list = updHandlersByNoMap.get(wrapper.getContent());
		if(list != null && !list.isEmpty()) {
			for(WrapperUpdatedHandler h : list) {
				h.onItemUpdated(event);
			}
		}	
		Object source = event.getSource();
		for(Entry<INakedObject, Set<ICell>> e : toRedrawCellsByUpdObject.entrySet()) {
			if(e.getKey() == source || e.getKey().equals(source)) {
				Set<ICell> set = e.getValue();
				if(set != null && !set.isEmpty()) {
					for(ICell cell : set) {
						cell.redraw();
					}
				}
			}
		}
	}

	
	
}
