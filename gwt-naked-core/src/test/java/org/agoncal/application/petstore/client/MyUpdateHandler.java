package org.agoncal.application.petstore.client;

import org.agoncal.application.petstore.domain.Order;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.event.AbstractUpdateHandler;
import com.hiperf.common.ui.client.event.UpdateResult;
import com.hiperf.common.ui.client.event.WrapperUpdatedEvent;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.util.PersistenceResult;
import com.hiperf.common.ui.client.widget.TablePanel;

public class MyUpdateHandler extends AbstractUpdateHandler<Order> {

	@Override
	public void beforeSet(INakedObject objToAdd, Order target, String viewName,
			String attribute, AsyncCallback<UpdateResult> callback)
			throws UpdateException {
		if(viewName != null && "Exception View".equals(viewName))
			throw new UpdateException("Test Exception");
		super.beforeSet(objToAdd, target, viewName, attribute, callback);
	}

	@Override
	public void afterSet(INakedObject objToAdd, Order target,
			IWrappedObjectForm form, String attribute) throws UpdateException {
		// TODO Auto-generated method stub
		super.afterSet(objToAdd, target, form, attribute);
	}

	@Override
	public void beforeSelect(String linkedObjectClass, Order target,
			String attribute, AsyncCallback<String> callback)
			throws UpdateException {
		// TODO Auto-generated method stub
		super.beforeSelect(linkedObjectClass, target, attribute, callback);
	}

	@Override
	public void beforeDelete(String attribute, Order target,
			AsyncCallback<String> callback) throws UpdateException {
		// TODO Auto-generated method stub
		super.beforeDelete(attribute, target, callback);
	}

	@Override
	public void afterDelete(String attribute, Order target)
			throws UpdateException {
		// TODO Auto-generated method stub
		super.afterDelete(attribute, target);
	}

	@Override
	public boolean mustRedraw(WrapperUpdatedEvent event, Order target) {
		// TODO Auto-generated method stub
		return super.mustRedraw(event, target);
	}

	@Override
	public String getCollectionToString(IWrapperListModel wrappedCollection) {
		// TODO Auto-generated method stub
		return super.getCollectionToString(wrappedCollection);
	}

	@Override
	public String beforeView(String linkedObjectClass, String attribute,
			Order target) throws UpdateException {
		// TODO Auto-generated method stub
		return super.beforeView(linkedObjectClass, attribute, target);
	}

	@Override
	public boolean isEditable(Order target, IWrapper parentWrapper,
			String attribute, String viewName) {
		// TODO Auto-generated method stub
		return super.isEditable(target, parentWrapper, attribute, viewName);
	}

	@Override
	public String beforeEdit(Order target) {
		// TODO Auto-generated method stub
		return super.beforeEdit(target);
	}

	@Override
	public void afterValidate(TablePanel tablePanel,
			PersistenceResult persistedObjects) {
		// TODO Auto-generated method stub
		super.afterValidate(tablePanel, persistedObjects);
	}

	@Override
	public boolean canValidate(Order target, String viewName) {
		// TODO Auto-generated method stub
		return super.canValidate(target, viewName);
	}

	@Override
	public void afterClose(IWrappedObjectForm form) {
		// TODO Auto-generated method stub
		super.afterClose(form);
	}

	@Override
	public void afterInit(IWrappedObjectForm wrappedObjectForm) {
		// TODO Auto-generated method stub
		super.afterInit(wrappedObjectForm);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	
	
}
