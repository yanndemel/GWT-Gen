package com.hiperf.gwtjpa.test.table.msgprovider.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.model.WrapperListModel;
import com.hiperf.common.ui.client.widget.TablePanel;
import com.hiperf.common.ui.client.widget.WrappedFlexTable;
import com.hiperf.common.ui.client.widget.WrappedFlexTable.HeaderCell;
import com.hiperf.common.ui.shared.WrapperContext;

/*
 * PRe requesites : 
 * 1. Make displayed POJO classes implement INakedObject
 * 2. Add a default constructor with no args to all POJOs that implements INakedObject
 * Basic operations on non persistent POJOS (add, remove rows)
 * Test switch columns
 * Test with and without MessageProvider
 * Test field default rendering
 * Test field formatters (string, long, date, INakedObject)
 * Test updateHandlers
 * 
 * */
public class MsgProviderTestIntegration extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "com.hiperf.gwtjpa.test.table.msgprovider.Basic";
	}

	private static native String getNodeName(Element elem) /*-{
		return (elem.nodeName || "").toLowerCase();
	}-*/;

	/**
	 * Removes all elements in the body, except scripts and iframes.
	 */
	public void gwtSetUp() {
		Element bodyElem = RootPanel.getBodyElement();

		List<Element> toRemove = new ArrayList<Element>();
		for (int i = 0, n = DOM.getChildCount(bodyElem); i < n; ++i) {
			Element elem = DOM.getChild(bodyElem, i);
			String nodeName = getNodeName(elem);
			if (!"script".equals(nodeName) && !"iframe".equals(nodeName)) {
				toRemove.add(elem);
			}
		}

		for (int i = 0, n = toRemove.size(); i < n; ++i) {
			DOM.removeChild(bodyElem, toRemove.get(i));
		}
	}

	@Test
	public void testColumnLabel() {
		GWT.create(WrapperContext.class);
		
		WrapperListModel rows = new WrapperListModel(B.class.getName());
		final WrappedFlexTable t = new WrappedFlexTable(rows);
		TablePanel tp = GWT.create(TablePanel.class);
		tp.initPanel(t, TablePanel.TYPE.ROOT, null, null);
		assertEquals(0, t.getRowCount());
		List<HeaderCell> headerWidgets = t.getHeaderWidgets();
		RootPanel.get().add(tp);
		assertEquals(1, headerWidgets.size());
		//test col label
		Node child = t.getElement().getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0);
		Element el = child.cast();
		assertEquals("My English Label", el.getInnerText());
		B b = new B(25.0);
		IWrapper w = GWT.create(B.class);
		w.setContent(b);
		t.getRows().addItem(w);
		assertEquals(1, t.getRowCount());
		
	}
	

}
