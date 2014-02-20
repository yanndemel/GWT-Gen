package com.hiperf.common.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.hiperf.common.ui.client.IFieldInfo;

public class HeaderInfo implements IsSerializable, Comparable<HeaderInfo> {
	private String attribute;
	private String label;
	private int index;
	private boolean displayed;
	private boolean helpText;
	private Boolean editable;

	public HeaderInfo() {
		super();
	}

	public HeaderInfo(String attribute, String label, int index,
			boolean displayed, Boolean editable) {
		super();
		this.attribute = attribute;
		this.label = label;
		this.index = index;
		this.displayed = displayed;
		this.helpText = false;
		this.editable  = editable;
	}

	public HeaderInfo(String att, IFieldInfo fi) {
		this.attribute = att;
		this.label = fi.getLabel();
		this.index = fi.getIndex();
		this.displayed = fi.isDisplayed();
		this.helpText = fi.getHelpTextKey() != null
				&& WidgetFactory.getHelpMessages().get(fi.getHelpTextKey()) != null;
		this.editable = null;
	}

	public String getAttribute() {
		return attribute;
	}

	public String getLabel() {
		return label;
	}

	public int getIndex() {
		return index;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}

	public boolean isDisplayed() {
		return displayed;
	}

	public boolean isHelpText() {
		return helpText;
	}

	@Override
	public int compareTo(HeaderInfo hi) {
		if (isDisplayed()) {
			if (hi.isDisplayed()) {
				return getIndex() - hi.getIndex();
			}
			return -1;
		}
		if (hi.isDisplayed())
			return 1;
		return 0;
	}

	public Boolean getEditable() {
		return editable;
	}
	
	

}
