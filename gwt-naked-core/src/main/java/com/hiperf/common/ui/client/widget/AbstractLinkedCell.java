package com.hiperf.common.ui.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.IClassInfo;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.ILinkedCell;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrappedTable;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.widget.LinkedCellPanel.LinkedMenuCellPanel;
import com.hiperf.common.ui.shared.WrapperContext;

public abstract class AbstractLinkedCell extends Composite implements ILinkedCell {

	protected static final int MAX_RELOAD_TRIES = 5;

	public interface Images extends ClientBundle {
		ImageResource add_small();
		ImageResource magnifier();
		ImageResource loadingAnimation();
	    ImageResource delete_small();
	    ImageResource cancelForm();
		ImageResource edit_small();
	}



	protected LinkedCellPanel cellPanel;
	protected IWrapper parentWrapper;
	protected String attribute;
	protected boolean persistent;
	protected boolean editable;
	protected boolean reloading;

	protected int stateIdx = -1;

	protected HandlerRegistration overHr;
	protected HandlerRegistration outHr;

	protected IPanelMediator mediator;

	protected int addMenuIdx = -1;
	protected int selectMenuIdx = -1;
	private boolean viewEditMenuPresent = false;
	public static final Images images = GWT.create(Images.class);

	public AbstractLinkedCell() {
		super();
	}

	protected AbstractLinkedCell(final IWrapper parentWrapper, final String attribute, boolean persistent, boolean editable, IPanelMediator mediator) {
		this();
		this.editable = editable;
		this.reloading = false;
		this.parentWrapper = parentWrapper;
		this.attribute = attribute;
		this.mediator = mediator;
		this.persistent = persistent;
		initLabel();
		this.cellPanel = new LinkedCellPanel();
		cellPanel.add(getLabel());
		initWidget(this.cellPanel);
		initMouseHandlers(persistent, editable);
		initLinkedObjectClass(parentWrapper, attribute);
		redraw();
		cellPanel.setStylePrimaryName("underline");
		cellPanel.setWidth("100%");
		cellPanel.setHeight("100%");
	}

	protected abstract void initLinkedObjectClass(IWrapper parent, String attr);

	protected void initMouseHandlers(boolean persistent, final boolean editable) {
		if(persistent) {
			overHr = cellPanel.addMouseOverHandler(new MouseOverHandler() {

				@Override
				public void onMouseOver(MouseOverEvent event) {
					event.stopPropagation();
					if(event.getSource() == cellPanel) {
						initMenuPanel(editable);
						getLabel().setVisible(false);
						cellPanel.getMenuPanel().setVisible(true);
					}
				}
			});
			outHr = cellPanel.addMouseOutHandler(new MouseOutHandler() {

				@Override
				public void onMouseOut(MouseOutEvent event) {
					event.stopPropagation();
					if(event.getSource() == cellPanel) {
						resetLabel();
					}
				}
			});
		}
	}

	protected int getViewEditMenuIdx() {
		viewEditMenuPresent  = true;
		if((addMenuIdx >= 0 && selectMenuIdx < 0) || (addMenuIdx < 0 && selectMenuIdx >= 0))
			return 1;
		if(addMenuIdx < 0 && selectMenuIdx < 0)
			return 0;
		if(addMenuIdx >= 0 && selectMenuIdx >= 0)
			return 2;
		return -1;
	}

	private int getDeleteMenuIdx() {
		if(viewEditMenuPresent) {
			return getViewEditMenuIdx() + 1;
		}
		if(selectMenuIdx >= 0)
			return 2;
		else if(addMenuIdx >= 0)
			return 1;
		return 0;
	}

	protected int getAddMenuIdx() {
		return addMenuIdx ;
	}

	protected int getSelectMenuIdx() {
		return selectMenuIdx ;
	}

	protected void initMenuPanel(boolean editable) {
		if(persistent) {
			IFieldInfo fi = WrapperContext.getFieldInfoByName().get(parentWrapper.getWrappedClassName()).get(attribute);
			LinkedMenuCellPanel menuPanel = cellPanel.initMenuPanel(getOffsetWidth(), getOffsetHeight());
			IClassInfo ci = WrapperContext.getClassInfoByName().get(getLinkedObjectClass());			
			boolean linkedObjectEditable = ci.isEditable();
			if(isCellEditable(editable, ci)) {
				if(linkedObjectEditable) {
					if(fi.canAddNew())
						initAddMenu(menuPanel);
					if(!fi.isOneToMany() && fi.canSelect()) {
						initSelectMenu(menuPanel);
					}
					if(fi.canEdit())
						initEditMenu(menuPanel);
					else
						initViewMenu(menuPanel);
					if(fi.canRemove())
						initDeleteMenu(menuPanel);
				}
				else {
					initSelectMenu(menuPanel);
					initViewMenu(menuPanel);
					if(fi.canRemove() && (fi.isManyToOne() || fi.isManyToMany()))
						initDeleteMenu(menuPanel);
				}
			} else {
				initViewMenu(menuPanel);
			}
			String text = getLabel().getText();
			if(text != null && text.length() > 0) {
				menuPanel.setWidth(getLabel().getOffsetWidth() + "px");
			}
		}
	}

	protected boolean isCellEditable(boolean editable, IClassInfo ci) {
		return editable && isEditable(getWrapper(), ci);
	}


	protected boolean isEditable(IWrapper wrapper, IClassInfo ci) {
		return true;
	}

	protected void doDelete() {
		final IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(parentWrapper.getWrappedClassName()).getUpdateHandler();
		if(updateHandler != null) {
			AsyncCallback<String> callback = new AsyncCallback<String>() {

				@Override
				public void onFailure(Throwable e) {
					ViewHelper.showException(e);
				}

				@Override
				public void onSuccess(String s) {
					if(s != null && s.length() > 0) {
						MessageBox.alert(s);
					} else {
						doDeleteAction();
						try {
							updateHandler.afterDelete(attribute, parentWrapper.getContent());
						} catch (UpdateException e) {
							ViewHelper.showException(e);
							return;
						}
					}
			}};
			try {
				updateHandler.beforeDelete(attribute, parentWrapper.getContent(), callback);
			} catch (UpdateException e) {
				ViewHelper.showException(e);
				return;
			}
		} else
			doDeleteAction();
	}

	protected abstract void doDeleteAction();

	protected abstract void initAddMenu(LinkedMenuCellPanel menuPanel);

	protected abstract void initSelectMenu(LinkedMenuCellPanel menuPanel);

	protected void initViewMenu(LinkedMenuCellPanel menuPanel) {
		if(!isNull()) {
			if(!menuPanel.isView()) {
				Image i = new Image(AbstractLinkedCell.images.magnifier());
				i.setTitle(getViewLabel());
				i.setStyleName("little-button");
				menuPanel.getMenu().setWidget(0, getViewEditMenuIdx(), i);
				i.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						cellPanel.getMenuPanel().setVisible(false);
						getLabel().setVisible(true);
						showDetail(false);
					}
				});
				menuPanel.setView(true);
			} else if(menuPanel.getMenu().getCellCount(0) > getViewEditMenuIdx()) {
				menuPanel.getMenu().getWidget(0, getViewEditMenuIdx()).setVisible(true);
			}
		} else if(menuPanel.isView() && menuPanel.getMenu().getCellCount(0) > getViewEditMenuIdx()) {
			menuPanel.getMenu().getWidget(0, getViewEditMenuIdx()).setVisible(true);
		}
	}


	protected void initEditMenu(LinkedMenuCellPanel menuPanel) {
		int editMenuIndex = menuPanel.isSelect() ? getViewEditMenuIdx() + 1 : getViewEditMenuIdx();
		if(!isNull()) {
			if(!menuPanel.isEdit()) {
				Image i = new Image(getEditImage());
				i.setTitle(getEditLabel());
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
				menuPanel.getMenu().getWidget(0, editMenuIndex).setVisible(true);
			}
		} else if(menuPanel.isEdit()) {
			menuPanel.getMenu().getWidget(0, editMenuIndex).setVisible(false);
		}
	}

	protected void initDeleteMenu(LinkedMenuCellPanel menuPanel) {
		int delMenuIdx = menuPanel.isSelect() && menuPanel.isEdit() ? getDeleteMenuIdx() + 1 : getDeleteMenuIdx();
		if(!isNull()) {
			if(!menuPanel.isDelete()) {
				Image i = new Image(AbstractLinkedCell.images.delete_small());
				i.setStyleName("little-button");
				menuPanel.getMenu().setWidget(0, delMenuIdx, i);
				i.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						doDelete();
					}
				});
				setDeleteImageTitle(i);
				menuPanel.setDelete(true);
			} else {
				menuPanel.getMenu().getWidget(0,delMenuIdx).setVisible(true);
			}
		} else if(menuPanel.isDelete()){
			menuPanel.getMenu().getWidget(0,delMenuIdx).setVisible(false);
		}

	}

	protected abstract void setDeleteImageTitle(Image i);

	public abstract void initLabel();
	public abstract String emptyText();

	public Image displayLoadingImage() {
		Image i = new Image(AbstractLinkedCell.images.loadingAnimation());
		i.setTitle(NakedConstants.constants.loadingData());
		Label l = getLabel();
		if(l != null)
			l.setVisible(false);
		if(cellPanel != null) {
			cellPanel.add(i);
			cellPanel.setCellHorizontalAlignment(i, HasHorizontalAlignment.ALIGN_CENTER);			
		}
		return i;
	}

	public void hideLoadingImage(final Image i) {
		i.setVisible(false);
		Label l = getLabel();
		if(l != null)
			l.setVisible(true);
		redraw();
	}

	public IWrapper getNotifiedWrapper() {
		IWrapper w = parentWrapper.getWrappedAttribute(attribute);
		return w==null?parentWrapper:w;
	}

	@Override
	public IWrapper getParentWrapper() {
		return this.parentWrapper;
	}

	@Override
	public abstract IWrapper getWrapper();

	@Override
	public String getAttribute() {
		return attribute;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public boolean isEditable() {
		return editable;
	}

	protected ImageResource getEditImage() {
		return images.edit_small();
	}

	protected abstract String getEditLabel();

	protected String getViewLabel() {
		return NakedConstants.messages.viewCurrentValue(1);
	}

	public WrappedFlexTable getWrappedFlexTable() {
		Widget o = getParent();
		while(o != null && !(o instanceof WrappedFlexTable)) {
			o = o.getParent();
		}
		return o instanceof WrappedFlexTable ? (WrappedFlexTable)o : null;
	}

	public IPanelMediator getMediator() {
		return mediator;
	}

	public LinkedCellPanel getCellPanel() {
		return cellPanel;
	}

	public HandlerRegistration getOverHr() {
		return overHr;
	}

	public HandlerRegistration getOutHr() {
		return outHr;
	}

	@Override
	public int getStateIdx() {
		return stateIdx;
	}

	public void setStateIdx(int stateIdx) {
		this.stateIdx = stateIdx;
	}

	public String getViewName() {
		IWrappedTable t = getParentTable();
		return t != null ? t.getViewName() : null;
	}

	protected IWrappedTable getParentTable() {
		Widget parent = getParent();
		if(parent != null) {
			if(parent instanceof IWrappedTable)
				return (IWrappedTable)parent;
			else {
				parent = parent.getParent();
				if(parent != null && parent instanceof IWrappedTable)
					return (IWrappedTable)parent;
			}
		}
		return null;
	}
	
	public void resetLabel() {
		if(cellPanel.getMenuPanel() != null)
			cellPanel.getMenuPanel().setVisible(false);
		getLabel().setVisible(true);
	}

	@Override
	public void setTabIndex(int idx) {}

	@Override
	protected void onAttach() {
		super.onAttach();
		if(mediator != null)
			mediator.registerAll();
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	/*@Override
	protected void onDetach() {		
		super.onDetach();
		if(mediator != null)
			mediator.unRegisterAll();
	}*/
	
	

}
