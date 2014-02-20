package com.hiperf.common.ui.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.util.PersistenceResult;
import com.hiperf.common.ui.client.widget.TablePanel;

public abstract class AbstractUpdateHandler<T extends INakedObject> implements IUpdateHandler<T> {

	@Override
	public void beforeSet(INakedObject objToAdd, T target, String viewName, String attribute, AsyncCallback<UpdateResult> callback)
			throws UpdateException {
		callback.onSuccess(null);
	}

	@Override
	public void afterSet(INakedObject objToAdd, T target, IWrappedObjectForm form, String attribute)
			throws UpdateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeSelect(String linkedObjectClass, T target, String attribute,
			AsyncCallback<String> callback) throws UpdateException {
		callback.onSuccess(null);
	}

	@Override
	public void beforeDelete(String attribute, T target,
			AsyncCallback<String> callback) throws UpdateException {
		callback.onSuccess(null);
	}

	@Override
	public void afterDelete(String attribute, T target) throws UpdateException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean mustRedraw(WrapperUpdatedEvent event, T target) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCollectionToString(IWrapperListModel wrappedCollection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String beforeView(String linkedObjectClass, String attribute, T target) throws UpdateException {
		return null;
	}

	@Override
	public boolean isEditable(T target, IWrapper parentWrapper, String attribute, String viewName) {
		return true;
	}

	@Override
	public String beforeEdit(T target) {
		return null;
	}

	@Override
	public void afterValidate(TablePanel tablePanel, PersistenceResult persistedObjects) {
	}

	@Override
	public boolean canValidate(T target, String viewName) {
		return isEditable(target, null, null, viewName);
	}

	/*@Override
	public void beforeShowDetails(WrappedObjectForm form) {
		// TODO Auto-generated method stub
		
	}*/

	@Override
	public void afterClose(IWrappedObjectForm form) {}

	@Override
	public void afterInit(IWrappedObjectForm wrappedObjectForm) {}
	
}
