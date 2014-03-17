package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.ILinkedCell;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.util.IntHolder;
import com.hiperf.common.ui.client.util.PersistenceResult;
import com.hiperf.common.ui.client.validation.ValidationHelper;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.WrapperListValidationResults;
import com.hiperf.common.ui.shared.WrapperValidationResults;
import com.hiperf.common.ui.shared.util.Id;



public class EditableWrappedFlexTable extends WrappedFlexTable implements ClickHandler  {

	private boolean editable;
	private int firstDataCol;
	private DeleteCheckBox lastChecked = null;

	public EditableWrappedFlexTable(IWrapperListModel rows, List<HeaderInfo> headerList) {
		super(WrapperContext.getFieldInfoByName().get(rows.getNakedObjectName()), rows, false, headerList);
		addClickHandler(this);
		this.editable= true;
		firstDataCol = 1;
	}

	public EditableWrappedFlexTable(IWrapperListModel rows, boolean showDeleteColumn, List<HeaderInfo> headerList) {
		super(WrapperContext.getFieldInfoByName().get(rows.getNakedObjectName()), rows, false, headerList);
		addClickHandler(this);
		this.editable= true;
		firstDataCol = showDeleteColumn ? 1 : 0;
	}

	public EditableWrappedFlexTable(IWrapperListModel rows) {
		this(rows, true, null);
	}

	protected void fillTable() {
		doFillTable();
	}


	public int getFirstDataCol() {
		return firstDataCol;
	}

	public void setFirstDataCol(int firstDataCol) {
		this.firstDataCol = firstDataCol;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean b) {
		if(b && !editable) {
			this.firstDataCol = 1;
			this.editable = b;
			if(isDisplayHeader()) {
				insertCell(0, 0);
				getCellFormatter().addStyleName(0, 0, "custom-table-th");
			}
			if(getRowCount() > 0) {
				for(int i=getFirstDataRow(); i<getRowCount(); i++) {
					insertCell(i, 0);
					Widget widget = getWidget(i, 1);
					if(widget instanceof ILinkedCell) {
						doInsertDeleteCheckBox(i, ((ILinkedCell)widget).getParentWrapper());
					} else {
						doInsertDeleteCheckBox(i, ((ICell)widget).getWrapper());
					}
					resetCellStyle(i);
				}
			}
		} else if(!b && editable) {
			this.editable = b;
			this.firstDataCol = 0;
			removeDeleteCheckBoxColumn();
		}

	}

	public void removeDeleteCheckBoxColumn() {
		for(int i = 0; i < getRowCount(); i++) {
			removeCell(i, 0);
			if(i>=getFirstDataRow())
				resetCellStyle(i);
		}
	}

	private void resetCellStyle(int row) {
		int j = getFirstDataCol();
		for (HeaderInfo hi : sortedFields) {
			if(hi.isDisplayed()) {
				int idx = j + hi.getIndex();
				setCellStyle(fieldInfoByName.get(hi.getAttribute()), row, idx, hi.getEditable());
			}
		}
	}

	protected void setWrappedObjectCell(int row, int col, IWrapper w,
			String att, boolean editable) throws AttributeNotFoundException {
		setWidget(row, col, new WrappedObjectCell(w, att, isPersistent(), editable && isEditable() && fieldInfoByName.get(att).isEditable(), mediator));
	}

	protected void setListBoxCell(int row, int col, IWrapper wrapper,
			String attribute) {
		setWidget(row, col, new EditableListBoxCell(wrapper, attribute, mediator));
	}

	@Override
	protected void insertDeleteCell(int row, IWrapper w) {
		if(isEditable() && getFirstDataCol() > 0) {
			doInsertDeleteCheckBox(row, w);
		}
	}

	protected void doInsertDeleteCheckBox(int row, IWrapper w) {
		DeleteCheckBox cb = new DeleteCheckBox(w);
		setWidget(row, 0, cb);
		if(mediator != null)
			mediator.addDeleteCheckBox(cb);
		getCellFormatter().addStyleName(row, 0, "custom-table-td");
		getCellFormatter().addStyleName(row, 0, "custom-table-td-editable");
	}

	@Override
	public void onClick(ClickEvent event) {
		if(editable) {
			Cell cell = getCellForEvent(event);
			if(cell!=null) {
				int row  = cell.getRowIndex();
				int col = cell.getCellIndex();
				if(col >= getFirstDataCol()) {
					if(ViewHelper.showDetail(this, row, col))
						event.stopPropagation();
				} else if(col == 0) {
					Widget w = getWidget(row, 0);
					if(w != null && w instanceof DeleteCheckBox) {
						DeleteCheckBox clicked = (DeleteCheckBox) w;
						if(event.getNativeEvent().getShiftKey()) {
							int i0 = -1;
							if(lastChecked == null && row > getFirstDataRow())
								i0 = 0;
							else if(lastChecked != null && lastChecked != clicked) {
								for(int i = getFirstDataRow(); i< getRowCount(); i++) {
									if( i != row) {
										w = getWidget(i, 0);
										if(w == lastChecked) {
											i0 = i;
											break;
										}		
									}
								}
							}
							if(i0 >= 0) {
								int min = row > 0 ? Math.min(i0, row-1) : 1;
								int max = Math.max(row - 1, i0);
								if(min <= max) {
									for(int i = min; i <= max; i++) {
										w = getWidget(i, 0);
										if(w != null && w instanceof DeleteCheckBox) {
											DeleteCheckBox db = (DeleteCheckBox) w;
											db.setValue(clicked.getValue(), true);
										}	
									}									
								}
							}
						} else {
							lastChecked  = clicked;
						}
					}
				}
			}
		} else {
			event.stopPropagation();
		}
	}

	protected void setWrappedCollectionCell(int row, int col, IWrapper w,
			String att, boolean editable) throws AttributeNotFoundException {
		IWrapperListModel wrappedCollection = w.getWrappedCollection(att, isPersistent());
		if(wrappedCollection != null) {
			setWidget(row, col, new WrappedCollectionCell(w, att, isPersistent(), editable && isEditable() && fieldInfoByName.get(att).isEditable(), mediator));
		}
	}

	@Override
	public void removeRows() {
		IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(getClassName()).getUpdateHandler();
		final IntHolder ok = new IntHolder(0);
		if(updateHandler != null) {
			for(int i = getFirstDataRow(); i<getRowCount(); i++) {
				Widget w = getWidget(i, 0);
				if(w != null && w instanceof DeleteCheckBox && ((DeleteCheckBox)w).getValue()) {
					
					IWrapper wr = ((ICell)w).getWrapper();
					if(updateHandler != null) {
						try {
							updateHandler.beforeDelete(null, wr.getContent(), new AsyncCallback<String>() {
	
								@Override
								public void onFailure(Throwable caught) {
									ViewHelper.showException(caught);
									ok.increment();
								}
	
								@Override
								public void onSuccess(String result) {
								}
							});
						} catch (UpdateException e) {
							ViewHelper.showException(e);
							ok.increment();
							break;
						}
					}
				}
			}
			if(ok.get() == 0) {
				doRemoveRows();
			}
		} else
			doRemoveRows();
	}

	protected void doRemoveRows() {
		for(int i = getFirstDataRow(); i<getRowCount(); i++) {
			Widget w = getWidget(i, 0);
			if(w != null && w instanceof DeleteCheckBox && ((DeleteCheckBox)w).getValue()) {
				IWrapper wr = ((ICell)w).getWrapper();
				rows.removeItem(wr);
				i--;
			}
		}
	}

	@Override
	public void insertNewRow(IWrapper w) {
		rows.addItem(rows.getItems().size(), w);
	}

	@Override
	public void selectAll(boolean select) {
		for(int i = getFirstDataRow(); i<getRowCount(); i++) {
			Widget w = getWidget(i, 0);
			if(w != null && w instanceof DeleteCheckBox)
				((DeleteCheckBox)w).setValue(select, true);
		}
	}

	@Override
	public void validate(final AsyncCallback<PersistenceResult> cb) {
		final WrappedObjectsRepository repo = WrappedObjectsRepository.getInstance();
		if(repo.hasToCommit()) {
			Map<IWrapper, Integer> toValidate = new HashMap<IWrapper, Integer>();
			for(INakedObject o : repo.getInsertedObjects()) {
				if(o.getClass().getName().equals(getClassName())) {
					IWrapper w = findWrapperByReference(o);
					if(w != null) {
						int i = findRowByReference(w);
						toValidate.put(w, i);
					}
				}
			}
			ValidationHelper.validateTable(toValidate, new AsyncCallback<WrapperListValidationResults>() {

				@Override
				public void onFailure(Throwable caught) {
					ViewHelper.showException(caught);
				}

				@Override
				public void onSuccess(
						WrapperListValidationResults res) {
					if(res == null) {
						doPersist(repo, cb);
					} else if(res.isValidationDone()) {
						if(res.hasErrors()) {
							StringBuilder sb = new StringBuilder(NakedConstants.constants.errorDataNotValid());
							for(Entry<String, WrapperValidationResults> e : res.getValidationErrorsByClassName().entrySet()) {
								String w = e.getKey();
								WrapperValidationResults wr = e.getValue();
								sb.append("\n\t"+NakedConstants.constants.element()).append(WrapperContext.getFormLabel(w, null)).append(" : ");
								for(Entry<String, Set<String>> entry : wr.getValidationErrorsByAttribute().entrySet()) {
									sb.append("\n\t\t").append(entry.getKey()).append(" ").append(entry.getValue().toString());
								}
							}
							MessageBox.alert(sb.toString());
						} else {
							doPersist(repo, cb);
						}
					}
				}
			});


		}
	}

	private void doPersist(final WrappedObjectsRepository repo, final AsyncCallback<PersistenceResult> cb) {
		ViewHelper.showWaitPopup(NakedConstants.constants.uploading());
		PersistenceServiceAsync service = PersistenceService.Util.getInstance();
		AsyncCallback<Map<Id, INakedObject>> callback = new AsyncCallback<Map<Id, INakedObject>>() {

			public void onFailure(Throwable caught) {
				if(caught.getMessage() != null) {
					MessageBox.alert((caught.getCause() != null) ? caught.getCause()
									.getMessage() : caught.getMessage());
				} else {
					MessageBox.alert(NakedConstants.constants.exceptionPersistDataDB());
				}
				ViewHelper.hideWaitPopup();
				if(cb!=null)
					cb.onFailure(caught);
			}

			public void onSuccess(Map<Id, INakedObject> result) {
				repo.setLastCommitDate(new Date().getTime());
				if (result != null && !result.isEmpty()) {
					 clearSortColumns(null);
					 for(Entry<Id, INakedObject> entry : result.entrySet()) {
						INakedObject value = entry.getValue();
						String className = value.getClass().getName();
						Id oldId = entry.getKey();
						if(className.equals(getClassName())) {
							resetRaw(oldId, value);
						}
					 }
					 redrawLinkedCells();
				}
				List<INakedObject> insertedObjects = new ArrayList<INakedObject>(repo.getInsertedObjects());
				repo.clear();
				ViewHelper.hideWaitPopup();
				if(cb != null)
					cb.onSuccess(new PersistenceResult(insertedObjects, result));
			}


		};
		service.persist(repo.getAllObjectsToPersist(), callback);
	}

	private void redrawLinkedCells() {
		int rowCount = getRowCount();
		if(rowCount > 0) {
			for(HeaderInfo hi : getSortedFields()) {
				if(hi.isDisplayed()) {
					IFieldInfo fi = fieldInfoByName.get(hi.getAttribute());
					if(fi.getDataType().equals(DataType.NAKED_OBJECT)) {
						int col = hi.getIndex() + getFirstDataCol();
						for(int i = 0; i<getRowCount(); i++) {
							((ICell)getWidget(i, col)).redraw();
						}
					}
				}
			}
		}
	}

	private void resetRaw(Id oldId, INakedObject newObject) {
		boolean found = false;
		for(int row = 0; row<getRowCount() && !found; row++) {
			for(int col = 0; col<getCellCount(row) && !found; col++) {
				Widget widget = getWidget(row, col);
				if(widget != null) {
					IWrapper wrapper = null;
					if(widget instanceof ILinkedCell) {
						wrapper = ((ILinkedCell)widget).getParentWrapper();
					} else if(widget instanceof ICell) {
						wrapper = ((ICell)widget).getWrapper();
					}

					if(wrapper != null && wrapper.getContent() != null && PersistenceManager.getId(wrapper).equals(oldId)) {
						wrapper.setContent(newObject);
						getRowFormatter().removeStyleName(row, "updatedRow");
						getRowFormatter().setStyleName(row, "custom-table-tr");
						if(row == 0 || (row > 1 && row % 2 == 0)) {
							getRowFormatter().addStyleName(row, "custom-table-tr-even");
						}
						redrawRow(row, oldId);
						found = true;
					}
				}
			}
		}
	}

	private IWrapper findWrapperByReference(INakedObject o) {
		for(IWrapper w : rows.getItems()) {
			if(o == w.getContent()) {
				return w;
			}
		}
		return null;
	}

	public int findRowByReference(IWrapper w) {
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

					if(wrapper != null && (wrapper == w || (wrapper != null && w != null && wrapper.getContent() != null
							&& w.getContent() != null
							&& wrapper.getContent() == w.getContent()))) {
						return row;
					}
				}
			}

		}
		return -1;
	}

	public void validateAndRefresh() {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		if(wor.hasToCommit()) {
			ViewHelper.showWaitPopup(NakedConstants.constants.uploading());
			PersistenceServiceAsync service = PersistenceService.Util.getInstance();
			AsyncCallback<Map<Id, INakedObject>> callback = new AsyncCallback<Map<Id, INakedObject>>() {

				public void onFailure(Throwable caught) {
					if(caught.getMessage() != null) {
						MessageBox.alert((caught.getCause() != null) ? caught.getCause()
										.getMessage() : caught.getMessage());
					} else {
						MessageBox.alert(NakedConstants.constants.exceptionPersistDataDB());
					}
					//GWT.log("Exception in persist", caught);
					ViewHelper.hideWaitPopup();
				}

				public void onSuccess(Map<Id, INakedObject> result) {
					wor.setLastCommitDate(new Date().getTime());
					TablePanel tp = (TablePanel)getTablePanel();
					wor.clear();
					refresh(1, tp.getNbRowsPerPage());
					ViewHelper.hideWaitPopup();
				}

			};
			service.persist(wor.getAllObjectsToPersist(), callback);
		}
	}


}
