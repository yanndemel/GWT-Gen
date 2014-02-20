package com.hiperf.common.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.widget.WrappedFlexTable;

public interface ICommitHandler {

	void commit(WrappedFlexTable table, AsyncCallback<Boolean> asyncCallback);

	
}

