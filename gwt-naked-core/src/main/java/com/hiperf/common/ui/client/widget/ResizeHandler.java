package com.hiperf.common.ui.client.widget;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.widget.WrappedFlexTable.HeaderCell;
import com.hiperf.common.ui.shared.HeaderInfo;

public class ResizeHandler {

	private WrappedFlexTable table;

	public Element tHeadElement;
	public Element headerRowElt;
	public Element lastMoveElement;
	public Element startMoveElement;
	public boolean dragging;
	public int dragStartColumn;
	public Element draggedCell;
	public String draggedAttribute;
	public int droppedColumn;
	public ResizeHandlerImpl resizeListener;

	public ResizeHandler(WrappedFlexTable table) {
		this.lastMoveElement = null;
		this.startMoveElement = null;
		this.dragging = false;
		this.dragStartColumn = -1;
		this.draggedCell = null;
		this.draggedAttribute = null;
		this.droppedColumn = -1;
		this.resizeListener = null;
		this.table = table;
	}



	private class ResizeHandlerImpl implements EventListener {
        /** currently selected TH element */
        private com.google.gwt.user.client.Element th;
        private com.google.gwt.user.client.Element tr;
        /** start mouse position */
        private int startX;
        /** current X position of the cursor */
        private int currentX;
        private int originalThWidth;
        private int thIndex;
        private int originalFollowingThWidth;
        private boolean resizing = false;

        private Set<Integer> autoFittedColumns = new HashSet<Integer>();

        private ResizeTimer timer = new ResizeTimer(this);

        public ResizeHandlerImpl() {}

        public void onBrowserEvent(Event event) {
			switch(DOM.eventGetType(event)) {
				case Event.ONDBLCLICK:
					event.stopPropagation();
	    			if(isOverBorder(event, getTh(event))) {
	    				autoFit(event);
	    			}
	    			break;
        		case Event.ONMOUSEDOWN:
        			event.stopPropagation();
        			com.google.gwt.user.client.Element myTh = getTh(event);
        			if (isOverBorder(event, myTh))
                		startResizing(event, myTh);
                	else
                		startDragging(event);
        			break;
        		case Event.ONMOUSEUP:
        			event.stopPropagation();
        			if (resizing)
        				stopResizing(event);
        			else if(th == null && !stopDragging(true)) {
    					doSort(DOM.eventGetTarget(event));
    				}
        			break;
        		case Event.ONMOUSEMOVE:
        			event.stopPropagation();
        			////GWT.log("Mouse MOVE");
        			if(resizing && th != null) {
        				this.currentX = getPositionX(event);
        				//interruptResizing(event);
        			}
        			else if(dragging && th == null) {
        				moveColumn(event);
        			}
        			if(!dragging && !resizing)
    					setCursor(event);
        			break;
        		case Event.ONMOUSEOUT:
        			event.stopPropagation();
        			if(th != null && resizing) {
						int top = table.getAbsoluteTop();
						int y = getPositionY(event);
						int x = getPositionX(event);
						if(y <= top || y >= (top + th.getOffsetHeight()) || x > table.getOffsetWidth() + table.getAbsoluteLeft())
							stopResizing(event);
        			} /*else if(th == null && dragging) {
        				int y = getPositionY(event);
        				int top = table.getAbsoluteTop();
        				GWT.log("y = "+y+", top =" +top+", startMoveElement.getOffsetHeight() = "+startMoveElement.getOffsetHeight());
						if(y <= top || y >= (top + startMoveElement.getOffsetHeight())) {
							stopDragging(false);
						}
        			}*/
        			break;
        		default:
        			break;

        	}
        }

		private void startDragging(Event nativeEvent) {
    		dragging = true;
    		lastMoveElement = DOM.eventGetTarget(nativeEvent);
    		startMoveElement = lastMoveElement;
    		com.google.gwt.user.client.Element td = lastMoveElement;
    		com.google.gwt.user.client.Element bodyElement = table.getTableBodyElement();
    		for (; td != null; td = DOM.getParent(td)) {
    		  // If it's a TD, it might be the one we're looking for.
    		  if (DOM.getElementProperty(td, "tagName").equalsIgnoreCase("th")) {
    			  break;
    		  }
    		  // If we run into this table's body, we're out of options.
    		  if (td == bodyElement) {
    		    return;
    		  }
    		}

    		if (td == null)
    			return;
    		Element tr = DOM
    				.getParent((com.google.gwt.user.client.Element) td);
    		dragStartColumn = DOM.getChildIndex(
    				(com.google.gwt.user.client.Element) tr,
    				(com.google.gwt.user.client.Element) td);
    		HeaderCell widget = table.getHeaderWidgets().get(dragStartColumn);
    		draggedAttribute = ((HeaderCell)widget).getAttribute();
    		draggedCell = widget.getElement();
    		DOM.setStyleAttribute(draggedCell, "opacity", "0.75");
    		DOM.setStyleAttribute(draggedCell, "filter", "alpha(opacity)");
    	}

    	public void doSort(com.google.gwt.user.client.Element elt) {
    		if(table.getRowCount()>1) {
    			com.google.gwt.user.client.Element td = elt;
        		com.google.gwt.user.client.Element bodyElement =  table.getTableBodyElement();
        		for (; td != null; td = DOM.getParent(td)) {
        		  // If it's a TD, it might be the one we're looking for.
        		  if (DOM.getElementProperty(td, "tagName").equalsIgnoreCase("th")) {
        			  break;
        		  }
        		  // If we run into this table's body, we're out of options.
        		  if (td == bodyElement) {
        		    return;
        		  }
        		}
        		if (td == null)
        			return;
        		Element tr = DOM
        				.getParent((com.google.gwt.user.client.Element) td);
        		int sortIdx = DOM.getChildIndex(
        				(com.google.gwt.user.client.Element) tr,
        				(com.google.gwt.user.client.Element) td);
        		if(sortIdx >= table.getFirstDataCol()) {
        			final HeaderCell headerCell = table.getHeaderWidgets().get(sortIdx);
        			table.clearSortColumns(headerCell);
        			sortHeader(headerCell);
        		}
    		}
    	}

    	private boolean stopDragging(boolean drop) {
    		try {
    			if (drop && droppedColumn >= table.getFirstDataCol()) {
    				HeaderInfo hi = ViewHelper.getHeaderInfo(draggedAttribute, table.getSortedFields());
    				int oldIdx = hi.getIndex();
    				int dropIdx = droppedColumn - table.getFirstDataCol();
    				////GWT.log("dropIdx = "+dropIdx+", oldIdx = "+oldIdx);
    				if(dropIdx != oldIdx) {
    					com.google.gwt.user.client.Element thElement = table.getThElement(droppedColumn);
						DOM.setElementAttribute(thElement, "class", "custom-table-th");
						DOM.setElementAttribute(thElement, "className", "custom-table-th");
						TablePanelMediator mediator = table.getMediator();
						mediator.hideColumn(hi);
    					hi.setIndex(dropIdx);
    					mediator.insertColumn(hi);
    					return true;
    				}
    			}
    		} finally {
    			DOM.setStyleAttribute(draggedCell, "opacity", "100");
    			//DOM.setStyleAttribute(draggedCell, "filter", "none");
    			ViewHelper.showPointerCursor(startMoveElement);
    			for(int i = 0; i<headerRowElt.getChildCount(); i++) {
	        		com.google.gwt.user.client.Element thElement = table.getThElement(i);
	        		ViewHelper.showPointerCursor(thElement);
					DOM.setElementAttribute(thElement, "class", "custom-table-th");
	        		DOM.setElementAttribute(thElement, "className", "custom-table-th");
	        	}
    			startMoveElement = null;
    			droppedColumn = -1;
    			draggedCell = null;
    			dragStartColumn = -1;
    			lastMoveElement = null;
    			draggedAttribute = null;
    			dragging = false;
    		}
    		return false;
    	}

    	private void moveColumn(Event nativeEvent) {
    		if (dragging) {
    			com.google.gwt.user.client.Element td = DOM.eventGetTarget(nativeEvent);
    			if(td == startMoveElement) {
    				ViewHelper.showMoveCursor(td);
    			}
    			if(td == lastMoveElement)
    				return;
    			lastMoveElement = td;
    			com.google.gwt.user.client.Element bodyElement = table.getTableBodyElement();
    		    for (; td != null; td = DOM.getParent(td)) {
    		      // If it's a TD, it might be the one we're looking for.
    		      String t = DOM.getElementProperty(td, "tagName");
    		      if (t == null)
      				return;
    		      if (t.equalsIgnoreCase("th")) {
    		    	  break;
    		      }
    		      // If we run into this table's body, we're out of options.
    		      if (td == bodyElement) {
    		        return;
    		      }
    		    }

    			if (td == null)
    				return;
    			for(int i=0; i<table.getHeaderWidgets().size(); i++) {
    				if(table.getThElement(i) == td) {
    					droppedColumn = i;
    					break;
    				}
    			}
    			Element tr = DOM
    					.getParent((com.google.gwt.user.client.Element) td);
    			int row = DOM.getChildIndex(table.getTableBodyElement(),
    					(com.google.gwt.user.client.Element) tr);
    			if (row == -1 && droppedColumn >= table.getFirstDataCol()) {

    				if(droppedColumn > dragStartColumn) {
    					DOM.setElementAttribute(td, "class", "custom-table-th-drop-right");
    					DOM.setElementAttribute(td, "className", "custom-table-th-drop-right");
    				}
    				else if(droppedColumn < dragStartColumn) {
    					DOM.setElementAttribute(td, "class", "custom-table-th-drop-left");
    					DOM.setElementAttribute(td, "className", "custom-table-th-drop-left");
    				}
    		        if(droppedColumn > (dragStartColumn  + 1)) {
    		        	for(int i = dragStartColumn; i<droppedColumn; i++) {
    		        		com.google.gwt.user.client.Element thElement = table.getThElement(i);
							DOM.setElementAttribute(thElement, "class", "custom-table-th");
    		        		DOM.setElementAttribute(thElement, "className", "custom-table-th");
    		        	}
    		        } else if(droppedColumn < dragStartColumn  - 1) {
    		        	for(int i = droppedColumn + 1; i<dragStartColumn; i++) {
    		        		com.google.gwt.user.client.Element thElement = table.getThElement(i);
							DOM.setElementAttribute(thElement, "class", "custom-table-th");
							DOM.setElementAttribute(thElement, "className", "custom-table-th");
    		        	}
    		        }

    			}

    		}
    	}


        /**
         * Sets a cursor style.
         *
         * @param event is an event.
         */
        protected void setCursor(Event event) {
        	com.google.gwt.user.client.Element th = getTh(event);
            if (this.th != null || (th != null && isOverBorder(event, th))) {
                DOM.setStyleAttribute(DOM.eventGetTarget(event), "cursor", "e-resize");
                this.currentX = getPositionX(event);
            } else
                DOM.setStyleAttribute(DOM.eventGetTarget(event), "cursor", "pointer");
        }


        /**
         * This method normally stops resizing and changes column width.
         *
         * @param event is an event to prevent on stop.
         */
        protected void stopResizing(Event event) {
        	if(getPositionX(event) != startX) {
        		doStopResizing(event);
        	}
        }

		private void doStopResizing(Event event) {
			resizing = false;
			timer.cancel();
			th = null;
			tr = null;
			startX = -1;
			currentX = -1;
			thIndex = -1;
			originalThWidth = -1;
			ViewHelper.preventEvent(event);
		}

        /**
         * Resizes selected and sibling columns.
         */
        protected void resize() {
            int position = this.currentX;
            int delta = position - startX;
            //GWT.log("delta = "+delta);
            if (delta != 0) {
                int thExpectedWidth = originalThWidth + delta;
                DOM.setStyleAttribute(th, "width", thExpectedWidth+"px");
                removeNoWrap(thIndex);

                if(thIndex != DOM.getChildCount(DOM.getParent(th)) - 1) {
                	DOM.setStyleAttribute(table.getThElement(thIndex + 1), "width", (originalThWidth + originalFollowingThWidth - thExpectedWidth)+"px");
                	removeNoWrap(thIndex + 1);
                }


            }
        }

		private void removeNoWrap(int col) {
			if(autoFittedColumns.contains(col)) {
				for(int j = 0; j<table.getRowCount(); j++) {
			    	Widget w = table.getWidget(j, col);
			    	if(w != null)
			    		w.removeStyleName("noWrap");
			    }
				autoFittedColumns.remove(col);
			}
		}

        private void autoFit(Event event) {
        	com.google.gwt.user.client.Element myTh = getTh(event);
        	if (isOverBorder(event, myTh)) {
        		int xPos = getPositionX(event);
        		com.google.gwt.user.client.Element myTr = DOM.getParent(myTh);
                int myThIndex = DOM.getChildIndex(myTr, myTh);
                int left = DOM.getAbsoluteLeft(myTh);
                if(xPos <= left + 2) {
                	myThIndex--;
                	if(myThIndex <= 0) {
                    	return;
                    } else {
                    	myTh = table.getThElement(myThIndex);
                    }
                }
                if(myThIndex < table.getFirstDataCol()) {
                	return;
                }

                DOM.setStyleAttribute(table.getElement(), "width", "1em");
                DOM.setStyleAttribute(myTh, "width", "100%");
            	initResizeHandler();


                for(int i = table.getFirstDataCol(); i<table.getHeaderWidgets().size(); i++) {
                	if(i != myThIndex) {
                		com.google.gwt.user.client.Element thElement = table.getThElement(i);
                		String styleAttribute = DOM.getStyleAttribute(thElement, "width");
						if(styleAttribute != null && (styleAttribute.endsWith("px") || styleAttribute.endsWith("em"))) {
							//DOM.removeElementAttribute(thElement, "width");
							DOM.setStyleAttribute(thElement, "width", "auto");
						}
                	}
                }
                if(!autoFittedColumns.contains(myThIndex)) {
                	for(int j = 0; j<table.getRowCount(); j++) {
                		table.getWidget(j, myThIndex).addStyleName("noWrap");
                    }
                    autoFittedColumns.add(myThIndex);
                }
                DOM.setStyleAttribute(table.getElement(), "whiteSpace", "pre");
                doStopResizing(event);

        	}
		}

        public Set<Integer> getAutoFittedColumns() {
			return autoFittedColumns;
		}

		/**
         * This method starts column resizing.
         *
         * @param event is an event.
         */
        protected void startResizing(Event event, com.google.gwt.user.client.Element myTh) {
            th = myTh;
            resizing = true;
            startX = getPositionX(event);
            currentX = startX;
            tr = DOM.getParent(th);
            thIndex = DOM.getChildIndex(tr, th);
            int left = DOM.getAbsoluteLeft(th);
            if(startX <= left + 2) {
            	thIndex--;
            	if(thIndex <= 0) {
                	th = null;
                	return;
                } else {
                	th = table.getThElement(thIndex);
                }
            }
            originalThWidth = getElementWidth(th);
            if(thIndex < table.getFirstDataCol()) {
            	th = null;
            	return;
            }

            if(thIndex < DOM.getChildCount(DOM.getParent(th)) - 1)
            	originalFollowingThWidth = getElementWidth(table.getThElement(thIndex + 1));
            else
            	originalFollowingThWidth = -1;
            DOM.setStyleAttribute(table.getElement(), "whiteSpace", "normal");
            DOM.setStyleAttribute(table.getElement(), "width", "100%");
            int cellCount = table.getHeaderWidgets().size();
			for(int i = table.getFirstDataCol(); i<cellCount; i++) {
        		com.google.gwt.user.client.Element thElement = table.getThElement(i);
        		int offsetWidth = thElement.getOffsetWidth();
        		DOM.setStyleAttribute(thElement, "whiteSpace", "normal");
        		DOM.setElementPropertyInt(thElement, "offsetWidth",  offsetWidth);
        		//DOM.setStyleAttribute(thElement, "width", offsetWidth+"p");
            }
            timer.scheduleRepeating(20);

            ViewHelper.preventEvent(event);
        }

        /**
         * Gets element height.
         *
         * @param element is an element
         * @return height in pizels.
         */
        protected int getElementHeight(com.google.gwt.user.client.Element element) {
            return DOM.getElementPropertyInt(element, "offsetHeight");
        }

        /**
         * Gets mouse X position.
         *
         * @param event is an event.
         * @return X position in pixels.
         */
        protected int getPositionY(Event event) {
            return DOM.eventGetClientY(event);
        }

        /**
         * Gets mouse Y position.
         *
         * @param event is an event.
         * @return Y position in pixels.
         */
        protected int getPositionX(Event event) {
            return DOM.eventGetClientX(event);
        }

        /**
         * This method looks for the TH element starting from the element which produced the event.
         *
         * @param event is an event.
         * @return a TH element or <code>null</code> if there is no such element.
         */
        protected com.google.gwt.user.client.Element getTh(Event event) {
        	com.google.gwt.user.client.Element element = DOM.eventGetTarget(event);
            while (element != null && !DOM.getElementProperty(element, "tagName").equalsIgnoreCase("th"))
                element = DOM.getParent(element);
            return element;
        }

        /**
         * This method detects whether the cursor is over the border between columns.
         *
         * @param event is an event.
         * @param th is TH element.
         * @return a result of check.
         */
        protected boolean isOverBorder(Event event, com.google.gwt.user.client.Element th) {
            int position = getPositionX(event);
            int left = DOM.getAbsoluteLeft(th);
            int width = getElementWidth(th);
            int index = DOM.getChildIndex(DOM.getParent(th), th);

            return position <= left + 1 && index > 0
                   || position >= left + width - 1 && index <= DOM.getChildCount(DOM.getParent(th)) - 1;
        }

		private void sortHeader(final HeaderCell headerCell) {
			table.displaySortArrow(headerCell);
			headerCell.sort();
		}

    }

    /**
     * This timer is invoked every time when column resizing might happen.
     *
     * @author <a href="mailto:sskladchikov@gmail.com">Sergey Skladchikov</a>
     */
    protected static class ResizeTimer extends Timer {
        /** resize listener that starts this timer */
        private ResizeHandlerImpl listener;

        /**
         * Creates the timer.
         *
         * @param listener is a resize listener.
         */
        public ResizeTimer(ResizeHandlerImpl listener) {
            this.listener = listener;
        }

        /**
         * See class docs.
         */
        public void run() {
            if (listener.th != null) {
                listener.resize();
            }
        }
    }

    /**
     * Gets element width.
     *
     * @param element is an element
     * @return width in pixels
     */
    private static int getElementWidth(com.google.gwt.user.client.Element element) {
        if (element == null) {
            return 0;
        }
        String widthStyle = DOM.getStyleAttribute(element, "top").toLowerCase();
        String px = Style.Unit.PX.name().toLowerCase();

        if (widthStyle != null && widthStyle.endsWith(px)) {
            return Integer.parseInt(widthStyle.substring(0, widthStyle.indexOf(px)));
        } else {
            return DOM.getElementPropertyInt(element, "clientWidth");
        }
    }

	public void createResizeListener() {
		this.resizeListener = new ResizeHandlerImpl();

	}

	public void removeAutoFittedColumn(int toDisplay) {
		if(resizeListener != null) {
			resizeListener.getAutoFittedColumns().remove(toDisplay);
		}
}

	public void initResizeHandler() {
		for(int i = 0; i < DOM.getChildCount(headerRowElt); i++) {
			Element child = DOM.getChild(headerRowElt, i);
			DOM.setStyleAttribute(child, "whiteSpace", "nowrap");
		}
	}
}