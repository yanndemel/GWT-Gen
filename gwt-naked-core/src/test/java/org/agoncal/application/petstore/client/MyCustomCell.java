package org.agoncal.application.petstore.client;

import com.google.gwt.user.client.ui.Composite;
import com.hiperf.common.ui.client.ICustomCell;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.widget.TextBoxCell;
import com.hiperf.common.ui.client.widget.custom.AbstractCustomCell;

public class MyCustomCell extends AbstractCustomCell {

	private TextBoxCell tbc;
	
	public MyCustomCell() {
		super();
	}

	public MyCustomCell(IWrapper w, String att) {
		super(w, att);
		tbc = new TextBoxCell(w, att, null);
		initWidget(tbc);
	}


	@Override
	public ICustomCell newInstance(IWrapper wrapper, String attribute,
			boolean editable, IPanelMediator mediator) {
		// TODO Auto-generated method stub
		return new MyCustomCell(wrapper, attribute);
	}

	@Override
	public void redraw() {
		// TODO Auto-generated method stub

	}

}
