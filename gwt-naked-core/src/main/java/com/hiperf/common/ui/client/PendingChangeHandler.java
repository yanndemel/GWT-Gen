package com.hiperf.common.ui.client;

import com.google.gwt.event.shared.EventHandler;
import com.hiperf.common.ui.shared.util.PendingChangeEvent;

public interface PendingChangeHandler extends EventHandler {

	void onChange(PendingChangeEvent e);
}
