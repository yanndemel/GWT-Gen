package com.hiperf.common.ui.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LinkedFileCellPanel extends VerticalPanel implements HasMouseOutHandlers, HasMouseOverHandlers, HasClickHandlers {

	private LinkedFileMenuCellPanel menuPanel;

	public class LinkedFileMenuCellPanel extends HorizontalPanel {
		private FlexTable menu;
		private boolean download;
		private boolean upload;
		private boolean edit;
		private int tot = 0;

		public LinkedFileMenuCellPanel(int width, int height) {
			super();
			menu = new FlexTable();
			menu.setStyleName("hover-color");
			setStyleName("hover-color");
			setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			setHeight(height+"px");
			menu.setBorderWidth(0);
			menu.setCellPadding(0);
			menu.setWidth("100%");
			setWidth(width+"px");
			add(menu);
		}

		public boolean isDownload() {
			return download;
		}

		public void setDownload(boolean download) {
			this.download = download;
			if(download)
				tot++;
		}

		public boolean isUpload() {
			return upload;
		}

		public void setUpload(boolean upload) {
			this.upload = upload;
			if(upload)
				tot++;
		}

		public boolean isEdit() {
			return edit;
		}

		public void setEdit(boolean e) {
			this.edit = e;
			if(e)
				tot++;
		}

		public void addImage(int row, int col, Image i) {
			if(tot == 0)
				col = 0;
			menu.setWidget(row, col, i);
			if(tot == 0) {
				menu.getFlexCellFormatter().setWidth(0, 0, "100%");
			}
			if(tot == 1) {
				menu.getFlexCellFormatter().setWidth(0, 0, "50%");
				menu.getFlexCellFormatter().setWidth(0, 1, "50%");
			}
			if(tot == 2) {
				menu.getFlexCellFormatter().setWidth(0, 0, "33%");
				menu.getFlexCellFormatter().setWidth(0, 1, "33%");
				menu.getFlexCellFormatter().setWidth(0, 2, "33%");
			}
			menu.getFlexCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
		}

	}

	public LinkedFileCellPanel() {
		super();
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	}

	public LinkedFileMenuCellPanel initMenuPanel(int width, int height) {
		menuPanel = new LinkedFileMenuCellPanel(width, height);
		menuPanel.setVisible(false);
		insert(menuPanel, 0);
		return menuPanel;
	}



	public HorizontalPanel getMenuPanel() {
		return menuPanel;
	}


	@Override
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return addDomHandler(handler, MouseOutEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return addDomHandler(handler, MouseOverEvent.getType());

	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return addDomHandler(handler, ClickEvent.getType());
	}

	public void hideMenu() {
		menuPanel.setVisible(false);
		menuPanel = null;
	}



}
