package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.safehtml.client.HasSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;

public class ResizeDialogBox extends ResizePopupPanel implements HasHTML,
		HasSafeHtml {

	public interface Caption extends HasAllMouseHandlers, HasHTML, HasSafeHtml,
			IsWidget {
	}

	/**
	 * Default implementation of Caption. This will be created as the header if
	 * there isn't a header specified.
	 */
	public class CaptionImpl extends HTML implements Caption {

		public CaptionImpl() {
			super();
			setStyleName("Caption");
		}
		
	}


	/**
	 * The default style name.
	 */
	protected static final String DEFAULT_STYLENAME = "naked-dialogBox";

	private Caption caption;

	/**
	 * Creates an empty dialog box specifying its "auto-hide" and "modal"
	 * properties. It should not be shown until its child widget has been added
	 * using {@link #add(Widget)}.
	 *
	 * @param autoHide
	 *            <code>true</code> if the dialog should be automatically hidden
	 *            when the user clicks outside of it
	 * @param modal
	 *            <code>true</code> if keyboard and mouse events for widgets not
	 *            contained by the dialog should be ignored
	 */
	public ResizeDialogBox(boolean autoHide, boolean modal) {
		super(autoHide, modal);
		caption = new CaptionImpl();

		// Add the caption to the top row of the decorator panel. We need to
		// logically adopt the caption so we can catch mouse events.
		
		Widget w = caption.asWidget();
		mainTable.setWidget(0, 0, w);
		setMovingPanelElement(w.getElement());
		//adopt(caption.asWidget());

		// Set the style name
		setStyleName(DEFAULT_STYLENAME);


	}
	
	

	public ResizeDialogBox() {
		/*Not modal because of scrolling problems in some browsers*/
		this(false, false);
	}



	/**
	 * Provides access to the dialog's caption.
	 *
	 * @return the logical caption for this dialog box
	 */
	public Caption getCaption() {
		return caption;
	}

	public String getHTML() {
		return caption.getHTML();
	}

	public String getText() {
		return caption.getText();
	}

	
	/**
	 * Sets the html string inside the caption by calling its
	 * {@link #setHTML(SafeHtml)} method.
	 *
	 * Use {@link #setWidget(Widget)} to set the contents inside the
	 * {@link ResizeDialogBox}.
	 *
	 * @param html
	 *            the object's new HTML
	 */
	public void setHTML(SafeHtml html) {
		caption.setHTML(html);
	}

	/**
	 * Sets the html string inside the caption by calling its
	 * {@link #setHTML(SafeHtml)} method. Only known safe HTML should be
	 * inserted in here.
	 *
	 * Use {@link #setWidget(Widget)} to set the contents inside the
	 * {@link ResizeDialogBox}.
	 *
	 * @param html
	 *            the object's new HTML
	 */
	public void setHTML(String html) {
		caption.setHTML(SafeHtmlUtils.fromTrustedString(html));
	}

	/**
	 * Sets the text inside the caption by calling its {@link #setText(String)}
	 * method.
	 *
	 * Use {@link #setWidget(Widget)} to set the contents inside the
	 * {@link ResizeDialogBox}.
	 *
	 * @param text
	 *            the object's new text
	 */
	public void setText(String text) {
		caption.setText(text);
	}



	@Override
	protected void doAttachChildren() {
		try {
			super.doAttachChildren();
		} finally {
			// See comment in doDetachChildren for an explanation of this call
			caption.asWidget().onAttach();
		}
	}

	@Override
	protected void doDetachChildren() {
		try {
			super.doDetachChildren();
		} finally {
			/*
			 * We need to detach the caption specifically because it is not part
			 * of the iterator of Widgets that the {@link SimplePanel} super
			 * class returns. This is similar to a {@link ComplexPanel}, but we
			 * do not want to expose the caption widget, as its just an internal
			 * implementation.
			 */
			caption.asWidget().onDetach();
		}
	}



	/**
	 * <b>Affected Elements:</b>
	 * <ul>
	 * <li>-caption = text at the top of the {@link ResizeDialogBox}.</li>
	 * <li>-content = the container around the content.</li>
	 * </ul>
	 *
	 * @see UIObject#onEnsureDebugId(String)
	 */
	@Override
	protected void onEnsureDebugId(String baseID) {
		super.onEnsureDebugId(baseID);
		caption.asWidget().ensureDebugId(baseID + "-caption");
		ensureDebugId(mainTable.getWidget(1, 0).getElement(), baseID, "content");
	}

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		// We need to preventDefault() on mouseDown events (outside of the
		// ResizableDialogBox content) to keep text from being selected when it
		// is dragged.
		NativeEvent nativeEvent = event.getNativeEvent();

		if (!event.isCanceled() && (event.getTypeInt() == Event.ONMOUSEDOWN)
				&& isCaptionEvent(nativeEvent)) {
			nativeEvent.preventDefault();
		}

		super.onPreviewNativeEvent(event);
	}

	private boolean isCaptionEvent(NativeEvent event) {
		EventTarget target = event.getEventTarget();
		if (Element.is(target)) {
			return mainTable.getWidget(0, 0).getElement().getParentElement().isOrHasChild(
					Element.as(target));
		}
		return false;
	}
}
