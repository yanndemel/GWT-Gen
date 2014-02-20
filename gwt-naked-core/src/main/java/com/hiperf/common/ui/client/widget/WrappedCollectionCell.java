package com.hiperf.common.ui.client.widget;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.ILazy;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrappedCollectionCell;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.event.UpdateResult;
import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.exception.UniqueItemException;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.model.LazyListModel;
import com.hiperf.common.ui.client.model.PersistentWrapperListModel;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.widget.LinkedCellPanel.LinkedMenuCellPanel;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.CollectionInfo;
import com.hiperf.common.ui.shared.util.Id;

public class WrappedCollectionCell extends AbstractLinkedCell implements IWrappedCollectionCell {

	public interface Images extends ClientBundle {
		ImageResource selectAndAdd();
	}

	public static final Images myImages = GWT.create(Images.class);
	protected String wrappedClassName;
	protected Text label;

	public WrappedCollectionCell() {
		super();
	}

	public WrappedCollectionCell(final IWrapper parentWrapper, final String attribute,
			boolean persistent, boolean editable, IPanelMediator mediator) {
		super(parentWrapper, attribute, persistent, editable, mediator);
		mediator.addWrapperObjectAddedHandler(this);
	}


	@Override
	protected void initLinkedObjectClass(final IWrapper parentWrapper,
			final String attribute) {
		this.wrappedClassName = parentWrapper.getWrappedCollectionClassName(attribute);
	}

	@Override
	public void redraw(String txt) {
		IWrapperListModel wrappedCollection = parentWrapper.getWrappedCollection(attribute, isPersistent());
		if(wrappedCollection != null) {
			try {
				label.setTotal(wrappedCollection.getServerCount());
			} catch (UniqueItemException e) {
				label.setText(txt);
			}
		}
	}



	@Override
	public void redraw() {
		IWrapperListModel wrappedCollection = parentWrapper.getWrappedCollection(attribute, isPersistent());
		if(wrappedCollection != null) {
			if(wrappedCollection.isLazy()) {
				LazyListModel lazyColl = (LazyListModel)wrappedCollection;
				if(!lazyColl.isInit() && !label.isNoReload()) {
					reload(lazyColl, MAX_RELOAD_TRIES);
				} else
					doRedraw(wrappedCollection);
			} else
				doRedraw(wrappedCollection);
		}
	}

	public void doRedraw(IWrapperListModel wrappedCollection) {
		try {
			label.setTotal(wrappedCollection.getServerCount());
		} catch (UniqueItemException e) {
			label.setText(wrappedCollection.toString());
		}
	}

	public void displaySingleElement(IWrapperListModel wrappedCollection)
			throws AttributeNotFoundException {
		IFieldInfo fi = WrapperContext.getFieldInfoByName().get(parentWrapper.getWrappedClassName()).get(attribute);
		String att = fi.getToStringAttribute();
		IWrapper iWrapper = wrappedCollection.getItems().get(0);
		if(att != null)
			mediator.addWrapperUpdatedHandler(att, iWrapper, this);
		String linkedAtt = fi.getRedrawOnUpdateLinkedObject();
		if(linkedAtt != null)
			mediator.addSpecialWrapperUpdatedHandler((INakedObject)iWrapper.getNakedAttribute(linkedAtt), this);
		redrawSingleElement(att, iWrapper);
	}


	@Override
	public void redrawSingleElement(final String att, final IWrapper iWrapper)
			throws AttributeNotFoundException {
		if(att != null && att.length() > 0) {
			label.setText(iWrapper.getAttribute(att));
		} else
			label.setText(iWrapper.getContent().toString());
	}

	private void reload(final LazyListModel lazyColl, final int nbTimes) {
		if(!reloading) {
			reloading = true;
			final Image i = displayLoadingImage();
			Id id = PersistenceManager.getId(parentWrapper);
			if(id != null) {
				PersistenceServiceAsync service = PersistenceService.Util.getInstance();
				AsyncCallback<CollectionInfo> callback = new AsyncCallback<CollectionInfo>() {

					public void onFailure(Throwable caught) {
						hideLoadingImage(i);
						reloading = false;
					}

					public void onSuccess(CollectionInfo result) {
						lazyColl.setSize(result.getSize());
						lazyColl.setDescription(result.getDescription());
						ILazy collection = (ILazy)parentWrapper.getCollection(attribute);
						collection.setInitialized(true);
						collection.setOriginalSize(result.getSize());
						collection.setDescription(result.getDescription());
						hideLoadingImage(i);
						doRedraw(lazyColl);
						reloading = false;
					}
				};
				service.getLazyCollection(parentWrapper.getWrappedClassName(), id, attribute, callback);
			}
		}


	}

	public void showDetail(boolean editable) {
		IWrapperListModel wrappedCollection = parentWrapper.getWrappedCollection(attribute, persistent);
		if(wrappedCollection != null && !wrappedCollection.isEmpty()) {
			boolean ok = true;
			IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
			if(updateHandler != null) {
				try {
					updateHandler.beforeView(wrappedClassName, attribute, parentWrapper.getContent());
				} catch (UpdateException e) {
					ok = false;
					ViewHelper.showException(e);
				}
			}
			if(ok)
				ViewHelper.displayChildTableForUser(editable?NakedConstants.constants.doubleClickToEdit():null, wrappedCollection, editable, this, getViewName());
		}

	}


	@Override
	public String getLinkedObjectClass() {
		return wrappedClassName;
	}

	@Override
	protected void doDeleteAction() {
		cellPanel.getMenuPanel().setVisible(false);
		label.setVisible(true);
		displaySelectTableForDelete();
	}

	protected void displaySelectTableForDelete() {
		ViewHelper.displaySelectDeleteTableForUser(NakedConstants.constants.selectItemsToDelete(), parentWrapper.getWrappedCollection(attribute, persistent), this, getViewName());
	}


	protected void initAddMenu(LinkedMenuCellPanel menuPanel) {
		if(!menuPanel.isAdd()) {
			addMenuIdx = 0;
			Image i = new Image(AbstractLinkedCell.images.add_small());
			i.setStyleName("little-button");
			menuPanel.getMenu().setWidget(0, getAddMenuIdx(), i);
			i.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					cellPanel.getMenuPanel().setVisible(false);
					label.setVisible(true);
					final String className = getLinkedObjectClass();
					Map<String, IFieldInfo> map = WrapperContext.getFieldInfoByName().get(parentWrapper.getWrappedClassName());
					for(Entry<String, IFieldInfo> e : map.entrySet()) {
						String att = e.getKey();
						IFieldInfo fi = e.getValue();
						if(fi.isNotNull() && parentWrapper.getNakedAttribute(att) == null) {
							MessageBox.alert(NakedConstants.messages.notNullField(fi.getLabel()));
							return;
						}
					}
					final IFieldInfo fieldInfo = map.get(attribute);
					final WrappedObjectsRepository wor  = WrappedObjectsRepository.getInstance();
					final int idx = wor.saveState();
					final WrappedCollectionCell wcc = WrappedCollectionCell.this;
					wcc.setStateIdx(idx);

					final IWrapper newWrapper = WrapperContext.getEmptyWrappersMap().get(className).newWrapper();
					final INakedObject newNakedObject = newWrapper.newNakedObject();
					newWrapper.setContent(newNakedObject);
					Map<String, IFieldInfo> idInfos = PersistenceManager.getIdInfos(className);
					if(idInfos.size() == 1) {
						String att = idInfos.keySet().iterator().next();
						IFieldInfo fi = idInfos.get(att);
						if(fi.isGeneratedId()) {
							switch(fi.getDataType()) {
							case LONG:
								try {
									newWrapper.setAttribute(att, PersistenceManager.nextLongId().toString());
								} catch (Exception e) {
									e.printStackTrace();
									MessageBox.alert(NakedConstants.constants.errorNewRecord()+e.getMessage());
								}
								break;
							case STRING:
								try {
									newWrapper.setAttribute(att, PersistenceManager.nextStringId());
								} catch (Exception e) {
									e.printStackTrace();
									MessageBox.alert(NakedConstants.constants.errorNewRecord()+e.getMessage());
								}
								break;
							default:
								MessageBox.alert(NakedConstants.constants.errorIdType());
								break;
							}
						}
					}
					String mappedByTmp = null;
					if(fieldInfo.isOneToMany()) {
						mappedByTmp = fieldInfo.getMappedBy();
						newWrapper.setNakedObjectAttribute(mappedByTmp, parentWrapper.getContent());
					} else if(fieldInfo.isManyToMany()) {
						if(fieldInfo.getMappedBy() == null) {
							Map<String, IFieldInfo> map2 = WrapperContext.getFieldInfoByName().get(className);
							for(Entry<String, IFieldInfo> e : map2.entrySet()) {
								IFieldInfo value = e.getValue();
								if(value.isManyToMany() && value.getTypeName().equals(parentWrapper.getWrappedClassName())
										&& attribute.equals(value.getMappedBy())) {
									newWrapper.addObjectToCollection(e.getKey(), parentWrapper.getContent());
									mappedByTmp = e.getKey();
									break;
								}
							}
						} else
							mappedByTmp = fieldInfo.getMappedBy();
					}
					if(mappedByTmp != null) {
						final String mappedBy = mappedByTmp;
						IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(className).getUpdateHandler();
						if(updateHandler != null) {
							try {
								updateHandler.beforeSet(newWrapper.getContent(), newWrapper.getContent(), getViewName(), attribute, new AsyncCallback<UpdateResult>() {

									@Override
									public void onFailure(Throwable caught) {
										ViewHelper.showException(caught);
									}

									@Override
									public void onSuccess(UpdateResult result) {
										if(result == null || result.getMessage() == null)
											displayNewForm(className, fieldInfo, mappedBy, wor, idx, wcc,
													newWrapper, newNakedObject);
										else
											MessageBox.alert(result.getMessage());
									}
								});
							} catch (UpdateException e) {
								ViewHelper.showException(e);
								return;
							}
						} else
							displayNewForm(className, fieldInfo, mappedBy, wor, idx, wcc,
									newWrapper, newNakedObject);
					}


				}

				private void displayNewForm(final String className,
						final IFieldInfo fieldInfo,
						final String mappedBy, final WrappedObjectsRepository wor, final int idx,
						final WrappedCollectionCell wcc,
						final IWrapper newWrapper,
						final INakedObject newNakedObject) {
					IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
					if(updateHandler != null) {
						try {
							updateHandler.beforeSet(newNakedObject, parentWrapper.getContent(), getViewName(), attribute, new AsyncCallback<UpdateResult>() {

								@Override
								public void onFailure(Throwable caught) {
									ViewHelper.showException(caught);
								}

								@Override
								public void onSuccess(UpdateResult result) {
									if(result == null || result.getMessage() == null)
										displayNewObjectForm(className, fieldInfo, mappedBy, wor, idx,
												wcc, newWrapper, newNakedObject);
									else
										MessageBox.alert(result.getMessage());
								}
							});
						} catch (UpdateException e) {
							ViewHelper.showException(e);
						}
					} else {
						displayNewObjectForm(className, fieldInfo, mappedBy, wor, idx,
								wcc, newWrapper, newNakedObject);
					}
				}

			});
			i.setTitle(NakedConstants.constants.addNewValue());
			menuPanel.setAdd(true);
		}

	}

	protected void displayNewObjectForm(String className,
			IFieldInfo fieldInfo,
			String mappedBy, final WrappedObjectsRepository wor, final int idx,
			final WrappedCollectionCell wcc, IWrapper newWrapper,
			INakedObject newNakedObject) {
		ICustomForm f = WrapperContext.getNewForm(className);
		wor.addInsertedObject(newNakedObject);
		if(f != null) {
			if(f instanceof WrappedObjectForm) {
				WrappedObjectForm wof = (WrappedObjectForm) f.newInstance(newWrapper, getParentTable(), true);
				wof.setParentViewName(getViewName());
				addFormToPanel(className, mappedBy, idx, wcc, wof);
			} else
				f.newInstance(newWrapper, getParentTable(), true).show();
		}
		else {
			final WrappedObjectForm form = new WrappedObjectForm(newWrapper, true, persistent, getViewName());
			addFormToPanel(className, mappedBy, idx, wcc, form);
		}
			
	}

	protected void addFormToPanel(String className, String mappedBy,
			final int idx, final WrappedCollectionCell wcc,
			final WrappedObjectForm form) {
		form.setFormTitle(WrapperContext.getNewFormLabel(className, getViewName()));
		form.setFieldEditable(mappedBy, false);
		ObjectPanel vp = new ObjectPanel(form, idx, true);
		mediator.showPopupPanel(vp, wcc);
		mediator.addValidateButton(vp, wcc, true);
	}

	@Override
	public boolean isNull() {
		IWrapperListModel wrappedCollection = parentWrapper.getWrappedCollection(attribute, false);
		return wrappedCollection == null || wrappedCollection.isEmpty();
	}

	@Override
	public String emptyText() {
		return NakedConstants.constants.noItem();
	}

	@Override
	protected String getEditLabel() {
		return NakedConstants.messages.editCurrentValue(2);
	}

	@Override
	protected void setDeleteImageTitle(Image i) {
		i.setTitle(NakedConstants.constants.selectItemsToDelete());
	}



	@Override
	public void initLabel() {
		label = new Text(emptyText());
		label.setStylePrimaryName("underline");
	}

	@Override
	public Text getLabel() {
		return label;
	}


	@Override
	public IWrapper getWrapper()  {
		return null;
	}

	@Override
	protected void initSelectMenu(LinkedMenuCellPanel menuPanel) {
		if(menuPanel.isSelect()) {
			menuPanel.getMenu().getWidget(0, getSelectMenuIdx()).setVisible(true);
		} else if(!menuPanel.isSelect()) {
			selectMenuIdx = addMenuIdx + 1;
			Image i = new Image(myImages.selectAndAdd());
			i.setTitle(getSelectLabel());
			i.setStyleName("little-button");
			menuPanel.getMenu().setWidget(0, getSelectMenuIdx(), i);
			i.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
					if(updateHandler != null) {
						try {
							updateHandler.beforeSelect(getLinkedObjectClass(), parentWrapper.getContent(),  attribute, new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
									ViewHelper.showException(caught);
									resetLabel();
								}

								@Override
								public void onSuccess(String filter) {
									doSelectAction(filter);
								}
							});
						} catch (UpdateException e) {
							ViewHelper.showException(e);
							return;
						}
					} else
						doSelectAction(null);
				}
			});
			menuPanel.setSelect(true);
		}

	}

	protected void doSelectAction(String filter) {
		cellPanel.getMenuPanel().setVisible(false);
		getLabel().setVisible(true);
		final WrappedCollectionCell wcc = WrappedCollectionCell.this;
		final String className = getLinkedObjectClass();
		final PersistentWrapperListModel list = new PersistentWrapperListModel(className);
		Map<String, IFieldInfo> linkedMap = WrapperContext.getFieldInfoByName().get(className);
		String curFilter = "";
		Id parentId = PersistenceManager.getId(parentWrapper);
		Object id = parentId.getFieldValues().get(0);
		boolean local = PersistenceManager.isLocal(id);
		for(Entry<String, IFieldInfo> entry : linkedMap.entrySet()) {
			IFieldInfo ifi = entry.getValue();
			String att = entry.getKey();
			String typeName = ifi.getTypeName();
			if(ifi.isManyToMany() && typeName.equals(parentWrapper.getWrappedClassName())) {
				Set<String> idKeys = PersistenceManager.getIdInfos(typeName).keySet();
				String assocAttr = idKeys.iterator().next();
				if(!local) {
					curFilter = "left join o."+att+" assoc where o."+assocAttr+" != "+id+" and (assoc."+assocAttr+" is null or assoc."+assocAttr+" != "+id+") ";
				}
				list.setConstantFilter(filter);
				list.setQueryDistinctEntities(true);
				break;
			}
		}
		if(!local) {
			String linkedIdAttr = PersistenceManager.getIdInfos(getLinkedObjectClass()).keySet().iterator().next();
			if(curFilter.length() > 0)
				curFilter += " and ";
			curFilter += "o."+linkedIdAttr + " not in (select oo."+linkedIdAttr+" from "+parentWrapper.getWrappedClassName() +
					" par_o inner join par_o."+attribute+" oo where par_o."+parentId.getFieldNames().get(0) + " = "+id+") ";
			list.setCurrentFilter(curFilter);
		}
		mediator.addNoDataFoundHideLoadingPopupHandler();
		if(filter != null && filter.length() > 0) {
			String currentFilter = list.getCurrentFilter();
			if(currentFilter == null || currentFilter.length() == 0) {
				list.setCurrentFilter(filter);
			} else {
				list.setCurrentFilter(PersistenceManager.appendFilters(currentFilter, filter));
			}
		}
		displaySelectTable(wcc, list);
	}

	protected void displaySelectTable(final WrappedCollectionCell wcc,
			final PersistentWrapperListModel list) {
		ViewHelper.displaySelectTableForUser(NakedConstants.constants.selectElementTitle(), list, wcc,  parentWrapper.getWrappedCollection(attribute, persistent), getViewName());
	}

	protected String getSelectLabel() {
		return NakedConstants.constants.selectAddExistingValue();
	}

	
	
	

}
