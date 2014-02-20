package com.hiperf.common.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.shared.util.Id;

public interface IWrapperListModel {
	boolean isEmpty();
	String getText();
	String getNakedObjectName();
	List<IWrapper> getItems();
	void reload(int pageNb, int rowsNb, AsyncCallback<Void> cb);
	boolean isPersistent();
	boolean isLazy();
	void setLazy(boolean b);
	void removeItem(IWrapper wrapper);
	void addItem(IWrapper newWrapper);
	void addItem(int beforeRow, IWrapper newWrapper);
	void sort(String attribute, boolean asc, int page, int nbRows, AsyncCallback<Void> cb);
	void addExcludedEntity(Id id);
	int getServerCount();
	String getDefaultSortAtt();
	Boolean isDefaultSortAsc();
}
