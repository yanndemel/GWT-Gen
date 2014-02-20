package com.hiperf.common.ui.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class ExcelMainForm extends FlexTable {

	static final String EXCEL_BTN_HEIGHT = "40px";
	static final String EXCEL_BTN_WIDTH = "400px";

	public ExcelMainForm(final TablePanel tp, final ICloseablePopupPanel panel) {
		super();
		Button expBtn = new Button(NakedConstants.constants.exportExcel());
		expBtn.setStyleName("naked-button");
		expBtn.setWidth(EXCEL_BTN_WIDTH);
		expBtn.setHeight(EXCEL_BTN_HEIGHT);
		expBtn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				ICloseablePopupPanel p = panel;
				if(panel == null) {
					p  = ViewHelper.newPopup(true);
				}
				ExcelExportForm excel = new ExcelExportForm(tp.getTable(), 0);
				p.clearMainPanel();
				p.add(excel);
				p.setPopupTitle(NakedConstants.constants.excelExport());
				p.show();
			}
		});
		setWidget(0, 0, expBtn);
		Button impBtn = new Button(NakedConstants.constants.importExcel());
		impBtn.setStyleName("naked-button");
		impBtn.setWidth(EXCEL_BTN_WIDTH);
		impBtn.setHeight(EXCEL_BTN_HEIGHT);
		impBtn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				ICloseablePopupPanel p = panel;
				if(panel == null) {
					p  = ViewHelper.newPopup(true);
				}
				ExcelImportForm excel = new ExcelImportForm(tp, panel);
				p.clearMainPanel();
				p.add(excel);
				p.setPopupTitle(NakedConstants.constants.excelImportExport());
				p.show();
			}
		});
		setWidget(1, 0, impBtn);
	}




}
