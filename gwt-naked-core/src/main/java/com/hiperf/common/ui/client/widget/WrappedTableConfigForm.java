package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizePopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.event.ChangeHeaderLabelEvent;
import com.hiperf.common.ui.client.event.DisplayColumnEvent;
import com.hiperf.common.ui.client.event.SwitchColumnsEvent;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.shared.CommonUtil;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.model.ScreenLabels;
import com.hiperf.common.ui.shared.util.TableConfig;

public class WrappedTableConfigForm extends VerticalPanel {

	private WrappedFlexTable table;
	private Grid mainTable;
	private Grid generalInfo;
	private int nbRowsPerPage;
	private boolean somethingChanged = false;
	private Map<String, HeaderInfo> changedHeaderLabels = new HashMap<String, HeaderInfo>();
	private Image image;
	protected boolean nbRowsChanged;

	interface Images extends ClientBundle {
		ImageResource littleArrow();
	}

	private class ConfigTextBox extends TextBox {

		private boolean changed = false;

		public ConfigTextBox() {
			super();
			addChangeHandler(new ChangeHandler() {

				@Override
				public void onChange(ChangeEvent event) {
					changed = true;
					somethingChanged = true;
					setVisibleLength(getLength() + 1);
				}
			});
		}

		private int getLength() {
			return getText() != null ? getText().length() : 0;
		}

		@Override
		public void setText(String text) {
			super.setText(text);
			setVisibleLength(text.length() + 1);
		}

		public boolean isChanged() {
			return changed;
		}



	}
	public static final Images images = GWT.create(Images.class);

	public WrappedTableConfigForm(WrappedFlexTable table) {
		this(table, -1, null);
	}

	public WrappedTableConfigForm(WrappedFlexTable table, int nbRows, Image i) {
		super();
		this.table = table;
		this.nbRowsPerPage = nbRows > 0 ? nbRows : TablePanel.DEFAULT_ROW_NB;
		this.somethingChanged = false;
		this.nbRowsChanged = false;
		this.image = i;
		setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		redraw();
	}


	private void redraw() {
		mainTable = new Grid(2,1);
		mainTable.setCellSpacing(0);
		mainTable.getRowFormatter().setStyleName(1, "disclosureGrid");
		add(mainTable);
		FlexTable g = new FlexTable();
		ScrollPanel sc = new ScrollPanel(g);
		g.setStyleName("config-table");
		Label l = new Label(NakedConstants.constants.headersInfo());
		l.setStyleName("bold");
		final DisclosurePanel d1 = new DisclosurePanel();
		Grid header = newDisclosureHeader(l);
		d1.setHeader(header);
		d1.add(sc);
		mainTable.setWidget(0, 0, d1);
		mainTable.getCellFormatter().setStyleName(0, 0, "disclosureGrid");
		l = new Label(NakedConstants.constants.generalInfo());
		l.setStyleName("bold");
		header = newDisclosureHeader(l);
		final DisclosurePanel d2 = new DisclosurePanel();
		d2.setHeader(header);
		d1.addOpenHandler(new OpenHandler<DisclosurePanel>() {
			
			@Override
			public void onOpen(OpenEvent<DisclosurePanel> event) {
				if(d2.isOpen())
					d2.setOpen(false);
			}
		});
		d2.addOpenHandler(new OpenHandler<DisclosurePanel>() {
			
			@Override
			public void onOpen(OpenEvent<DisclosurePanel> event) {
				if(d1.isOpen())
					d1.setOpen(false);
			}
		});
		generalInfo = new Grid(7, 2);
		redrawGeneralInfo();
		d2.add(generalInfo);
		mainTable.setWidget(1, 0, d2);
		mainTable.getCellFormatter().setStyleName(1, 0, "disclosureGrid");
		g.getCellFormatter().setStyleName(0, 0, "config-table-th");
		g.setText(0, 0, NakedConstants.constants.label());
		g.getCellFormatter().setStyleName(0, 1, "config-table-th");
		g.getCellFormatter().addStyleName(0, 1, "config-table-border-left");
		g.setText(0, 1, NakedConstants.constants.displayed());
		g.getCellFormatter().setStyleName(0, 2, "config-table-th");
		g.getCellFormatter().addStyleName(0, 2, "config-table-border-left");
		g.setText(0, 2, NakedConstants.constants.index());
		redrawGrid( g);
		Button b = new Button(NakedConstants.constants.saveConfig(), new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				saveConfig();
			}
		});
		b.setStylePrimaryName("naked-button");
		add(b);



	}

	private Grid newDisclosureHeader(Label l) {
		Grid header = new Grid(1, 2);
		header.setWidget(0, 0, new Image(images.littleArrow()));
		header.setWidget(0, 1, l);
		header.getCellFormatter().setWidth(0, 1, "100%");
		return header;
	}

	private void redrawGeneralInfo() {
		generalInfo.setStyleName("config-table");
		applyLineStyle(0);
		generalInfo.setText(0, 0, NakedConstants.constants.numberOfRows());
		ConfigTextBox tb = new ConfigTextBox();
		tb.setTabIndex(1);
		tb.setVisibleLength(4);
		tb.setMaxLength(4);
		tb.setText(Integer.toString(nbRowsPerPage));
		tb.setAlignment(TextAlignment.RIGHT);
		tb.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				nbRowsPerPage = Integer.parseInt(event.getValue());
				nbRowsChanged = true;
			}
		});
		generalInfo.setWidget(0, 1, tb);

		applyLineStyle(1);
		TableConfig tc = WrapperContext.getTableConfig(table.getClassName(), table.getViewName());
		
		generalInfo.setText(1, 0, NakedConstants.constants.tableTitle());
		ConfigTextBox tb2 = new ConfigTextBox();
		tb2.setTabIndex(2);
		if(tc != null && tc.getTableLabel() != null)
			tb2.setText(tc.getTableLabel());
		generalInfo.setWidget(1, 1, tb2);

		applyLineStyle(2);
		generalInfo.setText(2, 0, NakedConstants.constants.formTitle());
		tb2 = new ConfigTextBox();
		tb2.setTabIndex(3);
		if(tc != null && tc.getFormLabel() != null)
			tb2.setText(tc.getFormLabel());
		generalInfo.setWidget(2, 1, tb2);

		applyLineStyle(3);
		generalInfo.setText(3, 0, NakedConstants.constants.newElementTitle());
		tb2 = new ConfigTextBox();
		tb2.setTabIndex(4);
		if(tc != null && tc.getCreateLabel() != null)
			tb2.setText(tc.getCreateLabel());
		generalInfo.setWidget(3, 1, tb2);

		applyLineStyle(4);
		generalInfo.setText(4, 0, NakedConstants.constants.selectElementTitle());		
		tb2 = new ConfigTextBox();
		tb2.setTabIndex(5);
		if(tc != null && tc.getSelectLabel() != null)
			tb2.setText(tc.getSelectLabel());
		generalInfo.setWidget(4, 1, tb2);

		applyLineStyle(5);
		generalInfo.setText(5, 0, NakedConstants.constants.viewElementTitle());
		tb2 = new ConfigTextBox();
		tb2.setTabIndex(6);
		if(tc != null && tc.getViewLabel() != null)
			tb2.setText(tc.getViewLabel());
		generalInfo.setWidget(5, 1, tb2);

		applyLineStyle(6);
		generalInfo.setText(6, 0, NakedConstants.constants.editElementTitle());
		tb2 = new ConfigTextBox();
		tb2.setTabIndex(7);
		if(tc != null && tc.getEditLabel() != null)
			tb2.setText(tc.getEditLabel());
		generalInfo.setWidget(6, 1, tb2);

	}

	private void applyLineStyle(int row) {
		generalInfo.getRowFormatter().setStyleName(row, "custom-table-tr");
		generalInfo.getRowFormatter().addStyleName(row, "align-left");
		generalInfo.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
		generalInfo.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
	}

	public void saveConfig() {
		PersistenceServiceAsync service = PersistenceService.Util.getInstance();
		final String className = table.getClassName();
		final String viewName = table.getViewName();
		final List<HeaderInfo> sortedFields =  getChangedHeaders();
		String t = getChangedText(1, 1);
		String f = getChangedText(2, 1);
		String n = getChangedText(3, 1);
		String s = getChangedText(4, 1);
		String v = getChangedText(5, 1);
		String e = getChangedText(6, 1);
		final ScreenLabels sl;
		if(t != null || f != null || n != null || s != null || v != null | e != null) {
			sl = new ScreenLabels();
			sl.setLanguage(CommonUtil.getLanguage(NakedConstants.constants.locale()));
			sl.setTableLabel(t);
			sl.setCreateLabel(n);
			sl.setEditLabel(e);
			sl.setFormLabel(f);
			sl.setSelectLabel(s);
			sl.setViewLabel(v);
		} else
			sl = null;
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				ViewHelper.displayError(caught, NakedConstants.constants.exceptionSaveScreenConfig());
			}

			@Override
			public void onSuccess(Void result) {
				WrapperContext.addView(viewName != null && !viewName.equals(className) ? viewName + "#" + className : className, new TableConfig(getNbRowsPerPage(), sl, sortedFields));
				somethingChanged = false;
				changedHeaderLabels.clear();
				if(nbRowsChanged) {
					table.refresh(1, nbRowsPerPage);
					nbRowsChanged = false;
				}
				MessageBox.info(NakedConstants.constants.screenConfigSaved());				
				Widget o = WrappedTableConfigForm.this.getParent();
				while(o != null && !(o instanceof ResizePopupPanel)) {
					o = o.getParent();
				}
				if(o != null) {
					((ResizePopupPanel)o).hide();
				}
			}
		};
		service.saveConfiguration(viewName, className, getNbRowsPerPage(), sortedFields,
				sl, NakedConstants.constants.locale(), callback);
	}

	/**
	 * @return
	 */
	private List<HeaderInfo> getChangedHeaders() {
		List<HeaderInfo> l = new ArrayList<HeaderInfo>(table.getSortedFields().size());
		for(HeaderInfo hi : table.getSortedFields()) {
			if(changedHeaderLabels.containsKey(hi.getAttribute()))
				l.add(hi);
			else {
				l.add(new HeaderInfo(hi.getAttribute(), null, hi.getIndex(), hi.isDisplayed(), hi.getEditable()));
			}
		}
		return l;
	}

	/**
	 * @param j
	 * @param i
	 * @return
	 */
	private String getChangedText(int i, int j) {
		ConfigTextBox w = (ConfigTextBox)generalInfo.getWidget(i, j);
		if(w.isChanged())
			return w.getText();
		return null;
	}


	private void redrawGrid(final FlexTable g) {
		int row = 1;
		int tabIndex = 2;
		final Map<String, IFieldInfo> availableColumns = table.getFieldInfoByName();
		for(final HeaderInfo hi : table.getSortedFields()) {
			final String att = hi.getAttribute();
			final IFieldInfo fi = availableColumns.get(att);
			g.getRowFormatter().setStyleName(row, "custom-table-tr");
			final ConfigTextBox labelTb = new ConfigTextBox();
			labelTb.setTabIndex(tabIndex);
			labelTb.setText(WrapperContext.getHeaderDisplayed(table.getClassName(), hi));
			g.setWidget(row, 0, labelTb);
			g.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
			labelTb.setReadOnly(!hi.isDisplayed());
			labelTb.addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					changedHeaderLabels.put(hi.getAttribute(), hi);
					hi.setLabel(event.getValue());
					WrapperContext.getEventBus().fireEventFromSource(new ChangeHeaderLabelEvent(hi.getIndex(), event.getValue()), table);
				}
			});
			final ConfigTextBox idxTb = new ConfigTextBox();
			final String idx = Integer.toString(hi.getIndex() + 1);
			idxTb.setWidth("20px");
			idxTb.setValue(idx);
			idxTb.addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					try {
						int i = Integer.parseInt(event.getValue()) - 1;
						if(i >=0 && i<getDisplayedColumnsNumber(table)) {
							int oldIdx = hi.getIndex();
							String att2 = null;
							Map<String, IFieldInfo> availableColumns = table.getFieldInfoByName();
							for(String a : availableColumns.keySet()) {
								IFieldInfo f = availableColumns.get(a);

								if(f.isDisplayed() && !a.equals(att) && f.getIndex() == i) {
									att2 = ViewHelper.getHeaderInfo(a, table.getSortedFields()).getAttribute();
									break;
								}
							}
							WrapperContext.getEventBus().fireEventFromSource(new SwitchColumnsEvent(oldIdx, att, i, att2), table);
							table.sortFields();
							redrawGrid(g);
						} else {
							MessageBox.alert(NakedConstants.messages.errorValueOutOfBounds(getDisplayedColumnsNumber(table) - 1));
							idxTb.setValue(idx);
						}
					} catch(Exception e) {
						e.printStackTrace();
						idxTb.setValue(idx);
					}
				}
			});
			idxTb.setEnabled(hi.isDisplayed());
			CheckBox cb = new CheckBox();
			cb.setValue(hi.isDisplayed());
			cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if(event.getValue()) {
						labelTb.setReadOnly(false);
						hi.setIndex(getDisplayedColumnsNumber(table));
						fi.setDisplayed(true);
						hi.setDisplayed(true);
						idxTb.setEnabled(true);
						table.sortFields();
						WrapperContext.getEventBus().fireEventFromSource(new DisplayColumnEvent(att), table);
					} else {
						labelTb.setReadOnly(true);
						table.getMediator().hideColumn(hi);
					}
					redrawGrid(g);
				}
			});
			cb.setTabIndex(tabIndex + 1);
			idxTb.setTabIndex(tabIndex + 2);
			g.setWidget(row, 1, cb);
			g.setWidget(row, 2, idxTb);
			tabIndex += 3;
			row++;
		}
	}



	private static int getDisplayedColumnsNumber(WrappedFlexTable table) {
		int i = 0;
		for(HeaderInfo hi : table.getSortedFields()) {
			if(hi.isDisplayed())
				i++;
		}
		return i;
	}

	public int getNbRowsPerPage() {
		return nbRowsPerPage;
	}


	public void setNbRowsPerPage(int nbRowsPerPage) {
		this.nbRowsPerPage = nbRowsPerPage;
	}

	public boolean isSomethingChanged() {
		return somethingChanged;
	}

	public void setSomethingChanged(boolean somethingChanged) {
		this.somethingChanged = somethingChanged;
	}

	public boolean isNbRowsChanged() {
		return nbRowsChanged;
	}

	public void refresh() {
		table.refresh(1, nbRowsPerPage);
		nbRowsChanged = false;
	}




}
