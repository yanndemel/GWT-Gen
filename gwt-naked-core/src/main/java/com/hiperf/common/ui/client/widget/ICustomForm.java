package com.hiperf.common.ui.client.widget;

import com.hiperf.common.ui.client.IWrappedTable;
import com.hiperf.common.ui.client.IWrapper;

public interface ICustomForm {

	ICustomForm newInstance(IWrapper w, IWrappedTable table, boolean editable);
	
	void show();
	
}
