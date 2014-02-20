package com.hiperf.common.ui.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ResizeDialogBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.i18n.NakedConstants;

public class CloseablePopupPanel extends ResizeDialogBox {

	//private static final String NAKED_DIALOG_BOX = "naked-dialogBox";
	public static final String CLOSE_TABLE_1 = "<table width=\"100%\" height=\"100%\" ><tr><td align=\"center\" width=\"100%\"/>";
	public static final String CLOSE_TABLE_2 = "<td></td></tr></table>";

	public interface Images extends ClientBundle {
		ImageResource close_small();
	}

	private boolean hideOnEscape;
	private boolean resize = true;
	private boolean forceClose = false;
	private ScrollPanel scrollPanel = null;
	private FlexTable mainPanel = null;
	//private HorizontalPanel caption = null;
	private PopupMediator mediator;
	private Image img;

	public static final Images images = GWT.create(Images.class);

	public void initPopup(boolean hideOnEscape) {
		setStylePrimaryName(DEFAULT_STYLENAME);
		setGlassEnabled(true);
		this.hideOnEscape = hideOnEscape;
		mediator = new PopupMediator(this);
		init();
	}

	public CloseablePopupPanel() {
		super();
	}

	public void init() {
		
		newCloseImage();

		HTML caption = initCloseImage();
		
		// Add click handler to caption
		caption.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
			  event.stopPropagation();
		    // Get x,y caption click relative to the anchor
		    final int x = event.getRelativeX(img.getElement());
		    final int y = event.getRelativeY(img.getElement());
		    //GWT.log("x="+x+", y="+y+" -- img.getOffsetWidth()="+img.getOffsetWidth()+", img.getOffsetHeight()="+img.getOffsetHeight());
		    // Check click was within bounds of anchor
		    if(x >= 0 && y >= 0 &&
		      x <= img.getOffsetWidth() &&
		      y <= img.getOffsetHeight()) {
		        // Raise event on anchor
		    	img.fireEvent(event);
		    }
		  }
		});

		mainPanel = new FlexTable();
		mainPanel.setBorderWidth(0);
		mainPanel.setCellPadding(1);
		mainPanel.setCellSpacing(0);
		scrollPanel = new ScrollPanel(mainPanel);
		super.add(scrollPanel);
		
		
		
	}

	public Image newCloseImage() {
		this.img = new Image(images.close_small());
		img.setTitle(NakedConstants.constants.closePopup());
		img.setStylePrimaryName("close-button");
		img.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				hide();
			}
		});
		return img;
	}

	public HTML initCloseImage() {
		if(getCaption() != null && getCaption().getText() != null && getCaption().getText().length() > 0) {
			getCaption().setHTML(CLOSE_TABLE_1 + "<span>" + getCaption().getText() + "</span>"  + CLOSE_TABLE_2);
		}
		else
			getCaption().setHTML(CLOSE_TABLE_1 + "&nbsp;" + CLOSE_TABLE_2);		
		final HTML caption = ((HTML)getCaption());
		DivElement captionDiv = DivElement.as(caption.getElement());
		captionDiv.setAttribute("style", "height: 40px; vertical-align: middle; cursor: move;");
		Element el = caption.getElement();
		Element tdElt;
		if(el.getChildCount() > 0) {
			Node child = el.getChild(0);
			if(child.getChildCount() > 0)
				child = child.getChild(0);
			if(child.getChildCount() > 0)
				child = child.getChild(0);
			if(child.getChildCount() > 1) {
				child = child.getChild(1);
				tdElt = TableCellElement.as(child);
				tdElt.setAttribute("style", "cursor: pointer;");
				tdElt.appendChild(img.getElement());
			}
			else if(child.getChildCount() > 0) {
				child = child.getChild(0);
				tdElt = TableCellElement.as(child);
				tdElt.appendChild(img.getElement());
			} else 
				child.appendChild(img.getElement());
		}
		return caption;
	}

		@Override
	public void add(final Widget w) {

		if(mainPanel!=null) {
			mainPanel.setWidget(1, 0, w);
			DOM.setElementAttribute(mainPanel.getParent().getElement(), "align", "center");
			if(resize) {
				addPanelResizedListener(new PanelResizeListener() {

					@Override
					public void onResized(Integer width, Integer height) {
						mainPanel.setWidth("100%");
						mainPanel.setHeight("100%");
						mainPanel.getFlexCellFormatter().setWidth(1, 0, "100%");
						mainPanel.getFlexCellFormatter().setHeight(1, 0, "100%");
						Widget widget = mainPanel.getWidget(1, 0);
						widget.setWidth("100%");
						widget.setHeight("100%");

						if(widget instanceof AbstractObjectPanel) {
							AbstractObjectPanel w2 = (AbstractObjectPanel)widget;
							w2.getObjectForm().setWidth("100%");
							w2.getObjectForm().setHeight("100%");
							w2.getObjectForm().getFormGrid().setWidth("100%");
							w2.getObjectForm().getFormGrid().setHeight("100%");
						} else if(widget instanceof TablePanel) {
							TablePanel w2 = (TablePanel)widget;
							w2.getTable().setWidth("100%");
							w2.getTable().setHeight("100%");
							w2.getPanel().setWidth("100%");
							w2.getPanel().setHeight("100%");

						}
					}
				});
			}
		}
		else {
			super.add(w);
			if(resize) {
				addPanelResizedListener(new PanelResizeListener() {

					@Override
					public void onResized(Integer width, Integer height) {
						w.setWidth("100%");
						w.setHeight("100%");
					}
				});				
			}
		}

	}



	@Override
	protected void onLoad() {
		int clientWidth = Window.getClientWidth();
		if(clientWidth < getOffsetWidth()) {
			scrollPanel.setWidth(clientWidth + "px");
		}
		super.onLoad();
	}

	public void clearMainPanel() {
		if(mainPanel!=null  && mainPanel.getRowCount() > 1) {
			Element div = mainPanel.getWidget(1,0).getParent().getParent().getElement();
			if(div != null) {
				String s = div.getAttribute("style");
				if(s != null) {
					int i = s.indexOf("width");
					if(i >= 0) {
						s = removeAttribute(div, s, i);
					}
					i = s.indexOf("height");
					if(i >= 0) {
						s = removeAttribute(div, s, i);
					}
					getContainerElement().removeAttribute("width");
					getContainerElement().removeAttribute("height");
				}
			}
			mainPanel.clearCell(1, 0);

		}
	}

	private String removeAttribute(Element div, String s, int i) {
		int j = s.indexOf(";", i);
		if(j>i && j+1 < s.length()) {
			if(i>0)
				s = s.substring(0, i) + s.substring(j + 1);
			else
				s = s.substring(j + 1);
		} else if(i>0)
			s = s.substring(0, i);
		else
			s = "";
		div.setAttribute("style", s);
		return s;
	}

	public Widget getMainWidget() {
		if(mainPanel!=null && mainPanel.getRowCount() > 1)
			return mainPanel.getWidget(1,0);
		return getWidget();
	}

	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		if(hideOnEscape) {
			Event e = Event.as(event.getNativeEvent());

			switch(e.getTypeInt()) {
				case Event.ONKEYUP:
					switch(e.getKeyCode()) {
						case KeyCodes.KEY_ESCAPE:
							hide();
							event.cancel();
							break;
						default:
							break;
					}
					break;
				default:
					break;
			}
		}

	}
	
	



	public boolean isForceClose() {
		return forceClose;
	}

	public void setForceClose(boolean forceClose) {
		this.forceClose = forceClose;
	}

	@Override
	public void clear() {
		super.clear();
		mainPanel = null;
	}

	public PopupMediator getMediator() {
		return mediator;
	}

	@Override
	public void setPopupTitle(String s) {
		getCaption().setText(s);
		initCloseImage();
	}

	public void setResize(boolean resize) {
		this.resize = resize;
	}
	
	
}
