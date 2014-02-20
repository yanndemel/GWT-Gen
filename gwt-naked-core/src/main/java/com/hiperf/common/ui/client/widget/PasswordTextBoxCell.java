package com.hiperf.common.ui.client.widget;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IValidator;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.exception.ParseException;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.util.IntHolder;
import com.hiperf.common.ui.client.validation.ValidationHelper;
import com.hiperf.common.ui.shared.WrapperContext;

public class PasswordTextBoxCell extends PasswordTextBox implements ICell, ValueChangeHandler<String>, BlurHandler, FocusHandler, KeyUpHandler {

	private static final int LENGTH_MIN = 10;

	private IWrapper wrapper;
	private String attribute;
	private boolean error;
	private boolean hideOnValueChange;
	private IPanelMediator mediator;
	protected HandlerRegistration vhr;

	private List<IValidator> validators;

	protected PasswordTextBoxCell() {
		super();
	}

	public PasswordTextBoxCell(IWrapper wrapper, String attribute, IPanelMediator mediator) {
		super();
		addStyleDependentName("selected");
		this.wrapper = wrapper;
		this.attribute = attribute;
		this.mediator = mediator;
		error = false;
		redraw();
		vhr = addValueChangeHandler(this);
		addBlurHandler(this);
		addFocusHandler(this);
		addKeyUpHandler(this);
		IFieldInfo fieldInfo = WrapperContext.getFieldInfoByName().get(wrapper.getWrappedClassName()).get(attribute);
		List<IValidator> vs = fieldInfo.getValidators();
		this.validators = vs != null && !vs.isEmpty() ? new ArrayList<IValidator>(vs) : null;
	}

	public PasswordTextBoxCell(IWrapper wrapper, String attribute, IPanelMediator mediator, boolean hideOnValueChange) {
		this(wrapper, attribute, mediator);
		this.hideOnValueChange = hideOnValueChange;
	}

	public void redraw() {
		String text = wrapper.getAttribute(attribute);
		setText(text);
		if(hideOnValueChange && text != null) {
			setVisibleLength(Math.max(LENGTH_MIN, text.length()));
		}
		setTitle(null);
	}



	@Override
	public String getAttribute() {
		return attribute;
	}

	@Override
	public IWrapper getWrapper() {
		return wrapper;
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		//GWT.log("onValueChange");
		error = false;
		final String value = event.getValue();
		validate(value);
	}

	private void validate(final String value) {
		try {
			final int nbValidators = (validators != null)?validators.size():0;
			final IntHolder count = new IntHolder(0);
			AsyncCallback<String> callback = new AsyncCallback<String>() {

				@Override
				public void onFailure(Throwable e) {
					//GWT.log("Validation failure");
					count.increment();
					setError(e.getMessage());
				}

				@Override
				public void onSuccess(String msg) {
					//GWT.log("onSuccess "+((msg == null)?"OK":msg));
					count.increment();
					if(!error && msg == null && (nbValidators == 0 || count.get() == nbValidators)) {
						try {
							wrapper.setAttribute(attribute, value);
							unselect();
							if(hideOnValueChange) {
								mediator.hidePopup(true);
							}
						} catch (AttributeNotFoundException e) {
							setError(e.getMessage());
						} catch (ParseException e) {
							setError(e.getMessage());
						}
					} else if(msg != null) {
						setError(msg);
					}
				}
			};
			ValidationHelper.validateAttributeValue(wrapper, attribute, value, validators, callback);
		} catch (ValidationException e) {
			setError(e.getMessage());
		}
	}

	private void setError(String e) {
		error = true;
		addStyleDependentName("warning");
		unsinkEvents(Event.ONBLUR);
		setTitle(e);

	}

	@Override
	public void onBlur(BlurEvent event) {
		//GWT.log("onBlur");
		unselect();
	}

	@Override
	public void onFocus(FocusEvent event) {
		select();
	}

	private void unselect() {
		removeStyleDependentName("warning");
		error = false;
	}

	public void select() {
		setSelectionRange(0, getText().length());
	}



	@Override
	public void onKeyUp(KeyUpEvent event) {
		switch(event.getNativeKeyCode()) {
		case KeyCodes.KEY_ESCAPE:
			if(error) {
				redraw();
				unselect();
				error = false;
			} else {
				mediator.hidePopup(false);
			}
			break;
		case KeyCodes.KEY_ENTER:
			ValueChangeEvent.fire(this, getText());
			break;
		default:
			break;
		}

	}

	public boolean isError() {
		return error;
	}


}
