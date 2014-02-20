package com.hiperf.common.ui.shared;

import java.util.HashMap;
import java.util.Map;

import com.hiperf.common.ui.client.ICustomCell;

public class WidgetFactory {
	private static final Map<String, ICustomCell> CUSTOM_CELLS = new HashMap<String, ICustomCell>();
	private static final Map<String, String> HELP_MESSAGES = new HashMap<String, String>();
	private static final Map<String, String> ATTRIBUTES_LABELS = new HashMap<String, String>();
	private static final Map<String, String> TABLES_LABELS = new HashMap<String, String>();
	private static final Map<String, String> FORMS_LABELS = new HashMap<String, String>();

	public static Map<String, ICustomCell> getCustomCells() {
		return CUSTOM_CELLS;
	}

	public static Map<String, String> getHelpMessages() {
		return HELP_MESSAGES;
	}

	public static Map<String, String> getAttributesLabels() {
		return ATTRIBUTES_LABELS;
	}

	public static Map<String, String> getTablesLabels() {
		return TABLES_LABELS;
	}

	public static Map<String, String> getFormsLabels() {
		return FORMS_LABELS;
	}



}
