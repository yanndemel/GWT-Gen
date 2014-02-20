package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.model.LazyListModel;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.IConstants;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrapperContext;

public class ExcelExportForm extends FormPanel {

	private WrappedFlexTable table;
	private List<HeaderInfo> sortedFields;
	private VerticalPanel panel;
	private VerticalPanel mainPanel;
	private int top;

	public ExcelExportForm(WrappedFlexTable table, int top) {
		super();
		this.table = table;
		this.top = top;
		setAction(GWT.getModuleBaseURL() + IConstants.EXPORT_EXCEL_SERVICE + "?" + IConstants.CLASS + "=" + getClassName(table));
		setMethod(FormPanel.METHOD_POST);
		setEncoding(FormPanel.ENCODING_MULTIPART);
		this.panel = new VerticalPanel();
		setWidget(panel);
		panel.add(new Hidden(IConstants.CLASS, getClassName(table)));
		this.sortedFields = new ArrayList<HeaderInfo>();
		Map<String, IFieldInfo> availableColumns = table.getFieldInfoByName();
		for(Entry<String, IFieldInfo> e :availableColumns.entrySet()) {
			if(!e.getValue().isHidden()) {
				String att = e.getKey();			
				this.sortedFields.add(new HeaderInfo(att, availableColumns.get(att)));				
			}
		}
		Collections.sort(sortedFields);
		redraw();
		
	}

	public String getClassName(WrappedFlexTable table) {
		IWrapperListModel rows = table.getRows();
		if(rows instanceof LazyListModel) {
			LazyListModel m = (LazyListModel) rows;
			if(m.isMappedCollection()) {
				return m.getNakedObjectName();
			} else {
				return m.getParentWrapper().getWrappedClassName();
			}
		}
		return table.getClassName();
	}

	private void redraw() {
		mainPanel = new VerticalPanel();
		panel.add(mainPanel);
		Label l = new Label(NakedConstants.constants.availableColumns());
		l.setStyleName("popup-title");
		mainPanel.add(l);
		FlexTable g = new FlexTable();
		g.setStyleName("config-table");
		final ScrollPanel sc = new ScrollPanel(g);
		mainPanel.add(sc);
		g.insertRow(0);
		g.getCellFormatter().setStyleName(0, 0, "config-table-th");
		g.setText(0, 0, NakedConstants.constants.label());
		g.getCellFormatter().setStyleName(0, 1, "config-table-th");
		g.getCellFormatter().addStyleName(0, 1, "config-table-border-left");
		g.setText(0, 1, NakedConstants.constants.export());
		g.getCellFormatter().setStyleName(0, 2, "config-table-th");
		g.getCellFormatter().addStyleName(0, 2, "config-table-border-left");
		g.setText(0, 2, NakedConstants.constants.index());
		redrawGrid(g);
		final Button b = new Button(NakedConstants.constants.export());
		b.addClickHandler( new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				b.setEnabled(false);
				submit();
				MessageBox.info(NakedConstants.constants.waitForFile());				
			}
		});
		b.setStylePrimaryName("naked-button");
		b.setWidth(ExcelMainForm.EXCEL_BTN_WIDTH);
		b.setHeight(ExcelMainForm.EXCEL_BTN_HEIGHT);
		mainPanel.add(b);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				if(mainPanel.isAttached()) {
					int h = Window.getClientHeight() - top;
					if(h>200)
						h -= 100;
					
					sc.setHeight(Math.min(h, mainPanel.getOffsetHeight())  + "px");
					sc.setWidth((mainPanel.getOffsetWidth() + 20)  + "px");
				}
			}
		});
	}

	private void redrawGrid(final FlexTable g) {
		int row = 1;
		for(final HeaderInfo hi : sortedFields) {
			g.getRowFormatter().setStyleName(row, "custom-table-tr");

			TextBox tb = new TextBox();
			tb.setName(IConstants.LB_COL+row);
			tb.setText(WrapperContext.getHeaderDisplayed(table.getClassName(), hi));
			g.setWidget(row, 0, tb);
			final TextBox idxTb = new TextBox();
			idxTb.setName(IConstants.IDX_COL+row);
			final String idx = Integer.toString(row-1);
			idxTb.setWidth("20px");
			idxTb.setValue(idx);
			idxTb.addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					try {
						int i = Integer.parseInt(event.getValue());
						int colsNb = getDisplayedColumnsNumber();
						if(i >=0 && i<colsNb) {
							int oldIdx = hi.getIndex();
							HeaderInfo swapCol = null;
							for(HeaderInfo info : sortedFields) {
								if(i==info.getIndex()) {
									swapCol = info;
									break;
								}
							}
							if(swapCol!=null) {
								swapCol.setIndex(oldIdx);
							}
							hi.setIndex(i);
							Collections.sort(sortedFields);
							redrawGrid(g);
						} else {
							MessageBox.alert(NakedConstants.messages.errorValueOutOfBounds(colsNb - 1));
							idxTb.setValue(idx);
						}
					} catch(Exception e) {
						e.printStackTrace();
						idxTb.setValue(idx);
					}
				}
			});
			idxTb.setEnabled(hi.isDisplayed());
			CheckBox cb = new CheckBox();
			cb.setValue(hi.isDisplayed());
			cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if(event.getValue()) {
						hi.setIndex(getDisplayedColumnsNumber());
						hi.setDisplayed(true);
						idxTb.setEnabled(true);
					} else {
						int oldIdx = hi.getIndex();
						int newIdx = getDisplayedColumnsNumber() - 1;
						hi.setIndex(newIdx);
						hi.setDisplayed(false);
						for(HeaderInfo info : sortedFields) {
							int index = info.getIndex();
							if(info.isDisplayed() && index > oldIdx) {
								info.setIndex(index - 1);
							}
						}
					}
					Collections.sort(sortedFields);
					redrawGrid(g);
				}
			});
			g.setWidget(row, 1, cb);
			g.setWidget(row, 2, idxTb);
			Hidden h1 = new Hidden(IConstants.ATT_COL+row, hi.getAttribute());
			g.setWidget(row, 3, h1);
			row++;
		}
		IWrapperListModel rows = table.getRows();
		if(rows instanceof LazyListModel) {
			LazyListModel m = (LazyListModel) rows;
			g.setWidget(row, 0, new Hidden(IConstants.LAZY, Boolean.TRUE.toString()));
			row++;
			String id = PersistenceManager.getId(m.getParentWrapper()).toStringValue();
			g.setWidget(row, 0, new Hidden(IConstants.ID, id));
			row++;
			if(m.isMappedCollection()) {
				g.setWidget(row, 0, new Hidden(IConstants.MAPPED_BY, m.getMappedBy()));
			} else {
				g.setWidget(row, 0, new Hidden(IConstants.ATTRIBUTE, m.getAttribute()));
			}
			row++;
		} else if(table.getFilter() != null)
			g.setWidget(row, 0, new Hidden(IConstants.FILTER, table.getFilter()));
	}


	private int getDisplayedColumnsNumber() {
		int i = 0;
		for(HeaderInfo hi : sortedFields) {
			if(hi.isDisplayed())
				i++;
		}
		return i;
	}

}
