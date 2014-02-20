package com.hiperf.common.ui.client.widget;

import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IWrapper;

public class EnumCell extends LabelCell {

	public EnumCell(IWrapper wrapper, String attribute, IPanelMediator mediator) {
		super(wrapper, attribute, mediator);
	}

	public void redraw() {
		Enum value = (Enum) wrapper.getNakedAttribute(attribute);
		if(value != null) {
			setText(value.toString());
		} else
			setText(null);

	}

}
