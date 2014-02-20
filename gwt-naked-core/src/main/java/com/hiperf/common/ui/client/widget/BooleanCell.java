package com.hiperf.common.ui.client.widget;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IWrapper;

public class BooleanCell extends CheckBox implements ICell {

	protected IWrapper wrapper;
	protected String attribute;

	public BooleanCell() {
		// TODO Auto-generated constructor stub
	}


	public BooleanCell(IWrapper wrapper, String attribute) {
		super();
		this.wrapper = wrapper;
		this.attribute = attribute;
	}



	public BooleanCell(final IWrapper wrapper, final String attribute, final boolean enabled) {
		this(wrapper, attribute);
		setEnabled(enabled);
		redraw();
		addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				try {
					wrapper.setAttribute(attribute, event.getValue().toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public void redraw() {
		Boolean b = (Boolean)wrapper.getNakedAttribute(attribute);
		if(b == null || !b) {
			setValue(false);
		} else {
			setValue(true);
		}
	}

	@Override
	public String getAttribute() {
		return attribute;
	}

	@Override
	public IWrapper getWrapper() {
		return wrapper;
	}
}
