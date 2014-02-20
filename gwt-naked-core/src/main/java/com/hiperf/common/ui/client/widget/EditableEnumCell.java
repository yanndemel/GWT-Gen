package com.hiperf.common.ui.client.widget;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ListBox;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.shared.WrapperContext;

public class EditableEnumCell extends ListBox implements ICell {

	protected IWrapper wrapper;
	protected String attribute;
	protected Map<String,String> enumConstants;

	protected HandlerRegistration changeHandler;

	public EditableEnumCell() {
		super();
	}

	public EditableEnumCell(final IWrapper wrapper, final String attribute) {
		super();
		this.wrapper = wrapper;
		this.attribute = attribute;
		final String enumTypeName = WrapperContext.getFieldInfoByName().get(wrapper.getWrappedClassName()).get(attribute).getTypeName();
		initEnum(enumTypeName);
		redraw();
		this.changeHandler = addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				doOnChange();
			}
		});

	}

	protected void initEnum(final String enumTypeName) {
		this.enumConstants = WrapperContext.getEnumsByClassName().get(enumTypeName);
	}


	public void redraw() {
		Enum value = (Enum) wrapper.getNakedAttribute(attribute);
		int i = 1;
		clear();
		addItem("");
		for(Entry<String,String> s : enumConstants.entrySet()) {
			addItem(s.getValue(), s.getKey());
			if(value != null && s.getKey().equals(value.name())) {
				setSelectedIndex(i);
			}
			i++;
		}
	}



	public IWrapper getWrapper() {
		return wrapper;
	}

	public String getAttribute() {
		return attribute;
	}

	protected void doOnChange() {
		String value = getValue(getSelectedIndex());
		if(value != null && value.length() > 0)
			wrapper.setEnumAttribute(attribute, value);
		else
			wrapper.setEnumAttribute(attribute, null);
	}
}
