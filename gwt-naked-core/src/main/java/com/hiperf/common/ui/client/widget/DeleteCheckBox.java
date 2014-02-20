package com.hiperf.common.ui.client.widget;

import com.google.gwt.user.client.ui.CheckBox;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class DeleteCheckBox extends CheckBox implements ICell {
	
	private IWrapper wrapper;

	public DeleteCheckBox(IWrapper w) {
		super();
		this.wrapper = w;
		setTitle(NakedConstants.messages.deleteRow(1));
	}

	public IWrapper getWrapper() {
		return wrapper;
	}

	@Override
	public String getAttribute() {
		return null;
	}

	@Override
	public void redraw() {
	}

}
