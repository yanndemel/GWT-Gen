package com.hiperf.common.ui.client.widget.custom;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.hiperf.common.ui.client.ICustomCell;
import com.hiperf.common.ui.client.IPanelMediator;
import com.hiperf.common.ui.client.IWrappedTable;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.exception.ParseException;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.model.ScreenConfig;
import com.hiperf.common.ui.shared.model.ScreenHeaderInfo;

public class ClassAttributesCombo extends ListBox implements ICustomCell {

	protected IWrapper wrapper;
	protected String attribute;
	protected boolean editable;	
	
	public ClassAttributesCombo() {
		super();
	}

	public ClassAttributesCombo(final IWrapper wrapper, final String attribute,
			boolean editable) {
		this.wrapper = wrapper;
		this.attribute = attribute;
		this.editable = editable;
		setEnabled(editable);
		setWidth("100%");
		redraw();
		addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				try {
					wrapper.setAttribute(attribute, getItemText(getSelectedIndex()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean isCustomStyle(boolean table) {
		return false;
	}

	@Override
	public ICustomCell newInstance(IWrapper wrapper, String attribute,
			boolean editable, IPanelMediator mediator) {
		return new ClassAttributesCombo(wrapper, attribute, editable);
	}

	@Override
	public void setCellStyle(IWrappedTable table, int row, int idx) {
	}


	@Override
	public String getAttribute() {
		return attribute;
	}
	
	@Override
	public IWrapper getWrapper() {
		return wrapper;
	}

	@Override
	public void redraw() {
		if(editable) {			
			String attName = wrapper.getAttribute(attribute);
			clear();
			addItem("");
			ScreenHeaderInfo sci = (ScreenHeaderInfo) wrapper.getContent();
			ScreenConfig sc = sci.getScreenConfig();
			if(sc != null) {
				int i = 0;
				for(String att : WrapperContext.getFieldInfoByName().get(sc.getClassName()).keySet()) {
					addItem(att);
					if(attName != null && attName.equals(att)) {
						setSelectedIndex(i + 1);
					}
					i++;
				}
			}			
		} else {
			clear();
			addItem(wrapper.getAttribute(attribute));
		}

	}

}
