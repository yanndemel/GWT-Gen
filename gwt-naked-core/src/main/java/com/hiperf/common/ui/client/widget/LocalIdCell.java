package com.hiperf.common.ui.client.widget;

import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.shared.PersistenceManager;

public class LocalIdCell extends LabelCell {

	private static final String DEFAULT_LOCAL_ID = "tmpId";

	public LocalIdCell(IWrapper wrapper, String attribute, Object id) {
		super(wrapper, attribute, null);
		setText(DEFAULT_LOCAL_ID+((id instanceof Long)?id:((String)id).substring(PersistenceManager.SEQ_PREFIX.length())));
		setStyleName("local-id");
	}

	public void redraw() {
	}

	@Override
	public String getText() {
		return DEFAULT_LOCAL_ID;
	}



}
