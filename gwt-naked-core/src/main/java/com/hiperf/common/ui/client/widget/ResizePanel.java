package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class ResizePanel extends SimplePanel {
	private boolean bDragDrop = false;
	private int moveStartX, moveStartY;
	private boolean move = false ;
	private Element movingPanelElement;
	private List<PanelResizeListener> panelResizedListeners = new ArrayList<PanelResizeListener>();



	public ResizePanel(Element elem) {
		super(elem);
		listenForEvents();
	}

	public ResizePanel() {
	        super();
	        listenForEvents();
	}

	private void listenForEvents() {
		//listen to mouse-events
		DOM.sinkEvents(this.getElement(),
				Event.ONMOUSEDOWN |
				Event.ONMOUSEMOVE |
		        Event.ONMOUSEUP |
		        Event.ONMOUSEOVER
		);
	}

	/**
	 * processes the mouse-events to show cursor or change states
	 *  - mouseover
	 *  - mousedown
	 *  - mouseup
	 *  - mousemove
	 */
	@Override
	public void onBrowserEvent(Event event) {
        final int eventType = DOM.eventGetType(event);
        if (Event.ONMOUSEOVER == eventType) {
        	//show different cursors
            if (isCursorResize(event)) {
                DOM.setStyleAttribute(this.getElement(), "cursor", "se-resize");
            } else if(isCursorMove(event)){
                DOM.setStyleAttribute(this.getElement(),"cursor", "se-resize");
            }else {
                DOM.setStyleAttribute(this.getElement(), "cursor", "default");
            }
        }
        if (Event.ONMOUSEDOWN == eventType) {
            if (isCursorResize(event)) {
            	//enable/disable resize
                if (bDragDrop == false) {
                	bDragDrop = true;

                    DOM.setCapture(this.getElement());
                }
            } else if(isCursorMove(event)){
                DOM.setCapture(this.getElement());
                move = true;
                moveStartX = event.getScreenX() - this.getElement().getAbsoluteLeft();
                moveStartY = event.getScreenY() - this.getElement().getAbsoluteTop();
                addStyleName("unselectable");
            }
        } else if (Event.ONMOUSEMOVE == eventType) {
        	//reset cursor-type
            if(!isCursorResize(event)&&!isCursorMove(event)){
                DOM.setStyleAttribute(this.getElement(), "cursor", "default");
            }

            //calculate and set the new size
            if (bDragDrop == true) {
            	int absX = DOM.eventGetClientX(event);
                int absY = DOM.eventGetClientY(event);
                int originalX = DOM.getAbsoluteLeft(this.getElement());
                int originalY = DOM.getAbsoluteTop(this.getElement());

                //do not allow mirror-functionality
                if(absY>originalY && absX>originalX){
                    Integer height = absY-originalY+2;
                    this.setHeight(height + "px");

                    Integer width = absX-originalX+2;
                    this.setWidth(width + "px");
                    notifyPanelResizedListeners(width, height);
                }
            }else if(move == true){
                RootPanel.get().setWidgetPosition(this, event.getScreenX() - moveStartX, event.getScreenY() - moveStartY);
            }
        } else if (Event.ONMOUSEUP == eventType) {
        	//reset states
            if(move == true){
                move = false;
                DOM.releaseCapture(this.getElement());
            }
            if (bDragDrop == true) {
            	bDragDrop = false;
                DOM.releaseCapture(this.getElement());
            }
            removeStyleName("unselectable");
        }
	}

	/**
	 * returns if mousepointer is in region to show cursor-resize
	 * @param event
	 * @return true if in region
	 */
	protected boolean isCursorResize(Event event) {
        int cursorY  = DOM.eventGetClientY(event);
        int initialY = this.getAbsoluteTop();
        int height   = this.getOffsetHeight();

        int cursorX  = DOM.eventGetClientX(event);
        int initialX = this.getAbsoluteLeft();
        int width    = this.getOffsetWidth();

        //only in bottom right corner (area of 10 pixels in square)
        if (((initialX + width - 10) < cursorX && cursorX <= (initialX + width)) &&
        	((initialY + height - 10) < cursorY && cursorY <= (initialY + height)))
            return true;
        else
            return false;
	}

	/**
	 * sets the element in panel
	 * @param movingPanelElement
	 */
	public void setMovingPanelElement(Element movingPanelElement) {
        this.movingPanelElement = movingPanelElement;
	}

	/**
	 * is cursor in moving state?
	 * @param event event to process
	 * @return true if cursor is in movement
	 */
	protected boolean isCursorMove(Event event){
		if(movingPanelElement!=null){
	        int cursorY = DOM.eventGetClientY(event);
	        int cursorX = DOM.eventGetClientX(event);
	        //GWT.log("cursorX = "+cursorX+", cursorY = "+cursorY+", top = "+movingPanelElement.getAbsoluteTop()+", bottom = "+movingPanelElement.getAbsoluteBottom()+", left = "+movingPanelElement.getAbsoluteLeft()+", right = "+movingPanelElement.getAbsoluteRight());
	        if(cursorY >= movingPanelElement.getAbsoluteTop() && cursorY <= movingPanelElement.getAbsoluteBottom()
	        		&& movingPanelElement.getAbsoluteLeft() <= cursorX && cursorX <= movingPanelElement.getAbsoluteRight())
	        	return true;
	 		else
	 			return false;
		}else
			return false;
	}

    /**
     * Interface function to add a listener to this event
     * @param listener
     */
	public void addPanelResizedListener(PanelResizeListener listener) {
        panelResizedListeners.add(listener);
    }

	/**
     * Interface function to emit signal
     */
	private void notifyPanelResizedListeners(Integer width,Integer height) {
        for (PanelResizeListener p : panelResizedListeners) {
            p.onResized(width,height);
        }
    }
}