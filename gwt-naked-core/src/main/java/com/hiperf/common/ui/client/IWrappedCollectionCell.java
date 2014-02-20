package com.hiperf.common.ui.client;

import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.widget.Text;

public interface IWrappedCollectionCell extends ILinkedCell {

	void redrawSingleElement(final String att, final IWrapper iWrapper)
			throws AttributeNotFoundException;

	Text getLabel();

	void redraw(String txt);

	
}
