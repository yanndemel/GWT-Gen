package com.hiperf.common.ui.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAutoHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.widget.LinkedFileCellPanel.LinkedFileMenuCellPanel;
import com.hiperf.common.ui.shared.IConstants;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.LinkedFileInfo;

public class LinkedFileCell extends Composite implements ICell, IFilePanel {

	public interface Images extends ClientBundle {
		ImageResource iconDownload();
		ImageResource iconUpload();
		ImageResource edit();
	}

	protected LinkedFileCellPanel cellPanel;
	protected Label label;
	protected IWrapper wrapper;
	protected String attribute;
	protected boolean editable;
	protected boolean showLink;
	private LabelCell labelTb;

	private HandlerRegistration mouseOverHandler;
	private HandlerRegistration mouseOutHandler;
	protected IPanelMediator mediator;

	public static final Images images = GWT.create(Images.class);

	public LinkedFileCell() {
		super();
	}

	public LinkedFileCell(final IWrapper wrapper, final String attribute, boolean editable, boolean fileNameEditable, IPanelMediator mediator) {
		this(wrapper, attribute, editable, fileNameEditable, true, true, mediator);
	}

	public LinkedFileCell(final IWrapper wrapper, final String attribute, boolean editable, boolean fileNameEditable, boolean showLink, boolean redraw, IPanelMediator mediator) {
		this.wrapper = wrapper;
		this.attribute = attribute;
		this.editable = editable;
		this.showLink = showLink;
		this.cellPanel = new LinkedFileCellPanel();
		this.mediator = mediator;
		if(fileNameEditable) {
			this.label = null;
			this.labelTb = new LabelCell(wrapper, attribute, mediator);
			labelTb.setAutoHorizontalAlignment(HasAutoHorizontalAlignment.ALIGN_CENTER);
			this.cellPanel.add(labelTb);
		} else {
			this.label = new Label();
			this.label.setWordWrap(false);
			if(showLink)
				this.label.setStylePrimaryName("underline");
			this.cellPanel.add(label);
		}


		initWidget(this.cellPanel);
		if(showLink)
			initMouseHandlers();
		if(redraw)
			redraw();
		if(showLink) {
			cellPanel.setStylePrimaryName("underline");
		}
		cellPanel.setWidth("100%");
		cellPanel.setHeight("100%");
	}

	protected void initMouseHandlers() {
		mouseOverHandler = cellPanel.addMouseOverHandler(new MouseOverHandler() {



			@Override
			public void onMouseOver(MouseOverEvent event) {
				event.stopPropagation();
				HorizontalPanel mp = cellPanel.getMenuPanel();
				if(mp == null)
					mp = initMenuPanel();
				if(!mp.isVisible()) {
					mp.setVisible(true);
					setTextVisible(false);
					/*timer = initTimer();
					timer.scheduleRepeating(5000);*/
				}
			}
		});
		mouseOutHandler = cellPanel.addMouseOutHandler(new MouseOutHandler() {

			@Override
			public void onMouseOut(MouseOutEvent event) {
				event.stopPropagation();
				if(cellPanel.getMenuPanel() != null) {
					cellPanel.hideMenu();
					setTextVisible(true);
					//timer.cancel();
				}
			}
		});
	}

	protected LinkedFileMenuCellPanel initMenuPanel() {
		LinkedFileMenuCellPanel menuPanel = cellPanel.initMenuPanel(getOffsetWidth(), getOffsetHeight());
		initDownloadMenu(menuPanel);
		if(editable) {
			initUploadMenu(menuPanel);
		}
		if(label == null) {
			initEditNameMenu(menuPanel);
		}
		/*String text;
		if(label != null) {
			text = label.getText();
			if(text != null && text.length() > 0) {
				menuPanel.setWidth(label.getOffsetWidth() + "px");
			}
		} else {
			text = labelTb.getText();
			if(text != null && text.length() > 0) {
				menuPanel.setWidth(labelTb.getOffsetWidth() + "px");
			}
		}*/
		return menuPanel; 
	}

	protected void initEditNameMenu(LinkedFileMenuCellPanel menuPanel) {
		if(!menuPanel.isEdit() && canEdit()) {
			Image i = new Image(images.edit());
			i.setStyleName("little-button");
			menuPanel.addImage(0, 2, i);
			i.addClickHandler(onClickEdit());
			i.setTitle(editFileName());
			menuPanel.setEdit(true);
		}
	}

	protected String editFileName() {
		return NakedConstants.constants.editName();
	}

	protected ClickHandler onClickEdit() {
		return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				cellPanel.getMenuPanel().setVisible(false);
				setTextVisible(true);
				TextBoxCell tb = new TextBoxCell(wrapper, attribute, mediator, true);
				mediator.showPopupPanel(tb, labelTb);
				tb.setFocus(true);
				event.stopPropagation();
			}

		};
	}

	protected boolean canEdit() {
		return true;
	}

	protected void initUploadMenu(LinkedFileMenuCellPanel menuPanel) {
		final LinkedFileInfo fi = getLinkedFileInfo();
		if(fi != null && !menuPanel.isUpload()) {
			Image i = new Image(images.iconUpload());
			i.setStyleName("little-button");
			menuPanel.addImage(0, 1, i);
			i.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					doUpload(fi);
					cellPanel.getMenuPanel().setVisible(false);
					setTextVisible(true);
				}

			});
			i.setTitle(NakedConstants.constants.upload());
			menuPanel.setUpload(true);
		}

	}

	protected void doDownload() {
		boolean frameExists = (RootPanel.get("downloadiframe") != null);
		if(frameExists) {
			Widget widgetFrame = (Widget)RootPanel.get("downloadiframe");
			widgetFrame.removeFromParent();
		}
		LinkedFileInfo fi = getLinkedFileInfo();
		String id = wrapper.getAttribute(fi.getLinkedFileLocalKeyField());
		NamedFrame frame = new NamedFrame("downloadiframe");
		frame.setUrl(URL.encode(GWT.getModuleBaseURL() + IConstants.FILE_SERVICE + "?"+IConstants.FILE_CLASS+"="+fi.getLinkedFileClassName()+
				"&"+IConstants.FILE_NAME+"="+fi.getLinkedFileName()+"&"+IConstants.FILE_STORAGE_FIELD+"="+fi.getLinkedFileStorageFieldName()+
				"&"+IConstants.ACTION+"="+IConstants.DOWNLOAD_ACTION+"&"+IConstants.ID+"="+id));
		frame.setVisible(false);
		RootPanel.get().add(frame);
	}

	protected boolean initDownloadMenu(LinkedFileMenuCellPanel menuPanel) {
		LinkedFileInfo fi = getLinkedFileInfo();
		if(fi != null) {
			String id = wrapper.getAttribute(fi.getLinkedFileLocalKeyField());
			if(id != null && id.length() > 0 && !menuPanel.isDownload()) {
				Image i = new Image(images.iconDownload());
				i.setStyleName("little-button");
				menuPanel.addImage(0, 0, i);
				i.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						doDownload();
						cellPanel.getMenuPanel().setVisible(false);
						setTextVisible(true);
					}

				});
				i.setTitle(NakedConstants.constants.download());
				menuPanel.setDownload(true);
			}
			return true;
		}
		return false;
	}

	@Override
	public String getAttribute() {
		return attribute;
	}

	@Override
	public IWrapper getWrapper() {
		return wrapper;
	}

	protected Image displayLoadingImage() {
		final Image i = new Image(AbstractLinkedCell.images.loadingAnimation());
		i.setTitle(NakedConstants.constants.loadingData());
		setTextVisible(false);
		cellPanel.add(i);
		return i;
	}

	protected void setTextVisible(boolean b) {
		if(label != null)
			label.setVisible(b);
		else
			labelTb.setVisible(b);
	}

	protected void hideLoadingImage(final Image i) {
		i.setVisible(false);
		setTextVisible(true);
		redraw();
	}

	@Override
	public void redraw() {
		String s = wrapper.getAttribute(attribute);
		if(s != null && s.length() > 0)
			setCellText(s);
		else {
			LinkedFileInfo fi = getLinkedFileInfo();
			String id = wrapper.getAttribute(fi.getLinkedFileLocalKeyField());
			if(id != null && id.length() > 0) {
				PersistenceServiceAsync srv = PersistenceService.Util.getInstance();
				final Image i = displayLoadingImage();
				AsyncCallback<String> callback = new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						ViewHelper.displayError(caught, NakedConstants.constants.errorFileName());
						hideLoadingImage(i);
					}

					@Override
					public void onSuccess(String result) {

						try {
							if(wrapper.getAttribute(attribute) == null) {
								if(result != null && result.length() > 0)
									wrapper.setAttribute(attribute, result, false);
								else
									wrapper.setAttribute(attribute, NakedConstants.constants.emptyText(), false);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						hideLoadingImage(i);
					}
				};

				srv.getFileName(fi.getLinkedFileClassName(), fi.getLinkedFileName(), id, callback);
			} else
				setCellText(NakedConstants.constants.emptyText());

			/* else {
				 try {
					String ss = wrapper.getAttribute(fi.getLinkedFileName());
					 if(ss != null && !ss.isEmpty())
						 setCellText(ss);
					 else
						 setCellText(NakedConstants.constants.emptyText());
				} catch (AttributeNotFoundException e) {
					setCellText(NakedConstants.constants.emptyText());
				}
			}*/
		}
	}

	/**
	 * @return
	 */
	protected LinkedFileInfo getLinkedFileInfo() {
		return WrapperContext.getFieldInfoByName().get(wrapper.getWrappedClassName()).get(attribute).getLinkedFileInfo();
	}

	public void setFilename(String s) {
		try {
			wrapper.setAttribute(attribute, s);
			setCellText(s);
		} catch (Exception e) {}
	}

	public String getFilename() {
		return wrapper.getAttribute(attribute);
	}

	protected void setCellText(String s) {
		if(label != null)
			label.setText(s);
		else
			this.labelTb.setText(s);
	}

	@Override
	public void setId(String id) {}

	@Override
	public void setTabIndex(int idx) {
	}

	@Override
	public Widget getLabel() {
		return label == null ? labelTb : label;
	}

	protected void removeMouseHandlers() {
		if(mouseOutHandler != null)
			mouseOutHandler.removeHandler();
		if(mouseOverHandler != null)
			mouseOverHandler.removeHandler();			
	}

	protected void doUpload(final LinkedFileInfo fi) {
		FileTransfertUtil.doUpload(LinkedFileCell.this, fi, false);
	}

}
