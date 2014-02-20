package com.hiperf.common.ui.client.widget;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.IConstants;

public class ExcelImportForm extends FormPanel {


	private static final String BASE_URL = GWT.getModuleBaseURL() + "ImportService";

	private VerticalPanel hiddenFields;
	private Button viewBtn;
	private Button impBtn;

	public ExcelImportForm(final TablePanel tp, final ICloseablePopupPanel popup) {
		super();
		setMethod(FormPanel.METHOD_POST);
		setEncoding(FormPanel.ENCODING_MULTIPART);
		final VerticalPanel panel = new VerticalPanel();
		setWidget(panel);
		int i = 0;
		hiddenFields = new VerticalPanel();
		panel.add(hiddenFields);
		final WrappedFlexTable table = tp.getTable();
		Map<String, IFieldInfo> map = table.getFieldInfoByName();
		hiddenFields.add(new Hidden(IConstants.CLASS, table.getClassName()));
		List<HeaderInfo> sf = table.getSortedFields();
		for(Entry<String, IFieldInfo> e : map.entrySet()) {
			IFieldInfo fi = e.getValue();
			if((!fi.isHidden() || fi.isForceImport()) && fi.isImportable() && !fi.isGeneratedId() && (!fi.isJpaTransient() || fi.isForceImport()) && ((!fi.getDataType().equals(DataType.NAKED_OBJECT) && !fi.isCollection()) || fi.getImportAttribute() != null)) {
				hiddenFields.add(new Hidden(IConstants.ATT_COL+i, e.getKey()));
				
				String label = null;
				for(HeaderInfo hi : sf) {
					if(hi.getAttribute().equals(e.getKey())) {
						label = hi.getLabel();
						break;
					}
				}
				if(label == null)
					label = fi.getLabel();
				hiddenFields.add(new Hidden(IConstants.LB_COL+i, label));
				if(fi.getDataType().equals(DataType.NAKED_OBJECT) && fi.getImportAttribute() != null) {
					hiddenFields.add(new Hidden(IConstants.IMP_ATT_COL+i, fi.getImportAttribute()));
				}
				i++;
			}
		}
		viewBtn = new Button(NakedConstants.constants.downloadImportTemplate());
		viewBtn.setStyleName("naked-button");
		viewBtn.setWidth(ExcelMainForm.EXCEL_BTN_WIDTH);
		viewBtn.setHeight(ExcelMainForm.EXCEL_BTN_HEIGHT);
		viewBtn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				setAction(BASE_URL + "?" + IConstants.ACTION + "=" + IConstants.ACTION_VIEW);
				submit();
			}
		});
		panel.add(viewBtn);
		impBtn = new Button(NakedConstants.constants.uploadExcelFile());
		impBtn.setStyleName("naked-button");
		impBtn.setWidth(ExcelMainForm.EXCEL_BTN_WIDTH);
		impBtn.setHeight(ExcelMainForm.EXCEL_BTN_HEIGHT);
		impBtn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				setAction(BASE_URL);
				panel.remove(viewBtn);
				panel.remove(impBtn);
				final FlexTable g = new FlexTable();
				Label l = new Label(NakedConstants.constants.selectUploadFile());
				l.setStyleName("form-table-title-little");
				panel.add(g);
				g.setWidget(0, 0, l);
				g.getCellFormatter().setStyleName(0, 0, "form-table-title");
				final FileUpload fu = new FileUpload();
				fu.setName("uploadElt");
				g.setWidget(1, 0, fu);
				g.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
				final Button ok = new Button(NakedConstants.constants.ok());
				ok.setStylePrimaryName("naked-button");
				g.setWidget(2, 0, ok);
				g.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
				ok.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						submit();
					}
				});
				addSubmitHandler(new SubmitHandler() {

					@Override
					public void onSubmit(SubmitEvent event) {
						if(fu.getFilename() == null || fu.getFilename().length() == 0) {
							ViewHelper.displayError(NakedConstants.constants.errorSelectFile());
							event.cancel();
						} else {
							//fu.setEnabled(false);
							ok.setEnabled(false);
						}
					}
				});
				addSubmitCompleteHandler(new SubmitCompleteHandler() {

					@Override
					public void onSubmitComplete(SubmitCompleteEvent event) {
						String s = event.getResults();
						try {
							int idx = s.indexOf(IConstants.RESPONSE_TOKEN);
							String res = s.substring(idx + 3, s.indexOf(IConstants.RESPONSE_TOKEN, idx+3));
							if(res.equals(IConstants.OK)) {
								popup.hide();
								table.refresh(1, tp.getNbRowsPerPage());
								MessageBox.info(NakedConstants.constants.uploadDone());
							} else {
								popup.hide();
								MessageBox.alert(NakedConstants.constants.errorUpload() + "<br>" + res, true);
							}

						} catch (Exception e) {
							ViewHelper.showException(e);
							popup.hide();
						}

					}
				});




			}
		});
		panel.add(impBtn);
	}


}
