package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ResizePopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IValidator;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.exception.ParseException;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.util.IntHolder;
import com.hiperf.common.ui.client.validation.ValidationHelper;
import com.hiperf.common.ui.shared.WrapperContext;

public class EditableListBoxCell extends ListBoxCell {

	public interface Images extends ClientBundle {
	    ImageResource add_small();
	    ImageResource delete_small();
	    ImageResource edit_small();
	}

	private static final Images images = GWT.create(Images.class);

	private PopupPanel menuPanel;
	private ResizePopupPanel textBoxPanel;

	private List<IValidator> validators;

	public EditableListBoxCell(IWrapper wrapper, String attribute, IPanelMediator mediator) {
		super(wrapper, attribute, mediator);
		initMenuPanel();
		IFieldInfo fieldInfo = WrapperContext.getFieldInfoByName().get(wrapper.getWrappedClassName()).get(attribute);
		List<IValidator> vs = fieldInfo.getValidators();
		this.validators = vs != null && !vs.isEmpty() ? new ArrayList<IValidator>(vs) : null;
	}

	private class WrappedCollectionTextBox extends TextBox implements ValueChangeHandler<String>, FocusHandler, KeyPressHandler {

		private String initialText;
		private boolean editMode;
		private boolean error;

		public WrappedCollectionTextBox() {
			this(null, false);
		}

		public WrappedCollectionTextBox(String txt, boolean editMode) {
			super();
			addStyleName("custom-table-selected");
			this.initialText = txt;
			this.editMode = editMode;
			this.error = false;
			init();
			addValueChangeHandler(this);
			addFocusHandler(this);
			addKeyPressHandler(this);
		}

		private void init() {
			setText(initialText);
			if(initialText!=null) {
				setVisibleLength(initialText.length());
			}
			setTitle(null);
			textBoxPanel.removeStyleDependentName("warning");
		}

		@Override
		public void onValueChange(ValueChangeEvent<String> event) {
			final String value = event.getValue();
			final int nbValidators = (validators != null)?validators.size():0;
			final IntHolder count = new IntHolder(0);
			try {
				AsyncCallback<String> callback = new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable e) {
						count.increment();
						setError(e.getMessage());
					}

					@Override
					public void onSuccess(String msg) {
						count.increment();
						if(!error && msg == null && (nbValidators == 0 || count.get() == nbValidators)) {
							try {
								if(editMode) {
									wrapper.removeFromCollection(attribute, initialText);
								}
								wrapper.addToCollection(attribute, value);
								setVisible(false);
								textBoxPanel.hide();
							} catch (AttributeNotFoundException e) {
								setError(e.getMessage());
							} catch (ParseException e) {
								setError(e.getMessage());
							}
						} else if(msg != null)
							setError(msg);
					}
				};
				ValidationHelper.validateAttributeValue(wrapper, attribute, value, validators, callback);
			} catch (ValidationException e) {
				setError(e.getMessage());
			}

		}

		private void setError(String e) {
			this.error = true;
			textBoxPanel.addStyleDependentName("warning");
			setTitle(e);
			setSelectionRange(0, getText().length());
		}


		@Override
		public void onFocus(FocusEvent event) {
			setSelectionRange(0, getText().length());
		}

		@Override
		public void onKeyPress(KeyPressEvent event) {
			switch(event.getCharCode()) {
				case KeyCodes.KEY_ESCAPE:
					init();
					break;
				/*case KeyCodes.KEY_ENTER:
					sinkEvents(Event.ONCHANGE);
					textBoxPanel.hide();
					break;*/
				default:
					break;
			}
		}



	}

	protected void initMenuPanel() {
		IFieldInfo info = WrapperContext.getFieldInfoByName().get(wrapper.getWrappedClassName()).get(attribute);
		if(info.isEditable()) {
			menuPanel = new PopupPanel(true);
			textBoxPanel = new ResizePopupPanel(true);
			textBoxPanel.addPanelResizedListener(new PanelResizeListener() {

				@Override
				public void onResized(Integer width, Integer height) {
					textBoxPanel.getWidget().setWidth((width > 4) ? ((width - 4)+"px") : "4px");
				}
			});
			textBoxPanel.setGlassEnabled(true);
			menuPanel.setStylePrimaryName("naked-popupPanel");
			menuPanel.addStyleName("custom-PopupPanel");
			HorizontalPanel panel = new HorizontalPanel();
			menuPanel.add(panel);
			Image i = new Image(images.add_small());
			i.setStyleName("little-button");
			i.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					menuPanel.hide();
					textBoxPanel.clear();
					textBoxPanel.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop());
					WrappedCollectionTextBox tb = new WrappedCollectionTextBox();
					textBoxPanel.add(tb);
					textBoxPanel.show();
					tb.setFocus(true);
				}
			});
			i.setTitle(NakedConstants.constants.addElement());
			panel.add(i);
			i = new Image(images.delete_small());
			i.setStyleName("little-button");
			i.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					int nb = getItemCount();
					int nbSelected = 0;
					for(int i = 0; i<nb; i++) {
						if(isItemSelected(i - nbSelected)) {
							try {
								wrapper.removeFromCollection(attribute, getItemText(i - nbSelected));
								nbSelected++;
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			});
			i.setTitle(NakedConstants.constants.removeElement());
			panel.add(i);
			i = new Image(images.edit_small());
			i.setStyleName("little-button");
			i.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					int nb = getItemCount();
					List<Integer> selectedItems = new ArrayList<Integer>(nb);

					for(int i = 0; i<nb; i++) {
						if(isItemSelected(i)) {
							selectedItems.add(i);
						}
					}
					int size = selectedItems.size();
					if(size == 1) {
						menuPanel.hide();
						textBoxPanel.clear();
						textBoxPanel.center();
						WrappedCollectionTextBox tb = new WrappedCollectionTextBox(getItemText(selectedItems.get(0)), true);
						textBoxPanel.add(tb);
						textBoxPanel.show();
						tb.setFocus(true);
					} else if (size > 1) {
						MessageBox.alert(NakedConstants.constants.infoEditLines());
					}
				}
			});
			i.setTitle(NakedConstants.constants.editElement());
			panel.add(i);

			this.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					menuPanel.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() - 26);
					menuPanel.show();
				}
			});

		}
	}


}
