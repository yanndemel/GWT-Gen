package com.hiperf.common.ui.client.widget;

import com.google.gwt.user.client.ui.Label;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.ITextCell;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.shared.WrapperContext;

public class LabelCell extends Label implements ITextCell {

	protected IWrapper wrapper;
	protected String attribute;
	protected IPanelMediator mediator;

	public LabelCell() {
		super();
	}

	public LabelCell(IWrapper wrapper, String attribute, IPanelMediator mediator) {
		super();
		this.wrapper = wrapper;
		this.attribute = attribute;
		this.mediator = mediator;
		//this.setWordWrap(false);
		if(mediator != null) {
			IFieldInfo fi = WrapperContext.getFieldInfoByName().get(wrapper.getWrappedClassName()).get(attribute);
			String linkedAtt = fi.getRedrawOnUpdateLinkedObject();
			if(linkedAtt != null)
				mediator.addSpecialWrapperUpdatedHandler(wrapper.getContent(), this);
		}
		redraw();
	}

	public void redraw() {
		setText(wrapper.getAttribute(attribute));
	}

	@Override
	public String getAttribute() {
		return attribute;
	}

	@Override
	public IWrapper getWrapper() {
		return wrapper;
	}

	@Override
	public void setTabIndex(int idx) {
	}



}
