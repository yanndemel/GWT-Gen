package com.hiperf.common.ui.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ResizeDialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class MessageBox {

	private static final String INFO = "Information";

	public static enum TYPE{ALERT, INFO, CONFIRM, CONFIRM_YN, CONFIRM_YNC};

	private static ResizeDialogBox   dialog     = null;
	private static Grid dialogContent = null;
	private static HorizontalPanel buttonsPanel = null;

	public interface Images extends ClientBundle {
		ImageResource about();
		ImageResource warning();
	}

	public static final Images images = GWT.create(Images.class);

	public static void info(String title, String txt) {
		info(txt, title, null, false);
	}
	
	

	private static void setBoxTitle(String title) {
		setPopupTitle(dialog, title);
	}



	public static void setPopupTitle(ResizeDialogBox dialog, String title) {
		HTML html = (HTML)dialog.getCaption();
		html.setHTML(CloseablePopupPanel.CLOSE_TABLE_1 + "<span>" + title + "</span>"  + CloseablePopupPanel.CLOSE_TABLE_2);
		DivElement captionDiv = DivElement.as(html.getElement());
		captionDiv.setAttribute("style", "height: 40px; vertical-align: middle; cursor: move;");
	}

	public static void info(String txt) {
		info(INFO, txt);
	}
	
	public static void info(String txt, boolean html) {
		info(txt, INFO, null, html);
	}

	public static void info(String txt, String title, AsyncCallback<Boolean> callback, boolean html) {
		if (dialog == null) {
			initDialogBox(TYPE.INFO, callback);
		} else {
			reinitContent(TYPE.INFO, callback);
		}
		if(html)
			dialogContent.setWidget(0, 1, new HTML(txt));
		else
			dialogContent.setText(0, 1, txt);
		if(title != null)
			setBoxTitle(title);
		else
			setBoxTitle(NakedConstants.constants.info());
		dialog.show();
		dialog.center();
	}

	public static void alert(String txt) {
		alert(txt, null, false);
	}

	public static void alert(String txt, boolean html) {
		alert(txt, null, html);
	}

	public static void alert(String txt, boolean html, boolean alignLeft) {
		alert(txt, null, html, alignLeft);
	}
	
	public static void alert(String txt, AsyncCallback<Boolean> callback, boolean html) {
		alert(txt, callback, html, false);
	}
	
	public static void alert(String txt, AsyncCallback<Boolean> callback, boolean html, boolean alignLeft) {
		if (dialog == null) {
			initDialogBox(TYPE.ALERT, callback, alignLeft);
		} else {
			reinitContent(TYPE.ALERT, callback, alignLeft);
		}
		if(html)
			dialogContent.setHTML(0, 1, txt);
		else
			dialogContent.setText(0, 1, txt);
		setBoxTitle(NakedConstants.constants.alert());
		dialog.show();
		dialog.center();
	}



	private static void initDialogBox(TYPE type, AsyncCallback<Boolean> callback, boolean leftAlign) {
		dialog = initDialog();
	    /*dialog.setModal(true);
	    dialog.addStyleName("cursor-default");*/
	    dialogContent = new Grid(1,2);
	    dialogContent.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_MIDDLE);
	    dialogContent.getColumnFormatter().setWidth(0, "32px");
	    dialogContent.getColumnFormatter().setWidth(1, "428px");
	    DOM.setStyleAttribute(dialogContent.getCellFormatter().getElement(0, 1), "fontSize", "16px");
	    VerticalPanel vp = new VerticalPanel();
	    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

	    vp.add(dialogContent);
	    buttonsPanel = new HorizontalPanel();
	    buttonsPanel.setSpacing(5);
	    buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	    buttonsPanel.setWidth("100%");
	    vp.setCellHeight(buttonsPanel, "40px");
	    reinitContent(type,callback, leftAlign);

	    vp.add(buttonsPanel);
		vp.setSpacing(10);
	    dialog.add(vp);

	}



	protected static void align(boolean leftAlign) {
		if(leftAlign)
	    	leftAlignContent();
	    else
	    	centerContent();
	}
	
	private static void initDialogBox(TYPE type, AsyncCallback<Boolean> callback) {
		initDialogBox(type, callback, false);
	}



	protected static void centerContent() {
		dialogContent.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		DOM.setStyleAttribute(dialogContent.getCellFormatter().getElement(0, 1), "paddingLeft", "0px");
	}


	public static void leftAlignContent() {
		dialogContent.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
		DOM.setStyleAttribute(dialogContent.getCellFormatter().getElement(0, 1), "paddingLeft", "20px");
	}

	private static ResizeDialogBox initDialog() {
		ResizeDialogBox dialog = new ResizeDialogBox(false, true);
		setStyle(dialog);
	    return dialog;
	}



	public static void setStyle(ResizeDialogBox dialog) {
		dialog.setStylePrimaryName("naked-dialogBox");
	    dialog.getElement().getStyle().setZIndex(99);
	    dialog.setWidth("500px");
	    dialog.setGlassEnabled(true);
	    dialog.setAnimationEnabled(true);
	}

	private static void reinitContent(TYPE type, AsyncCallback<Boolean> callback) {
		reinitContent(type, callback, false);
	}

	private static void reinitContent(TYPE type, AsyncCallback<Boolean> callback, boolean alignLeft) {
		switch(type) {
	    case ALERT:
	    	dialogContent.setWidget(0, 0, new Image(images.warning()));
	    	dialog.addStyleDependentName("warning");
	    	addCloseButton(callback);
	    	break;
	    case INFO:
	    	dialogContent.setWidget(0, 0, new Image(images.about()));
	    	dialog.removeStyleDependentName("warning");
	    	addCloseButton(callback);
	    	break;
	    case CONFIRM:
	    	dialogContent.setWidget(0, 0, new Image(images.about()));
	    	dialog.removeStyleDependentName("warning");
	    	addOkCancelButtons(callback);
	    	break;
	    case CONFIRM_YN:
	    	dialogContent.setWidget(0, 0, new Image(images.about()));
	    	dialog.removeStyleDependentName("warning");
	    	addYesNoButtons(callback);
	    	break;
	    case CONFIRM_YNC:
	    	dialogContent.setWidget(0, 0, new Image(images.about()));
	    	dialog.removeStyleDependentName("warning");
	    	addYesNoCancelButtons(callback);
	    	break;
	    default:
	    	break;
	    }
		align(alignLeft);
	}

	public static void confirm(final String title, final String txt, AsyncCallback<Boolean> callback) {
		if (dialog == null) {
			initDialogBox(TYPE.CONFIRM, callback);
		} else {
			reinitContent(TYPE.CONFIRM, callback);
		}

		dialogContent.setText(0, 1, txt);
		setBoxTitle(title);
		dialog.show();
		dialog.center();
	}

	public static void confirm(TYPE type, final String title, final String txt, AsyncCallback<Boolean> callback) {
		confirm(type, title, txt, false, callback);
	}
	
	public static void confirm(TYPE type, final String title, final String txt, boolean html, AsyncCallback<Boolean> callback) {
		if (dialog == null) {
			initDialogBox(type, callback);
		} else {
			reinitContent(type, callback);
		}
		if(html)
			dialogContent.setHTML(0, 1, txt);
		else
			dialogContent.setText(0, 1, txt);
		setBoxTitle(title);
		dialog.show();
		dialog.center();
	}

	public static void confirm(String txt, AsyncCallback<Boolean> callback) {
		confirm(NakedConstants.constants.confirm(), txt, callback);
	}

	private static void addOkCancelButtons(final AsyncCallback<Boolean> callback) {
		buttonsPanel.clear();
		Button b = new Button(NakedConstants.constants.cancel());
		b.setStylePrimaryName("naked-button");
		b.setWidth("100px");
		b.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
			  event.stopPropagation();
			  dialog.hide();
			  callback.onSuccess(false);
		  }
		});
		buttonsPanel.add(b);
		buttonsPanel.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_RIGHT);
		b = new Button(NakedConstants.constants.ok());
		b.setStylePrimaryName("naked-button");
		b.setWidth("100px");
		b.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
		    dialog.hide();
		    callback.onSuccess(true);
		  }
		});
		buttonsPanel.add(b);
		buttonsPanel.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_LEFT);
	}

	private static void addYesNoCancelButtons(final AsyncCallback<Boolean> callback) {
		addYesNoButtons(callback);
		Button b = new Button(NakedConstants.constants.cancel());
		b.setStylePrimaryName("naked-button");
		b.setWidth("100px");
		b.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
			  event.stopPropagation();
			  dialog.hide();
			  callback.onSuccess(null);
		  }
		});
		buttonsPanel.add(b);
		buttonsPanel.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_RIGHT);

	}

	public static void addYesNoButtons(final AsyncCallback<Boolean> callback) {
		buttonsPanel.clear();
		Button b = new Button(NakedConstants.constants.yes());
		b.setStylePrimaryName("naked-button");
		b.setWidth("100px");
		b.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
		    dialog.hide();
		    callback.onSuccess(true);
		  }
		});
		buttonsPanel.add(b);
		buttonsPanel.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_RIGHT);
		b = new Button(NakedConstants.constants.no());
		b.setStylePrimaryName("naked-button");
		b.setWidth("100px");
		b.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
		    dialog.hide();
		    callback.onSuccess(false);
		  }
		});
		buttonsPanel.add(b);
		buttonsPanel.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_LEFT);
	}

	private static void addCloseButton(final AsyncCallback<Boolean> callback) {
		buttonsPanel.clear();
		Button close = new Button(NakedConstants.constants.close());
		close.setStylePrimaryName("naked-button");
		close.setWidth("200px");
		close.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
		    onHide(callback);
		  }
		});
		buttonsPanel.add(close);
		buttonsPanel.setCellHorizontalAlignment(close, HasHorizontalAlignment.ALIGN_CENTER);
	}


	public static String getTextIfShowing() {
		if(dialog.isShowing() && dialogContent != null)
			return dialogContent.getText(0, 1);
		return null;
	}



	public static void onHide(final AsyncCallback<Boolean> callback) {
		dialog.hide();
		if(callback != null) {
			callback.onSuccess(true);
		}
	} 

}
