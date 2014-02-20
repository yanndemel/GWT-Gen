package com.hiperf.common.ui.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface CollectionDataRemovedHandler extends EventHandler {

	void onItemRemoved(CollectionDataRemovedEvent event);
}
