package com.hiperf.common.ui.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ResizeDialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class PendingChangesDialogBox extends ResizeDialogBox {

	private static final String LARGE_BUTTON_STYLE = "large-button";
	private static final String DIALOG_WIDTH = "600px";

	private HorizontalPanel buttonsPanel;

	private static final PendingChangesDialogBox INSTANCE = new PendingChangesDialogBox();

	public static PendingChangesDialogBox getInstance() {
		return INSTANCE;
	}

	private PendingChangesDialogBox() {
		super();
		MessageBox.setStyle(this);
		
	    setWidth(DIALOG_WIDTH);
	    
	    Grid dialogContent = new Grid(1,2);
	    dialogContent.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_MIDDLE);
	    dialogContent.getColumnFormatter().setWidth(0, "32px");
	    dialogContent.getColumnFormatter().setWidth(1, "100%");
	    dialogContent.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
	    dialogContent.setWidget(0, 0, new Image(MessageBox.images.warning()));
	    VerticalPanel vp = new VerticalPanel();
	    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	    vp.setWidth("100%");
	    vp.add(dialogContent);
	    buttonsPanel = new HorizontalPanel();
	    buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	    buttonsPanel.setWidth("100%");
	    vp.setCellHeight(buttonsPanel, "1em");
	    vp.add(buttonsPanel);
		vp.setSpacing(10);
	    add(vp);
	    dialogContent.setText(0, 1, NakedConstants.constants.questionPendingModifs());
	    MessageBox.setPopupTitle(this, NakedConstants.constants.confirm());

	}

	public void show(AsyncCallback<Boolean> callback) {
		addButtons(callback);
		if(!isShowing()) {
			show();
			center();
		}
	}

	public void addButtons(final AsyncCallback<Boolean> callback) {
		buttonsPanel.clear();
		Button b = new Button(NakedConstants.constants.saveContinue());
		b.setStylePrimaryName(LARGE_BUTTON_STYLE);
		b.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
		    hide();
		    callback.onSuccess(true);
		  }
		});
		buttonsPanel.add(b);
		buttonsPanel.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_LEFT);
		b = new Button(NakedConstants.constants.discardContinue());
		b.setStylePrimaryName(LARGE_BUTTON_STYLE);
		b.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
		    hide();
		    callback.onSuccess(false);
		  }
		});
		buttonsPanel.add(b);
		buttonsPanel.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_CENTER);
		b = new Button(NakedConstants.constants.stayCurrentPage());
		b.setStylePrimaryName(LARGE_BUTTON_STYLE);
		b.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
			  event.stopPropagation();
			  hide();
			  callback.onSuccess(null);
		  }
		});
		buttonsPanel.add(b);
		buttonsPanel.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_RIGHT);
	}

}
