package org.agoncal.application.petstore.client;

import java.util.ArrayList;
import java.util.List;

import org.agoncal.application.petstore.domain.Order;
import org.junit.Test;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.model.PersistentWrapperListModel;
import com.hiperf.common.ui.client.model.WrapperListModel;
import com.hiperf.common.ui.client.widget.EditableWrappedFlexTable;
import com.hiperf.common.ui.client.widget.ICloseablePopupPanel;
import com.hiperf.common.ui.client.widget.MessageBox;
import com.hiperf.common.ui.client.widget.ObjectPanel;
import com.hiperf.common.ui.client.widget.SearchPanel;
import com.hiperf.common.ui.client.widget.SearchPanel.TreeCell;
import com.hiperf.common.ui.client.widget.TablePanel;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
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
public class PetStoreTestIntegration extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "org.agoncal.application.petstore.PetStore";
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
	public void testPersist() {
		GWT.create(WrapperContext.class);
		WrapperContext.addView(Order.class.getName(), "Test View", "Test View");
		String viewName = "Exception View";
		WrapperContext.addView(Order.class.getName(), viewName, "Test View");
		WrapperListModel rows = new PersistentWrapperListModel(Order.class.getName());
		final EditableWrappedFlexTable t = new EditableWrappedFlexTable(rows);	
		t.setViewName(viewName);
		TablePanel tp = new TablePanel();
		RootPanel.get().add(tp);
		tp.getTopMenuInfo().addTopBtn = true;
		tp.initPanel(t, TablePanel.TYPE.ROOT, null, null);
		assertEquals(0, t.getRowCount());
		Widget widget = tp.getTopMenuBar().getWidget(0, tp.getTopMenuInfo().insertMenuIdx);
		assertTrue(widget instanceof Image);
		tp.doAddTopPanelClick(tp.getTopMenuInfo().insertMenuIdx);
		String txt = MessageBox.getTextIfShowing();
		assertTrue(txt != null && txt.equals("Test Exception"));
		MessageBox.onHide(new AsyncCallback<Boolean>() {
			
			@Override
			public void onSuccess(Boolean result) {
				assertTrue(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				fail(caught.getMessage());
			}
		});
		t.setViewName(Order.class.getName());
		tp.doAddTopPanelClick(tp.getTopMenuInfo().insertMenuIdx);
		txt = MessageBox.getTextIfShowing();
		assertTrue(txt == null);
		assertTrue(WrappedObjectsRepository.getInstance().getInsertedObjects().size() > 0);
		Widget mainWidget = tp.getMediator().getPopupPanel().getMainWidget();
		assertTrue(mainWidget instanceof ObjectPanel);
		ObjectPanel op = (ObjectPanel)mainWidget;
		IWrappedObjectForm form = op.getObjectForm();
		IsWidget cell = form.getCell("creditCardExpiryDate");
		assertTrue(cell != null);
		assertTrue(cell instanceof MyCustomCell);
		tp.getMediator().getPopupPanel().hide();
		ICloseablePopupPanel sp = tp.showSearchPanel();
		Widget mw = sp.getMainWidget();
		assertTrue(mw instanceof SearchPanel);
		SearchPanel spp = (SearchPanel) mw;
		assertTrue(spp.getFieldsTree().getItemCount() > 0);
		Widget tc = spp.getFieldsTree().getItem(0).getChild(0).getWidget();
		assertTrue(tc instanceof TreeCell);
		TreeCell tcc = (TreeCell)tc;
		spp.displayFilterLine(tcc, tcc.getAttribute());
		
		
//		op.getObjectForm().
		
	}
	

}
