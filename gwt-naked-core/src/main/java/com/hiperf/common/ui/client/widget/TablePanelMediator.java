package com.hiperf.common.ui.client.widget;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IListBoxCell;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.PendingChangeHandler;
import com.hiperf.common.ui.client.WrapperUpdatedHandler;
import com.hiperf.common.ui.client.event.ChangeHeaderLabelEvent;
import com.hiperf.common.ui.client.event.ChangeHeaderLabelHandler;
import com.hiperf.common.ui.client.event.CollectionDataAddedEvent;
import com.hiperf.common.ui.client.event.CollectionDataAddedHandler;
import com.hiperf.common.ui.client.event.CollectionDataRemovedEvent;
import com.hiperf.common.ui.client.event.CollectionDataRemovedHandler;
import com.hiperf.common.ui.client.event.DisplayColumnEvent;
import com.hiperf.common.ui.client.event.DisplayColumnHandler;
import com.hiperf.common.ui.client.event.HideColumnEvent;
import com.hiperf.common.ui.client.event.HideColumnHandler;
import com.hiperf.common.ui.client.event.HideLoadingPopupEvent;
import com.hiperf.common.ui.client.event.HideLoadingPopupHandler;
import com.hiperf.common.ui.client.event.SwitchColumnsEvent;
import com.hiperf.common.ui.client.event.SwitchColumnsHandler;
import com.hiperf.common.ui.client.event.TotalPagesEvent;
import com.hiperf.common.ui.client.event.TotalPagesHandler;
import com.hiperf.common.ui.client.event.WrapperAddedEvent;
import com.hiperf.common.ui.client.event.WrapperAddedHandler;
import com.hiperf.common.ui.client.event.WrapperRemovedEvent;
import com.hiperf.common.ui.client.event.WrapperRemovedHandler;
import com.hiperf.common.ui.client.event.WrapperUpdatedEvent;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.validation.ValidationHelper;
import com.hiperf.common.ui.client.widget.TablePanel.PendingModifsImage;
import com.hiperf.common.ui.client.widget.WrappedFlexTable.HeaderCell;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.WrapperValidationResults;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.PendingChangeEvent;

public class TablePanelMediator extends PanelMediator implements PendingChangeHandler, TotalPagesHandler, CollectionDataAddedHandler,
		CollectionDataRemovedHandler, HideLoadingPopupHandler,
		WrapperUpdatedHandler,
		ChangeHeaderLabelHandler, DisplayColumnHandler, HideColumnHandler,
		SwitchColumnsHandler, WrapperAddedHandler, WrapperRemovedHandler {

	private final TablePanel tablePanel;
	private HandlerRegistration hideLoadingPopupHandler;
	private ICloseablePopupPanel popupContainer = null;

	public TablePanelMediator(TablePanel tablePanel) {
		super();
		this.tablePanel = tablePanel;

	}

	protected void doRegister() {
		super.doRegister();
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(WrapperUpdatedEvent.getType(), this));
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(PendingChangeEvent.getType(), this));
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(TotalPagesEvent.getType(), this));
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(CollectionDataAddedEvent.getType(), this));
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(CollectionDataRemovedEvent.getType(), this));
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(WrapperAddedEvent.getType(), this));
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(WrapperRemovedEvent.getType(), this));
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(DisplayColumnEvent.getType(), this));
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(HideColumnEvent.getType(), this));
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(SwitchColumnsEvent.getType(), this));		
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(ChangeHeaderLabelEvent.getType(), this));
		hideLoadingPopupHandler = WrapperContext.getEventBus().addHandler(HideLoadingPopupEvent.getType(), this);
	}

	@Override
	public void onItemRemoved(WrapperRemovedEvent event) {
		if(event.getSource() == tablePanel.getTable().getItems()) {
			IWrapper w = event.getObject();
			int rowNb = tablePanel.getTable().findRow(w);
			if(rowNb >= 0)
				tablePanel.getTable().removeRow(rowNb);
			WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
			if(!wor.isPauseModifListener() && tablePanel.getPendingModifsImage() != null) {
				if(wor.hasToCommit())
					tablePanel.getPendingModifsImage().show();
				else
					tablePanel.getPendingModifsImage().hide();
			}
		}
	}

	@Override
	public void onItemAdded(WrapperAddedEvent event) {
		if(event.getSource() == tablePanel.getTable().getItems()) {
			tablePanel.getTable().displayRow(event.getRow(), event.getObject());
			if(!WrappedObjectsRepository.getInstance().isPauseModifListener() && tablePanel.getPendingModifsImage() != null)
				tablePanel.getPendingModifsImage().show();
		}
	}

	@Override
	public void onSwitchColumns(SwitchColumnsEvent event) {
		if(event.getSource() == tablePanel.getTable()) {
			//GWT.log("onSwitchColumns "+event.getAtt1()+"("+event.getIndex1()+") / "+event.getAtt2()+"("+event.getIndex2()+")");
			WrappedFlexTable table = tablePanel.getTable();
			int i1 = event.getIndex1() + table.getFirstDataCol();
			com.google.gwt.user.client.Element h1 = table.getThElement(i1);
			HeaderCell hc1 = table.getHeaderWidgets().get(i1);
			int i2 = event.getIndex2() + table.getFirstDataCol();
			com.google.gwt.user.client.Element h2 = table.getThElement(i2);
			HeaderCell hc2 = table.getHeaderWidgets().get(i2);
			table.internalClearCell(h1, true);
			table.internalClearCell(h2, true);
			table.getHeaderWidgets().set(i1, hc2);
			table.getHeaderWidgets().set(i2, hc1);
			h1.setInnerText(hc2.getLabel());
			h2.setInnerText(hc1.getLabel());
			/*table.setHeaderCell(hc2, h1);
			table.setHeaderCell(hc1, h2);*/
			HeaderInfo hi1 = table.getSortedFields().get(event.getIndex1());
			HeaderInfo hi2 = table.getSortedFields().get(event.getIndex2());
			hi1.setIndex(event.getIndex2());
			hi2.setIndex(event.getIndex1());
			for(int i = 0; i<table.getRowCount(); i++) {
				IWrapper w = table.getWrapper(i);
				boolean editable = ViewHelper.isEditable(w);
				Id id = PersistenceManager.getIdLocalPart(w);
				table.clearCell(i, i2);
				table.displayCell(i, w, hi1, id, editable);
				table.clearCell(i, i1);
				table.displayCell(i, w, hi2, id, editable);
			}
		}
	}

	@Override
	public void onHideColumn(HideColumnEvent e) {
		if(e.getSource() == tablePanel.getTable()) {
			WrappedFlexTable table = tablePanel.getTable();
			int toRemove = e.getIndex() + table.getFirstDataCol();
			int cellCount = table.getHeaderWidgets().size();
			if(cellCount <=  toRemove)
				toRemove = cellCount - 1;
			for(int i = 0; i<table.getRowCount(); i++) {
				table.removeCell(i, toRemove);
			}

			if(table.isDisplayHeader()) {
				table.getThElement(toRemove).removeFromParent();
				table.getHeaderWidgets().remove(toRemove);
				if(table.getResizeHandler() != null) {
					table.getResizeHandler().removeAutoFittedColumn(toRemove);
				}
			}
		}
	}

	@Override
	public void onDisplayColumn(DisplayColumnEvent e) {
		if(e.getSource() == tablePanel.getTable()) {
			WrappedFlexTable table = tablePanel.getTable();
			String att = e.getAttribute();
			HeaderInfo hi = ViewHelper.getHeaderInfo(att, table.getSortedFields());
			displayColumn(hi, false);
		}
	}

	private void displayColumn(HeaderInfo hi, boolean createHeader) {
		WrappedFlexTable table = tablePanel.getTable();
		if(table.isDisplayHeader())
			table.displayHeaderCell(hi, createHeader);
		int toDisplay = hi.getIndex() + table.getFirstDataCol();
		if(table.getResizeHandler() != null) {
			table.getResizeHandler().removeAutoFittedColumn(toDisplay);
		}
		for(int i = 0; i<table.getRowCount(); i++) {
			table.insertCells(i, toDisplay, 1);
			IWrapper w = table.getWrapper(i);
			if(w != null) {
				boolean editable = ViewHelper.isEditable(w);
				table.displayCell(i, w, hi, PersistenceManager.getIdLocalPart(w), editable);
			}
		}
	}

	@Override
	public void onChangeHeaderLabel(ChangeHeaderLabelEvent e) {
		if(e.getSource() == tablePanel.getTable()) {
			WrappedFlexTable table = tablePanel.getTable();
			for(HeaderInfo hi : table.getSortedFields()) {
				if(hi.getIndex() == e.getIndex()) {
					hi.setLabel(e.getLabel());
					table.displayHeaderCell(hi);
				}
			}
		}
	}

	@Override
	public void onItemUpdated(WrapperUpdatedEvent event) {
		//GWT.log("TablePanelMediator : onItemUpdated"+event.toString());
		WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		IWrapper w = event.getWrapper();
		WrappedFlexTable table = tablePanel.getTable();
		if(table.isPersistent() && WrapperContext.getClassInfoByName().get(w.getWrappedClassName()).isEntity())
			wor.addUpdatedObject(w, event.getAttribute(), event.getOldValue(), event.getNewValue());
		int rowNb = table.findRow(w);
		if(rowNb >= 0) {
			table.getRowFormatter().setStyleName(rowNb, "updatedRow");
			table.redrawRow(rowNb, null);
		}
		INakedObject content = w.getContent();
		List<WrapperUpdatedHandler> list = updHandlersByNoMap.get(content);
		if(list != null && !list.isEmpty()) {
			for(WrapperUpdatedHandler h : list) {
				h.onItemUpdated(event);
			}
		}
		Set<ICell> set = toRedrawCellsByUpdObject.get(content);
		if(set != null && !set.isEmpty()) {
			for(ICell cell : set) {
				cell.redraw();
			}
		}
	}

	@Override
	public void onHideLoadingPopup(HideLoadingPopupEvent e) {
		if(e.alive()) {
			ViewHelper.hideWaitPopup();
			if(popupPanel != null && popupPanel.isShowing()) {
				ViewHelper.showPopupPanel(popupPanel);
			} else {
				ICloseablePopupPanel pop = (ICloseablePopupPanel) ViewHelper.getPopupPanel(tablePanel.getTable());
				if(pop != null && pop.isShowing()) {
					ViewHelper.showPopupPanel(pop);
				}
			}
			e.cancel();			
		}
	}
	
	protected void removeHideHandler() {    	
		if(hideLoadingPopupHandler != null) {
			hideLoadingPopupHandler.removeHandler();
			hideLoadingPopupHandler = null;
		}
    }

	@Override
	public void onItemRemoved(CollectionDataRemovedEvent event) {
		Map<IListBoxCell, CollectionDataRemovedHandler> map = removedHandlersByLbCellMap.get((INakedObject)event.getSource());
		if(map != null && !map.isEmpty()) {
			for(IListBoxCell lbc : map.keySet()) {
				CollectionDataRemovedHandler h = map.get(lbc);
				h.onItemRemoved(event);
			}
		}
	}

	@Override
	public void onItemAdded(CollectionDataAddedEvent event) {
		Map<IListBoxCell, CollectionDataAddedHandler> map = addedHandlersByLbCellMap.get((INakedObject)event.getSource());
		if(map != null && !map.isEmpty()) {
			for(IListBoxCell lbc : map.keySet()) {
				CollectionDataAddedHandler h = map.get(lbc);
				h.onItemAdded(event);
			}
		}

	}

	@Override
	public void onTotalPagesChanged(TotalPagesEvent e) {
		if(e.getSource() == tablePanel.getTable().getItems()) {
			tablePanel.getPagingPanel().setTotalPages(e.getTotal());
			tablePanel.getPagingPanel().setCurrentPage(e.getPage());
			if(popupContainer != null) {
				popupContainer.setPopupPosition(Math.max(Window.getClientWidth() - popupContainer.getOffsetWidth(), 0) / 2, Math.max(Window.getClientHeight() - popupContainer.getOffsetHeight(), 0) / 2);				
			}
		}
	}

	@Override
	public void onChange(PendingChangeEvent e) {
		PendingModifsImage img = tablePanel.getPendingModifsImage();
		if(!WrappedObjectsRepository.getInstance().hasToCommit()) {
			if(img != null)
				img.hide();
			PersistenceManager.reinitSequence();
		} else if(img != null)
			img.show();

	}

	public void addValidateButton(final ObjectPanel vp, final int rowNb) {
		Button b = new Button(NakedConstants.constants.validate());
		b.setStylePrimaryName("naked-button");
		vp.add(b);
		vp.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_CENTER);
		WrappedObjectsRepository.getInstance().getPendingValidations().add(vp.getObjectForm().getWrapper().getContent());
		b.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final IWrappedObjectForm form = vp.getObjectForm();
				AsyncCallback<WrapperValidationResults> callback = new AsyncCallback<WrapperValidationResults>() {

					@Override
					public void onFailure(Throwable caught) {}

					@Override
					public void onSuccess(WrapperValidationResults result) {
						if(result == null) {
							validate(vp, rowNb, form);
						} else if(result.isValidationDone()) {
							if(result.hasErrors()) {
								ValidationHelper.processValidationErrors(form, result.getValidationErrorsByAttribute(), result.getMessage());
							} else {
								validate(vp, rowNb, form);
							}
						}
					}

					private void validate(final ObjectPanel vp,
							final int rowNb,
							final IWrappedObjectForm form) {
						WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
						WrappedFlexTable table = tablePanel.getTable();
						boolean error= false;
						IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(table.getClassName()).getUpdateHandler();
						if(updateHandler != null) {
							try {
								INakedObject content = form.getWrapper().getContent();
								updateHandler.afterSet(content, content, form, null);
							} catch (UpdateException e) {
								ViewHelper.showException(e);
								error = true;
							}
						}
						if(!error) {
							wor.checkNewID(vp.getOriginalId(), form.getWrapper());
							int stateIdx = vp.getStateIdx();
							boolean pendingChanges = wor.hasPendingChanges(stateIdx);
							wor.clearSavedStatesIfNeeded(stateIdx);
							if(rowNb < 0) {
								table.insertNewRow(form.getWrapper());
							} else if(pendingChanges) {
								//Refresh updated line 
								table.getRowFormatter().setStyleName(rowNb, "updatedRow");
								table.redrawRow(rowNb, vp.getOriginalId());
							}
							WrappedObjectsRepository.getInstance().getPendingValidations().remove(form.getWrapper().getContent());
							popupPanel.setForceClose(true);
							popupPanel.hide();
						}
					}
				};
				ValidationHelper.validateForm(form, callback);
			}

		});
	}

	public void hideColumn(HeaderInfo hi) {
		int index = hi.getIndex();
		hi.setDisplayed(false);
		String att = hi.getAttribute();
		WrappedFlexTable table = tablePanel.getTable();
		table.sortFields();

		Map<String, IFieldInfo> availableColumns = table.getFieldInfoByName();
		IFieldInfo fi = availableColumns.get(att);

		hi.setIndex(availableColumns.size() - 1);
		fi.setDisplayed(false);

		WrapperContext.getEventBus().fireEventFromSource(new HideColumnEvent(index), table);
	}

	public void insertColumn(HeaderInfo hi) {
		hi.setDisplayed(true);
		String att = hi.getAttribute();
		WrappedFlexTable table = tablePanel.getTable();
		Map<String, IFieldInfo> availableColumns = table.getFieldInfoByName();
		IFieldInfo fi = availableColumns.get(att);
		fi.setDisplayed(true);
		for(HeaderInfo h : table.getSortedFields()) {
			if(!h.getAttribute().equals(hi.getAttribute()) && h.getIndex() >= hi.getIndex())
				h.setIndex(h.getIndex() + 1);
		}
		fi.setDisplayed(true);
		table.sortFields();
		displayColumn(hi, true);
	}

	public void addDeleteCheckBox(DeleteCheckBox cb) {
		cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(!event.getValue()) {
					CheckBox selectAllCheckbox = tablePanel.getSelectAllCheckbox();
					if(selectAllCheckbox != null && selectAllCheckbox.getValue())
						selectAllCheckbox.setValue(false);
				}
			}
		});
	}

	public TablePanel getTablePanel() {
		return tablePanel;
	}

	public void setPopupContainer(ICloseablePopupPanel popupPanel) {
		this.popupContainer  = popupPanel;
	}

}
