package com.hiperf.common.ui.client.widget;

import com.google.gwt.user.client.ui.Label;
import com.hiperf.common.ui.client.exception.UniqueItemException;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class Text extends Label {
	private int total;
	private boolean noReload;

	public Text() {
		super();
		this.total = 0;
		this.noReload = false;
		setWordWrap(false);
		setStylePrimaryName("underline");
	}

	public Text(int total) {
		this();
		this.total = total;
	}
	public Text(String txt) {
		super(txt);
		setStylePrimaryName("underline");
	}

	public void redraw() throws UniqueItemException {
		switch(total) {
			case 0:
				setText(NakedConstants.constants.emptyCell());
				break;
			case 1:
				throw new UniqueItemException();
			default:
				setText(NakedConstants.messages.nbItems(total));
				break;
		}
	}
	public void decTotal() throws UniqueItemException {
		total--;
		this.noReload = true;
		redraw();
	}
	public void incTotal() throws UniqueItemException {
		total++;
		this.noReload = true;
		redraw();
	}
	public void setTotal(int i) throws UniqueItemException {
		total = i;
		this.noReload = false;
		redraw();
	}

	public boolean isNoReload() {
		return noReload;
	}
}
