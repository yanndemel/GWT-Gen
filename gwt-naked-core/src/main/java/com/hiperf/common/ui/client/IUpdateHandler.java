package com.hiperf.common.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.event.UpdateResult;
import com.hiperf.common.ui.client.event.WrapperUpdatedEvent;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.util.PersistenceResult;
import com.hiperf.common.ui.client.widget.TablePanel;

public interface IUpdateHandler<T extends INakedObject> {

	void beforeSet(INakedObject objToAdd, T target, String viewName, String attribute, AsyncCallback<UpdateResult> callback) throws UpdateException;

	void afterSet(INakedObject objToAdd, T target, IWrappedObjectForm form, String attribute) throws UpdateException;

	void beforeSelect(String linkedObjectClass, T target, String attribute, AsyncCallback<String> callback) throws UpdateException;		

	void beforeDelete(String attribute, T target, AsyncCallback<String> callback) throws UpdateException;

	void afterDelete(String attribute, T target) throws UpdateException;

	boolean mustRedraw(WrapperUpdatedEvent event, T target);

	String getCollectionToString(IWrapperListModel wrappedCollection);

	String beforeView(String linkedObjectClass, String attribute, T target) throws UpdateException;

	boolean isEditable(T target, IWrapper parentWrapper, String attribute, String viewName);

	//returns the custom title of the form.
	String beforeEdit(T target);

	void afterValidate(TablePanel tablePanel, PersistenceResult result);

	boolean canValidate(T target, String viewName);

	void afterClose(IWrappedObjectForm form);

	void afterInit(IWrappedObjectForm wrappedObjectForm);

	//void beforeShowDetails(WrappedObjectForm form);



}
