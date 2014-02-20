package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.ICustomCell;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.ILinkedCell;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.IWrappedTable;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.util.PersistenceResult;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WidgetFactory;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.TableConfig;

public class WrappedObjectForm extends Grid implements IWrappedObjectForm, IWrappedTable {

	public interface Images extends ClientBundle {
		ImageResource mandatory();
		ImageResource help();
	}

	public static final Images images = GWT.create(Images.class);

	private FlexTable formGrid;
	private IWrapper wrapper;
	protected Map<String, IFieldInfo> fieldInfoByName;
	protected List<HeaderInfo> sortedFields;
	private boolean editable;
	private boolean persistent;
	private IPanelMediator mediator;
	private String parentViewName;
	private String title;

	private boolean focus;

	
	
	public WrappedObjectForm() {
		super();
	}

	public WrappedObjectForm(final IWrapper w, List<HeaderInfo> sortedFields, Map<String, IFieldInfo> map,
			boolean editable, boolean persistent, boolean useFormMediator, String parentViewName) {
		super(2,1);
		this.parentViewName = parentViewName;
		this.fieldInfoByName = map != null ? map : WrapperContext.getFieldInfoByName().get(w.getWrappedClassName());
		formGrid = new FlexTable();
		formGrid.setStyleName("child-custom-table");
		super.setWidget(0, 0, formGrid);
		getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
		this.wrapper = w;
		if(editable && wrapper.getContent() == null) {
			wrapper.setContent(w.newNakedObject());
		}
		this.editable = editable;
		this.persistent = persistent;
		if(useFormMediator)
			this.mediator = new FormMediator(this);
		else
			this.mediator = new DefaultMediator(this);

		init();
		IUpdateHandler uh = WrapperContext.getClassInfoByName().get(w.getWrappedClassName()).getUpdateHandler();
		if(uh != null) {
			uh.afterInit(this);
		}
	}

	public WrappedObjectForm(final IWrapper w, boolean editable, boolean persistent, String viewName) {
		this(w,null, null,editable,persistent, true, viewName);
	}

	private void init() {
		int idx = 0;
		if(this.sortedFields == null) {
			/*TableConfig hc =  WrapperContext.getTableConfig(wrapper.getWrappedClassName(), getViewName());
			if(hc != null)
				sortedFields = hc.getHeaders();
			else*/
			defaultSortFields();
		}
		TableConfig tc = WrapperContext.getTableConfig(wrapper.getWrappedClassName(), parentViewName);
		List<HeaderInfo> hh = tc != null ? tc.getHeaders() : null;
		Id id = PersistenceManager.getIdLocalPart(wrapper);
		for(HeaderInfo headerInfo : sortedFields) {
			String att = headerInfo.getAttribute();
			IFieldInfo fi = fieldInfoByName.get(att);
			boolean fieldEditable = isEditable() && fi.isEditable();
			if(headerInfo.isDisplayed() && fi.isDisplayedInForm()) {
				formGrid.insertRow(idx);

				String label = null;
				if(hh != null) {
					HeaderInfo h = ViewHelper.getHeaderInfo(att, hh);
					if(h != null)
						label = h.getLabel();
				}
				if(label == null)
					label = WrapperContext.getHeaderDisplayed(wrapper.getWrappedClassName(), headerInfo);
				formGrid.getRowFormatter().setStyleName(idx, "custom-table-tr");
				if(idx % 2 == 0)
					formGrid.getRowFormatter().addStyleName(idx, "custom-table-tr-even");
				
				HorizontalPanel leftCell = new HorizontalPanel();
				leftCell.setBorderWidth(0);
				leftCell.setSpacing(0);
				leftCell.setStyleName("form-table-th");
				leftCell.addStyleName("align-left");
				formGrid.setWidget(idx, 0, leftCell);
				if(!fieldEditable || (!fi.isNotNull() && !fi.isNotEmpty())) {
					leftCell.add(new Label(label));
					if(fi.getHelpTextKey() != null) {
						final String helpTxt = WidgetFactory.getHelpMessages().get(fi.getHelpTextKey());
						if(helpTxt != null && helpTxt.length() > 0) {
							Image i = new Image(images.help());
							i.setTitle(NakedConstants.constants.helpPopup());
							i.addClickHandler(new ClickHandler() {

								@Override
								public void onClick(ClickEvent event) {
									event.stopPropagation();
									MessageBox.info(helpTxt, true);
								}
							});
							leftCell.add(new HTML("&nbsp;"));
							leftCell.add(i);
						}
					}
				} else {
					Image i = new Image(images.mandatory());
					i.setTitle(NakedConstants.constants.mandatoryField());
					leftCell.add(i);
					leftCell.add(new Label(label));					
					if(fi.getHelpTextKey() != null) {
						final String helpTxt = WidgetFactory.getHelpMessages().get(fi.getHelpTextKey());
						if(helpTxt != null && helpTxt.length() > 0) {
							Image img = new Image(images.help());
							img.setTitle(NakedConstants.constants.helpPopup());
							leftCell.add(new HTML("&nbsp;"));
							leftCell.add(img);
							img.addClickHandler(new ClickHandler() {

								@Override
								public void onClick(ClickEvent event) {
									event.stopPropagation();
									MessageBox.info(helpTxt, true);
								}
							});
						}
					}
				}

				displayCell(idx, att, fi, 1, id, false);
				idx++;
			}
		}
		this.addAttachHandler(new AttachEvent.Handler() {

			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (isAttached()) {
					setTabOrder();
				} else {
					clearTabOrder();
				}
			}

		});
	}

	protected void clearTabOrder() {
		if(this.getRowCount() > 0) {
			for(int i = 0; i<getRowCount(); i++) {
				int j = getCellCount(i);
				if(j > 1) {
					Widget w = getWidget(i, j - 1);
					if(w instanceof ICell) {
						((ICell)w).setTabIndex(-1);
					}
				}
				if(i == getRowCount() - 1) {
					Widget w = getWidget(i, 0);
					if(w instanceof Button) {
						((Button)w).setTabIndex(-1);
					}
				}
			}
		}
	}

	private void setTabOrder() {
		if(this.getRowCount() > 0) {
			int k = 0;
			for(int i = 0; i<getRowCount(); i++) {
				int j = getCellCount(i);
				if(j > 1) {
					Widget w = getWidget(i, j - 1);
					if(w instanceof ICell) {
						((ICell)w).setTabIndex(k);
						k++;
					}
				}
				if(i == getRowCount() - 1) {
					Widget w = getWidget(i, 0);
					if(w instanceof Button) {
						((Button)w).setTabIndex(k);
					}
				}
			}

		}
	}

	private void displayCell(int idx, String att, IFieldInfo fi, int cellIdx, Id localId, boolean removeLinks) {
		boolean fieldEditable = isEditable() && fi.isEditable();
		//int tabIndex = idx+1;
		if(fi.getCustomCell() != null) {
			ICustomCell customCell = fi.getCustomCell().newInstance(wrapper, att, fieldEditable, mediator);
			if(customCell != null) {
				if(customCell.isCustomStyle(false))
					customCell.setCellStyle(this, idx, cellIdx);
				formGrid.setWidget(idx, cellIdx, (Widget)customCell);				
			}
		} else if(fi.isEnum()) {
			if(fieldEditable) {
				EditableEnumCell widget = new EditableEnumCell(wrapper, att);
				formGrid.setWidget(idx, cellIdx,widget);
			} else {
				formGrid.setWidget(idx, cellIdx, new EnumCell(wrapper, att, mediator));
			}
		} else {
			switch(fi.getDataType()) {
				case NAKED_OBJECT:
					if(fi.isCollection()) {
						IWrapperListModel wrappedCollection = wrapper.getWrappedCollection(att, persistent);
						if(wrappedCollection != null) {
							WrappedCollectionCell widget = new WrappedCollectionCell(wrapper, att, persistent, fieldEditable, mediator);
							formGrid.setWidget(idx, cellIdx, widget);
						}
					} else {
						WrappedObjectCell widget = new WrappedObjectCell(wrapper, att, persistent, fieldEditable, mediator);
						formGrid.setWidget(idx, cellIdx, widget);
					}
					if(removeLinks) {
						AbstractLinkedCell c = (AbstractLinkedCell) formGrid.getWidget(idx, cellIdx);
						c.getOutHr().removeHandler();
						c.getOverHr().removeHandler();
						c.getCellPanel().removeStyleName("underline");
						c.getCellPanel().addStyleName("cursor-default");
						c.getLabel().removeStyleName("underline");
						c.getLabel().addStyleName("cursor-default");
						c.getLabel().addStyleName("align-center");

					}
					break;
				case BOOLEAN:
					BooleanCell bc = new BooleanCell(wrapper, att, fieldEditable);
					formGrid.setWidget(idx, cellIdx, bc);
					break;
				case DATE:
					if(fieldEditable) {
						DateBoxCell widget = new DateBoxCell(wrapper, att);
						widget.getTextBox().addStyleName("noborder");
						formGrid.setWidget(idx, cellIdx, widget);
					} else {
						LabelCell widget = new LabelCell(wrapper, att, mediator);
						widget.setWordWrap(false);
						//widget.setTabIndex(tabIndex);
						formGrid.setWidget(idx, cellIdx, widget);
					}
					break;
				default:
					if(fi.isCollection()) {
						if(fieldEditable) {
							EditableListBoxCell widget = new EditableListBoxCell(wrapper, att, mediator);
							formGrid.setWidget(idx, cellIdx, widget);
						} else {
							formGrid.setWidget(idx, cellIdx, new ListBoxCell(wrapper, att, mediator));
						}
					} else if(fi.isLinkedFile()){
						formGrid.setWidget(idx, cellIdx, new LinkedFileCell(wrapper, att, fieldEditable, fieldEditable && !fi.isJpaTransient(), mediator));
					} else if(fieldEditable) {
						TextBoxCell widget = new TextBoxCell(wrapper, att, mediator);
						formGrid.setWidget(idx, cellIdx, widget);
					} else if(fi.isPreview()) {
						setWidget(idx, cellIdx, new ImageCell(wrapper, att));
					} else if(localId != null) {
						int i = 0;
						boolean found = false;
						for(String fn : localId.getFieldNames()) {
							if(fn.equals(att)) {
								setWidget(idx, cellIdx, new LocalIdCell(wrapper, att, localId.getFieldValues().get(i)));
								found = true;
								break;
							}
							i++;
						}
						if(!found)
							formGrid.setWidget(idx, cellIdx, new LabelCell(wrapper, att, mediator));
					} else {
						formGrid.setWidget(idx, cellIdx, new LabelCell(wrapper, att, mediator));
					}
			}
		}
		formGrid.getFlexCellFormatter().setHorizontalAlignment(idx, cellIdx, HasHorizontalAlignment.ALIGN_CENTER);
		if(!focus && fieldEditable) {
			final Widget wi = formGrid.getWidget(idx, cellIdx);
			if(wi instanceof Focusable) {
				Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand () {
			        public void execute () {
			        	((Focusable)wi).setFocus(true);
			        }
			    });
				focus = true;
			}
		}
	}

	public void setFieldEditable(String att, boolean editable) {
		Id id = PersistenceManager.getIdLocalPart(wrapper);
		IFieldInfo fi = fieldInfoByName.get(att);
		if(editable || (isEditable() && fi.isEditable())) {
			int i = 0;
			for(HeaderInfo hi : sortedFields) {
				if(hi.getAttribute().equals(att)) {
					if(hi.isDisplayed()) {
						break;
					}
					else
						return;
				}
				i++;
			}
			displayCell(i, att, fi, 1, id, !editable);
		}

	}

	protected void defaultSortFields() {
		List<String> atts = null;
		this.sortedFields = new ArrayList<HeaderInfo>();
		if(parentViewName != null) {
			TableConfig tc = WrapperContext.getTableConfig(wrapper.getWrappedClassName(), parentViewName);
			if(tc != null) {
				List<HeaderInfo> hh = tc.getHeaders();
				atts = new ArrayList<String>(hh.size());
				for(HeaderInfo hi : hh) {
					this.sortedFields.add(hi);
					atts.add(hi.getAttribute());
				}	
			}
		}
		for(Entry<String, IFieldInfo> e : fieldInfoByName.entrySet()) {
			if(!e.getValue().isHidden() && (atts == null || !atts.contains(e.getKey())))
				this.sortedFields.add(new HeaderInfo(e.getKey(), e.getValue()));
		}
		Collections.sort(sortedFields);
	}

	public void redraw() {
		int count = formGrid.getRowCount();
		for(int i = 0; i<count; i++) {
			redrawRow(i, null);
		}
	}

	public boolean isEditable() {
		return editable;
	}

	public boolean isPersistent() {
		return persistent;
	}

	@Override
	public void refresh(int page, int rows) {
		if(isPersistent()) {
			Id id = PersistenceManager.getId(wrapper);
			if(id!=null) {
				PersistenceServiceAsync service = PersistenceService.Util.getInstance();
				AsyncCallback<INakedObject> callback = new AsyncCallback<INakedObject>() {

					public void onFailure(Throwable caught) {
						ViewHelper.displayError(caught, NakedConstants.constants.exceptionDataDB());
					}

					public void onSuccess(INakedObject result) {

						if (result != null) {
							wrapper.setContent(result);
							init();
						} else {
							ViewHelper.displayError(NakedConstants.constants.exceptionDataDB());
						}
					}
				};
				service.get(wrapper.getWrappedClassName(), id, callback);
			}
		}

	}

	@Override
	public void insertNewRow(IWrapper w) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRows() {
		// TODO Auto-generated method stub

	}

	@Override
	public void validate(AsyncCallback<PersistenceResult> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearSortColumns(Widget cellToKeep) {
		// TODO Auto-generated method stub

	}

	@Override
	public IWrapper findWrapper(INakedObject oldValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public IWrapper getWrapper() {
		return wrapper;
	}

	@Override
	public int findRow(IWrapper w) {
		for(int i = 0; i< formGrid.getRowCount(); i++) {
			Widget cell = getCell(i);
			if(cell != null && cell instanceof ILinkedCell) {
				ILinkedCell lCell= (ILinkedCell)cell;
				if(lCell.getWrapper() == w)
					return i;
			}
		}
		return -1;
	}
	
	public ICell getCell(String att) {
		for(int i = 0; i< formGrid.getRowCount(); i++) {
			Widget cell = getCell(i);
			if(cell != null && cell instanceof ICell) {
				ICell lCell= (ICell)cell;
				if(lCell.getAttribute().equals(att))
					return lCell;
			}
		}
		return null;
	}

	public void setValueError(String att, String msg) {
		for(int i = 0; i< formGrid.getRowCount(); i++) {
			Widget cell = getCell(i);
			if(cell != null && cell instanceof ICell && ((ICell)cell).getAttribute().equals(att)) {
				DOM.setStyleAttribute(cell.getElement(), "borderStyle", "solid");
				DOM.setStyleAttribute(cell.getElement(), "borderColor", "rgb(247, 45, 65)");
				DOM.setStyleAttribute(cell.getElement(), "borderWidth", "2px");
				cell.setTitle(msg);
				return;
			}
		}
	}

	@Override
	public boolean clearCell(int row, int col) {
		return formGrid.clearCell(row, col);
	}

	@Override
	public void setWidget(int row, int col, Widget w) {
		formGrid.setWidget(row, col, w);
	}

	@Override
	public RowFormatter getRowFormatter() {
		return formGrid.getRowFormatter();
	}

	public void setFormTitle(String formTitle) {
		this.title = formTitle;		
	}

	public String getFormTitle() {
		return title;
	}

	@Override
	public void validateAndRefresh() {
		// TODO Auto-generated method stub

	}

	public Widget getCell(int row) {
		Widget cell = formGrid.getWidget(row, 1);
		if(cell == null)
			cell = formGrid.getWidget(row, 2);
		return cell;
	}

	public void clearErrors() {
		for(int i = 0; i< formGrid.getRowCount(); i++) {
			int cellIdx = 1;
			Widget cell = formGrid.getWidget(i, cellIdx);
			if(cell == null) {
				cellIdx = 2;
				cell = formGrid.getWidget(i, cellIdx);
			}
			String bStyle = DOM.getStyleAttribute(cell.getElement(), "borderStyle");
			String bColor = DOM.getStyleAttribute(cell.getElement(), "borderColor");
			if(bStyle != null && bColor != null && bStyle.equals("solid") && bColor.equals("rgb(247, 45, 65)")) {
				DOM.setStyleAttribute(cell.getElement(), "borderStyle", "none");
				DOM.setStyleAttribute(cell.getElement(), "borderWidth", "0px");
				cell.setTitle(null);
				formGrid.getCellFormatter().setStyleName(i, cellIdx, "custom-table-td");
			}

		}

	}

	@Override
	public void redrawRow(int i, Id id) {
		Widget c = getCell(i);
		if(c != null && c instanceof ICell) {
			ICell cell = (ICell)c;
			cell.redraw();
		}

	}

	public boolean hasErrors() {
		for(int i = 0; i< formGrid.getRowCount(); i++) {
			Widget c = getCell(i);
			if(c != null) {
				if(c  instanceof TextBoxCell) {
					TextBoxCell tb = (TextBoxCell) c;
					if(tb.isError()) {
						return true;
					}	
				} else if(c instanceof IValidationCell) {
					if(((IValidationCell)c).isError())
						return true;
				}
			}
		}
		return false;
	}

	public List<HeaderInfo> getSortedFields() {
		return sortedFields;
	}

	public FlexTable getFormGrid() {
		return formGrid;
	}

	public Map<String, IFieldInfo> getFieldInfoByName() {
		return fieldInfoByName;
	}

	public IPanelMediator getMediator() {
		return mediator;
	}

	@Override
	public String getViewName() {
		return parentViewName;
	}

	public String getParentViewName() {
		return parentViewName;
	}

	public void setParentViewName(String parentViewName) {
		this.parentViewName = parentViewName;
	}

	
	
}
