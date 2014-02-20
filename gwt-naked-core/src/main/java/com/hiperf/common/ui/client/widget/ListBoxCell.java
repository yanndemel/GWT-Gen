package com.hiperf.common.ui.client.widget;

import java.util.Collection;

import com.google.gwt.user.client.ui.ListBox;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IListBoxCell;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.shared.WrapperContext;

public class ListBoxCell extends ListBox implements IListBoxCell {

	private static final int VISIBLE_ITEMS_NB = 2;

	protected IWrapper wrapper;
	protected String attribute;

	protected IPanelMediator mediator;


	public ListBoxCell(boolean isMultipleSelect) {
		super(isMultipleSelect);
		
	}

	public ListBoxCell(final IWrapper wrapper, String attribute, IPanelMediator mediator) {
		super(true);
		this.wrapper = wrapper;
		this.attribute = attribute;
		this.mediator = mediator;
		setStyleName("custom-ListBox");
		addHandlers();
		redraw();
	}

	protected void addHandlers() {
		mediator.addCollectionDataAddedHandler(this);
		mediator.addCollectionDataRemovedHandler(this);
	}

	public void redraw() {
		IFieldInfo info = WrapperContext.getFieldInfoByName().get(wrapper.getWrappedClassName()).get(attribute);
		Collection c = wrapper.getCollection(attribute);
		if(c != null) {
			clear();
			for(Object o : c) {
				addItem(info.getFormatter().format(o));
			}
			if(c.size() > VISIBLE_ITEMS_NB) {
				setVisibleItemCount(VISIBLE_ITEMS_NB);
			} else {
				setVisibleItemCount(c.size());
			}
			setSelectedIndex(-1);
		}
	}

	public void addObjectItem(Object o) {
		addItem(WrapperContext.getFieldInfoByName().get(wrapper.getWrappedClassName()).get(attribute).getFormatter().format(o));
	}

	public IWrapper getWrapper() {
		return wrapper;
	}

	public String getAttribute() {
		return attribute;
	}


}
