package com.hiperf.common.ui.client.widget;

import java.util.Date;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.ResizePopupPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IFormatter;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.exception.ParseException;
import com.hiperf.common.ui.client.format.DateFormatter;
import com.hiperf.common.ui.shared.WrapperContext;

public class DateBoxCell extends DateBox implements ICell, ValueChangeHandler<Date> {

	private ResizePopupPanel panel;
	private IWrapper wrapper;
	private String attribute;
	private DateTimeFormat formatter;
	private String originalValue;

	public DateBoxCell(IWrapper wrapper, String attribute) {
		super();
		setFireNullValues(true);
		this.panel = new ResizePopupPanel(true) {

			@Override
			public void onPreviewNativeEvent(NativePreviewEvent event) {
				Event e = Event.as(event.getNativeEvent());
				switch(e.getTypeInt()) {
					case Event.ONKEYUP:
						switch(e.getKeyCode()) {
							case KeyCodes.KEY_ESCAPE:
								hideDatePicker();
								hide();
								event.cancel();
								break;
							default:
								break;
						}
						break;
					default:
						break;
				}
			}

		};
		this.panel.setStylePrimaryName("naked-popupPanel");
		this.panel.addStyleName("custom-PopupPanel");
		this.panel.setGlassEnabled(true);
		addStyleName("align-center");
		this.wrapper = wrapper;
		this.attribute = attribute;
		addValueChangeHandler(this);
		IFormatter f = WrapperContext.getFieldInfoByName().get(wrapper.getWrappedClassName()).get(attribute).getFormatter();
		if(f != null && f instanceof DateFormatter) {
			formatter = ((DateFormatter)f).getFormatter();
			setFormat(new DateBox.DefaultFormat(formatter));
		} else {
			formatter = DateTimeFormat.getFormat(DateFormatter.DEFAULT_DATE_FORMAT);
			setFormat(new DateBox.DefaultFormat(formatter));
		}
		Date date = (Date) wrapper.getNakedAttribute(attribute);
		if(date != null) {
			originalValue = formatter.format(date);
		}
		redraw();
		this.panel.add(this);

	}



	public void show(int left, int top) {
		this.panel.setPopupPosition(left,top);
		this.panel.show();
		showDatePicker();
	}



	public IWrapper getWrapper() {
		return wrapper;
	}



	public String getAttribute() {
		return attribute;
	}



	@Override
	public void redraw() {
		Date date = (Date) wrapper.getNakedAttribute(attribute);
		if(date != null) {
			setValue(date);
		}

	}

	private void setError(Exception e) {
		addStyleName("dateBoxFormatError");
		setTitle(e.getMessage());
	}

	@Override
	public void onValueChange(ValueChangeEvent<Date> event) {
		Date value = event.getValue();
		try {
			if(value == null) {
				wrapper.setAttribute(attribute, null);
				hideDatePicker();
				panel.hide();
			} else {
				String date = formatter.format(value);
				if(originalValue == null || !date.equals(originalValue)) {
					wrapper.setAttribute(attribute, date);
					hideDatePicker();
					panel.hide();
				}	
			}
		} catch (ParseException e) {
			setError(e);
		}

	}




}
