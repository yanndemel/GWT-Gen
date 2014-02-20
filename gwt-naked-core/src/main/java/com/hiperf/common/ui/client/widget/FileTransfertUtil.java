package com.hiperf.common.ui.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.hiperf.common.ui.client.exception.ParseException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.shared.IConstants;
import com.hiperf.common.ui.shared.util.LinkedFileInfo;

public class FileTransfertUtil {

	private static String getShortFileName(String filename) {
		if(filename != null && filename.length() > 0) {
			int i = filename.lastIndexOf("\\");
			if(i > 0)
				filename = filename.substring(i + 1);
			/*i = filename.lastIndexOf(".");
			if(i > 0)
				filename = filename.substring(0, i);*/
		}
		return filename;
	}

	public static ICloseablePopupPanel doUpload(final IFilePanel panel, final LinkedFileInfo fi, final boolean changeFileName) {
		final FormPanel fp = new FormPanel();
		fp.setAction(GWT.getModuleBaseURL() + IConstants.FILE_SERVICE);
		final VerticalPanel vp = new VerticalPanel();
	    fp.add(vp);
		vp.add(new Hidden(IConstants.FILE_CLASS, fi.getLinkedFileClassName()));
		vp.add(new Hidden(IConstants.FILE_NAME, fi.getLinkedFileName()));
		vp.add(new Hidden(IConstants.FILE_STORAGE_FIELD, fi.getLinkedFileStorageFieldName()));
		Grid g = new Grid(2,1);
		vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		vp.add(g);
		final FileUpload fu = new FileUpload();
		fu.setName("uploadElt");
		g.setWidget(0, 0, fu);
		g.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		final String oldFileName = panel.getFilename();
		fu.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String s = panel.getWrapper().getAttribute(panel.getAttribute());
				if(s != null && !s.equals(NakedConstants.constants.emptyText())) {
					AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

						@Override
						public void onFailure(Throwable caught) {}

						@Override
						public void onSuccess(Boolean b) {
							if(b!=null && b.booleanValue()) {
								panel.setFilename(getUploadFileName(changeFileName, fu.getFilename()));
							}
						}

					};
					MessageBox.confirm(NakedConstants.constants.replaceDocument(), callback);

				} else {
					panel.setFilename(getUploadFileName(changeFileName, fu.getFilename()));
				}
			}
		});
		return showFileDialog(panel, fi, fp, vp, g, fu, oldFileName);
	}

	protected static ICloseablePopupPanel showFileDialog(final IFilePanel panel,
			final LinkedFileInfo fi, final FormPanel fp,
			final VerticalPanel vp, final Grid g, final FileUpload fu,
			final String oldFileName) {
		final ICloseablePopupPanel cpp = ViewHelper.newPopup(true);
		cpp.add(fp);
		fp.setEncoding(FormPanel.ENCODING_MULTIPART);
	    fp.setMethod(FormPanel.METHOD_POST);

		final Button ok = new Button(NakedConstants.constants.ok());
		ok.setStylePrimaryName("naked-button");
		g.setWidget(1, 0, ok);
		g.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
		ok.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				fp.submit();
			}
		});
		SubmitHandler sh = new SubmitHandler() {

			@Override
			public void onSubmit(SubmitEvent event) {
				if(fu.getFilename() == null || fu.getFilename().length() == 0) {
					ViewHelper.displayError(NakedConstants.constants.errorSelectFile());
					event.cancel();
				} else {
					final String id = panel.getWrapper().getAttribute(fi.getLinkedFileLocalKeyField());
					if(id != null && id.length() > 0) {
						vp.add(new Hidden(IConstants.ID, id));
					}
					ok.setEnabled(false);
				}
			}
		};
		fp.addSubmitHandler(sh);
		SubmitCompleteHandler sch = new SubmitCompleteHandler() {

			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String s = event.getResults();
				boolean hide = true;
				try {
					int idx = s.indexOf(IConstants.RESPONSE_TOKEN);
					if(idx >= 0) {
						String id = s.substring(idx + 3);
						id = id.substring(0, id.indexOf(IConstants.RESPONSE_TOKEN));
						String linkedFileLocalKeyField = fi.getLinkedFileLocalKeyField();
						panel.getWrapper().setAttribute(linkedFileLocalKeyField, null);
						panel.getWrapper().setAttribute(linkedFileLocalKeyField, id);
						panel.setId(id);
					} else {
						idx = s.indexOf(IConstants.ERROR_TOKEN);
						if(idx >= 0) {
							String msg = s.substring(idx + 4);
							msg = msg.substring(0, msg.indexOf(IConstants.ERROR_TOKEN));
							MessageBox.alert(msg, true);
						} else
							MessageBox.alert(s, true);
						panel.setFilename(oldFileName);		
						hide = false;
					}
				} catch (ParseException e) {
					ViewHelper.showException(e);
					panel.setFilename(oldFileName);
					hide = false;
				}
				cpp.hide();
				if(!hide)
					showFileDialog(panel, fi, fp, vp, g, fu, oldFileName);
			}
		};
		fp.addSubmitCompleteHandler(sch);
		/*cpp.addCloseHandler(new CloseHandler<ICloseablePopupPanel>() {

			@Override
			public void onClose(CloseEvent<ICloseablePopupPanel> event) {
				if(fu.getFilename() == null || fu.getFilename().length() == 0) {
					//panel.getLabel().removeFromParent();
				}
			}
		});*/
		Element parentElt = fp.getElement().getParentElement();
		parentElt.setAttribute("align", "center");
		parentElt.getParentElement().getParentElement().getParentElement().setAttribute("width", "100%");
		cpp.setPopupTitle(NakedConstants.constants.selectUploadFile());
		cpp.center();
		cpp.show();
		return cpp;
	}

	public static String getUploadFileName(boolean changeFileName,
			String file) {
		return changeFileName ? getShortFileName(file) : file;
	}



}
