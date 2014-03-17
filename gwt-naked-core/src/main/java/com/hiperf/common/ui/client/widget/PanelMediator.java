package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PositionCallback;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.ICell;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.ILinkedCell;
import com.hiperf.common.ui.client.IListBoxCell;
import com.hiperf.common.ui.client.IObjectPanel;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrappedCollectionCell;
import com.hiperf.common.ui.client.IWrappedObjectCell;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.IWrappedTable;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.WrapperUpdatedHandler;
import com.hiperf.common.ui.client.event.CollectionDataAddedEvent;
import com.hiperf.common.ui.client.event.CollectionDataAddedHandler;
import com.hiperf.common.ui.client.event.CollectionDataRemovedEvent;
import com.hiperf.common.ui.client.event.CollectionDataRemovedHandler;
import com.hiperf.common.ui.client.event.WrappedObjectCellUpdatedHandler;
import com.hiperf.common.ui.client.event.WrapperObjectAddedEvent;
import com.hiperf.common.ui.client.event.WrapperObjectAddedHandler;
import com.hiperf.common.ui.client.event.WrapperSelectedEvent;
import com.hiperf.common.ui.client.event.WrapperSelectedHandler;
import com.hiperf.common.ui.client.event.WrapperUpdatedEvent;
import com.hiperf.common.ui.client.exception.UniqueItemException;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.validation.ValidationHelper;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.WrapperValidationResults;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.PendingChangeEvent;

public abstract class PanelMediator implements IPanelMediator, CloseHandler<ICloseablePopupPanel>, WrapperObjectAddedHandler {

    protected ICloseablePopupPanel popupPanel;

    //Registered select handler
    protected HandlerRegistration selectHandler;

    //Other registered handlers
    protected List<HandlerRegistration> handlerRegistry;

    //Other UnRegistered Handlers
    protected Map<INakedObject, List<WrapperUpdatedHandler>> updHandlersByNoMap;
    protected Map<INakedObject, Map<IListBoxCell, CollectionDataAddedHandler>> addedHandlersByLbCellMap;
    protected Map<INakedObject, Map<IListBoxCell, CollectionDataRemovedHandler>> removedHandlersByLbCellMap;

    protected Map<INakedObject, Set<IWrappedCollectionCell>> ccCellsByParentWrapperMap;
    protected Map<INakedObject, Set<ICell>> toRedrawCellsByUpdObject;

	private boolean registered = false;

    public PanelMediator() {
        super();
        doRegister();
    }

	public void registerAll() {
		if(!registered) {
			doRegister();
		}
	}

	protected void doRegister() {
		this.handlerRegistry = new ArrayList<HandlerRegistration>();
		this.updHandlersByNoMap = new HashMap<INakedObject, List<WrapperUpdatedHandler>>();
		this.addedHandlersByLbCellMap = new HashMap<INakedObject, Map<IListBoxCell,CollectionDataAddedHandler>>();
		this.removedHandlersByLbCellMap = new HashMap<INakedObject, Map<IListBoxCell,CollectionDataRemovedHandler>>();
		this.toRedrawCellsByUpdObject = new HashMap<INakedObject, Set<ICell>>();
		this.ccCellsByParentWrapperMap = new HashMap<INakedObject, Set<IWrappedCollectionCell>>();
		this.selectHandler = null;
		this.popupPanel = null;
		this.handlerRegistry.add(WrapperContext.getEventBus().addHandler(WrapperObjectAddedEvent.getType(), this));
		this.registered = true;
	}

    private void initPopupPanel() {
        this.popupPanel = ViewHelper.newPopup(true);
        this.popupPanel.addCloseHandler(this);
    }

    @Override
    public void addWrapperUpdatedHandler(final String att, final IWrapper iWrapper,
            final IWrappedCollectionCell cell) {
        INakedObject no = iWrapper.getContent();
        if(no != null) {
            List<WrapperUpdatedHandler> list = updHandlersByNoMap.get(no);
            if (list == null) {
                list = new ArrayList<WrapperUpdatedHandler>();
                updHandlersByNoMap.put(no, list);
            }
            WrapperUpdatedHandler wuh = new WrapperUpdatedHandler() {

                @Override
                public void onItemUpdated(WrapperUpdatedEvent event) {
                    boolean mustRedraw = false;
                    IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(cell.getLinkedObjectClass()).getUpdateHandler();
                    if(updateHandler != null) {
                        mustRedraw = updateHandler.mustRedraw(event, iWrapper.getContent());
                    }
                    mustRedraw = mustRedraw || iWrapper.getContent() == event.getSource();
                    if(mustRedraw)
                        cell.redrawSingleElement(att, iWrapper);
                }};
            list.add(wuh);
        }
    }

    @Override
    public void addWrapperObjectAddedHandler(
            final IWrappedCollectionCell cell) {
    	INakedObject content = cell.getParentWrapper().getContent();
		Set<IWrappedCollectionCell> set = ccCellsByParentWrapperMap.get(content);
    	if(set == null) {
    		set = new HashSet<IWrappedCollectionCell>();
    		ccCellsByParentWrapperMap.put(content, set);
    	}
    	set.add(cell);
    }


    @Override
    public void addCollectionDataAddedHandler(final IListBoxCell listBoxCell) {
        final IWrapper wrapper = listBoxCell.getWrapper();
        final String attribute = listBoxCell.getAttribute();
        Map<IListBoxCell, CollectionDataAddedHandler> map = addedHandlersByLbCellMap.get(wrapper.getContent());
        if(map == null) {
            map = new HashMap<IListBoxCell, CollectionDataAddedHandler>();
            addedHandlersByLbCellMap.put(wrapper.getContent(), map);
        }
        map.put(listBoxCell, new CollectionDataAddedHandler() {

            @Override
            public void onItemAdded(CollectionDataAddedEvent event) {
                Collection c = wrapper.getCollection(attribute);
                List l = new ArrayList(c);
                l.remove(event.getValue());
                WrappedObjectsRepository.getInstance().addUpdatedObject(wrapper, attribute, l, c);
                listBoxCell.redraw();
            }
        });

    }

    @Override
    public void addCollectionDataRemovedHandler(final IListBoxCell listBoxCell) {
        final IWrapper wrapper = listBoxCell.getWrapper();
        final String attribute = listBoxCell.getAttribute();
        Map<IListBoxCell, CollectionDataRemovedHandler> map = removedHandlersByLbCellMap.get(wrapper.getContent());
        if(map == null) {
            map = new HashMap<IListBoxCell, CollectionDataRemovedHandler>();
            removedHandlersByLbCellMap.put(wrapper.getContent(), map);
        }
        map.put(listBoxCell, new CollectionDataRemovedHandler() {

            public void onItemRemoved(CollectionDataRemovedEvent event) {
                Collection c = wrapper.getCollection(attribute);
                List l = new ArrayList(c);
                l.add(event.getValue());
                WrappedObjectsRepository.getInstance().addUpdatedObject(wrapper, attribute, l, c);
                listBoxCell.redraw();
            }
        });
    }

    protected void removeSelectHandler() {
        if(selectHandler != null)
            selectHandler.removeHandler();
        selectHandler = null;
    }

    @Override
    public void addWrapperUpdatedHandler(final IWrappedObjectCell cell) {
    	if(cell instanceof WrappedObjectCell) {
    		final IWrapper iWrapper = cell.getParentWrapper().getWrappedAttribute(cell.getAttribute());
            INakedObject no = iWrapper.getContent();
            if(no != null) {
                List<WrapperUpdatedHandler> list = updHandlersByNoMap.get(no);
                if (list == null) {
                    list = new ArrayList<WrapperUpdatedHandler>();
                    updHandlersByNoMap.put(no, list);
                }
                list.add(new WrappedObjectCellUpdatedHandler(iWrapper, cell));
            }
    	}

    }

    @Override
    public void replaceWrapperUpdatedHandler(INakedObject oldContent,
            IWrappedObjectCell cell) {
        if(oldContent == null)
            addWrapperUpdatedHandler(cell);
        else {
            final IWrapper iWrapper = cell.getParentWrapper().getWrappedAttribute(cell.getAttribute());
            List<WrapperUpdatedHandler> list = updHandlersByNoMap.get(oldContent);
            if(list != null && !list.isEmpty()) {
                List<WrapperUpdatedHandler> toAdd = new ArrayList<WrapperUpdatedHandler>();
                Iterator<WrapperUpdatedHandler> it = list.iterator();
                while(it.hasNext()) {
                    WrapperUpdatedHandler h = it.next();
                    if(h instanceof WrappedObjectCellUpdatedHandler) {
                        WrappedObjectCellUpdatedHandler uh = (WrappedObjectCellUpdatedHandler) h;
                        IWrappedObjectCell c = uh.getCell();
                        if(c.getAttribute().equals(cell.getAttribute())
                                && c.getParentWrapper().getContent().equals(oldContent)) {
                            toAdd.add(new WrappedObjectCellUpdatedHandler(iWrapper, cell));
                            it.remove();
                        }
                    }
                }
                if(!toAdd.isEmpty())
                    list.addAll(toAdd);
            }
        }
    }

    @Override
    public void addWrapperSelectedForDeleteHandler(final IWrappedCollectionCell linkedCell) {
        if(selectHandler != null)
            selectHandler.removeHandler();
        selectHandler = WrapperContext.getEventBus().addHandler(WrapperSelectedEvent.getType(), new WrapperSelectedHandler() {

            @Override
            public void onItemSelected(WrapperSelectedEvent event) {
                if(event.getSource() == linkedCell) {
                    WrappedFlexTable table = event.getSelectionTable();
                    int selectedRow = event.getSelectedRow();
					IWrapper w = table.getWrapper(selectedRow);
                    table.removeRow(selectedRow);
                    table.getRows().getItems().remove(selectedRow);
                    IWrapper parentWrapper = linkedCell.getParentWrapper();
                    IFieldInfo fi = WrapperContext.getFieldInfoByName().get(parentWrapper.getWrappedClassName()).get(linkedCell.getAttribute());
                    WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
                    if(fi.isOneToMany()) {
                        wor.addRemovedObject(w);
                    } else if(fi.isManyToMany()) {
                        wor.removeManyToMany(parentWrapper.getWrappedClassName(), PersistenceManager.getId(parentWrapper), linkedCell.getAttribute(), PersistenceManager.getId(w));
                    }
                    wor.setPauseModifListener(true);
                    parentWrapper.removeObjectFromCollection(linkedCell.getAttribute(), w.getContent());
                    wor.addWrapperToRefresh(parentWrapper);
                    wor.setPauseModifListener(false);
                    try {
                        linkedCell.getLabel().decTotal();
                    } catch (UniqueItemException e) {
                		linkedCell.getLabel().setText(table.getRows().getItems().get(0).toString());
                    }
                    if(table.getRowCount() == 0)
                        hidePopup(false);
                    WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
                    int idx = linkedCell.getStateIdx();
                    if(idx >= 0)
                        WrappedObjectsRepository.getInstance().clearSavedStatesIfNeeded(idx);
                }
            }
        });
    }

    @Override
    public void addWrapperSelectedHandler(ILinkedCell cell, HandlerRegistration handler) {
    	if(selectHandler != null)
            selectHandler.removeHandler();
    	selectHandler = handler;
    }
    
    @Override
    public void addWrapperSelectedHandler(ILinkedCell cell) {
        if(selectHandler != null)
            selectHandler.removeHandler();
        if(cell instanceof WrappedObjectCell) {
            addWrapperSelectedHandlerForObjectCell(cell);
        } else {
            addWrapperSelectedHandlerForCollectionCell(cell);
        }

    }

	protected void addWrapperSelectedHandlerForObjectCell(
			ILinkedCell cell) {
		final WrappedObjectCell objCell = (WrappedObjectCell)cell;
		selectHandler = WrapperContext.getEventBus().addHandler(WrapperSelectedEvent.getType(), new WrapperSelectedHandler() {

		    @Override
		    public void onItemSelected(WrapperSelectedEvent event) {
		        if(event.getSource() == objCell) {
		            WrappedFlexTable table = event.getSelectionTable();
		            IWrapper w = table.getWrapper(event.getSelectedRow());
		            IWrapper parentWrapper = objCell.getParentWrapper();
		            INakedObject content = w.getContent();
		            IWrapper wrapper = parentWrapper.getWrappedAttribute(objCell.getAttribute());
		            INakedObject oldContent = wrapper.getContent();
		            wrapper.setContent(content);
		            IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
		            if(updateHandler != null) {
		                try {
		                    updateHandler.afterSet(content, parentWrapper.getContent(), getForm(), objCell.getAttribute());
		                } catch (UpdateException e) {
		                    ViewHelper.showException(e);
		                    wrapper.setContent(oldContent);
		                    return;
		                }
		            }
		            objCell.updateAndNotifyWrappedObject(content);
		            replaceWrapperUpdatedHandler(oldContent, objCell);
		            objCell.displayLinkedElement(wrapper, content);
		            table.removeRow(event.getSelectedRow());
		            if(table.getRowCount() == 0)
		                hidePopup(false);
		        }
		    }

		});
	}
	
	protected IWrappedObjectForm getForm() {
		return null;
	}

	protected void addWrapperSelectedHandlerForCollectionCell(
			ILinkedCell cell) {
		final IWrappedCollectionCell linkedCell = (IWrappedCollectionCell)cell;
		selectHandler = WrapperContext.getEventBus().addHandler(WrapperSelectedEvent.getType(), new WrapperSelectedHandler() {

		    @Override
		    public void onItemSelected(WrapperSelectedEvent event) {
		        if(event.getSource() == linkedCell) {
		            WrappedFlexTable table = event.getSelectionTable();
		            int selectedRow = event.getSelectedRow();
					IWrapper w = table.getWrapper(selectedRow);
		            table.removeRow(selectedRow);
		            IWrapper parentWrapper = linkedCell.getParentWrapper();
		            IFieldInfo fi = WrapperContext.getFieldInfoByName().get(parentWrapper.getWrappedClassName()).get(linkedCell.getAttribute());
		            WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		            if(fi.isOneToMany()) {
		                String mappedBy = fi.getMappedBy();
		                if(!WrapperContext.getFieldInfoByName().get(w.getWrappedClassName()).get(mappedBy).isNotNull()) {
		                    Object oldVal = w.getNakedAttribute(mappedBy);
		                    w.setNakedObjectAttribute(mappedBy, null);
		                    wor.addUpdatedObject(w, mappedBy, oldVal, null);
		                } else {
		                    wor.addRemovedObject(w);
		                }
		                wor.setPauseModifListener(true);
		                parentWrapper.addObjectToCollection(linkedCell.getAttribute(), w.getContent());
		                wor.addWrapperToRefresh(parentWrapper);
		                wor.setPauseModifListener(false);
		            } else if(fi.isManyToMany()) {
		                parentWrapper.addObjectToCollection(linkedCell.getAttribute(), w.getContent());
		                wor.addManyToMany(parentWrapper.getWrappedClassName(), PersistenceManager.getId(parentWrapper), linkedCell.getAttribute(), PersistenceManager.getId(w));
		                wor.addWrapperToRefresh(parentWrapper);
		            }
		            linkedCell.redraw(w.toString());
		            if(table.getRowCount() == 0)
		                hidePopup(false);		            
		            WrapperContext.getEventBus().fireEvent(new PendingChangeEvent());
		            int idx = linkedCell.getStateIdx();
		            if(idx >= 0)
		                WrappedObjectsRepository.getInstance().clearSavedStatesIfNeeded(idx);
		        }
		    }
		});
	}

    @Override
    public void addSpecialWrapperUpdatedHandler(INakedObject no,
            ICell cell) {
        Set<ICell> set = toRedrawCellsByUpdObject.get(no);
        if(set == null) {
            set = new HashSet<ICell>();
            toRedrawCellsByUpdObject.put(no, set);
        }
        set.add(cell);

    }

    @Override
    public void onClose(CloseEvent<ICloseablePopupPanel> event) {
        Widget w = popupPanel.getMainWidget();
        if(w instanceof TablePanel) {
            TablePanel tp = (TablePanel) w;
            tp.getMediator().unRegisterAll(true);
        } else if(w instanceof WrappedObjectForm) {
            WrappedObjectForm form = (WrappedObjectForm)w;
            form.getMediator().unRegisterAll(true);
        } else if(w instanceof ObjectPanel) {
            final ObjectPanel panel = (ObjectPanel)w;
            final IWrappedObjectForm form = panel.getObjectForm();
            try {
                form.getMediator().unRegisterAll(true);
            } catch(Exception e) {}
            if(!popupPanel.isForceClose()) {
                final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
                final int idx = panel.getStateIdx();
                if(wor.hasPendingChanges(idx)) {
                    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

                        @Override
                        public void onFailure(Throwable caught) {}

                        @Override
                        public void onSuccess(Boolean b) {
                            if(b!=null && b.booleanValue()) {
                                if(panel.isRemoveFromLists() && form != null)
                                    wor.removeObjectFromLists(form.getWrapper());
                                wor.restoreSavedState(idx);
                                popupPanel.hide();
                                IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(form.getWrapper().getWrappedClassName()).getUpdateHandler();
                				if(updateHandler != null) {
            						updateHandler.afterClose(form);
                				}
                            } else {
                                popupPanel.show();
                            }
                        }
                    };
                    MessageBox.confirm(MessageBox.TYPE.CONFIRM_YN, NakedConstants.constants.confirm(), NakedConstants.constants.questionDiscardChanges(), callback);

                }
            }
        } else if(w instanceof TextBoxCell) {
            if(!popupPanel.isForceClose()) {
                final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
                final int idx = popupPanel.getMediator().getSavedStateIdx();
                if(idx >= 0 && wor.hasPendingChanges(idx)) {
                    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

                        @Override
                        public void onFailure(Throwable caught) {}

                        @Override
                        public void onSuccess(Boolean b) {
                            if(b!=null && b.booleanValue()) {
                                popupPanel.hide();
                                wor.restoreSavedState(idx);
                            } else {
                                popupPanel.show();
                            }
                        }
                    };
                    MessageBox.confirm(MessageBox.TYPE.CONFIRM_YN, NakedConstants.constants.confirm(), NakedConstants.constants.questionDiscardChanges(), callback);
                }
            } else {
                ((TextBoxCell)w).redraw();
            }
        } else if(w instanceof WrappedTableConfigForm) {
            final WrappedTableConfigForm cf = (WrappedTableConfigForm)w;
            if(cf.isSomethingChanged()) {
                AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

                    @Override
                    public void onFailure(Throwable caught) {}

                    @Override
                    public void onSuccess(Boolean b) {
                        if(b!=null && b.booleanValue()) {
                            cf.saveConfig();
                        } else {
                        	if(cf.isNbRowsChanged())
                            	cf.refresh();                            
                            popupPanel.hide();
                        }
                    }
                };
                MessageBox.confirm(MessageBox.TYPE.CONFIRM_YN, NakedConstants.constants.confirm(), NakedConstants.constants.questionSaveConfigChanges(), callback);
            } else if(cf.isNbRowsChanged()) {
            	cf.refresh();
            }
        }

    }

    @Override
    public void showPopupPanel(final Widget widget, final Widget sourceWidget) {
    	initPopup();
        final PositionCallback pc;
        if(widget instanceof TextBoxCell) {
            pc = new PositionCallback() {

                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    popupPanel.setPopupPosition(sourceWidget.getAbsoluteLeft(), sourceWidget.getAbsoluteTop());
                    widget.setWidth(offsetWidth+"px");
                }
            };
            popupPanel.getMediator().setSavedStateIdx(WrappedObjectsRepository.getInstance().saveState());            
            popupPanel.setPopupTitle(NakedConstants.constants.edit());
            if(sourceWidget instanceof LabelCell) {
            	popupPanel.addCloseHandler(new CloseHandler<ICloseablePopupPanel>() {

        			@Override
					public void onClose(CloseEvent<ICloseablePopupPanel> event) {
        				((LabelCell) sourceWidget).redraw();
        			}
            	});
            }
            
        } else if (widget instanceof WrappedTableConfigForm) {
            pc = new PositionCallback() {

                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    popupPanel.setPopupPosition(sourceWidget.getAbsoluteLeft(), sourceWidget.getAbsoluteTop());
                }
            };
            popupPanel.setPopupTitle(NakedConstants.constants.screenConfiguration());
        } else {
            pc = new PositionCallback() {

                @Override
                public void setPosition(final int offsetWidth, final int offsetHeight) {
					popupPanel.setPopupPosition(Math.max(Window.getClientWidth() - offsetWidth, 0) / 2, Math.max(Window.getClientHeight() - offsetHeight, 0) / 2);
                }
            };
        }
        if(widget instanceof IWrappedTable) {
            IWrappedTable t = (IWrappedTable)widget;
            if(!t.isEditable() || !t.isPersistent())
                popupPanel.setForceClose(true);
            popupPanel.setPopupTitle(t.getFormTitle());
        } else if(widget instanceof TablePanel) {      
        	final TablePanel tp = (TablePanel) widget;
        	tp.getMediator().setPopupContainer(popupPanel);
        	String t = null;
        	WrappedFlexTable table = tp.getTable();
			if(tp.getType().equals(TablePanel.TYPE.SELECT) || tp.getType().equals(TablePanel.TYPE.MULTIPLE_SELECT)) {
				t = WrapperContext.getSelectElementLabel(table.getClassName(), table.getViewName());				
			}
			if(t != null)
				popupPanel.setPopupTitle(t);
			else if(tp.getTableTitle() != null)
        		popupPanel.setPopupTitle(tp.getTableTitle());
        	if(sourceWidget instanceof WrappedCollectionCell && table.isEditableFormOnDoubleClick()) {
        		popupPanel.addCloseHandler(new CloseHandler<ICloseablePopupPanel>() {

        			@Override
					public void onClose(CloseEvent<ICloseablePopupPanel> event) {
						if(tp.getTable().getItems() != null
			        			&& tp.getTable().getItems().size() == 1) {
							IWrapper elt = tp.getTable().getItems().get(0);
							if(elt != null)
								((WrappedCollectionCell)sourceWidget).getLabel().setText(elt.toString());
							else
								((WrappedCollectionCell)sourceWidget).getLabel().setText(NakedConstants.messages.nbItems(1));
						}
					}
				});
        	}
        } else if(widget instanceof AbstractObjectPanel) {
        	 popupPanel.setPopupTitle((((AbstractObjectPanel)widget).getObjectForm().getFormTitle()));
        	 
        }
        popupPanel.add(widget);
		popupPanel.setPopupPositionAndShow(pc);
    }

	@Override
	public ICloseablePopupPanel newPopupPanel() {
		initPopup();
        return popupPanel;
	}

	public void initPopup() {
		if(popupPanel == null)
            initPopupPanel();
        popupPanel.setForceClose(false);
        popupPanel.setPopupTitle(null);
        popupPanel.clearMainPanel();
	}

    public void addValidateButton(final IObjectPanel ivp, final IWrappedCollectionCell cell, boolean insert) {
    	final ObjectPanel vp = (ObjectPanel)ivp;
        Button b = new Button(NakedConstants.constants.validate());
        b.setStylePrimaryName("naked-button");
        vp.add(b);
        vp.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_CENTER);
        final INakedObject no = vp.getObjectForm().getWrapper().getContent();
		WrappedObjectsRepository.getInstance().getPendingValidations().add(no);
        b.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                final IWrappedObjectForm form = vp.getObjectForm();
                AsyncCallback<WrapperValidationResults> callback = new AsyncCallback<WrapperValidationResults>() {

                    @Override
                    public void onFailure(Throwable caught) {}

                    @Override
                    public void onSuccess(WrapperValidationResults result) {
                        if(result == null) {
                            validate(cell, form);
                        } else if(result.isValidationDone()) {
                            if(result.hasErrors()) {
                                ValidationHelper.processValidationErrors(form, result.getValidationErrorsByAttribute(), result.getMessage());
                            } else {
                                validate(cell, form);
                            }
                        }
                    }

                    private void validate(final IWrappedCollectionCell cell,
                            final IWrappedObjectForm form) {
                        popupPanel.setForceClose(true);
                        popupPanel.hide();
                        IWrapper parentWrapper = cell.getParentWrapper();
                        String attribute = cell.getAttribute();
                        WrappedObjectsRepository wor  = WrappedObjectsRepository.getInstance();
                        wor.setPauseModifListener(true);
                        parentWrapper.addObjectToCollection(attribute, no);
                        if(WrapperContext.getFieldInfoByName().get(parentWrapper.getWrappedClassName()).get(attribute).isManyToMany())
                        	wor.addManyToMany(parentWrapper.getWrappedClassName(), PersistenceManager.getId(parentWrapper), attribute, PersistenceManager.getId(form.getWrapper()));
                        parentWrapper.setLazyTime(attribute, System.currentTimeMillis());
                        wor.addWrapperToRefresh(parentWrapper);
                        wor.setPauseModifListener(false);

                        IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
                        if(updateHandler != null) {
                            try {
                                updateHandler.afterSet(no, parentWrapper.getContent(), form, attribute);
                            } catch (UpdateException e) {
                                e.printStackTrace();
                            }
                        }
                        wor.getPendingValidations().remove(no);
                        cell.redraw();
                    }
                };
                ValidationHelper.validateForm(form, callback);
            }
        });
    }

    public void addValidateButton(final IObjectPanel ivp, final IWrappedObjectCell cell, final boolean insert) {
    	final ObjectPanel vp = (ObjectPanel)ivp;
        Button b = new Button(NakedConstants.constants.validate());
        b.setStylePrimaryName("naked-button");
        vp.add(b);
        vp.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_CENTER);
        final INakedObject no = vp.getObjectForm().getWrapper().getContent();
        final IWrappedObjectForm form = vp.getObjectForm();
        final IWrapper w = form.getWrapper();
		WrappedObjectsRepository.getInstance().getPendingValidations().add(no);
        b.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                AsyncCallback<WrapperValidationResults> callback = new AsyncCallback<WrapperValidationResults>() {

                    @Override
                    public void onFailure(Throwable caught) {}

                    @Override
                    public void onSuccess(WrapperValidationResults result) {
                        if(result == null) {
                            validate(vp, cell, insert, form);
                        } else if(result.isValidationDone()) {
                            if(result.hasErrors()) {
                                ValidationHelper.processValidationErrors(form, result.getValidationErrorsByAttribute(), result.getMessage());
                            } else {
                                validate(vp, cell, insert, form);
                            }
                        }
                    }

                    private void validate(final ObjectPanel vp,
                            final IWrappedObjectCell cell, final boolean insert,
                            final IWrappedObjectForm form) {
                        boolean error = false;
                        if(cell.isPersistent()) {
                            int idx = vp.getStateIdx();
                            WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
                            IWrapper parentWrapper = cell.getParentWrapper();
                            IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
                            if(updateHandler != null) {
                                try {
                                    updateHandler.afterSet(w.getContent(), parentWrapper.getContent(), getForm(cell, form), cell.getAttribute());
                                } catch (UpdateException e) {
                                    ViewHelper.showException(e);
                                    wor.restoreSavedState(idx);
                                    error = true;
                                }
                            }
                            if(!error) {
                                wor.checkNewID(vp.getOriginalId(), w);
                                Id id = PersistenceManager.getId(w);
                                if(id != null) {
                                    wor.clearSavedStatesIfNeeded(idx);
                                }
                                if(insert) {
                                    cell.getParentWrapper().setNakedObjectAttribute(cell.getAttribute(), w.getContent());
                                }
                                cell.redraw();
                                wor.addWrapperToRefresh(parentWrapper);
                                parentWrapper.setLazyTime(cell.getAttribute(), System.currentTimeMillis());
                                WrappedObjectsRepository.getInstance().getPendingValidations().remove(w.getContent());
                            }

                        }
                        if(!error) {
                            popupPanel.setForceClose(true);
                            popupPanel.hide();
                        }
                    }

					private IWrappedObjectForm getForm(IWrappedObjectCell cell,
							IWrappedObjectForm form) {
						if(cell != null && cell.getMediator() != null && cell.getMediator().getPopupPanel() != null) {
							Widget w = ((Widget)cell).getParent();
							if(w != null) {
								w = w.getParent();
								if(w != null && w instanceof IWrappedObjectForm) {
									return (IWrappedObjectForm) w;
								}								
							}
						}
						return form;
					}
                };
                ValidationHelper.validateForm(form, callback);
            }

        });


    }

    @Override
    public void unRegisterAll(boolean close) {
    	if(registered) {
	        removeSelectHandler();
	        for(HandlerRegistration hr : handlerRegistry) {
	            hr.removeHandler();
	        }
	        handlerRegistry.clear();
	        if(close) {
	        	removeHideHandler();
	        }
	        registered = false;
	        
    	}
    }

    protected void removeHideHandler() {    	
    }

	@Override
    public void addNoDataFoundHideLoadingPopupHandler() {
        if(popupPanel == null)
            initPopupPanel();
        popupPanel.getMediator().addNoDataFoundHideLoadingPopupHandler();
    }

    @Override
    public void hidePopup(boolean force) {
        if(popupPanel != null) {
            if(force)
                popupPanel.setForceClose(force);
            popupPanel.hide();
        }
    }

    @Override
    public void onItemAdded(WrapperObjectAddedEvent event) {
        INakedObject no = (INakedObject) event.getSource();
        Set<IWrappedCollectionCell> set = ccCellsByParentWrapperMap.get(no);
        if(set != null && !set.isEmpty()) {
        	for(IWrappedCollectionCell cell : set) {
	        	cell.redraw();
        	}
        }
       
    }

	@Override
	public ICloseablePopupPanel getPopupPanel() {
		return popupPanel;
	}



}
