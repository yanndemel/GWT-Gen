package com.hiperf.common.ui.client.widget;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LinkedCellPanel extends VerticalPanel implements HasMouseOutHandlers, HasMouseOverHandlers {

	private LinkedMenuCellPanel menuPanel;

	public class LinkedMenuCellPanel extends HorizontalPanel {
		private FlexTable menu;
		private boolean select;
		private boolean delete;
		private boolean edit;
		private boolean add;
		private boolean view;

		public LinkedMenuCellPanel(int width, int height) {
			super();
			menu = new FlexTable();
			menu.setStyleName("hover-color");
			setStyleName("hover-color");
			setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			setHeight(height+"px");
			menu.setBorderWidth(0);
			menu.setCellPadding(0);
			menu.setWidth("100%");
			DOM.setStyleAttribute(menu.getElement(), "textAlign", "center");
			setWidth(width+"px");
			add(menu);
		}
		public void setSelect(boolean select) {
			this.select = select;
		}
		public void setDelete(boolean delete) {
			this.delete = delete;
		}
		public void setEdit(boolean edit) {
			this.edit = edit;
		}
		public boolean isSelect() {
			return select;
		}
		public boolean isDelete() {
			return delete;
		}
		public boolean isEdit() {
			return edit;
		}

		public boolean isAdd() {
			return add;
		}
		public void setAdd(boolean add) {
			this.add = add;
		}
		public FlexTable getMenu() {
			return menu;
		}
		public boolean isView() {
			return view;
		}
		public void setView(boolean view) {
			this.view = view;
		}
		
		@Override
		public void clear() {
			menu.clear();
			select = false;
			delete = false;
			edit = false;
			add = false;
			view = false;
		}


	}

	public LinkedCellPanel() {
		super();
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	}

	public LinkedMenuCellPanel initMenuPanel(int width, int height) {
		if(menuPanel == null) {
			menuPanel = new LinkedMenuCellPanel(width, height);
			menuPanel.setVisible(false);
			insert(menuPanel, 0);
		}
		return menuPanel;
	}



	public LinkedMenuCellPanel getMenuPanel() {
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

	
}
