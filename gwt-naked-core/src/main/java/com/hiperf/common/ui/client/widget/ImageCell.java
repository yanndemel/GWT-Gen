package com.hiperf.common.ui.client.widget;

import com.google.gwt.user.client.ui.Image;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IWrapper;

public class ImageCell extends Image implements ICell {

	protected IWrapper wrapper;
	protected String attribute;
	
	
	
	public ImageCell(IWrapper wrapper, String attribute) {
		super();
		this.wrapper = wrapper;
		this.attribute = attribute;
	}

	public ImageCell() {
		super();
		// TODO Auto-generated constructor stub
	}

	

	@Override
	public IWrapper getWrapper() {
		// TODO Auto-generated method stub
		return wrapper;
	}

	@Override
	public String getAttribute() {
		// TODO Auto-generated method stub
		return attribute;
	}

	@Override
	public void redraw() {
		String url = wrapper.getAttribute(attribute);
		if(url != null)
			setUrl(url);
	}

	@Override
	public void setTabIndex(int idx) {
		// TODO Auto-generated method stub

	}

}
