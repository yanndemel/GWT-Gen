package com.hiperf.common.ui.client;

import com.google.gwt.event.shared.EventHandler;
import com.hiperf.common.ui.client.event.WrapperUpdatedEvent;

public interface WrapperUpdatedHandler extends EventHandler {

	void onItemUpdated(WrapperUpdatedEvent event);
}
