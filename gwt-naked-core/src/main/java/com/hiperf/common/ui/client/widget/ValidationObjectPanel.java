package com.hiperf.common.ui.client.widget;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class ValidationObjectPanel extends VerticalPanel {

	private WrappedObjectForm form;
	private Button validateBtn;
	
	public ValidationObjectPanel(WrappedObjectForm form) {
		super();
		this.form = form;
		add(form);
		validateBtn = new Button(NakedConstants.constants.validate());
		validateBtn.setStylePrimaryName("naked-button");
		add(validateBtn);
		setCellHorizontalAlignment(validateBtn, HasHorizontalAlignment.ALIGN_CENTER);
	}

	public Button getValidateBtn() {
		return validateBtn;
	}

	
	
	
}
