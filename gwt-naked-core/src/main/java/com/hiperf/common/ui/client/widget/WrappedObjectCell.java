package com.hiperf.common.ui.client.widget;

import java.util.Map;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.hiperf.common.ui.client.IClassInfo;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrappedObjectCell;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.event.UpdateResult;
import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.model.PersistentWrapperListModel;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.util.LazyManager;
import com.hiperf.common.ui.client.widget.LinkedCellPanel.LinkedMenuCellPanel;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.Id;

public class WrappedObjectCell extends AbstractLinkedCell implements IWrappedObjectCell {

	public interface Images extends ClientBundle {
		ImageResource select();
	}

	private String linkedObjectClass;
	protected Label label;
	public static final Images myImages = GWT.create(Images.class);

	public WrappedObjectCell() {
		super();
	}


	public WrappedObjectCell(final IWrapper parentWrapper, final String attribute,
			boolean persistent, boolean editable, IPanelMediator mediator) {
		super(parentWrapper, attribute, persistent, editable, mediator);
		mediator.addWrapperUpdatedHandler(this);
	}


	@Override
	protected void initLinkedObjectClass(final IWrapper parentWrapper,
			final String attribute) {
		this.linkedObjectClass = WrapperContext.getFieldInfoByName().get(parentWrapper.getWrappedClassName()).get(attribute).getTypeName();
	}


	public void redraw() {
		final IWrapper wrapper = parentWrapper.getWrappedAttribute(attribute);
		INakedObject content = wrapper.getContent();
		if(content != null) {
			if(LazyManager.isLazy(wrapper)) {
				Id id = PersistenceManager.getId(parentWrapper);
				if(id!=null) {
					getLinkedObject(wrapper, id, MAX_RELOAD_TRIES, false, editable);
				}
			} else  {
				displayLinkedElement(wrapper, content);
			}
		} else {
			if(cellPanel != null && cellPanel.getMenuPanel() != null)
				cellPanel.getMenuPanel().setVisible(false);
			label.setVisible(true);
			label.setText(emptyText());
		}
	}


	public void displayLinkedElement(final IWrapper wrapper,
			INakedObject content) throws AttributeNotFoundException {
		String att = WrapperContext.getFieldInfoByName().get(parentWrapper.getWrappedClassName()).get(attribute).getToStringAttribute();
		Id id = PersistenceManager.getId(wrapper);
		WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		Map<Id, IWrapper> map = wor.getUpdatedWrappersByClassName().get(getLinkedObjectClass());
		if(map != null) {
			IWrapper w = map.get(id);
			if(w != null && w.getContent() != wrapper.getContent()) {
				content = w.getContent();
				parentWrapper.setNakedObjectAttribute(attribute, content, false);
			}
		}
		String s;
		if(att != null && att.length() > 0) {
			s = wrapper.getAttribute(att);
		} else {
			s = content.toString();
			if(s == null) {
				id = PersistenceManager.getId(content);
				if(id != null) {
					if(id.isLocal())
						s = NakedConstants.constants.validationWaiting();
					else
						s = id.toString();

				}
			}
		}
		if(s != null && s.trim().length() > 0)
			label.setText(s);
		else
			label.setText(NakedConstants.constants.emptyText());
	}

	private Timer retryTimer = null;

	private Timer getRetryTimer(boolean create, final boolean showDetail, final boolean isEditable) {
		if(create && retryTimer == null)
			retryTimer = new Timer() {

				private int nbFireTimes = MAX_RELOAD_TRIES;
				private boolean show = showDetail;
				private boolean edit = isEditable;

				@Override
				public void cancel() {
					super.cancel();
					nbFireTimes = MAX_RELOAD_TRIES;
				}

				@Override
				public void run() {
					getLinkedObject(parentWrapper.getWrappedAttribute(attribute), PersistenceManager.getId(parentWrapper), nbFireTimes--, show, edit);
			}};
		return retryTimer;
	}

	private void getLinkedObject(final IWrapper wrapper, final Id id, final int nbTimes, final boolean showDetail, final boolean isEditable) {
		if(!reloading && !id.isLocal()) {
			reloading = true;
			final Image i = displayLoadingImage();
			PersistenceServiceAsync service = PersistenceService.Util.getInstance();
			AsyncCallback<INakedObject> callback = new AsyncCallback<INakedObject>() {

				public void onFailure(Throwable caught) {
					hideLoadingImage(i);
					Timer t = getRetryTimer(true, showDetail, isEditable);
					if(nbTimes > 0) {
						t.schedule(3000);
					}
					else {
						t.cancel();
						ViewHelper.displayError(caught, NakedConstants.constants.exceptionDataDB());
					}
					reloading = false;
				}

				public void onSuccess(INakedObject result) {
					Timer t = getRetryTimer(false, showDetail, isEditable);
					if(t != null)
						t.cancel();
					if (result != null) {
						INakedObject oldContent = wrapper.getContent();
						wrapper.setContent(result);
						wrapper.setLazy(false);
						mediator.replaceWrapperUpdatedHandler(oldContent, WrappedObjectCell.this);
						parentWrapper.setNakedObjectAttribute(attribute, result, false);
						parentWrapper.removeLazyAttribute(attribute);
						hideLoadingImage(i);
						if(showDetail) {
							showDetail(editable);
						}
					} else {
						hideLoadingImage(i);
						ViewHelper.displayError(NakedConstants.constants.exceptionDataDB());
					}
					reloading = false;
				}
			};
			service.getLinkedObject(parentWrapper.getWrappedClassName(), id, attribute, callback);
		}
	}


	public void showDetail(boolean editable) {
		final IWrapper wrapper = parentWrapper.getWrappedAttribute(attribute);
		if(LazyManager.isLazy(wrapper)) {
			Id id = PersistenceManager.getId(parentWrapper);
			if(id!=null) {
				getLinkedObject(wrapper, id, MAX_RELOAD_TRIES, true, editable);
			}
		} else {
			ICustomForm f = WrapperContext.getNewForm(wrapper.getWrappedClassName());
			boolean isEdit = WrapperContext.getFieldInfoByName().get(parentWrapper.getWrappedClassName()).get(attribute).isEditable() && editable;
			if(f != null) {
				if(f instanceof WrappedObjectForm) {
					WrappedObjectForm wof = (WrappedObjectForm) f.newInstance(wrapper, getParentTable(), isEdit);
					wof.setParentViewName(getViewName());
					addFormToPanel(wrapper, isEdit);
				} else
					f.newInstance(wrapper, getParentTable(), isEdit).show();
			}
			else {
				addFormToPanel(wrapper, isEdit);
			}
							
		}
	}


	protected void addFormToPanel(final IWrapper wrapper, boolean isEdit) {
		if(isEdit) {
			IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(linkedObjectClass).getUpdateHandler();
			String title = null;
			if(updateHandler != null) {
				if(updateHandler != null)
					title = updateHandler.beforeEdit(wrapper.getContent());
			}
			WrappedObjectForm form = new WrappedObjectForm(wrapper, true, persistent, getViewName());
			ObjectPanel vp = new ObjectPanel(form,  WrappedObjectsRepository.getInstance().saveState(), false);
			if(title == null) {
				title = WrapperContext.getTitle(getViewName(), this);
				if(title == null)
					title = WrapperContext.getEditElementLabel(wrapper.getWrappedClassName(), getViewName());
			}
			
			form.setFormTitle(title);
			mediator.showPopupPanel(vp, this);
			mediator.addValidateButton(vp, this, false);
		} else {
			boolean ok = true;
			IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
			if(updateHandler != null) {
				try {
					updateHandler.beforeView(parentWrapper.getWrappedClassName(), attribute, parentWrapper.getContent());
				} catch (UpdateException e) {
					ok = false;
					ViewHelper.showException(e);
				}
			}
			if(ok)
			{
				WrappedObjectForm form = new WrappedObjectForm(wrapper, false, persistent, getViewName());
				ObjectPanel vp = new ObjectPanel(form);					
				String title = WrapperContext.getTitle(getViewName(), this);
				if(title == null)
					title = WrapperContext.getViewElementLabel(wrapper.getWrappedClassName(), getViewName());						
				form.setFormTitle(title);
				mediator.showPopupPanel(vp, this);
			}
		}
	}


	public void updateAndNotifyWrappedObject(INakedObject content) {
		parentWrapper.setNakedObjectAttribute(attribute, content);
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
					IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(className).getUpdateHandler();
					if(updateHandler != null) {
						try {
							updateHandler.beforeSet(newNakedObject, newNakedObject, getViewName(),  attribute, new AsyncCallback<UpdateResult>() {

								@Override
								public void onFailure(Throwable caught) {
									ViewHelper.showException(caught);
								}

								@Override
								public void onSuccess(UpdateResult result) {
									if(result == null || result.getMessage() == null)
										displayNewForm(className, newWrapper, newNakedObject);
									else
										MessageBox.alert(result.getMessage());
								}
							});
						} catch (UpdateException e) {
							ViewHelper.showException(e);
							return;
						}
					} else
						displayNewForm(className, newWrapper, newNakedObject);
				}

				private void displayNewForm(final String className,
						final IWrapper newWrapper,
						final INakedObject newNakedObject) {
					IUpdateHandler updateHandler;
					updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
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
										displayNewObjectForm(className, newWrapper, newNakedObject);
									else
										MessageBox.alert(result.getMessage());
								}
							});
						} catch (UpdateException e) {
							ViewHelper.showException(e);
						}
					} else
						displayNewObjectForm(className, newWrapper, newNakedObject);
				}

				private void displayNewObjectForm(String className,
						IWrapper newWrapper, INakedObject newNakedObject) {
					ICustomForm f = WrapperContext.getNewForm(className);
					WrappedObjectsRepository wor  = WrappedObjectsRepository.getInstance();
					int stateIdx = wor.saveState();
					wor.addInsertedObject(newNakedObject);
					if(f != null) {
						if(f instanceof WrappedObjectForm) {
							WrappedObjectForm wof = (WrappedObjectForm) f;
							wof.setParentViewName(getViewName());
							addFormToPanel(className, stateIdx, wof);
						} else
							f.newInstance(newWrapper, getParentTable(), true).show();
					}
					else {
						WrappedObjectForm form = new WrappedObjectForm(newWrapper, true, persistent, getViewName());
						addFormToPanel(className, stateIdx, form);
					}
					
				}
			});
			i.setTitle(NakedConstants.constants.addNewValue());
			menuPanel.setAdd(true);

		}
	}


	protected void initSelectMenu(LinkedMenuCellPanel menuPanel) {
		if(!menuPanel.isSelect()) {
			selectMenuIdx = addMenuIdx + 1;
			Image i = new Image(myImages.select());
			i.setStyleName("little-button");
			menuPanel.getMenu().setWidget(0, getSelectMenuIdx(), i);
			i.setTitle(getSelectLabel());
			menuPanel.setSelect(true);
			i.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
					if(updateHandler != null) {
						try {
							updateHandler.beforeSelect(getLinkedObjectClass(), parentWrapper.getContent(), attribute, new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable e) {
									ViewHelper.showException(e);
									resetLabel();
								}

								@Override
								public void onSuccess(String filter) {
									doSelectAction(filter);
								}
							});
						} catch (UpdateException e) {
							ViewHelper.showException(e);
						}
					} else {
						doSelectAction(null);
					}

				}


			});
		}

	}




	protected void doSelectAction(String filter) {
		cellPanel.getMenuPanel().setVisible(false);
		label.setVisible(true);

		PersistentWrapperListModel list = new PersistentWrapperListModel(getLinkedObjectClass());

		Object obj = parentWrapper.getNakedAttribute(attribute);
		if(obj != null) {
			StringBuilder sb = new StringBuilder();
			if(filter != null && filter.length() > 0)
				sb.append(" and (");
			else
				sb.append("(");
			INakedObject no = (INakedObject)obj;
			Id id = PersistenceManager.getId(no);
			if(!PersistenceManager.isLocal(id)) {
				for(int i=0; i<id.getFieldNames().size(); i++) {
					sb.append("o.").append(id.getFieldNames().get(i)).append(" != ").append(id.getFieldValues().get(i));
					if(i < id.getFieldNames().size() - 1)
						sb.append(" and ");
					else
						sb.append(" )");
				}
			}
			if(filter != null && filter.length() > 0)
				filter = filter + sb.toString();
			else
				filter = sb.toString();
		}
		if(filter != null && filter.length() > 0) {
			list.setCurrentFilter(filter);
			list.setConstantFilter(filter);
		}
		list.setKeepInsertedObjects(true);
		mediator.addNoDataFoundHideLoadingPopupHandler();
		ViewHelper.displaySelectTableForUser(WrapperContext.getSelectElementLabel(getLinkedObjectClass(), getViewName()),
				list,WrappedObjectCell.this, getViewName());
	}

	protected String getEditLabel() {
		return NakedConstants.constants.editValue();
	}

	@Override
	public String getLinkedObjectClass() {
		return linkedObjectClass;
	}

	@Override
	public boolean isNull() {
		IWrapper w = parentWrapper.getWrappedAttribute(attribute);
		return w == null || w.getContent()==null;
	}

	@Override
	public String emptyText() {
		return NakedConstants.constants.emptyText();
	}

	@Override
	protected void doDeleteAction() {
		updateAndNotifyWrappedObject(null);
		label.setText(emptyText());
	}

	@Override
	protected void setDeleteImageTitle(Image i) {
		i.setTitle(NakedConstants.constants.deleteCurrentValue());
	}


	@Override
	public void initLabel() {
		label = new Label(emptyText());
		label.setStylePrimaryName("underline");
	}


	@Override
	public Label getLabel() {
		return label;
	}


	@Override
	public IWrapper getWrapper()  {
		return parentWrapper.getWrappedAttribute(attribute);
	}


	@Override
	protected void initEditMenu(LinkedMenuCellPanel menuPanel) {
		int editMenuIndex = menuPanel.isSelect() ? getViewEditMenuIdx() + 1 : getViewEditMenuIdx();
		if(!isNull()) {
			if(!menuPanel.isEdit()) {
				Image i;
				/*final boolean linkedClassEditable = WrapperContext.getClassInfoByName().get(getLinkedObjectClass()).isEditable();
				if(editable) {*/
					i = new Image(getEditImage());
					//i.setTitle(linkedClassEditable ? getEditLabel() : getViewLabel());
					i.setTitle(getEditLabel());
				/*}
				else {
					i = new Image(AbstractLinkedCell.images.magnifier());
					i.setTitle(getViewLabel());
				}*/
				i.setStyleName("little-button");
				menuPanel.getMenu().setWidget(0, editMenuIndex, i);
				i.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						cellPanel.getMenuPanel().setVisible(false);
						getLabel().setVisible(true);
						showDetail(true);
					}
				});
				menuPanel.setEdit(true);
			} else {
				/*if(menuPanel.isAdd() && menuPanel.isSelect() && menuPanel.isDelete())
					menuPanel.getMenu().getWidget(0, 3).setVisible(true);
				else if(menuPanel.isSelect() && menuPanel.isDelete())
					menuPanel.getMenu().getWidget(0, 2).setVisible(true);
				else if(menuPanel.isAdd() && menuPanel.isEdit())
					menuPanel.getMenu().getWidget(0, 0).setVisible(true);
				else
					menuPanel.getMenu().getWidget(0, 0).setVisible(true);*/
				menuPanel.getMenu().getWidget(0, editMenuIndex).setVisible(true);
			}
		} else if(menuPanel.isEdit()) {
			menuPanel.getMenu().getWidget(0, editMenuIndex).setVisible(false);
			/*if(menuPanel.isAdd() && menuPanel.isSelect() && menuPanel.isDelete())
				menuPanel.getMenu().getWidget(0, 3).setVisible(false);
			else if(menuPanel.isSelect() || menuPanel.isDelete())
				menuPanel.getMenu().getWidget(0, 2).setVisible(false);
			else
				menuPanel.getMenu().getWidget(0, 0).setVisible(false);*/
		}
	}


	@Override
	protected boolean isEditable(IWrapper wrapper, IClassInfo ci) {
		return ViewHelper.isEditable(getWrapper(), ci, parentWrapper, attribute, getViewName());
	}

	protected String getSelectLabel() {
		return NakedConstants.constants.selectExistingValue();
	}
	
	public void reinitAddMenu() {
		initAddMenu(cellPanel.getMenuPanel());
	}
	
	public void reinitSelectMenu() {
		initSelectMenu(cellPanel.getMenuPanel());
	}


	public void clearMenuPanel() {
		cellPanel.getMenuPanel().clear();
	}


	protected void addFormToPanel(String className, int stateIdx,
			WrappedObjectForm form) {
		form.setFormTitle(WrapperContext.getNewFormLabel(className, getViewName()));
		ObjectPanel panel = new ObjectPanel(form, stateIdx, false);
		mediator.showPopupPanel(panel, WrappedObjectCell.this);
		mediator.addValidateButton(panel, WrappedObjectCell.this, true);
	}
	

}
