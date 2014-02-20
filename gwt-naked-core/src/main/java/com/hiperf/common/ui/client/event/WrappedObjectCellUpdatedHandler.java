package com.hiperf.common.ui.client.event;

import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrappedObjectCell;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.WrapperUpdatedHandler;
import com.hiperf.common.ui.shared.WrapperContext;

public class WrappedObjectCellUpdatedHandler implements WrapperUpdatedHandler {

	private IWrappedObjectCell cell;
	private IWrapper wrapper;
	
	public WrappedObjectCellUpdatedHandler(IWrapper wrapper, IWrappedObjectCell cell) {
		this.cell = cell;
		this.wrapper = wrapper;
	}


	@Override
	public void onItemUpdated(WrapperUpdatedEvent event) {
		boolean mustRedraw = false;
		IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(cell.getLinkedObjectClass()).getUpdateHandler();
		if(updateHandler != null) {
			mustRedraw = updateHandler.mustRedraw(event, wrapper.getContent());
		}
		mustRedraw = mustRedraw || wrapper.getContent() == event.getSource();
		if(mustRedraw)
			cell.redraw();
	}


	public IWrappedObjectCell getCell() {
		return cell;
	}


	public IWrapper getWrapper() {
		return wrapper;
	}
	
	

}
