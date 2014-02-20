package com.hiperf.common.ui.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class ObjectPanel extends AbstractObjectPanel {

	private int stateIdx;
	private boolean removeFromLists;

	public ObjectPanel(IWrappedObjectForm objectForm) {
		super(objectForm);
		this.stateIdx = -1;
	}

	public ObjectPanel(IWrappedObjectForm objectForm, int stateIdx,
			boolean removeFromLists) {
		super(objectForm);
		this.stateIdx = stateIdx;
		this.removeFromLists = removeFromLists;
	}

	protected void initTopMenu(boolean isNew) {
		super.initTopMenu();
		int col = 0;
		if (objectForm instanceof WrappedObjectForm && ((WrappedObjectForm)objectForm).isPersistent() && !isNew) {
			col = addRefreshMenu(col);
		}

	}

	private int addRefreshMenu(int col) {
		Image i = new Image(TablePanel.images.refresh());
		i.setStyleName("topMenu");
		i.setTitle(NakedConstants.constants.refresh());
		i.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				((WrappedObjectForm)objectForm).refresh(1, TablePanel.DEFAULT_ROW_NB);
			}
		});
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, i);
		return col + 1;
	}

	public int getStateIdx() {
		return stateIdx;
	}

	public boolean isRemoveFromLists() {
		return removeFromLists;
	}

}
