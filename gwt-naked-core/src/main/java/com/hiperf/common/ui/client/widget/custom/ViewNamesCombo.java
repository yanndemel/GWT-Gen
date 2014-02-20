package com.hiperf.common.ui.client.widget.custom;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.hiperf.common.ui.client.ICustomCell;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IWrappedTable;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.exception.ParseException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.widget.MessageBox;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.model.ScreenConfig;
import com.hiperf.common.ui.shared.model.ScreenHeaderInfo;

public class ViewNamesCombo extends ListBox implements ICustomCell {

	//Wrapper of com.hiperf.common.ui.shared.ScreenConfig
	protected IWrapper wrapper;
	//attibute viewName
	private final static String ATTRIBUTE = "viewName";
	protected boolean editable;

	public ViewNamesCombo() {
		super();
	}

	public ViewNamesCombo(final IWrapper wrapper, final String attribute, boolean editable) {
		super();
		this.wrapper = wrapper;
		this.editable = editable;
		final ScreenConfig o = (ScreenConfig) wrapper.getContent();
		redraw();
		if(o.getId() < 0) {
			addChangeHandler(new ChangeHandler() {

				@Override
				public void onChange(ChangeEvent event) {
					WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
					if(wor.isSavedStateListEmpty()) {
						MessageBox.alert(NakedConstants.constants.cannotChangeScreenName());
						redraw();
					} else {
						try {
							String key = getValue(getSelectedIndex());
							int i = key.indexOf("#");
							if(i > 0) {
								o.setClassName(key.substring(i+1));
								wrapper.setAttribute(ATTRIBUTE, key.substring(0, i));
							} else {
								o.setClassName(key);
								wrapper.setAttribute(ATTRIBUTE, key);
							}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							if(o != null && o.getClassName() != null) {
								if(o.getHeaders() != null && !o.getHeaders().isEmpty()) {
									wor.getInsertedObjects().removeAll(o.getHeaders());
								}
								wrapper.removeAllObjectsFromCollection("headers");
								Map<String, IFieldInfo> map = WrapperContext.getFieldInfoByName().get(o.getClassName());
								for(Entry<String, IFieldInfo> e: map.entrySet()) {
									ScreenHeaderInfo shi = new ScreenHeaderInfo();
									shi.setId(PersistenceManager.nextLongId());
									wor.addInsertedObject(shi);
									IFieldInfo fi = e.getValue();
									shi.setAttribute(e.getKey());
									shi.setDisplayed(fi.isDisplayed());
									shi.setIndex(fi.getIndex());
									shi.setScreenConfig(o);
									wrapper.addObjectToCollection("headers", shi, true);
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			});
		}

	}


	@Override
	public String getAttribute() {
		return ATTRIBUTE;
	}

	@Override
	public IWrapper getWrapper() {
		return wrapper;
	}

	public boolean isCustomStyle(boolean table) {
		return false;
	}

	@Override
	public ICustomCell newInstance(IWrapper wrapper,
			String attribute, boolean editable, IPanelMediator mediator) {
		return new ViewNamesCombo(wrapper, attribute, editable);
	}

	@Override
	public void setCellStyle(IWrappedTable table, int row, int idx) {}

	@Override
	public void redraw() {
		ScreenConfig o = (ScreenConfig) wrapper.getContent();
		String name = o.getViewName();
		String className = o.getClassName();
		String key = name != null && className != null && !name.equals(className) ? name + "#" +className : className;
		if(o.getId() >= 0) {
			addItem(WrapperContext.getViewLabel(key), key);
			setEnabled(false);
		} else {
			clear();
			if(editable) {
				addItem("");
				Map<String, String> allViews = WrapperContext.getAllViews();
				int i = 0;
				for(String k : allViews.keySet()) {
					String v = WrapperContext.getViewLabel(k);
					addItem(v, k);
					if(k.equals(key)) {
						setSelectedIndex(i + 1);
					}
					i++;
				}
			} else {
				addItem(WrapperContext.getViewLabel(key), key);
			}
		}
	}



}
