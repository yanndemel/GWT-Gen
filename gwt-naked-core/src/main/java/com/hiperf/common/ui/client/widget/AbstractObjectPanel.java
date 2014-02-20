package com.hiperf.common.ui.client.widget;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.hiperf.common.ui.client.IObjectPanel;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.util.Id;

public abstract class AbstractObjectPanel extends VerticalPanel implements IObjectPanel {

	protected FlexTable topMenuBar;
	protected IWrappedObjectForm objectForm;
	protected Id originalId;


	protected AbstractObjectPanel(IWrappedObjectForm objectForm) {
		super();
		this.objectForm = objectForm;
		this.originalId = PersistenceManager.getId(objectForm.getWrapper());
		initTopMenu();
		add(topMenuBar);
		add(objectForm);
	}

	protected void initTopMenu() {
		topMenuBar = new FlexTable();
		topMenuBar.insertRow(0);
	}

	public FlexTable getTopMenuBar() {
		return topMenuBar;
	}

	public IWrappedObjectForm getObjectForm() {
		return objectForm;
	}

	public Id getOriginalId() {
		return originalId;
	}



}
