package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IClassInfo;
import com.hiperf.common.ui.client.ICustomCell;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.ILinkedCell;
import com.hiperf.common.ui.client.IRowFormatter;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrappedTable;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.model.LinkedWrapperListModel;
import com.hiperf.common.ui.client.model.PersistentWrapperListModel;
import com.hiperf.common.ui.client.util.PersistenceResult;
import com.hiperf.common.ui.client.util.WrapperComparator;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.Id;


/**
 *
 * */
public class WrappedFlexTable extends FlexTable implements DoubleClickHandler, IWrappedTable, ContextMenuHandler  {

	protected final IWrapperListModel rows;
	protected TablePanelMediator mediator;
	private boolean displayHeader;
	private ResizeHandler resizeHandler;
	protected List<HeaderInfo> sortedFields;
	protected Map<String, IFieldInfo> fieldInfoByName;
	protected IClassInfo classInfo;
	private boolean editableFormOnDoubleClick;
	private List<HeaderCell> headerWidgets;
	private String viewName;
	private boolean ctxMenuShow = false;
	private PopupPanel contextMenu;
	private boolean refreshing = false;

	public interface Images extends ClientBundle {
		ImageResource sortArrowUp();
	    ImageResource sortArrowDown();
	    ImageResource loadingBig();
	}

	public static final Images images = GWT.create(Images.class);

	public WrappedFlexTable(IWrapperListModel rows) {
		this(WrapperContext.getFieldInfoByName().get(rows.getNakedObjectName()), rows, true, null);
	}

	public WrappedFlexTable(IWrapperListModel rows, List<HeaderInfo> headerList) {
		this(WrapperContext.getFieldInfoByName().get(rows.getNakedObjectName()), rows, true, headerList);
	}

	public WrappedFlexTable(Map<String, IFieldInfo> attributesTypesByName, IWrapperListModel rows, boolean openFormOnDblClick, List<HeaderInfo> headerList) {
		super();
		this.resizeHandler = new ResizeHandler(this);
		initHeaderRow();
		this.setStylePrimaryName("custom-table");
		this.displayHeader = true;
		this.fieldInfoByName = new HashMap<String, IFieldInfo>(attributesTypesByName);
		if(headerList == null || headerList.isEmpty()) {
			this.sortedFields = new ArrayList<HeaderInfo>();
			Set<Entry<String, IFieldInfo>> set = fieldInfoByName.entrySet();
			for(Entry<String, IFieldInfo> e : set) {
				if(!e.getValue().isHidden())
					this.sortedFields.add(new HeaderInfo(e.getKey(), e.getValue()));
			}			
			sortFields();
		} else {
			this.sortedFields = headerList;
			List<String> savedAttList = new ArrayList<String>(sortedFields.size());
			Set<String> allAtts = fieldInfoByName.keySet();
			Iterator<HeaderInfo> it = sortedFields.iterator();
			while(it.hasNext()) {
				HeaderInfo hi = it.next();
				if(!allAtts.contains(hi.getAttribute()) || fieldInfoByName.get(hi.getAttribute()).isHidden())
					it.remove();
				else
					savedAttList.add(hi.getAttribute());
			}
			for(String att : allAtts) {
				IFieldInfo fi = fieldInfoByName.get(att);
				if(!savedAttList.contains(att) && !fi.isHidden()) {
					HeaderInfo newHi = new HeaderInfo(att, fi);
					newHi.setDisplayed(false);
					newHi.setIndex(this.sortedFields.size());
					this.sortedFields.add(newHi);
				}
			}
			sortFields();
		}
		this.rows = rows;
		this.classInfo = WrapperContext.getClassInfoByName().get(getClassName());

		addDoubleClickHandler(this);

		this.editableFormOnDoubleClick = false;
		if(isDisplayHeader()) {
            DOM.sinkEvents(resizeHandler.tHeadElement, Event.MOUSEEVENTS | Event.ONDBLCLICK);
            resizeHandler.createResizeListener();
			DOM.setEventListener(resizeHandler.tHeadElement, resizeHandler.resizeListener);
		}
		DOM.setStyleAttribute(this.getElement(), "whiteSpace", "pre");
		
		 addDomHandler(this, ContextMenuEvent.getType());

	}

	public IWrapper getWrapper(int row) {
		for(int j = 0; j<getCellCount(row); j++) {
			Widget w = getWidget(row, j);
			if(w!=null && w instanceof ICell) {
				if(w instanceof ILinkedCell)
					return ((ILinkedCell)w).getParentWrapper();
				else
					return ((ICell)w).getWrapper();
			}
		}
		return null;
	}

	public WrappedFlexTable(IWrapperListModel rows, boolean editableFormOnDoubleClick) {
		this(rows);
		this.editableFormOnDoubleClick = editableFormOnDoubleClick;
	}

	public WrappedFlexTable(IWrapperListModel list, List<HeaderInfo> headers,
			boolean editableFormOnDoubleClick) {
		this(list, headers);
		this.editableFormOnDoubleClick = editableFormOnDoubleClick;
	}

	public Map<String, IFieldInfo> getFieldInfoByName() {
		return fieldInfoByName;
	}

	public void initTable() {
		fillTable();
		displayHeader();
		resizeHandler.initResizeHandler();
	}
	
	public class HeaderCell extends HorizontalPanel {

		protected String attribute;
		protected Boolean sort;
		private String label;

		public HeaderCell() {
			super();
			sort = null;
		}
		
		public HeaderCell(String att, String label, String title) {
			super();
			setTitle(title);
			this.attribute = att;
			this.sort = null;
			this.label = label;
			Label lbl = new Label(label);
			add(lbl);
			try {
				disableTextSelectInternal(lbl.getElement(), true);
			} catch(Exception e) {}
			setWidth("100%");
			com.google.gwt.user.client.Element parent = DOM.getParent(lbl.getElement());
			if(parent != null) {
				DOM.setElementProperty(parent, "align", "center");
				DOM.removeElementAttribute(parent, "style");
			}
			DOM.setStyleAttribute(lbl.getElement(), "fontSize", "15px");
			DOM.setStyleAttribute(lbl.getElement(), "paddingLeft", "3px");
			DOM.setStyleAttribute(lbl.getElement(), "paddingRight", "3px");
			DOM.setStyleAttribute(lbl.getElement(), "paddingTop", "2px");
			DOM.setStyleAttribute(lbl.getElement(), "paddingBottom", "2px");
		}

		public HeaderCell(String att, String label) {
			this(att, label, NakedConstants.constants.sortGlobal());
		}

		public void clearSort() {
			if(sort != null) {
				sort = null;
				if(getWidgetCount() >= 2)
					remove(1);
			}
		}

		public String getAttribute() {
			return attribute;
		}

		public Boolean getSort() {
			return sort;
		}

		public void setSort(Boolean sort) {
			this.sort = sort;
		}

		public void sort() {
			Widget o = getTablePanel();
			if(o != null) {
				TablePanel panel = (TablePanel)o;
				SearchPanel sp = panel.getSearchPanel();
				if(sp != null && sp.isFilterPresent()) {
					sp.initSortByTable();
					sp.applyFilter(WrappedFlexTable.this, panel.getCurrentPage(), panel.getNbRowsPerPage());
				} else
					rows.sort(attribute, (sort==null)?true:sort, 1, panel.getNbRowsPerPage(), updateRefreshState());
			}
		}

		public String getLabel() {
			return label;
		}



	}

	private class LocalHeaderCell extends HeaderCell {

		public LocalHeaderCell(String att, String label) {
			super(att, label, NakedConstants.constants.sortByPage());
		}

		public void sort() {
			Comparator<IWrapper> comparator = new WrapperComparator(attribute);
			if(sort == null || !sort)
				Collections.sort(rows.getItems(), comparator);
			else
				Collections.sort(rows.getItems(), Collections.reverseOrder(comparator));
			removeAllRows();
			fillTable();
		}

	}

	private class ObjectCollectionHeaderCell extends LocalHeaderCell {

		public ObjectCollectionHeaderCell(String att, String label) {
			super(att, label);
			// TODO Auto-generated constructor stub
		}

		public void sort() {
			Comparator<IWrapper> comparator = new Comparator<IWrapper>() {

				@Override
				public int compare(IWrapper o1, IWrapper o2) {
					if(o1 == o2)
						return 0;
					IWrapperListModel c1 = o1.getWrappedCollection(attribute, false);
					IWrapperListModel c2 = o2.getWrappedCollection(attribute, false);
					int s1 = c1.getServerCount();
					int s2 = c2.getServerCount();					
					int diff = s1 - s2;
					if(diff != 0)
						return diff;
					if(s1 == 1) {
						return c1.toString() != null ? c1.toString().compareTo(c2.toString()) : c2.toString() != null ? -1 : o1.hashCode() - o2.hashCode();
					}
					return o1.hashCode() - o2.hashCode();
				}};
			if(sort == null || !sort)
				Collections.sort(rows.getItems(), comparator);
			else
				Collections.sort(rows.getItems(), Collections.reverseOrder(comparator));
			removeAllRows();
			fillTable();
		}

	}

	private class SimpleCollectionHeaderCell extends LocalHeaderCell {

		public SimpleCollectionHeaderCell(String att, String label) {
			super(att, label);
			// TODO Auto-generated constructor stub
		}

		public void sort() {
			Comparator<IWrapper> comparator = new Comparator<IWrapper>() {

				@Override
				public int compare(IWrapper o1, IWrapper o2) {
					if(o1 == o2)
						return 0;
					Collection c1 = o1.getCollection(attribute);
					Collection c2 = o2.getCollection(attribute);
					if(c1 == null)
						return (c2 == null ? 0 : -1);
					if(c2 == null)
						return 1;
					else {
						int s1 = c1.size();
						int diff = s1 - c2.size();
						if(diff != 0)
							return diff;
						if(s1 == 1) {
							return c1.toString() != null ? c1.toString().compareTo(c2.toString()) : c2.toString() != null ? -1 : o1.hashCode() - o2.hashCode();
						}
						return o1.hashCode() - o2.hashCode();
					} 
				}};
			if(sort == null || !sort)
				Collections.sort(rows.getItems(), comparator);
			else
				Collections.sort(rows.getItems(), Collections.reverseOrder(comparator));
			removeAllRows();
			fillTable();
		}

	}



	protected void fillTable() {
		doFillTable();
	}

	protected void doFillTable() {
		for (IWrapper w : rows.getItems()) {
			displayRow(-1, w);
		}
	}

	public boolean isDisplayHeader() {
		return displayHeader;
	}

	public void setDisplayHeader(boolean b) {
		if (!this.displayHeader && b) {
			displayHeader();
		} else if (this.displayHeader && !b) {
			resizeHandler.tHeadElement.removeFromParent();
		}
		this.displayHeader = b;
	}

	protected void displayHeader() {
		doDisplayHeader();
	}

	private void doDisplayHeader() {
        if(getFirstDataCol() > 0) {
        	for(int i=0; i<getFirstDataCol(); i++) {
				setHeaderWidget(i, new HeaderCell());
        	}
        }
		for (HeaderInfo hi : sortedFields) {
			if(hi.isDisplayed()) {
				HeaderCell cell = displayHeaderCell(hi);
				if(rows.getDefaultSortAtt() != null && cell.getAttribute() != null && cell.getAttribute().equals(rows.getDefaultSortAtt())) {
					cell.setSort(rows.isDefaultSortAsc() != null ? !rows.isDefaultSortAsc() : false);
					displaySortArrow(cell);
				}
			}
		}

	}

	private void initHeaderRow() {
		resizeHandler.tHeadElement = DOM.createTHead();
		DOM.insertChild(getElement(), resizeHandler.tHeadElement, 0);
        resizeHandler.headerRowElt = DOM.createTR();
        DOM.appendChild(resizeHandler.tHeadElement, resizeHandler.headerRowElt);
        headerWidgets = new ArrayList<HeaderCell>();
	}

	public void sortFields() {
		Collections.sort(sortedFields);
		int i=0;
		for (HeaderInfo hi : sortedFields) {
			if(hi.isDisplayed()) {
				hi.setIndex(i);
				i++;
			}
		}
		for (HeaderInfo hi : sortedFields) {
			if(!hi.isDisplayed()) {
				hi.setIndex(i);
				i++;
			}
		}
	}

	public HeaderCell displayHeaderCell(HeaderInfo hi) {
		return displayHeaderCell(hi, false);
	}

	public HeaderCell displayHeaderCell(HeaderInfo hi, boolean force) {
		String att = hi.getAttribute();
		IFieldInfo fi = fieldInfoByName.get(att);
		int idx = getFirstDataCol() + hi.getIndex();
		HeaderCell th;
		if(rows instanceof LinkedWrapperListModel && PersistenceManager.getId(((LinkedWrapperListModel) rows).getParentWrapper()).isLocal()) {
			th = new LocalHeaderCell(att, WrapperContext.getHeaderDisplayed(getClassName(), hi));
			setHeaderWidget(idx, th, force);
		} else if(isServerSortable(fi)) {
			String sortCriteria = fi.getSortCriteria();
			th = new HeaderCell(sortCriteria != null ? sortCriteria : att,  WrapperContext.getHeaderDisplayed(getClassName(), hi));
			setHeaderWidget(idx, th, force);
		} else if(!fi.isCollection()) {
			th = new LocalHeaderCell(att,  WrapperContext.getHeaderDisplayed(getClassName(), hi));
			setHeaderWidget(idx, th, force);
		} else {
			if(fi.getDataType().equals(DataType.NAKED_OBJECT)) {
				th = new ObjectCollectionHeaderCell(att, WrapperContext.getHeaderDisplayed(getClassName(), hi));
				setHeaderWidget(idx, th, force);
			} else {
				th = new SimpleCollectionHeaderCell(att, WrapperContext.getHeaderDisplayed(getClassName(), hi));
				setHeaderWidget(idx, th, force);
			}

		}
		return th;
	}

	public boolean isServerSortable(IFieldInfo fi) {
		return isPersistent() && (fi.getSortCriteria() != null || (!fi.isCollection() && !fi.isJpaTransient() && !fi.getDataType().equals(DataType.NAKED_OBJECT)));
	}


	public void setHeaderWidget(int column, HeaderCell widget) {
		setHeaderWidget(column, widget, false);
	}


	/**
     * This method sets a widget for the specified header cell.
     *
     * @param column is a column number.
     * @param widget is a widget to be added to the cell.
     * @param force to force creation af a new header cell
     */
    public void setHeaderWidget(int column, HeaderCell widget, boolean force) {
    	com.google.gwt.user.client.Element th = getThElement(column);
    	if(force) {
    		if(th == null)
    			th = addHeader(column, widget);
    		else {
    			th = insertHeader(column, widget);
    		}
    	}
    	else if(th == null) {
    		th = addHeader(column, widget);
    	} else {
    		internalClearCell(th, true);
    		headerWidgets.set(column, widget);
    	}
        setHeaderCell(widget, th);
        adopt(widget);
    }

    private com.google.gwt.user.client.Element insertHeader(int column,
			HeaderCell widget) {

		com.google.gwt.user.client.Element th;
		if(column > headerWidgets.size()) {
			th = addHeader(column, widget);
			headerWidgets.add(widget);
		}
		else {
			th = DOM.createTH();
			applyThStyles(th);
			DOM.insertChild(resizeHandler.headerRowElt, th, column);
			headerWidgets.add(column, widget);
		}
		return th;

	}

	private void applyThStyles(com.google.gwt.user.client.Element th) {
		DOM.setStyleAttribute(th, "nowrap", "true");
		DOM.setElementAttribute(th, "class", "custom-table-th");
		DOM.setElementAttribute(th, "className", "custom-table-th");
	}

	private com.google.gwt.user.client.Element addHeader(int column,
			HeaderCell widget) {
		com.google.gwt.user.client.Element th;
		th = addHeaderCell();
		if(column > headerWidgets.size())
			headerWidgets.add(widget);
		else
			headerWidgets.add(column, widget);
		applyThStyles(th);
		return th;
	}

	public void setHeaderCell(HeaderCell hc,
			com.google.gwt.user.client.Element th) {
		DOM.appendChild(th, hc.getElement());
	}

	protected com.google.gwt.user.client.Element addHeaderCell() {
        return resizeHandler.headerRowElt.appendChild(DOM.createTH());
	}




	 /**
     * This method gets a TH element.
     *
     * @param column is a column number.
     * @return an element.
     */
    public com.google.gwt.user.client.Element getThElement(int column) {
    	if(resizeHandler.headerRowElt.getChildCount() > column)
    		return DOM.getChild(resizeHandler.headerRowElt, column);
    	return null;
    }

	protected int getFirstDataRow() {
		return 0;
	}

	public int getFirstDataCol() {
		return 0;
	}


	public void displayRow(int beforeRow, IWrapper w) {
		int row;
		int rowCount = getRowCount();
		if(beforeRow < 0) {
			row = insertRow(rowCount);
		} else {
			row = insertRow(beforeRow);
		}
		if(classInfo != null && classInfo.getRowFormatter() != null) {
			IRowFormatter rowFormatter = classInfo.getRowFormatter();
			String style = rowFormatter.getDefaultRowStyle();
			if(style != null)
				getRowFormatter().setStyleName(row, style);
			else
				applyDefaultRowStyle(row);
			style = rowFormatter.getCustomRowStyle(w.getContent());
			if(style != null)
				getRowFormatter().addStyleName(row, style);
			else
				applyDefaultEvenStyle(beforeRow, row, rowCount);
		} else {
			applyDefaultRowStyle(row);
			applyDefaultEvenStyle(beforeRow, row, rowCount);
		}
		boolean editable = ViewHelper.isEditable(w, classInfo, null, null, getViewName());
		if(editable)
			insertDeleteCell(row,w);

		Id id = PersistenceManager.getIdLocalPart(w);
		for (HeaderInfo hi : sortedFields) {
			if(hi.isDisplayed()) {
				try {
					displayCell(row, w, hi, id, editable);
				} catch (Exception e) {}
			}
		}
	}

	private void applyDefaultEvenStyle(int beforeRow, int row, int rowCount) {
		if(beforeRow == 0 && rowCount > 0) {
			if(getRowFormatter().getStyleName(1).equals("custom-table-tr")) {
				getRowFormatter().addStyleName(row, "custom-table-tr-even");
			}
		}
		else if(row == 0 || (row > 1 && row % 2 == 0)) {
			getRowFormatter().addStyleName(row, "custom-table-tr-even");
		}
	}

	private void applyDefaultRowStyle(int row) {
		getRowFormatter().setStyleName(row, "custom-table-tr");
	}

	public void displayCell(int row, IWrapper w, HeaderInfo hi, Id localId, boolean wrapperEditable) {
		if(hi.isDisplayed()) {
			String att = hi.getAttribute();
			IFieldInfo fi = fieldInfoByName.get(att);
			int idx = getFirstDataCol() + hi.getIndex();
			boolean editable = isEditable() && fi.isEditable() && wrapperEditable;
			if(fi.getCustomCell() != null) {
				ICustomCell customCell = fi.getCustomCell().newInstance(w, att, editable, mediator);
				setCellStyle(fi, row, idx, editable);
				if(customCell.isCustomStyle(true))
					customCell.setCellStyle(this, row, idx);
				setWidget(row, idx, (Widget)customCell);
			} else {
				setCellStyle(fi, row, idx, editable);
				switch(fi.getDataType()) {
					case NAKED_OBJECT:
						if (fi.isCollection()) {
							setWrappedCollectionCell(row, idx, w, att, editable);
						} else {
							setWrappedObjectCell(row, idx, w, att, editable);
						}
						break;
					case BOOLEAN:
						setWidget(row, idx, new BooleanCell(w, att, editable));
						break;
					default:
						if (fi.isCollection()) {
							setListBoxCell(row, idx, w, att);
						} else if(fi.isEnum()) {
							if(editable) {
								setComboBoxCell(row, idx, w, att);
							} else {
								setWidget(row, idx, new EnumCell(w, att, mediator));
							}
						} else if(fi.isPreview()) {
							setWidget(row, idx, new ImageCell(w, att));
						} else if(fi.isLinkedFile()){
							setWidget(row, idx, new LinkedFileCell(w, att, editable && fi.getLinkedFileInfo().isLinkedFileUpload(), editable && !fi.isJpaTransient(), mediator));
						} else if(localId != null) {
							int i = 0;
							boolean found = false;
							for(String fn : localId.getFieldNames()) {
								if(fn.equals(att)) {
									setWidget(row, idx, new LocalIdCell(w, att, localId.getFieldValues().get(i)));
									found = true;
									break;
								}
								i++;
							}
							if(!found) {
								setWidget(row, idx, new LabelCell(w, att, mediator));
								if(!editable)
									getCellFormatter().removeStyleName(row, idx, "custom-table-td-editable");
							}
						}
						else {
							setWidget(row, idx, new LabelCell(w, att, mediator));
							if(!editable)
								getCellFormatter().removeStyleName(row, idx, "custom-table-td-editable");
						}
						break;
					}
				}
			}

	}


	protected void setCellStyle(IFieldInfo fi, int row, int col, Boolean forceEditable) {
		getCellFormatter().addStyleName(row, col, "custom-table-td");
		getFlexCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
		if(forceEditable != null) {
			if(forceEditable) {
				getCellFormatter().addStyleName(row, col, "custom-table-td-editable");
			}
			return;
		}
		if(isEditable() && fi.isEditable()) {
			getCellFormatter().addStyleName(row, col, "custom-table-td-editable");
		}
	}

	protected void setWrappedCollectionCell(int row, int col, IWrapper w,
			String att, boolean editable) throws AttributeNotFoundException {
		IWrapperListModel wrappedCollection = w.getWrappedCollection(att, isPersistent());
		if(wrappedCollection != null) {
			if(!isEditable() && (!wrappedCollection.getItems().isEmpty() || wrappedCollection.isLazy()))
				setWidget(row, col, new WrappedCollectionCell(w, att, isPersistent(), false, mediator));
			else
				setWidget(row, col, new WrappedCollectionCell(w, att, isPersistent(), editable && isEditable() && fieldInfoByName.get(att).isEditable(), mediator));
		} else
			setText(row, col, null);
	}


	protected void insertDeleteCell(int row, IWrapper w) {

	}

	protected void setCustomCell(int row, int col, IWrapper w,
			String att) {

	}

	protected void setWrappedObjectCell(int row, int col, IWrapper w,
			String att, boolean editable) throws AttributeNotFoundException {
		IWrapper o = w.getWrappedAttribute(att);
		if(o.getContent()!=null)
			setWidget(row, col, new WrappedObjectCell(w, att, isPersistent(), false, mediator));
		else
			setText(row, col, null);
	}

	public boolean isEditable() {
		return false;
	}

	protected void setListBoxCell(int row, int col, IWrapper wrapper,
			String attribute) {
		setWidget(row, col, new ListBoxCell(wrapper, attribute, mediator));
	}

	private void setComboBoxCell(int row, int col, IWrapper wrapper, String attribute) {
		setWidget(row, col, new EditableEnumCell(wrapper, attribute));
	}

	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		if(!isEditable() && editableFormOnDoubleClick && isPersistent()) {
			Element td = getEventTargetCell((Event)event.getNativeEvent());
			if (td == null)
				return;
			showDetail(td);
		}
	}

	public void showDetail(Element td) {
		Element tr = DOM.getParent(td);
		int row = DOM.getChildIndex(this.getBodyElement(), tr);
		if(row < 0)
			row = DOM.getChildIndex(this.getBodyElement(), DOM.getParent(tr));
		int column = DOM.getChildIndex(tr, td);
		IWrapper w = findWrapper(row, column);
		if(w != null) {
			boolean canEdit = isEditable() || isEditableFormOnDoubleClick();
			boolean canValidate = canEdit;
			IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(w.getWrappedClassName()).getUpdateHandler();
			String title = null;
			if(updateHandler != null) {
				canValidate = canValidate & updateHandler.canValidate(w.getContent(), viewName);
				canEdit = canEdit && updateHandler.isEditable(w.getContent(), null, null, viewName);
				title = updateHandler.beforeEdit(w.getContent());
			}
			doShowDetail(row, w, canEdit, canValidate, title);
		}
	}

	protected void doShowDetail(int row, IWrapper w, boolean canEdit, boolean canValidate, String title) {
		ICustomForm f = WrapperContext.getNewForm(getClassName());
		if(f != null) {
			if(f instanceof WrappedObjectForm) {
				WrappedObjectForm wof = (WrappedObjectForm) f.newInstance(w, this, canEdit || canValidate);
				wof.setParentViewName(viewName);
				addFormToPanel(row, w, canEdit, canValidate, title, wof);
			} else
				f.newInstance(w, this, canEdit || canValidate).show();
		}
		else {
			WrappedObjectForm form = new WrappedObjectForm(w, sortedFields, fieldInfoByName, canEdit, isPersistent(), true, viewName);
			/*if(updateHandler != null) {
				updateHandler.beforeShowDetails(form);
			}*/
			addFormToPanel(row, w, canEdit, canValidate, title, form);
		}
	}

	protected void addFormToPanel(int row, IWrapper w, boolean canEdit,
			boolean canValidate, String title, WrappedObjectForm form) {
		ObjectPanel vp;
		if(canEdit || canValidate) {
			vp = new ObjectPanel(form,  WrappedObjectsRepository.getInstance().saveState(), false);
			form.setFormTitle(title == null ? WrapperContext.getEditElementLabel(w.getWrappedClassName(), viewName) : title);
			mediator.addValidateButton(vp, row);
		}
		else {
			vp = new ObjectPanel(form);
			form.setFormTitle(title == null ? WrapperContext.getViewElementLabel(w.getWrappedClassName(), viewName) : title);
		}
		mediator.showPopupPanel(vp, this);
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public void redrawRow(int row, Id id) {
		for(int col = 0; col<getCellCount(row); col++) {
			Widget widget = getWidget(row, col);
			if(widget != null && widget instanceof ICell) {
				ICell c = (ICell) widget;
				if(c instanceof LocalIdCell && id != null && id.isLocal()) {
					setWidget(row, col, new LabelCell(c.getWrapper(), c.getAttribute(), mediator));
				} else
					((ICell)widget).redraw();
			}
		}
	}

	public IWrapper findWrapper(int row, int col) {
		Widget widget = getWidget(row, col);
		if(widget!=null) {
			return getWrapper(widget);
		} else {
			for(int j=0; j<getCellCount(row); j++) {
				if(j != col) {
					widget = getWidget(row, j);
					if(widget!=null) {
						return getWrapper(widget);
					}
				}
			}
		}
		return null;
	}

	private IWrapper getWrapper(Widget widget) {
		if(widget instanceof ILinkedCell) {
			return ((ILinkedCell)widget).getParentWrapper();
		} else if(widget instanceof ICell) {
			return ((ICell)widget).getWrapper();
		}
		return null;
	}

	public int findRow(Id id) {
		for(int row = 0; row<getRowCount(); row++) {
			for(int col = 0; col<getCellCount(row); col++) {
				Widget widget = getWidget(row, col);
				if(widget != null) {
					IWrapper wrapper = null;
					if(widget instanceof ILinkedCell) {
						wrapper = ((ILinkedCell)widget).getParentWrapper();
					} else if(widget instanceof ICell) {
						wrapper = ((ICell)widget).getWrapper();
					}

					if(wrapper != null && PersistenceManager.getId(wrapper).equals(id)) {
						return row;
					}
				}
			}
		}
		return -1;
	}

	protected int findRow(INakedObject no) {
		for(int row = 0; row<getRowCount(); row++) {
			for(int col = 0; col<getCellCount(row); col++) {
				Widget widget = getWidget(row, col);
				if(widget != null) {
					IWrapper wrapper = null;
					if(widget instanceof ILinkedCell) {
						wrapper = ((ILinkedCell)widget).getParentWrapper();
					} else if(widget instanceof ICell) {
						wrapper = ((ICell)widget).getWrapper();
					}

					if(wrapper != null && (wrapper.getContent() == no || (wrapper != null &&  wrapper.getContent() != null
							&& no != null
							&& wrapper.getContent().equals(no)))) {
						return row;
					}
				}
			}

		}
		return -1;
	}

	public int findRow(IWrapper w) {
		for(int row = 0; row<getRowCount(); row++) {
			for(int col = 0; col<getCellCount(row); col++) {
				Widget widget = getWidget(row, col);
				if(widget != null) {
					IWrapper wrapper = null;
					if(widget instanceof ILinkedCell) {
						wrapper = ((ILinkedCell)widget).getParentWrapper();
					}
					else if(widget instanceof ICell) {
						wrapper = ((ICell)widget).getWrapper();
					}

					if(wrapper != null && (wrapper == w || (wrapper != null && w != null && wrapper.getContent() != null
							&& w.getContent() != null
							&& wrapper.getContent().equals(w.getContent())))) {
						return row;
					}
				}
			}

		}
		return -1;
	}


	@Override
	public void validate(AsyncCallback<PersistenceResult> callback) {}

	public void validateAndRefresh() {}


	@Override
	public void removeRows() {}

	public void refresh(int pageNb, int rowsNb) {
		if(isPersistent() && !refreshing ) {
			refreshing = true;
			HeaderCell sortedColumn = getSortedColumn();
			if(sortedColumn != null && !(sortedColumn instanceof LocalHeaderCell)) {
				rows.sort(sortedColumn.getAttribute(), sortedColumn.getSort(), pageNb, rowsNb, updateRefreshState());
			} else {
				rows.reload(pageNb, rowsNb, updateRefreshState());
			}
		}
	}

	public AsyncCallback<Void> updateRefreshState() {
		return new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				unsetRefreshing();
			}

			@Override
			public void onSuccess(Void result) {
				unsetRefreshing();
			}
		};
	}



	public void clearSortColumns(Widget cellToKeep) {
		if(isDisplayHeader()) {
			for(HeaderCell w : headerWidgets) {
				if(w != null && w!=cellToKeep) {
					w.clearSort();
				}
			}
		}
	}

	public HeaderCell getSortedColumn() {
		if(isDisplayHeader()) {
			for(HeaderCell w : headerWidgets) {
				if(w.getSort() != null) {
					return w;
				}
			}
		}
		return null;
	}


	@Override
	public boolean isPersistent() {
		return rows.isPersistent();
	}


	public void selectAll(boolean select) {}

	@Override
	public IWrapper findWrapper(INakedObject o) {
		for(IWrapper w : rows.getItems()) {
			if(o.equals(w.getContent())) {
				return w;
			}
		}
		return null;
	}

	public String getClassName() {
		return rows.getNakedObjectName();
	}

	public boolean isFiltered() {
		if(isPersistent()) {
			return ((PersistentWrapperListModel)rows).getCurrentFilter() != null;
		}
		return false;
	}

	public String getFilter() {
		if(isPersistent()) {
			return ((PersistentWrapperListModel)rows).getCurrentFilter();
		}
		return null;
	}

	public void filter(String jpql, int page, int rowNb, boolean caseSensitive) {
		if(isPersistent()) {
			HeaderCell sortedColumn = getSortedColumn();
			boolean way = false;
			String sortedCol = null;
			if(sortedColumn != null && !(sortedColumn instanceof LocalHeaderCell)) {
				sortedCol = sortedColumn.getAttribute();
				way = (sortedColumn.getSort() != null)?sortedColumn.getSort():false;
			}
			((PersistentWrapperListModel)rows).filter(jpql, sortedCol, way, page, rowNb, caseSensitive, updateRefreshState());
		}
	}

	public void clearFilter() {
		if(isPersistent()) {
			((PersistentWrapperListModel)rows).resetCurrentFilter();
		}
	}

	public void setFilter(String filter) {
		if(isPersistent()) {
			((PersistentWrapperListModel)rows).setCurrentFilter(filter);
		}
	}

	protected Widget getTablePanel() {
		return ViewHelper.getTablePanel(this);
	}

	private native static void disableTextSelectInternal(Element e, boolean disable)/*-{
		if (disable) {
			e.ondrag = function () { return false; };
			e.onselectstart = function () { return false; };
			e.style.MozUserSelect="none"
		} else {
			e.ondrag = null;
			e.onselectstart = null;
			e.style.MozUserSelect="text"
		}
	}-*/;

	public List<HeaderInfo> getSortedFields() {
		return sortedFields;
	}

    public List<IWrapper> getItems() {
    	return rows.getItems();
    }

	public TablePanelMediator getMediator() {
		return mediator;
	}

	public void setMediator(TablePanelMediator mediator) {
		this.mediator = mediator;
	}

	public ResizeHandler getResizeHandler() {
		return resizeHandler;
	}

	public List<HeaderCell> getHeaderWidgets() {
		return headerWidgets;
	}

	@Override
	public void insertNewRow(IWrapper w) {}

	public void insertCells(int row, int column, int count) {
		super.insertCells(row, column, count);
	}

	public boolean internalClearCell(com.google.gwt.user.client.Element td, boolean clearInnerHTML) {
		return super.internalClearCell(td, clearInnerHTML);
	}

	public IWrapperListModel getRows() {
		return rows;
	}

	public void displaySortArrow(HeaderCell headerCell) {
		if(headerCell.getSort() == null) {
			headerCell.setSort(true);
		}
		else {
			headerCell.setSort(!headerCell.getSort());
			if(headerCell.getWidgetCount() >= 2)
				headerCell.remove(1);
		}
		if(headerCell.getSort()) {
			Image i = new Image(images.sortArrowUp());
			i.setTitle(NakedConstants.constants.sortDesc());
			headerCell.add(i);
		} else {
			Image i = new Image(images.sortArrowDown());
			i.setTitle(NakedConstants.constants.sortAsc());
			headerCell.add(i);
		}
	}

	public com.google.gwt.user.client.Element getTableBodyElement() {
		return getBodyElement();
	}

	public boolean hasConstantFilter() {
		if(isPersistent()) {
			List<String> l = ((PersistentWrapperListModel)rows).getConstantFilterFields();
			return  l!= null && !l.isEmpty();
		}
		return false;
	}

	public List<String> getConstantFilterFields() {
		if(isPersistent()) {
			return ((PersistentWrapperListModel)rows).getConstantFilterFields();
		}
		return null;
	}

	public String getConstantFilter() {
		if(isPersistent()) {
			return ((PersistentWrapperListModel)rows).getConstantFilter();
		}
		return null;
	}

	public List<String> getEnabledFilterFields() {
		if(isPersistent()) {
			return ((PersistentWrapperListModel)rows).getEnabledFilterFields();
		}
		return null;
	}

	public boolean isEditableFormOnDoubleClick() {
		return editableFormOnDoubleClick;
	}

	@Override
	public void onContextMenu(ContextMenuEvent event) {
		// stop the browser from opening the context menu
	    event.preventDefault();
	    event.stopPropagation();
	    if(!ctxMenuShow ) {
	    	ctxMenuShow = true;
	    	if(this.contextMenu == null) {
	    		this.contextMenu = new PopupPanel(true);
	    		this.contextMenu.addStyleName("context-menu");
	    	} else {
	    		this.contextMenu.clear();
	    	}
		    if(initContextMenu(event)) {
			    this.contextMenu.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
			    this.contextMenu.show();
		    }
		    ctxMenuShow = false;
	    }	    
	}

	protected boolean initContextMenu(final ContextMenuEvent event) {
		final Element td = getEventTargetCell((Event)event.getNativeEvent());
		if (td != null) {
			MenuBar mb = new MenuBar(true);
			this.contextMenu.add(mb);
			mb.addItem(NakedConstants.constants.showDetails(), new Command() {
				
				@Override
				public void execute() {
					showDetail(td);
					contextMenu.hide();
				}
			});
			return true;
		}
		return false;
		
	}

	@Override
	public String getFormTitle() {		
		return WrapperContext.getTableLabel(getClassName(), viewName);
	}

	@Override
	protected void onDetach() {		
		super.onDetach();
		if(mediator != null) {
			mediator.unRegisterAll(false);
		}
	}


	@Override
	protected void onAttach() {		
		super.onAttach();
		if(mediator != null) {
			mediator.registerAll();
		}
	}

	protected void unsetRefreshing() {
		refreshing = false;
	}
}
