package com.hiperf.gwtjpa.test.table.basic.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.event.SwitchColumnsEvent;
import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.exception.ParseException;
import com.hiperf.common.ui.client.model.WrapperListModel;
import com.hiperf.common.ui.client.validation.ValidationHelper;
import com.hiperf.common.ui.client.widget.BooleanCell;
import com.hiperf.common.ui.client.widget.DeleteCheckBox;
import com.hiperf.common.ui.client.widget.EditableListBoxCell;
import com.hiperf.common.ui.client.widget.EditableWrappedFlexTable;
import com.hiperf.common.ui.client.widget.TablePanel;
import com.hiperf.common.ui.client.widget.TextBoxCell;
import com.hiperf.common.ui.client.widget.WrappedFlexTable;
import com.hiperf.common.ui.client.widget.WrappedFlexTable.HeaderCell;
import com.hiperf.common.ui.client.widget.WrappedObjectForm;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.WrapperValidationResults;

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
public class BasicTestIntegration extends GWTTestCase {
	
	@Override
	public String getModuleName() {
		return "com.hiperf.gwtjpa.test.table.basic.Basic";
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
	public void testAddRemoveRows() {
		GWT.create(WrapperContext.class);
		
		WrapperListModel rows = new WrapperListModel(A.class.getName());
		final WrappedFlexTable t = new WrappedFlexTable(rows);
		TablePanel tp = new TablePanel();
		tp.initPanel(t, TablePanel.TYPE.ROOT, null, null);
		assertEquals(0, t.getRowCount());
		List<HeaderCell> headerWidgets = t.getHeaderWidgets();
		
		assertEquals(8, headerWidgets.size());
		int rowCount = 2;
		int colCount = headerWidgets.size();
		Map<String, IFieldInfo> classInfo = WrapperContext.getFieldInfoByName().get(A.class.getName());
		
		int cityIndex = classInfo.get("city").getIndex();
		//test col index
		assertEquals(1, cityIndex);
		//test 
		assertEquals("city", headerWidgets.get(cityIndex).getLabel());
		IWrapper w = GWT.create(A.class);
		B bb1= new B(14.5);
		Set<B> set = new HashSet<B>();
		set.add(bb1);
		
		A a = new A();
		a.setId(1L);
		a.setCity("Paris");
		a.setCollectionField(set);
		a.setObjectField(bb1);
		w.setContent(a);
		rows.addItem(w);
		IWrapper w2 = GWT.create(A.class);
		a = new A();
		a.setId(2L);
		a.setCity("London");
		w2.setContent(a);
		rows.addItem(w2);
		//test rowcount
		assertEquals(rowCount, t.getRowCount());
		//test Cols count
		assertEquals(colCount, t.getCellCount(0));
		//test Object display
		assertEquals("Paris", t.getText(0, cityIndex));
		
		//Test setAttribute
		Exception ex = null;
		try {
			w2.setAttribute("id", "stringValue");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex != null && ex instanceof ParseException);
		ex = null;
		try {
			w2.setAttribute("missingAttribute", "stringValue");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex != null && ex instanceof AttributeNotFoundException);
		//test int field
		ex = null;
		try {
			w2.setAttribute("intField", "1.023");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex != null && ex instanceof ParseException);
		ex = null;
		try {
			w2.setAttribute("intField", "-236");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex == null);
		//test double field
		ex = null;
		try {
			w2.setAttribute("doubleField", "blabla");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex != null && ex instanceof ParseException);
		ex = null;
		try {
			w2.setAttribute("doubleField", "-236.12354987");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex == null);
		//test double field
		ex = null;
		try {
			w2.setAttribute("floatField", "blabla");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex != null && ex instanceof ParseException);
		ex = null;
		try {
			w2.setAttribute("floatField", "-236.12354987");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex == null);
		
		//test INakedObject field
		ex = null;
		try {
			w2.setAttribute("objectField", "2");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex != null && ex instanceof ParseException);
		ex = null;
		B b = new B(3d);
		try {
			w2.setNakedObjectAttribute("objectFieldUnknown", b);
		} catch (AttributeNotFoundException e) {
			ex = e;
		}
		assertEquals(true, ex != null && ex instanceof AttributeNotFoundException);
		ex = null;
		try {
			w2.setNakedObjectAttribute("objectField", b, true);
		} catch (AttributeNotFoundException e) {
			ex = e;
		}
		assertEquals(true, ex == null);
		assertEquals(a.getObjectField(), b);
				
		//test date field
		ex = null;
		try {
			w2.setAttribute("lastDate", "28-12-2014");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(true, ex != null && ex instanceof ParseException);
		ex = null;
		String dt = "2014 Dec 28";
		try {
			w2.setAttribute("lastDate", dt);
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(null, ex);
		assertEquals(dt, t.getText(1, 2));
		//test rename London to Milan
		ex = null;
		try {
			w2.setAttribute("city", "Milan");
		} catch (AttributeNotFoundException e) {
			ex = e;
		} catch (ParseException e) {
			ex = e;
		}
		assertEquals(null, ex);
		assertEquals("Milan", t.getText(1, cityIndex));
		
		final EditableWrappedFlexTable t2 = new EditableWrappedFlexTable(rows);
		tp = GWT.create(TablePanel.class);
		tp.getTopMenuInfo().deleteTopBtn = true;
		Panel p = new SimplePanel();
		tp.initPanel(t2, TablePanel.TYPE.ROOT, null, p);
		assertEquals(rowCount, t2.getRowCount());
		assertEquals(colCount+1, t2.getCellCount(0));
		Widget cb = t2.getWidget(0, 0);
		assertEquals(true, cb instanceof DeleteCheckBox);
		assertEquals(true, tp.getTopMenuInfo().removeMenuIdx >= 0);
		
		//test switch columns
		tp.getMediator().onSwitchColumns(new SwitchColumnsEvent(0, "id", 1, "city") {
			@Override
			  public Object getSource() {
			    return t2;
			  }
		});		
		
		assertEquals("Milan", t2.getText(1, 1));
		
		//test remove rows : delete "Paris"
		((DeleteCheckBox)cb).setValue(true);
		t2.removeRows();
		assertEquals(rowCount - 1, t2.getRowCount());
		assertEquals("Milan", t2.getText(0, 1));
		
		List<HeaderInfo> headerList = new ArrayList<HeaderInfo>();
		headerList.add(new HeaderInfo("id", WrapperContext.getFieldInfoByName().get(A.class.getName()).get("id")));
		headerList.add(new HeaderInfo("city", "Ma Ville", 2, true, false));
		headerList.add(new HeaderInfo("falseAttribute", "Ma Ville", 2, true, false));
		WrappedFlexTable tt = new WrappedFlexTable(rows, headerList);
		tp = new TablePanel();
		tp.initPanel(tt, TablePanel.TYPE.ROOT, null, null);
		assertEquals(2, tt.getHeaderWidgets().size());
		HeaderCell hc = tt.getHeaderWidgets().get(1);
		hc.setSort(true);
		hc.sort();
		hc.clearSort();
		tt = new WrappedFlexTable(rows);
		tt.initTable();
		
	}
	
	
	@Test
	public void testValidators() {
		GWT.create(WrapperContext.class);
		WrapperListModel rows = new WrapperListModel(C.class.getName());
		IWrapper w = (IWrapper)GWT.create(C.class);
		C c = new C();
		w.setContent(c);
		rows.addItem(w);
		WrappedObjectForm form = new WrappedObjectForm(w, true, false, null);
		RootPanel.get().add(form);
		Widget wi = form.getWidget(0, 0);
		assertEquals(true, wi instanceof FlexTable);
		FlexTable t = (FlexTable) wi;
		wi = t.getWidget(0, 1);
		assertEquals(true, wi instanceof TextBoxCell);
		TextBoxCell tb = (TextBoxCell) wi;
		tb.setValue("2", true);
		assertEquals(true, tb.isError());
		tb.setValue("6", true);
		assertEquals(false, tb.isError());
		wi = t.getWidget(1, 1);
		assertEquals(true, wi instanceof TextBoxCell);
		tb = (TextBoxCell) wi;
		tb.setValue("1000", true);
		assertEquals(true, tb.isError());
		tb.setValue("99", true);
		assertEquals(false, tb.isError());
		wi = t.getWidget(2, 1);
		assertEquals(true, wi instanceof TextBoxCell);
		tb = (TextBoxCell) wi;
		tb.setValue("100.8", true);
		assertEquals(true, tb.isError());
		tb.setValue("5.01", true);
		assertEquals(true, tb.isError());
		tb.setValue("99", true);
		assertEquals(false, tb.isError());
		wi = t.getWidget(3, 1);
		assertEquals(true, wi instanceof TextBoxCell);
		tb = (TextBoxCell) wi;
		tb.setValue("toto", true);
		assertEquals(true, tb.isError());
		tb.setValue("titi@toto", true);
		assertEquals(true, tb.isError());
		tb.setValue("titi@toto.com", true);
		assertEquals(false, tb.isError());
		wi = t.getWidget(4, 1);
		assertEquals(true, wi instanceof TextBoxCell);
		tb = (TextBoxCell) wi;
		tb.setValue("-2");
		tb.setValue(null, true);
		assertEquals(true, tb.isError());
		tb.setValue("5.32", true);
		assertEquals(false, tb.isError());
		wi = t.getWidget(5, 1);
		assertEquals(true, wi instanceof EditableListBoxCell);
		
		ValidationHelper.validateForm(form, new AsyncCallback<WrapperValidationResults>() {

			@Override
			public void onFailure(Throwable caught) {
				fail(caught.getMessage());
			}

			@Override
			public void onSuccess(WrapperValidationResults result) {
				if(result != null) {
					if(result.isValidationDone()) {
						assertEquals(true, result != null && result.getValidationErrorsByAttribute() != null);
						Set<String> set = result.getValidationErrorsByAttribute().get("list");
						assertEquals(true, set != null && !set.isEmpty());	
					}
						
				}
				
			}});
		wi = t.getWidget(6, 1);
		assertEquals(true, wi instanceof TextBoxCell);
		tb = (TextBoxCell) wi;
		tb.setValue("a", true);
		assertEquals(true, tb.isError());
		tb.setValue("abcdeftgrkl,nlk,kl", true);
		assertEquals(true, tb.isError());
		tb.setValue("absd", true);
		assertEquals(false, tb.isError());
		wi = t.getWidget(7, 1);
		assertEquals(true, wi instanceof BooleanCell);
		BooleanCell bc = (BooleanCell) wi;
		bc.setValue(false, true);
		ValidationHelper.validateForm(form, new AsyncCallback<WrapperValidationResults>() {

			@Override
			public void onFailure(Throwable caught) {
				fail(caught.getMessage());
			}

			@Override
			public void onSuccess(WrapperValidationResults result) {
				if(result != null) {
					if(result.isValidationDone()) {
						assertEquals(true, result != null && result.getValidationErrorsByAttribute() != null);
						Set<String> set = result.getValidationErrorsByAttribute().get("isTrue");
						assertEquals(true, set != null && !set.isEmpty());	
					}
						
				}
				
			}});
		ArrayList<String> al = new ArrayList<String>();
		al.add("toto");
		c.setList(al);
		EditableWrappedFlexTable et = new EditableWrappedFlexTable(rows);
		TablePanel tp = new TablePanel();
		tp.initPanel(et, TablePanel.TYPE.ROOT, null, null);
		assertEquals(1, et.getRowCount());
		RootPanel.get().clear();
		RootPanel.get().add(et);
		et.setEditable(true);
		et.showDetail(et.getWidget(0, 1).getElement());
		
	}
	

}
