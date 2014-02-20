package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.event.HideLoadingPopupEvent;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.widget.WrappedFlexTable.HeaderCell;
import com.hiperf.common.ui.shared.HeaderInfo;
import com.hiperf.common.ui.shared.IConstants;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.model.Filter;
import com.hiperf.common.ui.shared.model.FilterType;
import com.hiperf.common.ui.shared.model.FilterValue;
import com.hiperf.common.ui.shared.util.TableConfig;

public class SearchPanel extends VerticalPanel {

	private static final String PERCENT = "%";
	private static final String DESC = "DESC";
	private static final String ASC = "ASC";
	private static final String EMPTY = "";
	private static final String BLANK = " ";
	private static final DateTimeFormat DATE_FORMATTER = DateTimeFormat
			.getFormat(IConstants.DT_YYYY_MM_DD);
	private static final String COMMA = ",";
	private static final String NOT = "not ";
	private static final String NOT_EQ = "<>";
	private static final String LIKE = "like";
	private static final String EQUALS = "equals";
	private static final String IN = "in";
	private static final String NOT_LIKE = NOT + LIKE;
	private static final String NOT_EQUALS = NOT + EQUALS;
	private static final String BETWEEN = "between";
	private static final String NOT_BETWEEN = NOT + BETWEEN;
	private static final String LT = "<";
	private static final String GT = ">";
	private static final String LTE = "<=";
	private static final String GTE = ">=";
	private static final String EQ = "=";
	private static final String NOT_IN = NOT + IN;
	private static final String DOT = ".";
	private static final String SLASH = "/";
	private static final String OR = "OR";
	private static final String AND = "AND";

	interface Images extends ClientBundle {

		ImageResource search_filter();

		ImageResource save();

		ImageResource save_as();

		ImageResource nofilter();

		ImageResource applyFilter();

		ImageResource smallDelete();

		ImageResource search_small();

		ImageResource write_small();

		ImageResource clearSort();
	}

	protected static final Images images = GWT.create(Images.class);

	private ListBox filterList;
	private WrappedFlexTable table;
	private FlexTable topMenu;
	private VerticalPanel mainPanel;
	private FlexTable filterContentTable;
	private FlexTable sortByTable;
	private Tree fieldsTree;
	private int nbRowsPerPage;
	private List<String> disabledFields = null;
	private List<String> enabledFields = null;
	private CheckBox caseCheckbox;
	private TablePanel panel;

	private class NumberListBox extends ListBox {

		private DataType type;

		public DataType getType() {
			return type;
		}

		public NumberListBox(DataType type) {
			super();
			this.type = type;
		}

	}

	private class StringListBox extends ListBox {
	}

	private class DateListBox extends ListBox {
	}

	private class ObjectLabel extends Label {

		private ObjectTreeCell item;

		public void setObjectTreeCell(ObjectTreeCell item) {
			this.item = item;
		}

		public ObjectTreeCell getItem() {
			return item;
		}

	}

	private class DeleteFieldImage extends Image {
		private String attribute;
		private TreeCell treeCell;

		public DeleteFieldImage(ImageResource resource, TreeCell treeCell,
				String att) {
			super(resource);
			setStyleName("custom-button");
			setTitle(NakedConstants.constants.removeLine());
			this.attribute = att;
			this.treeCell = treeCell;
		}

		public String getAttribute() {
			return attribute;
		}

		public TreeCell getTreeCell() {
			return treeCell;
		}

	}

	private interface ITreeItem {
		Map<String, IFieldInfo> getFieldInfoByName();

		String getLabel();

		String getAttributePrefix();

		void addItem(TreeItem item);

		TreeItem addItem(Widget widget);

		String getClassName();

		ITreeItem getParent();

		boolean isCollection();
	}

	public class TreeCell extends Label {
		private String attribute;
		private ITreeItem treeItem;

		public TreeCell(final ITreeItem treeItem, String att, String label) {
			this.attribute = att;
			this.treeItem = treeItem;
			setStyleName("gwt-TreeItem");
			addStyleName("filter-tree-item");
			setText(label);
			addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					displayFilterLine(TreeCell.this, attribute);
				}

			});
		}

		public String getAttribute() {
			return attribute;
		}

		public ITreeItem getTreeItem() {
			return treeItem;
		}

	}

	private class ObjectTreeCell extends TreeItem implements ITreeItem {
		private Map<String, IFieldInfo> fieldInfoByName;
		private String attributePrefix;
		private String label;
		private boolean init;
		private String className;
		private boolean collection;
		private List<TreeCell> childs;

		public ObjectTreeCell(String attPrefix, String lbl, Label l,
				Map<String, IFieldInfo> fieldInfoByName, String className) {
			this(attPrefix, lbl, l, fieldInfoByName, className, false);
		}

		public ObjectTreeCell(String attPrefix, String lbl, Label l,
				Map<String, IFieldInfo> fieldInfoByName, String className,
				boolean collection) {
			super(l);
			this.attributePrefix = attPrefix;
			this.fieldInfoByName = fieldInfoByName;
			this.label = lbl;
			this.init = false;
			this.className = className;
			this.collection = collection;
		}

		public String getClassName() {
			return className;
		}

		public Map<String, IFieldInfo> getFieldInfoByName() {
			return fieldInfoByName;
		}

		public String getAttributePrefix() {
			return attributePrefix;
		}

		public String getLabel() {
			return label;
		}

		public boolean isInitialized() {
			return init;
		}

		public void setInit(boolean init) {
			this.init = init;
		}

		@Override
		public ITreeItem getParent() {
			return (ITreeItem) getParentItem();
		}

		public boolean isCollection() {
			return collection;
		}

		public List<TreeCell> getChilds() {
			return childs;
		}

		@Override
		public TreeItem addItem(Widget widget) {
			if (widget instanceof TreeCell) {
				if (childs == null)
					childs = new ArrayList<SearchPanel.TreeCell>();
				childs.add((TreeCell) widget);
			}
			return super.addItem(widget);
		}

	}

	private class RootItem extends TreeItem implements ITreeItem {

		private Map<String, IFieldInfo> fieldInfoByName;

		public RootItem(Label l, Map<String, IFieldInfo> fieldInfoByName) {
			super(l);
			this.fieldInfoByName = fieldInfoByName;
			addClassFields(this);
			setState(true);
		}

		public String getAttributePrefix() {
			return null;
		}

		public String getLabel() {
			return null;
		}

		public Map<String, IFieldInfo> getFieldInfoByName() {
			return fieldInfoByName;
		}

		@Override
		public String getClassName() {
			return table.getClassName();
		}

		@Override
		public ITreeItem getParent() {
			return null;
		}

		@Override
		public boolean isCollection() {
			return false;
		}

	}

	private class SearchListBox extends ListBox {

		public SearchListBox(boolean multiple) {
			this(multiple, null);
		}

		public SearchListBox(boolean multiple, Map<String, String> values) {
			super(multiple);
			if (multiple) {
				setVisibleItemCount(3);
			}
			if (values != null && !values.isEmpty())
				fillListBox(values);
		}

		public SearchListBox(boolean multiple, String className,
				String attribute, String attPrefix) {
			super(multiple);
			if (multiple) {
				setVisibleItemCount(3);
			}
			PersistenceServiceAsync service = PersistenceService.Util
					.getInstance();
			AsyncCallback<Map<String, String>> callback = new AsyncCallback<Map<String, String>>() {

				@Override
				public void onFailure(Throwable caught) {
					ViewHelper.displayError(caught,
							NakedConstants.constants.exceptionDataDB());
				}

				@Override
				public void onSuccess(Map<String, String> result) {
					if (result != null && !result.isEmpty()) {
						fillListBox(result);
					}
				}
			};
			service.getAll(table.getClassName(), getActiveFilter(), attPrefix, className, attribute, callback);
		}

		public String getActiveFilter() {
			return table.getFilter() != null ? table.getFilter() : table.getConstantFilter();
		}

		public void fillListBox(Map<String, String> map) {
			for (Entry<String, String> e : map.entrySet()) {
				String key = e.getKey();
				if (e.getValue() == null) {
					if (!IConstants.NULL.equals(key))
						addItem(key, "'" + escapeQuotes(key) + "'");
					else
						addItem(key, (String) null);
				} else
					addItem(key, e.getValue());
			}
		}

	}

	private void saveFilter(final Filter f) {
		PersistenceServiceAsync service = PersistenceService.Util.getInstance();
		AsyncCallback<Long> callback = new AsyncCallback<Long>() {

			@Override
			public void onFailure(Throwable caught) {
				ViewHelper.displayError(caught,
						NakedConstants.constants.exceptionSaveFilter());
			}

			@Override
			public void onSuccess(Long result) {
				if (f.getId() == null && result != null && filterList != null) {
					filterList.addItem(f.getName(), result.toString());
					filterList.setSelectedIndex(filterList.getItemCount() - 1);
				}
				f.setId(result);
				MessageBox.info(NakedConstants.constants.filterSaved());
			}
		};
		service.saveFilter(f, callback);

	}

	public SearchPanel(TablePanel panel) {
		super();
		this.panel = panel;
		this.table = panel.getTable();
		topMenu = new FlexTable();
		caseCheckbox = new CheckBox();
		mainPanel = new VerticalPanel();
		mainPanel.setWidth("100%");
		mainPanel.setStyleName("search-panel");
		filterContentTable = new FlexTable();
		filterContentTable.setStyleName("filter-table");
		sortByTable = new FlexTable();
		sortByTable.setStyleName("filter-table");
		add(topMenu);
		add(mainPanel);
		add(sortByTable);
		topMenu.insertRow(0);
		Image img = new Image(images.search_filter());
		img.setStyleName("custom-button");
		topMenu.setWidget(0, 0, img);
		img.setTitle(NakedConstants.constants.selectFilter());
		img.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				if (filterList == null) {
					initFilterList();
				} else {
					filterList.getParent().setVisible(true);
				}

			}
		});
		img = new Image(images.save());
		img.setStyleName("custom-button");
		topMenu.setWidget(0, 1, img);
		img.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				saveFilter(false);
			}

		});
		img.setTitle(NakedConstants.constants.saveFilter());
		img = new Image(images.save_as());
		img.setStyleName("custom-button");
		topMenu.setWidget(0, 2, img);
		img.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				saveFilter(true);
			}
		});
		img.setTitle(NakedConstants.constants.saveFilterAs());

		img = new Image(images.nofilter());
		img.setStyleName("custom-button");
		topMenu.setWidget(0, 3, img);
		img.setTitle(NakedConstants.constants.clearFilter());
		img.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				clearFilter();
				if (table.isFiltered()) {
					final String old = table.getFilter();
					table.clearFilter();
					final WrappedObjectsRepository wor = WrappedObjectsRepository
							.getInstance();
					if (wor.hasToCommit()) {
						AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

							@Override
							public void onFailure(Throwable caught) {
							}

							@Override
							public void onSuccess(Boolean b) {
								if (b != null) {
									if (b.booleanValue()) {
										wor.saveAll(new AsyncCallback<Void>() {

											@Override
											public void onFailure(
													Throwable caught) {
												ViewHelper.displayListError(
														table.getRows(), caught);
											}

											@Override
											public void onSuccess(Void result) {
												table.refresh(1, nbRowsPerPage);
											}
										});
									} else {
										wor.clear();
										table.refresh(1, nbRowsPerPage);
									}
								} else {
									WrapperContext
											.getEventBus()
											.fireEventFromSource(
													new HideLoadingPopupEvent(),
													table.getItems());
									table.setFilter(old);
								}
							}
						};
						PendingChangesDialogBox.getInstance().show(callback);
					} else {
						table.refresh(1, nbRowsPerPage);
					}
				}
			}
		});

		img = new Image(images.clearSort());
		img.setStyleName("custom-button");
		topMenu.setWidget(0, 4, img);
		img.setTitle(NakedConstants.constants.clearSort());
		img.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				table.clearSortColumns(null);
				((ListBox) sortByTable.getWidget(0, 1))
						.setItemSelected(0, true);
			}
		});
		img = new Image(images.applyFilter());
		img.setStyleName("custom-button");
		topMenu.setWidget(0, 5, img);
		img.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				doApplyFilter(table, 1, nbRowsPerPage);

			}
		});
		img.setTitle(NakedConstants.constants.applyFilter());

		if (table.hasConstantFilter()) {
			disabledFields = table.getConstantFilterFields();
		}
		if (table.getEnabledFilterFields() != null) {
			enabledFields = table.getEnabledFilterFields();
		}
		clearFilter();
	}

	public String getSortAttribute() {
		ListBox lb = (ListBox) sortByTable.getWidget(0, 1);
		int idx = lb.getSelectedIndex();
		if (idx == 0)
			return null;
		return lb.getValue(idx);
	}

	public void initSortByTable() {
		sortByTable.clear();
		sortByTable.removeAllRows();
		sortByTable.insertRow(0);
		sortByTable.setText(0, 0, NakedConstants.constants.sortBy());
		final ListBox lb = new ListBox();
		HeaderCell sortedColumn = table.getSortedColumn();
		int i = 0;
		sortByTable.setWidget(0, 1, lb);
		final ListBox wayLb = new ListBox();
		wayLb.addItem(NakedConstants.constants.asc(), ASC);
		wayLb.addItem(NakedConstants.constants.desc(), DESC);
		sortByTable.setWidget(0, 2, wayLb);
		lb.addItem(EMPTY);
		for (HeaderInfo hi : table.getSortedFields()) {
			String attribute = hi.getAttribute();
			IFieldInfo fi = table.getFieldInfoByName().get(attribute);
			if (hi.isDisplayed() && table.isServerSortable(fi)) {
				lb.addItem(WrapperContext.getHeaderDisplayed(
						table.getClassName(), hi),
						fi.getSortCriteria() != null ? fi.getSortCriteria()
								: attribute);
				i++;
				if (sortedColumn != null && sortedColumn.getAttribute() != null
						&& sortedColumn.getAttribute().equals(attribute)) {
					lb.setSelectedIndex(i);
					wayLb.setSelectedIndex(sortedColumn.getSort() ? 0 : 1);
				}
			}
		}
		lb.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				int idx = lb.getSelectedIndex();
				switch (idx) {
				case 0:
					table.clearSortColumns(null);
					break;
				default:
					table.clearSortColumns(null);
					updateSortAttribute(lb, wayLb, idx);
					break;
				}
			}
		});
		wayLb.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				int idx = lb.getSelectedIndex();
				switch (idx) {
				case 0:
					break;
				default:
					updateSortAttribute(lb, wayLb, idx);
					break;
				}
			}
		});
	}

	private void updateSortAttribute(ListBox lb, ListBox wayLb, int idx) {
		String att = lb.getValue(idx);
		for (HeaderCell cell : table.getHeaderWidgets()) {
			if (cell.getAttribute() != null && cell.getAttribute().equals(att)) {
				boolean way = wayLb.getValue(wayLb.getSelectedIndex()).equals(
						ASC) ? false : true;
				cell.setSort(way);
				table.displaySortArrow(cell);
				break;
			}
		}
	}

	private void displayLine(final String attribute, final IFieldInfo fi,
			final String className, int row, boolean collection, final String attPrefix) {
		if (fi.isEnum()) {
			final StringListBox stringOperators = new StringListBox();
			stringOperators.addItem(EQUALS);
			stringOperators.addItem(NOT_EQUALS);
			stringOperators.addItem(IN);
			stringOperators.addItem(NOT_IN);
			filterContentTable.setWidget(row, 2, stringOperators);
			Map<String, String> map = WrapperContext.getEnumsByClassName().get(
					fi.getTypeName());
			final Map<String, String> map2 = new HashMap<String, String>(
					map.size());
			for (Entry<String, String> e : map.entrySet()) {
				map2.put(e.getValue(), fi.getTypeName() + DOT + e.getKey());
			}
			SearchListBox slb = new SearchListBox(false, map2);
			filterContentTable.setWidget(row, 3, slb);
			stringOperators.addChangeHandler(new ChangeHandler() {

				@Override
				public void onChange(ChangeEvent event) {
					int row = getMyFilterRow(event.getSource(), 2);
					Widget w = filterContentTable.getWidget(row, 3);
					if (w instanceof SearchListBox) {
						SearchListBox slb = (SearchListBox) w;
						if (stringOperators.getSelectedIndex() == 2
								|| stringOperators.getSelectedIndex() == 3) {
							if (!slb.isMultipleSelect()) {
								filterContentTable.clearCell(row, 3);
								filterContentTable.setWidget(row, 3,
										new SearchListBox(true, map2));
							}
						} else if (slb.isMultipleSelect()) {
							filterContentTable.clearCell(row, 3);
							filterContentTable.setWidget(row, 3,
									new SearchListBox(false, map2));
						}
					}
				}
			});

		} else {
			final TextBox tb = new TextBox();
			switch (fi.getDataType()) {
			case STRING:
			case CHAR:
				final StringListBox stringOperators = new StringListBox();
				stringOperators.addItem(EQUALS);
				stringOperators.addItem(NOT_EQUALS);
				stringOperators.addItem(LIKE);
				stringOperators.addItem(NOT_LIKE);
				if (!collection) {
					stringOperators.addItem(IN);
					stringOperators.addItem(NOT_IN);
				}
				stringOperators.addChangeHandler(new ChangeHandler() {

					@Override
					public void onChange(ChangeEvent event) {
						int row = getMyFilterRow(event.getSource(), 2);
						Widget w = filterContentTable.getWidget(row, 3);
						if (w instanceof SearchListBox) {
							SearchListBox slb = (SearchListBox) w;
							if (stringOperators.getSelectedIndex() == 4
									|| stringOperators.getSelectedIndex() == 5) {
								if (!slb.isMultipleSelect()) {
									filterContentTable.clearCell(row, 3);
									filterContentTable.setWidget(row, 3,
											new SearchListBox(true));
								}
							} else if (slb.isMultipleSelect()) {
								filterContentTable.clearCell(row, 3);
								filterContentTable.setWidget(row, 3,
										new SearchListBox(false));
							}
						}
					}
				});
				filterContentTable.setWidget(row, 2, stringOperators);
				filterContentTable.setWidget(row, 3, tb);
				final Image img = new Image(images.search_small());
				img.setStyleName("custom-button");
				img.setTitle(NakedConstants.constants.getAllValues());
				filterContentTable.setWidget(row, 4, img);
				img.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						int row = getMyFilterRow(event.getSource(), 4);
						filterContentTable.clearCell(row, 3);
						filterContentTable.clearCell(row, 4);
						Image i = new Image(images.write_small());
						img.setStyleName("custom-button");
						i.setTitle(NakedConstants.constants.writeSearch());
						ListBox lb = (ListBox) filterContentTable.getWidget(
								row, 2);
						String itemText = lb.getItemText(lb.getSelectedIndex());
						if (itemText.equals(IN) || itemText.equals(NOT_IN)) {
							filterContentTable.setWidget(row, 3,
									new SearchListBox(true, className,
											attribute, attPrefix));
						} else {
							filterContentTable.setWidget(row, 3,
									new SearchListBox(false, className,
											attribute, attPrefix));
						}
						filterContentTable.setWidget(row, 4, i);
						i.addClickHandler(new ClickHandler() {

							@Override
							public void onClick(ClickEvent event) {
								event.stopPropagation();
								int row = getMyFilterRow(event.getSource(), 4);
								filterContentTable.clearCell(row, 3);
								filterContentTable.clearCell(row, 4);
								filterContentTable.setWidget(row, 3,
										new TextBox());
								filterContentTable.setWidget(row, 4, img);
							}
						});
					}
				});
				break;
			case BOOLEAN:
				filterContentTable.setWidget(row, 2, new CheckBox());
				break;
			case BYTE:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				final NumberListBox numberOperators = new NumberListBox(
						fi.getDataType());
				numberOperators.addItem(EQ);
				numberOperators.addItem(NOT_EQ);
				numberOperators.addItem(GTE);
				numberOperators.addItem(LTE);
				numberOperators.addItem(GT);
				numberOperators.addItem(LT);
				if (!collection) {
					numberOperators.addItem(IN);
					numberOperators.addItem(NOT_IN);
				}
				numberOperators.addItem(BETWEEN);
				numberOperators.addItem(NOT_BETWEEN);
				numberOperators.addChangeHandler(new ChangeHandler() {

					@Override
					public void onChange(ChangeEvent event) {
						int row = getMyFilterRow(event.getSource(), 2);
						int idx = numberOperators.getSelectedIndex();
						// BETWEEN & not between
						if (idx == 8 || idx == 9) {
							if (filterContentTable.getCellCount(row) == 4) {
								filterContentTable.setText(row, 4, AND);
								filterContentTable.setWidget(row, 5,
										new TextBox());
							}
						} else if (filterContentTable.getCellCount(row) == 6) {
							filterContentTable.removeCells(row, 4, 2);
						}
					}
				});
				filterContentTable.setWidget(row, 2, numberOperators);
				filterContentTable.setWidget(row, 3, tb);
				break;
			case DATE:
				final DateListBox l = new DateListBox();
				l.addItem(EQ);
				l.addItem(NOT_EQ);
				l.addItem(GTE);
				l.addItem(LTE);
				l.addItem(GT);
				l.addItem(LT);
				l.addItem(BETWEEN);
				l.addItem(NOT_BETWEEN);
				l.addChangeHandler(new ChangeHandler() {

					@Override
					public void onChange(ChangeEvent event) {
						int row = getMyFilterRow(event.getSource(), 2);
						int idx = l.getSelectedIndex();
						// BETWEEN & not between
						if (idx == 6 || idx == 7) {
							if (filterContentTable.getCellCount(row) == 4) {
								filterContentTable.setText(row, 4, AND);
								filterContentTable.setWidget(row, 5,
										new DateBox());
							}
						} else if (filterContentTable.getCellCount(row) == 6) {
							filterContentTable.removeCells(row, 4, 2);
						}
					}
				});
				filterContentTable.setWidget(row, 2, l);
				filterContentTable.setWidget(row, 3, new DateBox());
				break;
			default:
				break;
			}
		}
	}

	private void addClassFields(final ITreeItem treeItem) {
		Map<String, IFieldInfo> fieldInfoByName = treeItem.getFieldInfoByName();
		String className = treeItem.getClassName();
		List<HeaderInfo> sortedFields = new ArrayList<HeaderInfo>();
		if(className.equals(table.getClassName())) {
			sortedFields = new ArrayList<HeaderInfo>(table.getSortedFields());
		} else {
			TableConfig tc = WrapperContext.getTableConfig(className, null);
			if (tc != null && tc.getHeaders() != null
					&& !tc.getHeaders().isEmpty()) {
				sortedFields = new ArrayList<HeaderInfo>(tc.getHeaders());
			} else {
				for(Entry<String, IFieldInfo> e : fieldInfoByName.entrySet()) {
					sortedFields.add(new HeaderInfo(e.getKey(), fieldInfoByName.get(e.getKey())));
				}
			}
			Collections.sort(sortedFields);			
		}
		for (HeaderInfo hi : sortedFields) {
			String att = hi.getAttribute();
			if (disabledFields == null
					|| (treeItem.getAttributePrefix() == null && !disabledFields
							.contains(att))
					|| (treeItem.getAttributePrefix() != null && !disabledFields
							.contains(treeItem.getAttributePrefix() + DOT + att))) {
				IFieldInfo fi = fieldInfoByName.get(att);
				HeaderCell hc = null;
				for (HeaderCell cell : table.getHeaderWidgets()) {
					if(att.equals(cell.getAttribute()) && cell.isVisible()) {
						hc = cell;
						break;
					}
				}
				String label = hi.getLabel();
				if(label == null)
					label = fi.getLabel();
				/*TableConfig tc = WrapperContext.getTableConfig(
						className, null);
				if (tc != null && tc.getHeaders() != null
						&& !tc.getHeaders().isEmpty()) {
					hi = ViewHelper.getHeaderInfo(att, tc.getHeaders());
					if (hi != null && hi.getLabel() != null)
						label = hi.getLabel();
				}*/
				if ((((hi != null && hi.isDisplayed()) || (hi == null && fi
						.isDisplayed())) ||  (hc != null) || (enabledFields != null && ((treeItem
						.getAttributePrefix() == null && enabledFields
						.contains(att)) || (treeItem.getAttributePrefix() != null && enabledFields
						.contains(treeItem.getAttributePrefix() + DOT + att)))))
						&& !fi.isJpaTransient()
						&& !fi.isHidden()
						&& label != null
						&& label.length() > 0) {
					if (fi.isCollection()) {
						if (fi.isOneToMany()) {
							Map<String, IFieldInfo> map = WrapperContext
									.getFieldInfoByName()
									.get(fi.getJoinClass());
							if (canFilter(map)) {
								ObjectLabel l = new ObjectLabel();
								l.setStyleName("bold");
								l.setText(label);
								if (treeItem.getAttributePrefix() == null) {
									final ObjectTreeCell item = new ObjectTreeCell(
											att, label, l, map,
											fi.getJoinClass(), true);
									initObjectTreeCell(treeItem, l, item);
								} else {
									final ObjectTreeCell item = new ObjectTreeCell(
											treeItem.getAttributePrefix() + DOT
													+ att, treeItem.getLabel()
													+ SLASH + label, l, map,
											fi.getJoinClass(), true);
									initObjectTreeCell(treeItem, l, item);
								}
							}
						}
					} else {
						switch (fi.getDataType()) {
						case NAKED_OBJECT:
							Map<String, IFieldInfo> map = WrapperContext
									.getFieldInfoByName().get(fi.getTypeName());
							if (canFilter(map)) {
								ObjectLabel l = new ObjectLabel();
								l.setStyleName("bold");
								l.setText(label);
								if (treeItem.getAttributePrefix() == null) {
									final ObjectTreeCell item = new ObjectTreeCell(
											att, label, l, map,
											fi.getTypeName());
									initObjectTreeCell(treeItem, l, item);
								} else {
									final ObjectTreeCell item = new ObjectTreeCell(
											treeItem.getAttributePrefix() + DOT
													+ att, treeItem.getLabel()
													+ SLASH + label, l, map,
											fi.getTypeName());
									initObjectTreeCell(treeItem, l, item);
								}
							}
							break;
						default:
							treeItem.addItem(new TreeCell(treeItem, att, label));
							break;
						}
					}
				}
			}

		}
	}

	public boolean canFilter(Map<String, IFieldInfo> map) {
		boolean ok = false;
		for (String attrib : map.keySet()) {
			IFieldInfo ifi = map.get(attrib);
			if (ifi.isDisplayed() && !ifi.isJpaTransient()
					&& ifi.getLabel() != null && ifi.getLabel().length() > 0
					&& (!ifi.isCollection() || ifi.isOneToMany())) {
				ok = true;
				break;
			}
		}
		return ok;
	}

	private void initObjectTreeCell(final ITreeItem treeItem, ObjectLabel l,
			final ObjectTreeCell item) {
		treeItem.addItem(item);
		l.setObjectTreeCell(item);
		l.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				if (!item.isInitialized()) {
					addClassFields(item);
					item.setState(true);
					item.setInit(true);
				}
			}
		});
	}

	private void initFilter() {
		mainPanel.clear();
		fieldsTree = new Tree();
		fieldsTree.addStyleName("filter-tree");
		Label l = new Label();
		l.setText(NakedConstants.constants.selectField());
		l.setStyleName("bold");
		fieldsTree.addItem(new RootItem(l, table.getFieldInfoByName()));
		mainPanel.add(fieldsTree);
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(new Label(NakedConstants.constants.caseSensitive()));
		hp.add(caseCheckbox);
		mainPanel.add(hp);
		mainPanel.add(filterContentTable);
	}

	private String generateInClause(String hql, String txt,
			List<String> selectedValues, boolean isNull, boolean isString) {
		if (isNull) {
			if (IN.equals(txt)) {
				if (selectedValues.size() > 0)
					hql += " IS NULL OR " + hql;
				else
					hql += " IS NULL ";
			} else {
				if (selectedValues.size() > 0)
					hql += " IS NOT NULL OR " + hql;
				else
					hql += " IS NOT NULL ";
			}
		}
		if (selectedValues.size() > 0) {
			if (IN.equals(txt)) {
				hql += " IN (";
			} else {
				hql += " NOT IN (";
			}
			int l = 0;
			for (String s : selectedValues) {
				if (isString)
					hql += "'" + escapeQuotes(s) + "'";
				else
					hql += s;
				l++;
				if (l < selectedValues.size())
					hql += ", ";
			}
			hql += ")";
		}
		return hql;
	}

	private String generateQueryFromString(String hql, int row,
			StringListBox lb, String join) {
		String txt = lb.getItemText(lb.getSelectedIndex());
		Widget widget = filterContentTable.getWidget(row, 3);
		if (!IN.equals(txt) && !NOT_IN.equals(txt)) {
			if (widget instanceof SearchListBox) {
				SearchListBox slb = (SearchListBox) widget;
				String itemText = slb.getItemText(slb.getSelectedIndex());
				String value = slb.getValue(slb.getSelectedIndex());
				if (itemText.equals(IConstants.NULL)) {
					if (EQUALS.equals(txt) || LIKE.equals(txt)) {
						hql += " IS NULL ";
					} else {
						hql += " IS NOT NULL ";
					}
				} else if (EQUALS.equals(txt)) {
					hql += " = " + value;
				} else if (LIKE.equals(txt)) {
					hql += " like '" + toLike(itemText) + "'";
				} else if (NOT_EQUALS.equals(txt)) {
					hql += " <> " + value;
				} else if (NOT_LIKE.equals(txt)) {
					hql += " not like '" + toLike(itemText) + "'";
				} else {
					hql += " <> " + value;
				}
			} else {
				TextBox tb = (TextBox) widget;
				String itemText = tb.getText();
				if (itemText == null
						|| itemText.length() == 0
						|| itemText.trim().equalsIgnoreCase(
								IConstants.NULL_VALUE)) {
					if (EQUALS.equals(txt) || LIKE.equals(txt)) {
						hql += " IS NULL ";
					} else {
						hql += " IS NOT NULL ";
					}
				} else if (EQUALS.equals(txt)) {
					if (!caseCheckbox.getValue())
						hql += " = '" + escapeQuotes(itemText.toUpperCase()) + "'";
					else
						hql += " = '" + escapeQuotes(itemText) + "'";
				} else if (NOT_EQUALS.equals(txt)) {
					if (join == null) {
						if (!caseCheckbox.getValue())
							hql += " <> '" + escapeQuotes(itemText.toUpperCase()) + "'";
						else
							hql += " <> '" + escapeQuotes(itemText) + "'";
					} else {
						hql = hql.substring(0, 12) + "oo" + hql.substring(13);
						hql = " o.id not in (select oo.id from "
								+ table.getClassName() + " oo " + join
								+ " where " + hql + " = '";
						if (!caseCheckbox.getValue())
							hql += escapeQuotes(itemText.toUpperCase()) + "')";
						else
							hql += escapeQuotes(itemText) + "')";
					}
				} else if (LIKE.equals(txt)) {
					if (!caseCheckbox.getValue())
						hql += " LIKE '" + toLike(itemText).toUpperCase() + "'";
					else
						hql += " LIKE '" + toLike(itemText) + "'";
				} else if (NOT_LIKE.equals(txt)) {
					if (join == null) {
						if (!caseCheckbox.getValue())
							hql += " NOT LIKE '"
									+ toLike(itemText).toUpperCase() + "'";
						else
							hql += " NOT LIKE '" + toLike(itemText) + "'";
					} else {
						join = join.substring(0, 12) + "oo"
								+ join.substring(13);
						hql = " o.id not in (select oo.id from "
								+ table.getClassName() + " oo " + join
								+ " where " + hql + " LIKE '";
						if (!caseCheckbox.getValue())
							hql += toLike(itemText).toUpperCase() + "')";
						else
							hql += toLike(itemText) + "')";
					}
				}
			}
		} else {
			if (widget instanceof SearchListBox) {
				SearchListBox slb = (SearchListBox) widget;
				List<String> selectedValues = new ArrayList<String>();
				boolean isNull = false;
				for (int k = 0; k < slb.getItemCount(); k++) {
					if (slb.isItemSelected(k)) {
						String itemText = slb.getItemText(k);
						if (IConstants.NULL.equals(itemText)) {
							isNull = true;
						} else {
							String value = slb.getValue(k);
							selectedValues.add(value);
						}
					}
				}
				hql = generateInClause(hql, txt, selectedValues, isNull, false);
			} else {
				TextBox tb = (TextBox) widget;
				String itemText = tb.getText();

				if (itemText != null && itemText.length() > 0) {
					String[] split = itemText.split(COMMA);
					List<String> selectedValues = new ArrayList<String>();
					boolean isNull = false;
					for (String s : split) {
						String trim = s.trim();
						if (IConstants.NULL_VALUE.equalsIgnoreCase(trim)) {
							isNull = true;
						} else {
							if (!caseCheckbox.getValue())
								selectedValues.add(trim.toUpperCase());
							else
								selectedValues.add(trim);
						}
					}
					hql = generateInClause(hql, txt, selectedValues, isNull,
							true);
				}
			}
		}
		return hql;
	}

	private String toLike(String itemText) {
		if (itemText.contains(PERCENT))
			return escapeQuotes(itemText);
		else
			return PERCENT + escapeQuotes(itemText) + PERCENT;
	}

	private String generateQueryFromNumber(String hql, int row,
			NumberListBox nb, String join) {
		TextBox tb = (TextBox) filterContentTable.getWidget(row, 3);
		String txt = nb.getItemText(nb.getSelectedIndex());
		String value = tb.getText();
		if (value != null) {
			value = value.trim();
			switch (nb.getType()) {
			case LONG:
				value += "L";
				break;
			case DOUBLE:
				value += "d";
				break;
			default:
				break;
			}
		}

		if (!IN.equals(txt) && !NOT_IN.equals(txt)) {
			boolean isNull = value == null || value.length() == 0
					|| IConstants.NULL_VALUE.equalsIgnoreCase(value.trim());
			if (EQ.equals(txt)) {
				if (isNull) {
					hql += " is null ";
				} else {
					hql += " = " + value + BLANK;
				}
			} else if (NOT_EQ.equals(txt)) {
				if (join == null) {
					if (isNull) {
						hql += " is not null ";
					} else {
						hql += " <> " + value + BLANK;
					}
				} else {
					hql = hql.substring(0, 12) + "oo" + hql.substring(13);
					hql = " o.id not in (select oo.id from "
							+ table.getClassName() + " oo " + join + " where "
							+ hql
							+ (isNull ? " is null) " : (" = " + value + ") "));
				}

			} else if (BETWEEN.equals(txt)) {
				hql += " BETWEEN "
						+ value
						+ " AND "
						+ ((TextBox) filterContentTable.getWidget(row, 5))
								.getText() + BLANK;
			} else if (NOT_BETWEEN.equals(txt)) {
				hql += " NOT BETWEEN "
						+ value
						+ " AND "
						+ ((TextBox) filterContentTable.getWidget(row, 5))
								.getText() + BLANK;
			} else {
				hql += txt + BLANK + value + BLANK;
			}
		} else if (value != null && value.length() > 0) {
			String[] split = value.split(COMMA);
			List<String> selectedValues = new ArrayList<String>();
			boolean isNull = false;
			for (String s : split) {
				String trim = s.trim();
				if (IConstants.NULL_VALUE.equalsIgnoreCase(trim)) {
					isNull = true;
				} else
					selectedValues.add(trim);
			}
			hql = generateInClause(hql, txt, selectedValues, isNull, false);
		}
		return hql;
	}

	private String generateQueryFromDate(String hql, int row, DateListBox nb,
			String join) {
		DateBox tb = (DateBox) filterContentTable.getWidget(row, 3);
		String txt = nb.getItemText(nb.getSelectedIndex());
		Date value = tb.getValue();
		if (EQ.equals(txt)) {
			if (value == null) {
				hql += " is null ";
			} else {
				hql += " = " + IConstants.DT_SEP + DATE_FORMATTER.format(value)
						+ IConstants.DT_SEP + " ";
			}
		} else if (NOT_EQ.equals(txt)) {
			if (join == null) {
				if (value == null) {
					hql += " is not null ";
				} else {
					hql += " <> " + IConstants.DT_SEP
							+ DATE_FORMATTER.format(value) + IConstants.DT_SEP
							+ " ";
				}
			} else {
				hql = hql.substring(0, 12) + "oo" + hql.substring(13);
				hql = " o.id not in (select oo.id from "
						+ table.getClassName()
						+ " oo "
						+ join
						+ " where "
						+ hql
						+ (value == null ? " is null) " : (" = "
								+ IConstants.DT_SEP
								+ DATE_FORMATTER.format(value)
								+ IConstants.DT_SEP + ") "));
			}

		} else if (BETWEEN.equals(txt)) {
			hql += " BETWEEN "
					+ IConstants.DT_SEP
					+ DATE_FORMATTER.format(value)
					+ IConstants.DT_SEP
					+ " AND "
					+ IConstants.DT_SEP
					+ DATE_FORMATTER.format(((DateBox) filterContentTable
							.getWidget(row, 5)).getValue()) + IConstants.DT_SEP
					+ " ";
		} else if (NOT_BETWEEN.equals(txt)) {
			if (join == null) {
				hql += " NOT BETWEEN "
						+ IConstants.DT_SEP
						+ DATE_FORMATTER.format(value)
						+ IConstants.DT_SEP
						+ " AND "
						+ IConstants.DT_SEP
						+ DATE_FORMATTER.format(((DateBox) filterContentTable
								.getWidget(row, 5)).getValue())
						+ IConstants.DT_SEP + " ";
			} else {
				hql = hql.substring(0, 12) + "oo" + hql.substring(13);
				hql = " o.id not in (select oo.id from "
						+ table.getClassName()
						+ " oo "
						+ join
						+ " where "
						+ hql
						+ (value == null ? " is null) " : (" BETWEEN "
								+ IConstants.DT_SEP
								+ DATE_FORMATTER.format(value)
								+ IConstants.DT_SEP
								+ " AND "
								+ IConstants.DT_SEP
								+ DATE_FORMATTER
										.format(((DateBox) filterContentTable
												.getWidget(row, 5)).getValue())
								+ IConstants.DT_SEP + ") "));
			}
		} else {
			hql += txt + " " + IConstants.DT_SEP + DATE_FORMATTER.format(value)
					+ IConstants.DT_SEP + " ";
		}
		return hql;
	}

	public int displayFilterLine(TreeCell treeCell, String attribute) {
		ITreeItem treeItem = treeCell.getTreeItem();
		IFieldInfo fi = treeItem.getFieldInfoByName().get(attribute);
		String l = fi.getLabel();
		if (treeItem.getLabel() != null) {
			l = treeItem.getLabel() + " / " + l;
		}
		int rowCount = filterContentTable.getRowCount();
		if (rowCount > 0) {
			filterContentTable.insertRow(rowCount);
			final ListBox andOrList = new ListBox();
			andOrList.addItem("AND", AND);
			andOrList.addItem("OR", OR);
			filterContentTable.setWidget(rowCount, 0, andOrList);
			rowCount++;
		}
		int row = filterContentTable.insertRow(rowCount);
		DeleteFieldImage i = new DeleteFieldImage(images.smallDelete(),
				treeCell, attribute);
		filterContentTable.setWidget(row, 0, i);
		filterContentTable.setText(row, 1, l);
		filterContentTable.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				if (event.getSource() == filterContentTable) {
					Cell cell = filterContentTable.getCellForEvent(event);
					if (cell != null && cell.getCellIndex() == 0) {
						int rowIdx = cell.getRowIndex();
						Widget w = filterContentTable.getWidget(rowIdx, 0);
						if (w instanceof DeleteFieldImage) {
							TreeCell myCell = ((DeleteFieldImage) w)
									.getTreeCell();
							TreeItem root = (TreeItem) myCell.getTreeItem();
							root.insertItem(0, myCell);
							filterContentTable.removeRow(rowIdx);
							if (filterContentTable.getRowCount() > rowIdx) {
								filterContentTable.removeRow(rowIdx);
							} else if (filterContentTable.getRowCount() >= 2) {
								filterContentTable.removeRow(rowIdx - 1);
							}
						}
					}
				}

			}
		});
		displayLine(attribute, fi, treeItem.getClassName(), row,
				treeItem.isCollection(), treeItem.getAttributePrefix());
		TreeItem root = (TreeItem) treeCell.getTreeItem();
		for (int k = 0; k < root.getChildCount(); k++) {
			TreeItem child = (TreeItem) root.getChild(k);
			if (!(child instanceof ObjectTreeCell)) {
				if (treeCell.getAttribute().equals(
						((TreeCell) child.getWidget()).getAttribute()))
					root.removeItem(child);
			}
		}

		return row;
	}

	private void displayFilter(Filter f) {
		filterContentTable.removeAllRows();
		initFilter();
		for (FilterValue fv : f.getValues()) {
			Iterator<Widget> it = fieldsTree.iterator();
			String att = null;
			TreeCell cell = null;
			while (it.hasNext()) {
				Widget w = it.next();
				if (w instanceof TreeCell) {
					cell = (TreeCell) w;
					att = cell.getAttribute();
					ITreeItem treeItem = cell.getTreeItem();
					if (treeItem instanceof ObjectTreeCell) {
						ObjectTreeCell objectTreeCell = (ObjectTreeCell) treeItem;
						if (!objectTreeCell.isInitialized()) {
							addClassFields(objectTreeCell);
							objectTreeCell.setInit(true);
						}
						att = cell.getTreeItem().getAttributePrefix() + DOT
								+ att;
					}
					if (fv.getAttribute().equals(att)) {
						displayFilter(fv, cell);
					}
				} else if (w instanceof ObjectLabel) {
					ObjectTreeCell objectTreeCell = ((ObjectLabel) w).getItem();
					if (!objectTreeCell.isInitialized()) {
						addClassFields(objectTreeCell);
						objectTreeCell.setInit(true);
					}
					if (objectTreeCell.getChilds() != null
							&& !objectTreeCell.getChilds().isEmpty()) {
						for (TreeCell tc : objectTreeCell.getChilds()) {
							if ((objectTreeCell.getAttributePrefix() + DOT + tc
									.getAttribute()).equals(fv.getAttribute())) {
								displayFilter(fv, tc);
								break;
							}
						}
					}
				}
			}
		}

		if (f.getSortAttribute() != null) {
			ListBox lb = (ListBox) sortByTable.getWidget(0, 1);
			ListBox wayLb = (ListBox) sortByTable.getWidget(0, 2);
			if (f.getSortAsc()) {
				wayLb.setSelectedIndex(0);
			} else {
				wayLb.setSelectedIndex(1);
			}
			table.clearSortColumns(null);
			for (int i = 0; i < lb.getItemCount(); i++) {
				String a = lb.getValue(i);
				if (a != null && a.equals(f.getSortAttribute())) {
					lb.setSelectedIndex(i);
					updateSortAttribute(lb, wayLb, i);
					break;
				}
			}
		}
		if (f.getCaseSensitive() != null && f.getCaseSensitive()) {
			caseCheckbox.setValue(true);
		}
	}

	private void displayFilter(FilterValue fv, TreeCell cell) {
		int row = displayFilterLine(cell, cell.getAttribute());
		if (fv.getIdx() > 0) {
			ListBox l = (ListBox) filterContentTable.getWidget(row - 1, 0);
			for (int i = 0; i < l.getItemCount(); i++) {
				if ((fv.isPreviousAnd() && l.getItemText(i).equals(AND))
						|| (!fv.isPreviousAnd() && l.getItemText(i).equals(OR))) {
					l.setSelectedIndex(i);
					break;
				}
			}
		}
		switch (fv.getType()) {
		case BOOLEAN:
			((CheckBox) filterContentTable.getWidget(row, 2)).setValue(Boolean
					.valueOf(trim(fv.getValue1())));
			break;
		default:
			ListBox l = (ListBox) filterContentTable.getWidget(row, 2);
			for (int i = 0; i < l.getItemCount(); i++) {
				if (fv.getOperator().equals(l.getItemText(i))) {
					l.setSelectedIndex(i);
					NativeEvent event = Document.get().createChangeEvent();
					DomEvent.fireNativeEvent(event, l);
				}
			}
			Widget wid = filterContentTable.getWidget(row, 3);
			if (fv.getType().equals(FilterType.DATE)) {
				((DateBox) wid).setValue(DATE_FORMATTER.parse(trim(fv.getValue1())));
				if (fv.getOperator().equals(BETWEEN)
						|| fv.getOperator().equals(NOT_BETWEEN)) {
					((DateBox) filterContentTable.getWidget(row, 5))
							.setValue(DATE_FORMATTER.parse(fv.getValue2()));
				}
			} else if(wid instanceof SearchListBox){
				SearchListBox slb = (SearchListBox) wid;
				for(int i = 0; i<slb.getItemCount(); i++) {
					if(slb.getValue(i) != null && fv.getValue1() != null 
							&& slb.getValue(i).equals(trim(fv.getValue1()))) {
						slb.setSelectedIndex(i);
						break;
					}
				}
			} else {
				((TextBox) wid).setText(trim(fv.getValue1()));
				if (fv.getOperator().equals(BETWEEN)
						|| fv.getOperator().equals(NOT_BETWEEN)) {
					((TextBox) filterContentTable.getWidget(row, 5)).setText(fv
							.getValue2());
				}
			}
			break;
		}
	}

	private String trim(String s) {
		if(s != null) {
			int i1 = s.indexOf("'");
			int i2 = s.lastIndexOf("'");
			if(i1 >=0 && i2 > i1) {
				return s.substring(i1+1, i2);
			}
		}
		return s;
	}

	public void setNbRowsPerPage(int nbRowsPerPage) {
		this.nbRowsPerPage = nbRowsPerPage;

	}

	private void saveFilter(boolean saveAs) {
		if (isFilterPresent()) {
			final Filter f = new Filter();
			if (!saveAs
					&& filterList != null
					&& filterList.getItemCount() > 1
					&& filterList.getSelectedIndex() > 0
					&& filterList.getValue(filterList.getSelectedIndex()) != null) {
				f.setId(Long.valueOf(filterList.getValue(filterList
						.getSelectedIndex())));
			} else if (saveAs && f.getId() != null)
				f.setId(null);
			f.setClassName(table.getClassName());
			f.setViewName(table.getViewName());
			f.setCaseSensitive(caseCheckbox.getValue() ? true : null);

			for (int row = 0; row < filterContentTable.getRowCount(); row++) {
				Widget widget = filterContentTable.getWidget(row, 0);
				if (widget instanceof DeleteFieldImage) {
					DeleteFieldImage i = (DeleteFieldImage) widget;
					FilterValue fv = new FilterValue();
					fv.setFilter(f);
					f.getValues().add(fv);

					String myAtt = EMPTY;
					ITreeItem parent = (ITreeItem) i.getTreeCell()
							.getTreeItem();
					if (!(parent instanceof RootItem)) {
						myAtt = parent.getAttributePrefix() + DOT;
					}
					myAtt += i.getAttribute();
					fv.setAttribute(myAtt);

					fv.setIdx(row / 2);
					if (row == 0) {
						fv.setPreviousAnd(true);
					} else {
						ListBox lb = (ListBox) filterContentTable.getWidget(
								row - 1, 0);
						fv.setPreviousAnd(AND.equals(lb.getValue(lb
								.getSelectedIndex())) ? true : false);
					}

					widget = filterContentTable.getWidget(row, 2);
					if (widget instanceof CheckBox) {
						CheckBox cb = (CheckBox) widget;
						fv.setType(FilterType.BOOLEAN);
						fv.setValue1(cb.getValue().toString());
					} else if (widget instanceof StringListBox) {
						StringListBox lb = (StringListBox) widget;
						fv.setType(FilterType.STRING);
						String ope = lb.getItemText(lb.getSelectedIndex());
						fv.setOperator(ope);
						Widget widget2 = filterContentTable.getWidget(row, 3);
						if (widget2 instanceof TextBox) {
							TextBox tb1 = (TextBox) widget2;
							fv.setValue1(tb1.getValue());
						} else {
							ListBox lb1 = (ListBox) widget2;
							List<String> values = new ArrayList<String>();
							for (int k = 0; k < lb1.getItemCount(); k++) {
								if (lb1.isItemSelected(k)) {
									values.add(lb1.getValue(k));
								}
							}
							String s = EMPTY;
							int k = 0;
							for (String v : values) {
								s += v;
								if (k < values.size() - 1)
									s += ", ";
								k++;
							}
							fv.setValue1(s);
						}
					} else if (widget instanceof NumberListBox) {
						NumberListBox nb = (NumberListBox) widget;
						fv.setType(FilterType.NUMBER);
						String ope = nb.getItemText(nb.getSelectedIndex());
						fv.setOperator(ope);
						TextBox tb1 = (TextBox) filterContentTable.getWidget(
								row, 3);
						fv.setValue1(tb1.getValue());
						if (BETWEEN.equals(ope) || NOT_BETWEEN.equals(ope)) {
							fv.setValue2(((TextBox) filterContentTable
									.getWidget(row, 5)).getText());
						}
					} else if (widget instanceof DateListBox) {
						DateListBox nb = (DateListBox) widget;
						fv.setType(FilterType.DATE);
						String ope = nb.getItemText(nb.getSelectedIndex());
						fv.setOperator(ope);
						DateBox tb1 = (DateBox) filterContentTable.getWidget(
								row, 3);
						fv.setValue1(DATE_FORMATTER.format(tb1.getValue()));
						if (BETWEEN.equals(ope) || NOT_BETWEEN.equals(ope)) {
							fv.setValue2(DATE_FORMATTER
									.format(((DateBox) filterContentTable
											.getWidget(row, 5)).getValue()));
						}
					}

				}
			}

			String sortAtt = getSortAttribute();
			if (sortAtt != null) {
				ListBox wayLb = (ListBox) sortByTable.getWidget(0, 2);
				f.setSortAttribute(sortAtt);
				f.setSortAsc(wayLb.getValue(wayLb.getSelectedIndex()).equals(
						ASC) ? true : false);
			}

			if (f.getId() == null) {
				final ICloseablePopupPanel cpp = ViewHelper.newPopup(true);
				cpp.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop());
				FlexTable t = new FlexTable();
				cpp.add(t);
				final TextBox tb = new TextBox();
				Image i = new Image(TablePanel.images.validateForm());
				i.setStyleName("custom-button");
				i.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						createFilter(f, cpp, tb);
					}
				});
				t.setText(0, 0, NakedConstants.constants.enterFilterName());
				tb.addKeyUpHandler(new KeyUpHandler() {
					
					@Override
					public void onKeyUp(KeyUpEvent event) {
						switch(event.getNativeKeyCode()) {
						case KeyCodes.KEY_ENTER:
							event.stopPropagation();
							createFilter(f, cpp, tb);
							break;
						default:
							break;
						}
					}
				});
				t.setWidget(1, 0, tb);
				t.setWidget(1, 1, i);
				t.getFlexCellFormatter().setColSpan(0, 0, 2);
				cpp.show();
				tb.setFocus(true);
			} else {
				final String name = filterList.getItemText(filterList
						.getSelectedIndex());
				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Boolean b) {
						if (b != null && b.booleanValue()) {
							f.setName(name);
							saveFilter(f);
						}
					}
				};
				MessageBox.confirm(NakedConstants.messages.replaceFilter(name),
						callback);
			}

		}
		// GWT.log("selected : "+att);

	}

	public void clearFilter() {
		filterContentTable.removeAllRows();
		initFilter();
		if (filterList != null) {
			filterList.getParent().setVisible(false);
			filterList.setSelectedIndex(0);
		}
		panel.setFilterImage(false);
	}

	private void createFilter(final Filter f, final ICloseablePopupPanel cpp,
			final TextBox tb) {
		final String name = tb.getText();

		if (f.getId() != null) {
			AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

				@Override
				public void onFailure(Throwable caught) {
				}

				@Override
				public void onSuccess(Boolean b) {
					if (b != null && b.booleanValue()) {
						saveAndAddFilter(f, name);
					}
				}
			};
			MessageBox.confirm(NakedConstants.messages.replaceFilter(name),
					callback);
		} else {
			saveAndAddFilter(f, name);
		}
		cpp.hide();
	}

	private void initFilterList() {
		final Grid g = new Grid(1, 3);
		g.setText(0, 0, NakedConstants.constants.selectFilter());
		filterList = new ListBox();
		g.setWidget(0, 1, filterList);
		Image img = new Image(images.smallDelete());
		img.setStyleName("custom-button");
		img.setTitle(NakedConstants.constants.removeFilter());
		final PersistenceServiceAsync service = PersistenceService.Util
				.getInstance();
		img.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final int i = filterList.getSelectedIndex();
				if (i >= 0) {
					final String v = filterList.getValue(i);
					if (v != null && v.length() > 0) {
						AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

							@Override
							public void onFailure(Throwable caught) {
								ViewHelper.showException(caught);
							}

							@Override
							public void onSuccess(Boolean result) {
								if (result != null && result) {
									service.removeFilter(Long.valueOf(v),
											new AsyncCallback<Void>() {

												@Override
												public void onFailure(
														Throwable caught) {
													MessageBox.alert(caught
															.getMessage());
												}

												@Override
												public void onSuccess(
														Void result) {
													filterList.removeItem(i);
													clearFilter();
												}
											});
								}
							}
						};
						MessageBox.confirm(
								NakedConstants.messages
										.removeFilterQuestion(filterList
												.getItemText(i)), callback);
					}
				}
			}
		});
		g.setWidget(0, 2, img);
		filterList.addItem(NakedConstants.constants.loadingFilters());
		AsyncCallback<Map<Long, String>> callback = new AsyncCallback<Map<Long, String>>() {

			@Override
			public void onFailure(Throwable caught) {
				ViewHelper.displayError(caught,
						NakedConstants.constants.errorLoadingFilters());
			}

			@Override
			public void onSuccess(Map<Long, String> result) {
				if (result != null && !result.isEmpty()) {
					filterList.clear();
					filterList.addItem(null);
					for (Entry<Long, String> e : result.entrySet()) {
						filterList.addItem(e.getValue(), e.getKey().toString());
					}
					filterList.addChangeHandler(new ChangeHandler() {

						@Override
						public void onChange(ChangeEvent event) {
							String name = filterList.getItemText(filterList
									.getSelectedIndex());
							if (name != null && name.length() > 0) {
								PersistenceServiceAsync service = PersistenceService.Util
										.getInstance();
								AsyncCallback<Filter> callback = new AsyncCallback<Filter>() {

									@Override
									public void onFailure(Throwable caught) {
										ViewHelper.displayError(caught,
												NakedConstants.constants
														.errorGettingFilter());
									}

									@Override
									public void onSuccess(Filter result) {
										displayFilter(result);
									}

								};
								service.getFilter(
										Long.parseLong(filterList
												.getValue(filterList
														.getSelectedIndex())),
										callback);
							} else {
								clearFilter();
							}

						}
					});
					insert(g, 1);
				} else {
					MessageBox.info(NakedConstants.constants.noFilter());
				}

			}

		};
		service.getFilters(table.getViewName(), table.getClassName(), callback);

	}

	private void saveAndAddFilter(final Filter f, final String name) {
		f.setName(name);
		saveFilter(f);

	}

	private String getJpql() {
		String jpql = EMPTY;
		String attClause = EMPTY;
		Map<String, String> allJoins = new TreeMap<String, String>();
		for (int row = 0; row < filterContentTable.getRowCount(); row++) {

			Widget w = filterContentTable.getWidget(row, 0);
			if (w instanceof DeleteFieldImage) {
				attClause = "o.";
				DeleteFieldImage i = (DeleteFieldImage) w;
				String att = EMPTY;
				String join = null;
				ITreeItem parent = (ITreeItem) i.getTreeCell().getTreeItem();
				String joinName = null;
				ITreeItem root = parent;
				List<String> joins = new ArrayList<String>();
				while(root != null && !(root.getParent() instanceof RootItem)) {	
					if(root instanceof ObjectTreeCell && root.isCollection()) {
						joins.add(root.getAttributePrefix());
					}
					root = root.getParent();					
				}
				if(root != null) {
					if(root instanceof ObjectTreeCell && root.isCollection()) {
						joins.add(root.getAttributePrefix());
					}
				}
				if(!joins.isEmpty()) {
					join = " inner join o.";
					for(int k = joins.size() - 1; k>=0; k--) {
						if(k<joins.size() - 1)
							join += "inner join ";
						String jj = joins.get(k);
						String j2 = jj;
						for(Entry<String,String> e : allJoins.entrySet()) {
							if(j2.contains(e.getKey() + ".")) {
								j2 = j2.replace(e.getKey(), e.getValue());
							}
						}
						join += j2 + " ";
						if(!allJoins.containsKey(jj)) {
							joinName = "j"+allJoins.size();
							allJoins.put(jj, joinName);							
						}
						joinName = allJoins.get(jj);
						join += joinName + " ";
						attClause = " " + parent.getAttributePrefix().replace(jj, joinName) + DOT + i.getAttribute();
					}
					
				} else /*
				if(root != null && root instanceof ObjectTreeCell && root.isCollection()) {
					joinName = "j" + j;
					join = " inner join o." + root.getAttributePrefix() + " "
							+ joinName + " ";
					attClause = " j" + j + DOT + i.getAttribute();
					j++;
				}
				if (parent instanceof ObjectTreeCell
						&& ((ObjectTreeCell) parent).isCollection()) {
					if(join == null) {
						join = " inner join o." + parent.getAttributePrefix() + " "
								+ "j" + j + " ";
					} else {
						join = join + " inner join "+joinName + DOT + parent.getAttributePrefix().substring(parent.getAttributePrefix().lastIndexOf(".") + 1) + " "
								+ "j" + j + " ";
					}
					joinName = "j" + j;
					attClause = " j" + j + DOT + i.getAttribute();
					j++;
				} else*/ if (!(parent instanceof RootItem)) {
					att = parent.getAttributePrefix() + DOT;
					att += i.getAttribute();
					attClause += att;
				} else {
					att += i.getAttribute();
					attClause += att;
				}

				w = filterContentTable.getWidget(row, 2);
				if (w instanceof CheckBox) {
					CheckBox cb = (CheckBox) w;
					attClause += cb.getValue() ? " = true " : " = false ";
				} else if (w instanceof StringListBox) {
					StringListBox lb = (StringListBox) w;
					if (!caseCheckbox.getValue()
							&& filterContentTable.getWidget(row, 3) instanceof TextBox) {
						attClause = "UPPER(" + attClause + ")";
					}
					attClause = generateQueryFromString(attClause, row, lb,
							join);
				} else if (w instanceof NumberListBox) {
					NumberListBox nb = (NumberListBox) w;
					attClause = generateQueryFromNumber(attClause, row, nb,
							join);
				} else if (w instanceof DateListBox) {
					DateListBox nb = (DateListBox) w;
					attClause = generateQueryFromDate(attClause, row, nb, join);
				}
				if (join != null) {
					if (!jpql.contains("where"))
						jpql = " where " + jpql;
					jpql = join + jpql;
				}
				jpql += attClause;
			} else {
				ListBox lb = (ListBox) w;
				jpql += BLANK + lb.getItemText(lb.getSelectedIndex()) + BLANK;
			}
		}
		if (table.getConstantFilter() != null) {
			jpql = appendConstantFilter(jpql);
		}
		return jpql;
	}

	public String appendConstantFilter(String jpql) {
		String f = table.getConstantFilter().trim();
		Map<String, String> keyByJoinCons = getKeyByJoin(f);
		Map<String, String> keyByJoinJpql = getKeyByJoin(jpql);
		String order = null;
		int idx = f.indexOf(" order ");
		if (idx >= 0) {
			order = f.substring(idx);
			if (idx > 0)
				f = f.substring(0, idx);
		}
		if(keyByJoinCons != null && !keyByJoinCons.isEmpty()) {
			if(keyByJoinJpql != null && !keyByJoinJpql.isEmpty()) {
				int k = f.indexOf("where ");
				String joinCons, whereCons = null;
				if(k > 0) {
					joinCons = f.substring(0, k);
					whereCons = f.substring(k+6);
				} else {
					joinCons = f;
				}
				StringBuilder sb = new StringBuilder(joinCons);
				for(Entry<String,String> e : keyByJoinJpql.entrySet()) {
					if(!keyByJoinCons.containsKey(e.getKey())) {
						sb.append(" inner join ").append(e.getKey()).append(" ").append(e.getValue()).append(" ");
					}
				}
				
				sb.append(" where ");
				if(whereCons != null)
					sb.append(whereCons);
				k = jpql.indexOf("where ");
				if(k > 0) {
					whereCons = jpql.substring(k+6);
					for(Entry<String,String> e : keyByJoinJpql.entrySet()) {
						if(keyByJoinCons.containsKey(e.getKey())) {
							whereCons = whereCons.replace(e.getValue(), keyByJoinCons.get(e.getKey()));
						}
					}
					sb.append(" and ").append(whereCons);
				}
				if(order != null) {
					sb.append(" ").append(order);
				}
				return sb.toString();		
			} else {
				for(Entry<String,String> e : keyByJoinCons.entrySet()) {
					jpql = jpql.replace(e.getKey(), e.getValue());					
				}
			}
			
		}
		/*if(!keyByJoin.isEmpty()) {
			Map<String, String> keyByJoin2 = new HashMap<String, String>();
			
			for(Entry<String,String> e : keyByJoin.entrySet()) {
				
			}
		}*/
		
		if (f.startsWith(",") || f.startsWith("inner ")
				|| f.startsWith("left ") || f.startsWith("join ")) {
			idx = f.indexOf("where ");
			if (idx > 0) {
				int i = jpql.indexOf("where ");
				if (i >= 0) {
					String q1 = (i > 0) ? jpql.substring(0, i) : null;
					String q2 = jpql.substring(i + 6);
					if (i > 0)
						jpql = f.substring(0, idx - 1) + BLANK + q1 + " where "
								+ q2 + " and " + f.substring(idx + 6);
					else
						jpql = f + " and " + q2;

				} else {
					jpql = f + " and " + jpql;
				}
			}
		} else
			jpql += " and " + f;
		if (order != null)
			jpql += order;
		return jpql;
	}

	private Map<String, String> getKeyByJoin(String f) {
		if(f == null || !f.contains("inner") || !f.contains("join") || !f.contains("left"))
			return null;
		int l = f.indexOf(" where");
		String f2 = f;
		if(l>0) {
			f2 = f.substring(0, l);
		}
		if(f.startsWith(","))
			f = f.substring(1).trim();
		f2 = f2.replaceAll("inner", "");
		f2 = f2.replaceAll("join", "");
		f2 = f2.replaceAll("left", "");
		String[] joins = f2.split(" ");
		if(joins != null && joins.length > 0) {
			List<String> ll = new ArrayList<String>(joins.length);
			for(String s : joins) {
				if(s != null && s.trim().length() > 0) {
					ll.add(s);
				}				
			}
			joins = ll.toArray(new String[0]);
			if(joins.length > 0) {
				Map<String, String> keyByJoin = new HashMap<String, String>();
				for(int k = 0; k<joins.length; k++) {
					String join = joins[k].trim();
					if(!join.isEmpty() && join.contains(".") && k < (join.length() - 1) && !join.contains("(")) {
						//jpql = jpql.replaceAll(join, joins[k+1]);
						keyByJoin.put(join, joins[k+1]);
					}
				}
				return keyByJoin;
			}
		}
		return null;
	}

	public int getMyFilterRow(Object source, int col) {
		int row = 0;
		for (int i = 0; i < filterContentTable.getRowCount(); i++) {
			if (filterContentTable.getCellCount(i) > col
					&& source == filterContentTable.getWidget(i, col)) {
				row = i;
				break;
			}
		}
		return row;
	}

	public boolean isFilterPresent() {
		return filterContentTable != null
				&& filterContentTable.getRowCount() > 0;
	}

	public void doApplyFilter(final WrappedFlexTable table, int pageNb,
			int nbRows) {
		if (isFilterPresent()) {
			applyFilter(table, pageNb, nbRows);
		} else {
			table.refresh(pageNb, nbRows);
			panel.setFilterImage(false);
		}
	}

	public void applyFilter(final WrappedFlexTable table, int pageNb, int nbRows) {
		table.filter(getJpql(), pageNb, nbRows, caseCheckbox.getValue());
		panel.setFilterImage(true);
	}

	protected String escapeQuotes(String key) {
		if(key != null)
			return key.replaceAll("'", "''");
		return null;
	}

	public Tree getFieldsTree() {
		return fieldsTree;
	}

	
}
