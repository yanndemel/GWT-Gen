package com.hiperf.common.ui.client.widget;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PositionCallback;
import com.google.gwt.user.client.ui.ResizePopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IClassInfo;
import com.hiperf.common.ui.client.ICustomCell;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.ILinkedCell;
import com.hiperf.common.ui.client.IWrappedTable;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.event.HideLoadingPopupEvent;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.util.LazyManager;
import com.hiperf.common.ui.client.widget.TablePanel.TYPE;
import com.hiperf.common.ui.client.widget.TablePanel.TopMenuInfo;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.TableConfig;

public class ViewHelper {

	private static final String LOGIN_PAGE_KEY = "0516084601320131301563xqs561qscsq56q3c3qsc58568";
	private static final PopupPanel waitPopupPanel = new PopupPanel(false, true);
	private static int show = 0;
	private static int zindex = 101;

	public static void showException(String msg, Throwable caught) {
		if(!(caught instanceof StatusCodeException)) {
			String s = caught.getCause() != null ? caught.getCause().getMessage() : caught.getMessage();
			alert(msg, s);
		}
	}
	
	public static void showException(Throwable caught) {
		if(!(caught instanceof StatusCodeException)) {
			String s = caught.getMessage();
			alert(s);
		}
	}
	public static void alert(String msg, String s) {
		if(s != null && !s.isEmpty()) {
			if(s.contains(LOGIN_PAGE_KEY))
				MessageBox.alert(NakedConstants.constants.sessionTimeout());
			else
				MessageBox.alert(msg + " : " + s);
		}
	}
	
	public static void alert(String s) {
		if(s != null && !s.isEmpty()) {
			if(s.contains(LOGIN_PAGE_KEY))
				MessageBox.alert(NakedConstants.constants.sessionTimeout());
			else
				MessageBox.alert(s);
		}
	}
	
	public static boolean showDetail(final WrappedFlexTable table, final int row, final int col) {
		final ICell iCell = (ICell)table.getWidget(row, col);
		if(iCell != null && !(iCell instanceof ICustomCell)) {
			IFieldInfo fi = null;
			ILinkedCell linkedCell = null;
			if(iCell instanceof ILinkedCell) {
				if(iCell.getWrapper() == null || iCell.getWrapper().getContent() == null || !LazyManager.isLazy(iCell.getWrapper())) {
					linkedCell = (ILinkedCell)iCell;
					fi = WrapperContext.getFieldInfoByName().get(linkedCell.getParentWrapper().getWrappedClassName()).get(iCell.getAttribute());
				}
			} else {
				fi = WrapperContext.getFieldInfoByName().get(iCell.getWrapper().getWrappedClassName()).get(iCell.getAttribute());
			}
			if(fi != null && fi.isEditable() && !fi.isEnum() && ViewHelper.isEditable(iCell.getWrapper())) {
				DataType dataType = fi.getDataType();
				switch (dataType) {
					case BOOLEAN:
						break;
					case DATE:
						DateBoxCell db = new DateBoxCell(iCell.getWrapper(), iCell.getAttribute());
						db.show(iCell.getAbsoluteLeft(), iCell.getAbsoluteTop());
						break;
					case NAKED_OBJECT:
						if(iCell.getWrapper() == null || iCell.getWrapper().getContent() != null)
							linkedCell.showDetail(table.isEditable() && linkedCell.isEditable());
						break;
					default:
						if(!fi.isCollection() && !fi.isLinkedFile()) {
							TextBoxCell tb = new TextBoxCell(iCell.getWrapper(), iCell.getAttribute(), table.getMediator(), true);
							table.getMediator().showPopupPanel(tb, (Widget)iCell);
							tb.setFocus(true);
						}
						break;
					}
			}
			return true;
		}
		return false;
	}

	public static void showWaitCursor() {
		DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "wait");
	}

	public static void showDefaultCursor() {
		DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "default");
	}

	public static void showMoveCursor(Element elt) {
		DOM.setStyleAttribute(elt, "cursor", "move");
	}

	public static void showPointerCursor(Element elt) {
		DOM.setStyleAttribute(elt, "cursor", "pointer");
	}

	public static void hidePopupPanel(Widget w) {
		Widget o = w.getParent();
		while(o != null && !(o instanceof ResizePopupPanel) && !(o instanceof PopupPanel)) {
			o = o.getParent();
		}
		if(o != null) {
			if(o instanceof ResizePopupPanel)
				((ResizePopupPanel)o).hide();
			else if(o instanceof PopupPanel)
				((PopupPanel)o).hide();
		}
	}

	public static Widget getTablePanel(Widget w) {
		Widget o = w.getParent();
		while(o != null && !(o instanceof TablePanel)) {
			o = o.getParent();
		}
		return o;
	}
	
	public static Widget getPopupPanel(Widget w) {
		Widget o = w.getParent();
		while(o != null && !(o instanceof ICloseablePopupPanel)) {
			o = o.getParent();
		}
		return o;
	}

	public static Widget getWrappedTable(Widget w) {
		Widget o = w.getParent();
		while(o != null && !(o instanceof IWrappedTable)) {
			o = o.getParent();
		}
		return o;
	}


	public static HeaderInfo getHeaderInfo(String att, List<HeaderInfo> sortedFields) {
		for(HeaderInfo hi : sortedFields) {
			if(att.equals(hi.getAttribute())) {
				return hi;
			}
		}
		return null;
	}

	public static int getIndex(String att, List<HeaderInfo> sortedFields) {
		if(sortedFields != null && !sortedFields.isEmpty()) {
			int i = 0;
			for(HeaderInfo hi : sortedFields) {
				if(att.equals(hi.getAttribute())) {
					if(hi.isDisplayed()) {
						return hi.isHelpText() ? i + 1 : i;
					}
					else
						return -1;
				}
				i++;
			}
		}
		return -1;
	}

	public static void preventEvent(Event event) {
	    event.preventDefault();
	    event.stopPropagation();
	}

	public static void adjustElementSize(Element element, Element parent, boolean adjustHeight) {
	    int originalHeight = DOM.getElementPropertyInt(parent, "clientHeight");
	    int originalWidth = DOM.getElementPropertyInt(parent, "clientWidth");

	    int height = originalHeight;
	    int width = originalWidth;

	    boolean completed;
	    do {
	        DOM.setStyleAttribute(element, "width", width + "px");
	        int widthNow = DOM.getElementPropertyInt(parent, "clientWidth");
	        completed = widthNow <= originalWidth;
	        if (!completed)
	            width = width + originalWidth - widthNow;

	        if (adjustHeight) {
	            DOM.setStyleAttribute(element, "height", height + "px");
	            int heightNow = DOM.getElementPropertyInt(parent, "clientHeight");
	            completed = completed && heightNow <= originalHeight;
	            if (heightNow > originalHeight)
	                height = height + originalHeight - heightNow;
	        }
	    } while (!completed);
	}

	public static void adjustWidgetSize(Widget widget, Element parent, boolean adjustHeight) {
	    adjustElementSize(widget.getElement(), parent, adjustHeight);
	}


	private static void displayCell(Panel p, AbstractLinkedCell linkedCell,
			TablePanel tp) {
		if(linkedCell == null) {
			p.clear();
			p.add(tp);
		} else {
			linkedCell.getMediator().showPopupPanel(tp, linkedCell);
		}
	}

	private static void displayDefaultTable(WrappedFlexTable t, String title, TableConfig hc, Panel p, TablePanel tp,
			IWrapperListModel list, boolean editable, AbstractLinkedCell linkedCell, TYPE panelType,
			IWrapperListModel toExclude, Panel topMenuPanel, String viewName) {
		if(t == null)
			t = createTable(list, editable, panelType, hc);
		if(viewName != null)
			t.setViewName(viewName);
		boolean toShow = false;
		if(linkedCell == null) {
			if(tp == null) {
				tp = (TablePanel)GWT.create(TablePanel.class);
				tp.initTablePanel(t, panelType == null ? TYPE.ROOT : panelType, null, topMenuPanel);
			}
			if(p == null) {
				p = (Panel)ViewHelper.newPopup(true);
				final ICloseablePopupPanel popupPanel = ViewHelper.newPopup(true);
				if(title != null)
					popupPanel.setPopupTitle(title);
				p = (Panel)popupPanel;
				toShow = true;
			}
		}
		else if(tp == null) {
			tp = (TablePanel)GWT.create(TablePanel.class);
			tp.initTablePanel(t, panelType, linkedCell, topMenuPanel);
		}
		if(title != null)
			tp.setTitle(title);
		if(list.isLazy() || list.isEmpty()) {
			if(toExclude != null && toExclude.getItems() != null && !toExclude.getItems().isEmpty()) {
				for(IWrapper w : toExclude.getItems()) {
					Id id = PersistenceManager.getId(w);
					if(id != null && !id.isLocal())
						list.addExcludedEntity(id);
				}
			}
			int rowsNb = hc != null && hc.getNbRows() > 0 ? hc.getNbRows() : TablePanel.DEFAULT_ROW_NB;
			tp.setNbRowsPerPage(rowsNb);
			list.reload(1, rowsNb, t.updateRefreshState());
		} else if(tp.getPagingPanel() != null) {
			tp.getPagingPanel().setTotalPages(1);
		}
		displayCell(p, linkedCell, tp);
		if(toShow) {
			showPopupPanel((CloseablePopupPanel)p);
			/*if(list.isLazy() || list.isEmpty())
				ViewHelper.showWaitPopup();*/
		}

	}

	private static WrappedFlexTable createTable(IWrapperListModel list,
			boolean editable, TYPE panelType, TableConfig hc) {
		WrappedFlexTable t;
		if(panelType.equals(TablePanel.TYPE.VIEW_CHILDREN)) {
			t = new WrappedFlexTable(list, hc == null ? null : hc.getHeaders(), editable);
		} else if(editable) {
			t = new EditableWrappedFlexTable(list, hc == null ? null : hc.getHeaders());
		} else {
			t = new WrappedFlexTable(list, hc == null ? null : hc.getHeaders());
		}
		return t;
	}

	public static void showPopupPanel(final ICloseablePopupPanel panel) {
		PositionCallback pc = new PositionCallback() {

		    @Override
		    public void setPosition(int offsetWidth, int offsetHeight) {
		        panel.setPopupPosition(Math.max(Window.getClientWidth() - offsetWidth, 0) / 2, Math.max(Window.getClientHeight() - offsetHeight, 0) / 2);
		    }
		};
		panel.setPopupPositionAndShow(pc);
	}


	private static void displayTableForUser(final WrappedFlexTable table, final String title, final Panel panel, TablePanel tp, final IWrapperListModel list, final boolean editable,
			final AbstractLinkedCell linkedCell, final TYPE panelType, IWrapperListModel toExclude, Panel topMenuPanel, String viewName) {
		displayDefaultTable(table, title, WrapperContext.getTableConfig(list.getNakedObjectName(), viewName), panel, tp, list, editable, linkedCell, panelType, toExclude, topMenuPanel, viewName);
	}

	public static void displayTableForUser(final String title, final Panel panel, final IWrapperListModel list, final boolean editable,
			final AbstractLinkedCell linkedCell, final TYPE panelType, IWrapperListModel toExclude, Panel topMenuPanel, String viewName) {
		displayTableForUser(null, title, panel, null, list, editable, linkedCell, panelType, toExclude, topMenuPanel, viewName);
	}

	public static void displayTableForUser(final String title, final Panel panel, final IWrapperListModel list, final boolean editable,
			final AbstractLinkedCell linkedCell, final TYPE panelType, Panel topMenuPanel, String viewName) {
		displayTableForUser(title, panel, list, editable, linkedCell, panelType, null, topMenuPanel, viewName);
	}


	public static void displaySelectDeleteTableForUser(String title, final IWrapperListModel list,
			WrappedCollectionCell linkedCell, String viewName) {
		linkedCell.getMediator().addWrapperSelectedForDeleteHandler(linkedCell);
		displayTableForUser(title, null, list, false, linkedCell, TablePanel.TYPE.MULTIPLE_SELECT, null, viewName);
	}

	public static void displaySelectTableForUser(String title, final IWrapperListModel list,
			AbstractLinkedCell linkedCell, IWrapperListModel toExclude, String viewName) {
		linkedCell.getMediator().addWrapperSelectedHandler(linkedCell);
		displayTableForUser(title, null, list, false, linkedCell, TablePanel.TYPE.MULTIPLE_SELECT, toExclude, null, viewName);
	}

	public static void displaySelectTableForUser(String title, final IWrapperListModel list,
			AbstractLinkedCell linkedCell, String viewName) {
		if(linkedCell != null)
			linkedCell.getMediator().addWrapperSelectedHandler(linkedCell);
		TablePanel.TYPE type;
		if(linkedCell == null || linkedCell instanceof WrappedCollectionCell)
			type = TYPE.MULTIPLE_SELECT;
		else
			type = TYPE.SELECT;
		displayTableForUser(title, null, list, false, linkedCell, type, null, viewName);
	}

	public static void displayChildTableForUser(String title,
			IWrapperListModel list, boolean isEditable,
			WrappedCollectionCell linkedCell, String viewName) {
		displayTableForUser(title, null, list, isEditable, linkedCell, TablePanel.TYPE.VIEW_CHILDREN, null, viewName);
	}

	public static void displayRootTableForUser(final Panel panel, final WrappedFlexTable table, final TablePanel tp,
			final IWrapperListModel list, final boolean editable, final Panel topMenuPanel, final String viewName) {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		if(wor.hasToCommit()) {
			AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

				@Override
				public void onFailure(Throwable caught) {}

				@Override
				public void onSuccess(Boolean b) {
					if(b!=null) {
						if(b.booleanValue()) {
							wor.saveAll(new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									displayListError(list, caught);
								}

								@Override
								public void onSuccess(
										Void result) {
									displayTableForUser(table, null, panel, tp, list, editable, null, TablePanel.TYPE.ROOT, null, topMenuPanel, viewName);
								}
							});
						} else {
							wor.clear();
							displayTableForUser(table, null, panel, tp, list, editable, null, TablePanel.TYPE.ROOT, null, topMenuPanel, viewName);
						}
					} else {
						WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), list.getItems());
					}
				}
			};
			PendingChangesDialogBox.getInstance().show(callback);
		} else {
			displayTableForUser(table, null, panel, tp, list, editable, null, TablePanel.TYPE.ROOT, null, topMenuPanel, viewName);
		}
	}
	
	public static void displayRootTableForUser(final WrappedFlexTable table, final Panel panel, final IWrapperListModel list,
			final boolean editable , final Panel topMenuPanel, final String viewName) {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		if(wor.hasToCommit()) {
			AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

				@Override
				public void onFailure(Throwable caught) {}

				@Override
				public void onSuccess(Boolean b) {
					if(b!=null) {
						if(b.booleanValue()) {
							wor.saveAll(new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									displayListError(list, caught);
								}

								@Override
								public void onSuccess(
										Void result) {
									displayTableForUser(table, null, panel, null, list, editable, null, TablePanel.TYPE.ROOT, null, topMenuPanel, viewName);
								}
							});
						} else {
							wor.clear();
							displayTableForUser(table, null, panel, null, list, editable, null, TablePanel.TYPE.ROOT, null, topMenuPanel, viewName);
						}
					} else {
						WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), list.getItems());
					}
				}
			};
			PendingChangesDialogBox.getInstance().show(callback);
		} else {
			displayTableForUser(table, null, panel, null, list, editable, null, TablePanel.TYPE.ROOT, null, topMenuPanel, viewName);
		}

	}

	public static void displayRootTableForUser(final Panel panel, final IWrapperListModel list,
			final boolean editable, final String viewName) {
		displayRootTableForUser(panel, list, editable , null, viewName);
	}
	
	public static void displayRootTableForUser(final Panel panel, final IWrapperListModel list,
			final boolean editable , final Panel topMenuPanel, final String viewName) {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		if(wor.hasToCommit()) {
			AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

				@Override
				public void onFailure(Throwable caught) {}

				@Override
				public void onSuccess(Boolean b) {
					if(b!=null) {
						if(b.booleanValue()) {
							wor.saveAll(new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									displayListError(list, caught);
								}

								@Override
								public void onSuccess(
										Void result) {
									displayTableForUser(null, panel, list, editable, null, TablePanel.TYPE.ROOT, topMenuPanel, viewName);
								}
							});
						} else {
							wor.clear();
							displayTableForUser(null, panel, list, editable, null, TablePanel.TYPE.ROOT, topMenuPanel, viewName);
						}
					} else {
						WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), list.getItems());
					}
				}
			};
			PendingChangesDialogBox.getInstance().show(callback);
		} else {
			displayTableForUser(null, panel, list, editable, null, TablePanel.TYPE.ROOT, topMenuPanel, viewName);
		}

	}
	
	public static void displayRootTableForUser(final Panel panel, final IWrapperListModel list, final boolean editable,
			final boolean showAddButton, final boolean showDeleteButton,
			final boolean showValidateButton, final boolean showPendingModifsButton, final Panel topMenuPanel, 
			final String viewName, final WrappedFlexTable table) {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		if(wor.hasToCommit()) {
			AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

				@Override
				public void onFailure(Throwable caught) {}

				@Override
				public void onSuccess(Boolean b) {
					if(b!=null) {
						if(b.booleanValue()) {
							wor.saveAll(new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									displayListError(list, caught);
								}

								@Override
								public void onSuccess(
										Void result) {
									displayTableForUser(panel, list, editable, showAddButton, showDeleteButton, showValidateButton, showPendingModifsButton, topMenuPanel, viewName, table);
								}
							});
						} else {
							wor.clear();
							displayTableForUser(panel, list, editable, showAddButton, showDeleteButton, showValidateButton, showPendingModifsButton, topMenuPanel, viewName, table);
						}
					} else {
						WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), list.getItems());
					}
				}
			};
			PendingChangesDialogBox.getInstance().show(callback);
		} else {
			displayTableForUser(panel, list, editable, showAddButton, showDeleteButton, showValidateButton, showPendingModifsButton, topMenuPanel, viewName, table);
		}
	}

	public static void displayRootTableForUser(final Panel panel, final IWrapperListModel list, final boolean editable,
			final boolean showAddButton, final boolean showDeleteButton,
			final boolean showValidateButton, final boolean showPendingModifsButton, final Panel topMenuPanel, 
			final String viewName) {
		displayRootTableForUser(panel, list, editable, showAddButton, showDeleteButton, showValidateButton, showPendingModifsButton, topMenuPanel, viewName, null);
	}

	protected static void displayTableForUser(Panel panel,
			IWrapperListModel list, boolean editable, boolean showAddButton,
			boolean showDeleteButton, boolean showValidateButton,
			boolean showPendingModifsButton, Panel topMenuPanel, String viewName, WrappedFlexTable table) {
		TableConfig hc = WrapperContext.getTableConfig(list.getNakedObjectName(), viewName);
		displayDefaultTable(hc, panel, list, editable, showAddButton, showDeleteButton, showValidateButton, showPendingModifsButton, topMenuPanel, viewName, table);
	}



	public static void displayDefaultTable(TableConfig hc, Panel panel,
			IWrapperListModel list, boolean editable, boolean showAddButton,
			boolean showDeleteButton, boolean showValidateButton,
			boolean showPendingModifsButton, Panel topMenuPanel, String viewName, WrappedFlexTable t) {
		if(t == null) {
			List<HeaderInfo> headers = (hc != null) ? hc.getHeaders() : null;
			if(editable) {
				t = new EditableWrappedFlexTable(list, showDeleteButton, headers);
			} else {
				t = new WrappedFlexTable(list, headers);
			}	
		}
		
		if(viewName != null)
			t.setViewName(viewName);
		TablePanel tp = (TablePanel)GWT.create(TablePanel.class);
		tp.initTablePanel(t, TYPE.ROOT, null, topMenuPanel);
		TopMenuInfo mi = tp.getTopMenuInfo();
		mi.addTopBtn = showAddButton;
		mi.deleteTopBtn = showDeleteButton;
		mi.saveTopBtn = showValidateButton;
		mi.pendingModifsTopBtn = showPendingModifsButton;
		tp.reInitTopMenu();
		if(list.isLazy() || list.isEmpty()) {
			list.reload(1, TablePanel.DEFAULT_ROW_NB, t.updateRefreshState());
		} else {
			tp.getPagingPanel().setTotalPages(1);
		}
		displayCell(panel, null, tp);
	}

	public static boolean isEditable(IWrapper w, IClassInfo ci, IWrapper parentWrapper, String attribute, String viewName) {
		if(ci.getUpdateHandler() != null && w.getContent() != null)
			return ci.getUpdateHandler().isEditable(w.getContent(), parentWrapper, attribute, viewName);
		return true;
	}

	public static boolean isEditable(IWrapper w) {
		return w == null || isEditable(w, WrapperContext.getClassInfoByName().get(w.getWrappedClassName()), null, null, null);
	}


	public static void showWaitPopup() {
		showWaitPopup(NakedConstants.constants.loadingData());
	}
	
	public static void showWaitPopup(String msg) {
		show++;
		//GWT.log("-->show "+show);
		waitPopupPanel.clear();
		VerticalPanel vp = new VerticalPanel();
		vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		waitPopupPanel.add(vp);
		vp.add(new Image(WrappedFlexTable.images.loadingBig()));
		vp.add(new Label(msg));
		waitPopupPanel.getElement().getStyle().setZIndex(zindex);
		waitPopupPanel.center();
		waitPopupPanel.show();
		zindex  += 100;
	}

	public static void hideWaitPopup() {
		if(show > 0)
			show--;
		GWT.log("-->hide "+show);
		if(show == 0) {
			waitPopupPanel.hide();
			zindex = 101;
		}
		
	}

	public static PopupPanel getWaitPopupPanel() {
		return waitPopupPanel;
	}

	public static ICloseablePopupPanel newPopup(boolean hideOnEscape) {
		ICloseablePopupPanel p = GWT.create(CloseablePopupPanel.class);
		p.initPopup(hideOnEscape);
		return p;
	}

	public static void displayListError(final IWrapperListModel list,
			Throwable caught) {
		if(list != null)
			WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), list.getItems());
		if(caught.getMessage() != null) {
			showException((caught.getCause() != null) ? caught.getCause() : caught);
		} else {
			MessageBox.alert(NakedConstants.constants.exceptionPersistDataDB());
		}
	}

	public static void displayPersistenceError(Throwable caught) {
		displayListError(null, caught);
	}

	public static String getRootUrl() {
		return GWT.getModuleBaseURL().substring(0, GWT.getModuleBaseURL().lastIndexOf(GWT.getModuleName()));
	}
	
	
	public static double getRounded(double val)
    {
        double exp = Math.pow(10, 2);
        double rounded = val * exp;
        return Math.floor(rounded) / exp;
    }

	public static void hideWaitPopup(Throwable caught) {
		hideWaitPopup();
		showException(caught);
	}

	public static void displayError(Throwable caught, String message) {
		if(caught != null) {
			showException(message, caught);			
		}
		else
			MessageBox.alert(message);
	}

	public static void displayError(String message) {
		displayError(null, message);
	}

}
