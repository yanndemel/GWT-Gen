package com.hiperf.common.ui.client.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.ICommitHandler;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.event.HideLoadingPopupEvent;
import com.hiperf.common.ui.client.event.UpdateResult;
import com.hiperf.common.ui.client.event.WrapperSelectedEvent;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.util.PersistenceResult;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;


/**
 * Panel used to display a WrappedFlexTable<br>
 * the topMenuBar holds the buttons (see {@link #getTopMenuBar()}<br>
 * the tableTiltle ,holds the title of the table
 * */
public class TablePanel extends SimplePanel implements PanelResizeListener {

	public static class TopMenuInfo {
		public boolean refreshTopBtn;
		public boolean addTopBtn;
		public boolean deleteTopBtn;
		public boolean configureTopBtn;
		public boolean filterTopBtn;
		public boolean excelTopBtn;
		public boolean saveTopBtn;
		public boolean pendingModifsTopBtn;
		public int removeMenuIdx;
		public int insertMenuIdx;
		public int refreshMenuIdx;
		public int configureMenuIdx;
		public int excelMenuIdx;
		public int filterMenuIdx;
		public int basketMenuIdx;

		public TopMenuInfo(boolean refreshTopBtn, boolean addTopBtn,
				boolean deleteTopBtn, boolean configureTopBtn,
				boolean filterTopBtn, boolean excelTopBtn, boolean saveTopBtn,
				boolean pendingModifsTopBtn, int removeMenuIdx,
				int insertMenuIdx, int refreshMenuIdx, int configureMenuIdx,
				int excelMenuIdx, int filterMenuIdx, int basketMenuIdx) {
			this.refreshTopBtn = refreshTopBtn;
			this.addTopBtn = addTopBtn;
			this.deleteTopBtn = deleteTopBtn;
			this.configureTopBtn = configureTopBtn;
			this.filterTopBtn = filterTopBtn;
			this.excelTopBtn = excelTopBtn;
			this.saveTopBtn = saveTopBtn;
			this.pendingModifsTopBtn = pendingModifsTopBtn;
			this.removeMenuIdx = removeMenuIdx;
			this.insertMenuIdx = insertMenuIdx;
			this.refreshMenuIdx = refreshMenuIdx;
			this.configureMenuIdx = configureMenuIdx;
			this.excelMenuIdx = excelMenuIdx;
			this.filterMenuIdx = filterMenuIdx;
			this.basketMenuIdx = basketMenuIdx;
		}
	}

	public static enum TYPE{ROOT, SELECT, VIEW_CHILDREN, MULTIPLE_SELECT};

	public static int DEFAULT_ROW_NB = 20;

	interface Images extends ClientBundle {
		ImageResource refresh();
		ImageResource add();
		ImageResource delete();
		ImageResource validateForm();
		ImageResource configure();
		ImageResource prevPageArrow();
		ImageResource nextPageArrow();
		ImageResource filter();
		ImageResource filter_edit();
		ImageResource pending();
		ImageResource excel();
	}

	protected static final Images images = GWT.create(Images.class);

	public static class PendingModifsImage extends Image {

		private static final int SLEEP = 500;

		private Timer timer;

		private PendingModifsImage(ImageResource resource) {
			super(resource);
			this.timer = new Timer() {

				@Override
				public void run() {
					if(isVisible())
						setVisible(false);
					else
						setVisible(true);
				}};
		}

		public void show() {
			this.timer.scheduleRepeating(SLEEP);
		}

		public void hide() {
			this.timer.cancel();
			setVisible(false);
		}

	}

	private String tableTitle;
	protected Grid tablePanel;

	private FlexTable topMenuBar;
	private SearchPanel searchPanel;

	protected DecoratorPanel decoPanel;
	protected WrappedFlexTable table;
	private TablePanelMediator mediator;

	private PendingModificationsPanel pendingModificationsPanel;
	private PendingModifsImage pendingModifsImage;
	private CheckBox selectAllCheckbox;
	private WrappedTableConfigForm config = null;
	private Widget excel = null;
	private PagingPanel pagingPanel = null;
	protected TYPE type;

	protected TopMenuInfo topMenuInfo = new TopMenuInfo(false, false, false,
			true, true, true, false, false, -1, -1, -1,
			-1, -1, -1, -1);

	private ClickHandler pendingClickHandler = null;

	protected boolean showTopMenu;
	protected boolean topMenu = true;

	private long lastRefresh = 0;

	private Image filterImage;

	public class PagingPanel extends Grid {

		private static final String SLASH = "/";
		private int currentPage;
		private int totalPages;
		private TextBox tb;

		public PagingPanel() {
			super(1, 5);
			super.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_MIDDLE);
			tb = new TextBox();
			this.currentPage = 1;

			tb.setText("1");
			tb.setWidth("30px");
			tb.addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {

					try {
						int nb = Integer.parseInt(event.getValue());
						if(nb > 0 && nb <= totalPages) {
							currentPage = nb;
							refresh(currentPage);
						} else {
							tb.setText(Integer.toString(currentPage));
						}
					} catch (Exception e) {
						tb.setText(Integer.toString(currentPage));
					}

				}
			});
			Image i = new Image(images.prevPageArrow());
			i.setStyleName("custom-button");
			i.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					if(currentPage > 1) {
						currentPage--;
						tb.setText(Integer.toString(currentPage));
						refresh(currentPage);
					}
				}
			});
			setWidget(0, 0, i);
			setWidget(0, 1, tb);
			setText(0, 2, SLASH);
			i = new Image(images.nextPageArrow());
			i.setStyleName("custom-button");
			i.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					if(currentPage<totalPages) {
						currentPage++;
						tb.setText(Integer.toString(currentPage));
						refresh(currentPage);
					}

				}
			});
			setWidget(0, 4, i);

		}

		public int getCurrentPage() {
			return currentPage;
		}

		public void setCurrentPage(int currentPage) {
			this.currentPage = currentPage;
			tb.setText(Integer.toString(currentPage));
		}

		public void setTotalPages(int n) {
			totalPages = n;
			setText(0, 3, Integer.toString(n));
		}

		public int getTotalPages() {
			return totalPages;
		}



	}


	public TYPE getType() {
		return type;
	}

	public void initTablePanel(final WrappedFlexTable table,
			final TYPE type, final AbstractLinkedCell linkedCell,
			Panel topMenuPanel) {
		this.type = type;
		if(table != null) {
			initMenu(table, type);
			initPanel(table, type, linkedCell, topMenuPanel);
		}
	}

	public TablePanel() {
		super();
	}

	public void initPanel(final WrappedFlexTable table, final TYPE type,
			final AbstractLinkedCell linkedCell, Panel topMenuPanel ) {
		this.type = type;
		if(table.isPersistent()) {
			this.pagingPanel = new PagingPanel();
			if(topMenuPanel == null)
				this.tablePanel = new Grid(3, 1);
			else
				this.tablePanel = new Grid(2, 1);
		} else
			this.tablePanel = new Grid(2, 1);
		add(tablePanel);
		this.table = table;
		if(topMenuInfo.configureTopBtn)
			this.config = new WrappedTableConfigForm(table);
		if(topMenuInfo.excelTopBtn) {
			if(type.equals(TYPE.ROOT) && topMenuInfo.addTopBtn && WrapperContext.getClassInfoByName().get(table.getClassName()).isImportable())
				this.excel = new ExcelMainForm(this, null);
			else
				this.excel = new ExcelExportForm(table, 0);
		}
		if(topMenuInfo.pendingModifsTopBtn) {
			pendingModifsImage = initBasketImage();
			this.pendingModificationsPanel = new PendingModificationsPanel(pendingModifsImage);
		}
		if(topMenu)
			initTopMenu();
		int firstRow = 0;
		if(!type.equals(TYPE.ROOT)) {
			String lbl = WrapperContext.getTitle(table.getViewName(), linkedCell);
			if(lbl == null)
				lbl = WrapperContext.getTableLabel(table.getClassName(), table.getViewName());
			setTableTitle(lbl);
			/*this.tableTitle = new Label(lbl);
			this.tablePanel.setWidget(0, 0, tableTitle);
			this.tablePanel.getCellFormatter().setStyleName(0, 0, "form-table-title");
			firstRow = 1;*/
		}
		if(topMenu) {
			if(topMenuPanel == null) {
				showTopMenu = true;
				this.tablePanel.setWidget(firstRow, 0, topMenuBar);
				this.tablePanel.getCellFormatter().setHorizontalAlignment(firstRow, 0, HasHorizontalAlignment.ALIGN_LEFT);
			} else {
				showTopMenu = false;
				topMenuPanel.clear();
				topMenuPanel.add(topMenuBar);
				firstRow = -1;
			}			
		}
		addMainTable();
		this.tablePanel.getCellFormatter().setHorizontalAlignment(firstRow+1, 0, HasHorizontalAlignment.ALIGN_LEFT);
		if(pagingPanel != null) {
			this.tablePanel.setWidget(firstRow+2, 0, pagingPanel);
			this.tablePanel.getCellFormatter().setHorizontalAlignment(firstRow+2, 0, HasHorizontalAlignment.ALIGN_CENTER);
		}
		if(linkedCell != null && (type.equals(TYPE.SELECT) || type.equals(TYPE.MULTIPLE_SELECT))) {
			table.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					Cell cell = table.getCellForEvent(event);
					if(cell != null) {
						int row = cell.getRowIndex();
						WrapperContext.getEventBus().fireEventFromSource(new WrapperSelectedEvent(table, row), linkedCell);
						if(type.equals(TYPE.SELECT)) {
							if(linkedCell != null)
								linkedCell.getMediator().hidePopup(false);
							else
								mediator.hidePopup(false);
						}
					}
				}
			});
			DOM.setStyleAttribute(this.table.getElement(), "cursor", "pointer");
		}
		this.mediator = new TablePanelMediator(this);
		this.table.setMediator(mediator);
		this.table.fillTable();
		this.table.displayHeader();
		this.table.getResizeHandler().initResizeHandler();
	}

	public static PendingModifsImage initBasketImage() {
		PendingModifsImage i = new PendingModifsImage(images.pending());
		i.setStyleName("custom-button");
		i.setTitle(NakedConstants.constants.viewPendingModifs());
		i.setVisible(false);
		return i;
	}

	private void initMenu(final WrappedFlexTable table, final TYPE type) {
		if(type.equals(TYPE.ROOT)) {
			if(table.isPersistent()) {
				topMenuInfo.refreshTopBtn = true;
			}
			if(table.isEditable()) {
				topMenuInfo.addTopBtn = true;
				topMenuInfo.deleteTopBtn = true;
				if(table.isPersistent()) {
					topMenuInfo.saveTopBtn = true;
					topMenuInfo.pendingModifsTopBtn = true;
				} else {
					topMenuInfo.configureTopBtn = false;
					topMenuInfo.filterTopBtn = false;
					topMenuInfo.excelTopBtn = false;
				}
			}
		}
	}

	public void addMainTable() {
		if(type.equals(TYPE.ROOT)) {
			tablePanel.setWidget(showTopMenu ? 1 : 0, 0, table);
			table.setStyleName("naked-dialogBox");
			table.addStyleName("root-table");
		} else {
			decoPanel = null;
			table.setStyleName("child-custom-table");
			this.tablePanel.setWidget(showTopMenu ? 1 : 0, 0, table);
		}
	}

	public void removeMainTable() {
		if(type.equals(TYPE.ROOT)) {
			decoPanel.remove(table);
		} else {
			this.tablePanel.remove(table);
		}
	}

	public void setMainTable() {
		if(type.equals(TYPE.ROOT)) {
			decoPanel.remove(table);
		} else {
			this.tablePanel.remove(table);
		}
	}

	private void initTopMenu() {
		topMenuBar = new FlexTable();
		topMenuBar.insertRow(0);
		DOM.setStyleAttribute(topMenuBar.getElement(), "borderCollapse", "collapse");
		doInitTopMenu();

	}

	private void doInitTopMenu() {
		int col = 0;
		if(table.isEditable() && topMenuInfo.deleteTopBtn)
			col = addSelectAllCheckBox(col);
		if(topMenuInfo.refreshTopBtn)
			col = addRefreshMenu(col);
		if(topMenuInfo.addTopBtn)
			col = addInsertMenu(col);
		if(topMenuInfo.deleteTopBtn)
			col = addRemoveMenu(col);
		if(topMenuInfo.configureTopBtn)
			col = addConfigureMenu(col);
		if(topMenuInfo.filterTopBtn)
			col = addFilterMenu(col);
		if(topMenuInfo.excelTopBtn)
			col = addExcelMenu(col);
		if(topMenuInfo.saveTopBtn)
			col = addValidateMenu(col);
		if(topMenuInfo.pendingModifsTopBtn)
			col = addBasketMenu(col);
	}

	public void reInitTopMenu() {
		topMenuBar.clear();
		doInitTopMenu();
	}

	public void removeSelectAllCheckBox() {
		if(topMenuBar != null && selectAllCheckbox != null) {
			for(int col = 0; col < topMenuBar.getCellCount(0); col++) {
				if(selectAllCheckbox == topMenuBar.getWidget(0, col)) {
					topMenuBar.removeCell(0, col);
					indexMenus(col);
				}
			}
		}
	}

	private void indexMenus(int col) {
		if(topMenuInfo.removeMenuIdx >= col)
			topMenuInfo.removeMenuIdx--;
		if(topMenuInfo.insertMenuIdx >= col)
			topMenuInfo.insertMenuIdx--;
		if(topMenuInfo.refreshMenuIdx >= col)
			topMenuInfo.refreshMenuIdx--;
		if(topMenuInfo.configureMenuIdx >= col)
			topMenuInfo.configureMenuIdx--;
		if(topMenuInfo.excelMenuIdx >= col)
			topMenuInfo.excelMenuIdx--;
		if(topMenuInfo.filterMenuIdx >= col)
			topMenuInfo.filterMenuIdx--;
		if(topMenuInfo.basketMenuIdx >= col)
			topMenuInfo.basketMenuIdx--;
	}

	private int addSelectAllCheckBox(int col) {
		selectAllCheckbox = new CheckBox();
		selectAllCheckbox.setStyleName("custom-button");
		selectAllCheckbox.setTitle(NakedConstants.constants.selectAll());
		selectAllCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				table.selectAll(event.getValue());
			}
		});
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, selectAllCheckbox);
		topMenuBar.getFlexCellFormatter().setAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		return col + 1;

	}

	protected int addValidateMenu(int col) {
		Image i = newValidateImage();
		i.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				ICommitHandler cm = WrapperContext.getCommitMethod(table.getClassName());
				if(cm == null) {
					table.validate(new AsyncCallback<PersistenceResult>() {

						@Override
						public void onFailure(Throwable caught) {}

						
						@Override
						public void onSuccess(PersistenceResult result) {
							afterValidate(result);
							if ((table.getItems() == null || table.getItems()
									.size() == 0)
									&& ((searchPanel != null && searchPanel
											.isFilterPresent()) || (pagingPanel != null && pagingPanel
											.getTotalPages() > 1))) {
								if (searchPanel != null)
									searchPanel.clearFilter();
								refreshTable(1, config.getNbRowsPerPage());
							}
						}
						});
					if(selectAllCheckbox != null) {
						selectAllCheckbox.setValue(false);
					}
				} else {
					cm.commit(table, new AsyncCallback<Boolean>() {

						@Override
						public void onFailure(Throwable caught) {}

						@Override
						public void onSuccess(Boolean result) {
							if(result != null && result)
								afterValidate(null);
							
						}
					});
				}
				
			}

			private void afterValidate(PersistenceResult result) {
				IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(table.getClassName()).getUpdateHandler();
				if(updateHandler != null) {
					updateHandler.afterValidate(TablePanel.this, result);
				}
			}
		});
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, i);
		topMenuBar.getFlexCellFormatter().setAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		return col + 1;
	}

	public static Image newValidateImage() {
		Image i = new Image(images.validateForm());
		i.setStyleName("custom-button");
		i.setTitle(NakedConstants.constants.saveAll());
		return i;
	}

	public void removeDeleteMenu() {
		if(topMenuBar != null && topMenuInfo.removeMenuIdx >= 0) {
			topMenuBar.removeCell(0, topMenuInfo.removeMenuIdx);
			indexMenus(topMenuInfo.removeMenuIdx);
			topMenuInfo.removeMenuIdx = -1;
		}
	}

	private int addRemoveMenu(int col) {
		Image i = new Image(images.delete());
		i.setStyleName("custom-button");
		i.setTitle(NakedConstants.messages.deleteRow(2));
		i.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				table.removeRows();
			}
		});
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, i);
		topMenuBar.getFlexCellFormatter().setAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		topMenuInfo.removeMenuIdx = col;
		return col + 1;
	}

	public void removeInsertMenu() {
		if(topMenuBar != null && topMenuInfo.insertMenuIdx >= 0) {
			topMenuBar.removeCell(0, topMenuInfo.insertMenuIdx);
			indexMenus(topMenuInfo.insertMenuIdx);
			topMenuInfo.insertMenuIdx = -1;
		}
	}

	private int addInsertMenu(final int col) {
		Image i = new Image(images.add());
		i.setStyleName("custom-button");
		i.setTitle(NakedConstants.constants.newRecord());
		i.addClickHandler(onTopMenuAddClick(col));
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, i);
		topMenuBar.getFlexCellFormatter().setAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		topMenuInfo.insertMenuIdx = col;
		return col + 1;
	}

	protected ClickHandler onTopMenuAddClick(final int col) {
		return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				doAddTopPanelClick(col);
			}

			
		};
	}

	private void displayForm(String className, IWrapper newWrapper,
			INakedObject content, int stateIdx) {
		WrappedObjectsRepository wor  = WrappedObjectsRepository.getInstance();
		if(stateIdx == -2)
			stateIdx = wor.saveState();
		wor.addInsertedObject(content);
		ICustomForm f = WrapperContext.getNewForm(className);
		if(f != null) {
			if(f instanceof WrappedObjectForm) {
				WrappedObjectForm wof = (WrappedObjectForm) f.newInstance(newWrapper, table, true);
				wof.setParentViewName(table.getViewName());
				addFormToPanel(className, stateIdx, wof);		
			} else {
				f.newInstance(newWrapper, table, true).show();
			}
		} else {
			WrappedObjectForm form = new WrappedObjectForm(newWrapper, true, table.isPersistent(), table.getViewName());
			addFormToPanel(className, stateIdx, form);					
		}
	}
	
	public void removeRefreshMenu() {
		if(topMenuBar != null && topMenuInfo.refreshMenuIdx >= 0) {
			topMenuBar.removeCell(0, topMenuInfo.refreshMenuIdx);
			indexMenus(topMenuInfo.refreshMenuIdx);
			topMenuInfo.refreshMenuIdx = -1;
		}
	}

	private int addRefreshMenu(int col) {
		Image i = new Image(images.refresh());
		i.setStyleName("custom-button");
		i.setTitle(NakedConstants.constants.refreshTable());
		i.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
				if(wor.hasToCommit()) {
					AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

						@Override
						public void onFailure(Throwable caught) {}

						@Override
						public void onSuccess(Boolean b) {
							if(b!=null) {
								if(b.booleanValue()) {
									wor.saveAll(new AsyncCallback<Void>() {

										@Override
										public void onFailure(Throwable caught) {
											ViewHelper.displayListError(table.getRows(), caught);
										}

										@Override
										public void onSuccess(
												Void result) {
											refreshTable(pagingPanel.getCurrentPage(), config.getNbRowsPerPage());
										}
									});
								} else {
									wor.clear();
									refreshTable(pagingPanel.getCurrentPage(), config.getNbRowsPerPage());
								}
							} else {
								WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), table.getItems());
							}
						}
					};
					PendingChangesDialogBox.getInstance().show(callback);
				} else {
					refreshTable(pagingPanel.getCurrentPage(), config.getNbRowsPerPage());
				}
				if(selectAllCheckbox != null) {
					selectAllCheckbox.setValue(false);
				}
			}
		});
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, i);
		topMenuBar.getFlexCellFormatter().setAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		topMenuInfo.refreshMenuIdx = col;
		return col + 1;
	}

	public void removeConfigureMenu() {
		if(topMenuBar != null && topMenuInfo.configureMenuIdx >= 0) {
			topMenuBar.removeCell(0, topMenuInfo.configureMenuIdx);
			indexMenus(topMenuInfo.configureMenuIdx);
			topMenuInfo.configureMenuIdx = -1;
		}
	}

	private int addConfigureMenu(int col) {
		final Image i = new Image(images.configure());
		i.setStyleName("custom-button");
		i.setTitle(NakedConstants.constants.configureTable());
		i.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				ICloseablePopupPanel panel = ViewHelper.newPopup(true);
				config = new WrappedTableConfigForm(table, config.getNbRowsPerPage(), i);
				panel.add(config);
				mediator.showPopupPanel(config, i);
			}
		});
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, i);
		topMenuBar.getFlexCellFormatter().setAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		topMenuInfo.configureMenuIdx = col;
		return col + 1;
	}

	public void removeExcelMenu() {
		if(topMenuBar != null && topMenuInfo.excelMenuIdx >= 0) {
			topMenuBar.removeCell(0, topMenuInfo.excelMenuIdx);
			indexMenus(topMenuInfo.excelMenuIdx);
			topMenuInfo.excelMenuIdx = -1;
		}
	}

	private int addExcelMenu(int col) {
		final Image i = new Image(images.excel());
		i.setStyleName("custom-button");
		if(type.equals(TYPE.ROOT) && topMenuInfo.addTopBtn)
			i.setTitle(NakedConstants.constants.excelImportExport());
		else
			i.setTitle(NakedConstants.constants.excelExport());
		i.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				ICloseablePopupPanel panel = ViewHelper.newPopup(true);
				if(type.equals(TYPE.ROOT) && topMenuInfo.addTopBtn && WrapperContext.getClassInfoByName().get(table.getClassName()).isImportable()) {
					excel = new ExcelMainForm(TablePanel.this, panel);
					panel.setPopupTitle(NakedConstants.constants.excelImportExport());
				}
				else {
					excel = new ExcelExportForm(table, i.getAbsoluteTop());
					panel.setPopupTitle(NakedConstants.constants.excelExport());
				}
				panel.add(excel);
				panel.setPopupPosition(i.getAbsoluteLeft(), i.getAbsoluteTop());				
				panel.show();
			}
		});
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, i);
		topMenuBar.getFlexCellFormatter().setAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		topMenuInfo.excelMenuIdx = col;
		return col + 1;
	}

	public void removeFilterMenu() {
		if(topMenuBar != null && topMenuInfo.filterMenuIdx >= 0) {
			topMenuBar.removeCell(0, topMenuInfo.filterMenuIdx);
			indexMenus(topMenuInfo.filterMenuIdx);
			topMenuInfo.filterMenuIdx = -1;
		}
	}

	private int addFilterMenu(int col) {
		final Image i = initFilterImage(false);
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, i);
		topMenuBar.getFlexCellFormatter().setAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		topMenuInfo.filterMenuIdx = col;
		return col + 1;
	}

	private Image initFilterImage(boolean edit) {
		filterImage = new Image(edit ? images.filter_edit() : images.filter());
		filterImage.setStyleName("custom-button");
		filterImage.setTitle(NakedConstants.constants.filterData());
		filterImage.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				showFilterPanel(event);
			}
		});
		return filterImage;
	}

	public void setFilterImage(boolean edit) {
		topMenuBar.clearCell(0, topMenuInfo.filterMenuIdx);
		topMenuBar.setWidget(0, topMenuInfo.filterMenuIdx, initFilterImage(edit));
	}
	
	public void removeBasketMenu() {
		if(topMenuBar != null && topMenuInfo.basketMenuIdx >= 0) {
			topMenuBar.removeCell(0, topMenuInfo.basketMenuIdx);
			indexMenus(topMenuInfo.basketMenuIdx);
			topMenuInfo.basketMenuIdx = -1;
		}
	}

	private int addBasketMenu(int col) {
		List<TablePanel> l = new ArrayList<TablePanel>(1);
		l.add(this);
		if(pendingClickHandler == null ) {
			pendingClickHandler  = newPendingModifsClickHandler(pendingModificationsPanel, pendingModifsImage, l);
			pendingModifsImage.addClickHandler(pendingClickHandler);
		}
		topMenuBar.insertCell(0, col);
		topMenuBar.setWidget(0, col, pendingModifsImage);
		topMenuBar.getFlexCellFormatter().setAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		topMenuInfo.basketMenuIdx = col;
		return col + 1;
	}

	public static ClickHandler newPendingModifsClickHandler(final PendingModificationsPanel modifsPanel, final Image pendingModifsImage, final Collection<TablePanel> panelsToRefresh) {
		return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				final ICloseablePopupPanel panel = ViewHelper.newPopup(true);
				modifsPanel.redraw();
				panel.add(modifsPanel);
				panel.setPopupPosition(pendingModifsImage.getAbsoluteLeft(), pendingModifsImage.getAbsoluteTop());
				panel.addCloseHandler(new CloseHandler<ICloseablePopupPanel>() {

					@Override
					public void onClose(CloseEvent<ICloseablePopupPanel> event) {
						final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
						wor.setPauseModifListener(false);
						wor.setPauseFireEvents(false);
						if(modifsPanel.isModified()) {
							if(!modifsPanel.isClearAll()) {
								AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

									@Override
									public void onFailure(Throwable caught) {}

									@Override
									public void onSuccess(Boolean b) {
										if(b!=null && b.booleanValue()) {
											for(TablePanel tp : panelsToRefresh)
												tp.refreshTable(wor);
										}
									}
								};
								MessageBox.confirm(NakedConstants.constants.keepModif(), callback);
							} else {
								wor.clear();
								for(TablePanel tp : panelsToRefresh)
									tp.refreshTable(wor);
							}
						}
						panel.hide();
					}
				});
				panel.show();
			}
		};
	}

	public int getNbRowsPerPage() {
		return config.getNbRowsPerPage();
	}
	
	public void setNbRowsPerPage(int nb) {
		config.setNbRowsPerPage(nb);
	}

	public int getCurrentPage() {
		return pagingPanel.getCurrentPage();
	}

	public int getTotalPages() {
		return pagingPanel.getTotalPages();
	}


	public FlexTable getTopMenuBar() {
		return topMenuBar;
	}

	public PendingModifsImage getPendingModifsImage() {
		return pendingModifsImage;
	}

	public String getTableTitle() {
		return tableTitle;
	}

	public void setTableTitle(String tableTitle) {
		this.tableTitle = tableTitle;
	}

	public WrappedFlexTable getTable() {
		return table;
	}

	public PagingPanel getPagingPanel() {
		return pagingPanel;
	}

	public TablePanelMediator getMediator() {
		return mediator;
	}

	private void refresh(int currentPage) {
		if(searchPanel != null)
			searchPanel.doApplyFilter(table, currentPage, config.getNbRowsPerPage());
		else
			refreshTable(currentPage, config.getNbRowsPerPage());
	}

	public void refreshTable(final WrappedObjectsRepository wor) {
		boolean refresh = pendingModificationsPanel.isClearAll();
		if(!refresh) {
			wor.clear(false);
			wor.getInsertedObjects().addAll(pendingModificationsPanel.getInsertedObjects());
			wor.getUpdatedObjects().putAll(pendingModificationsPanel.getUpdatedObjects());
			wor.getRemovedObjectsIdsByClassName().putAll(pendingModificationsPanel.getRemovedObjects());
			wor.getManyToManyAddedByClassName().putAll(pendingModificationsPanel.getManyToManyAdded());
			wor.getManyToManyRemovedByClassName().putAll(pendingModificationsPanel.getManyToManyRemoved());
			if(!wor.hasToCommit()) {
				refresh = true;
				PersistenceManager.reinitSequence();
			}
		}
		if(!refresh) {
			table.validateAndRefresh();
			pendingModifsImage.hide();
		}
		else {
			if(wor.hasToCommit()) {
				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {}

					@Override
					public void onSuccess(Boolean b) {
						if(b!=null) {
							if(b.booleanValue()) {
								wor.saveAll(new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										ViewHelper.displayListError(table.getRows(), caught);
									}

									@Override
									public void onSuccess(
											Void result) {
										refresh();
									}
								});
							} else {
								wor.clear();
								refresh();
							}
						} else {
							WrapperContext.getEventBus().fireEventFromSource(new HideLoadingPopupEvent(), table.getItems());
						}
					}
				};
				PendingChangesDialogBox.getInstance().show(callback);
			} else {
				refreshTable(1, getNbRowsPerPage());
			}
		}
	}

	public CheckBox getSelectAllCheckbox() {
		return selectAllCheckbox;
	}



	public DecoratorPanel getDecoratorPanel() {
		return decoPanel;
	}

	public void setTable(WrappedFlexTable table) {
		this.table = table;
	}

	public PendingModificationsPanel getPendingModificationsPanel() {
		return pendingModificationsPanel;
	}

	public void setPendingModifsImage(PendingModifsImage pendingModifsImage) {
		this.pendingModifsImage = pendingModifsImage;
	}

	public void setPendingModificationsPanel(
			PendingModificationsPanel pendingModificationsPanel) {
		this.pendingModificationsPanel = pendingModificationsPanel;
	}

	@Override
	public void onResized(Integer width, Integer height) {
		setWidth("100%");
		setHeight("100%");
	}

	public Grid getPanel() {
		return tablePanel;
	}

	public void refresh() {
		if(pendingModifsImage != null)
			pendingModifsImage.hide();
		refreshTable(1, getNbRowsPerPage());
		
	}

	private void showFilterPanel(ClickEvent event) {
		event.stopPropagation();
		showSearchPanel();
		
	}

	public ICloseablePopupPanel showSearchPanel() {
		final ICloseablePopupPanel panel = ViewHelper.newPopup(true);
		if(searchPanel == null)
			searchPanel = new SearchPanel(this);
		panel.add(searchPanel);
		searchPanel.setNbRowsPerPage(getNbRowsPerPage());
		searchPanel.initSortByTable();
		panel.setPopupPosition(filterImage.getAbsoluteLeft(), filterImage.getAbsoluteTop());
		panel.setPopupTitle(NakedConstants.constants.filterData());
		panel.show();
		return panel;
	}

	private void refreshTable(int curPage, int nbPages) {
		table.refresh(curPage, nbPages);
		lastRefresh  = new Date().getTime();
	}

	public long getLastRefresh() {
		return lastRefresh;
	}

	public SearchPanel getSearchPanel() {
		return searchPanel;
	}

	public void setTopMenu(boolean topMenu) {
		this.topMenu = topMenu;
	}

	protected void addFormToPanel(String className, int stateIdx,
			WrappedObjectForm form) {
		form.setFormTitle(WrapperContext.getNewFormLabel(className, table.getViewName()));
		ObjectPanel panel = new ObjectPanel(form, stateIdx, false);
		mediator.showPopupPanel(panel, table);
		mediator.addValidateButton(panel, -1);
	}

	public TopMenuInfo getTopMenuInfo() {
		return topMenuInfo;
	}

	public void doAddTopPanelClick(final int col) {
		final String className = table.getClassName();
		IWrapper w = WrapperContext.getEmptyWrappersMap().get(className);
		final IWrapper newWrapper = w.newWrapper();
		final INakedObject content = w.newNakedObject();
		newWrapper.setContent(content);
		if(table.isPersistent()) {
			Map<String, IFieldInfo> idInfos = PersistenceManager.getIdInfos(className);
			if(idInfos.size() == 1) {
				String att = idInfos.keySet().iterator().next();
				IFieldInfo fi = idInfos.get(att);
				if(fi.isGeneratedId()) {
					switch(fi.getDataType()) {
					case LONG:
						try {
							newWrapper.setAttribute(att, PersistenceManager.nextLongId().toString());
						} catch (Exception e) {
							e.printStackTrace();
							MessageBox.alert(NakedConstants.constants.errorNewRecord()+e.getMessage());
							topMenuBar.removeCell(0, col);
						}
						break;
					case STRING:
						try {
							newWrapper.setAttribute(att, PersistenceManager.nextStringId());
						} catch (Exception e) {
							e.printStackTrace();
							MessageBox.alert(NakedConstants.constants.errorNewRecord()+e.getMessage());
							topMenuBar.removeCell(0, col);
						}
						break;
					default:
						MessageBox.alert(NakedConstants.constants.errorIdType());
						topMenuBar.removeCell(0, col);
						break;
					}
				}
			}
		}
		IUpdateHandler updateHandler = WrapperContext.getClassInfoByName().get(className).getUpdateHandler();
		if(updateHandler != null) {
			try {
				updateHandler.beforeSet(newWrapper.getContent(), newWrapper.getContent(), table.getViewName(), null, new AsyncCallback<UpdateResult>() {

					@Override
					public void onFailure(Throwable caught) {
						ViewHelper.showException(caught);
					}

					@Override
					public void onSuccess(UpdateResult result) {
						if(result == null)
							displayForm(className, newWrapper, content, -2);
						else if(result.getMessage() == null) {
							displayForm(className, newWrapper, content, result.getBeforeState());
						} else
							MessageBox.alert(result.getMessage());
					}
				});
			} catch (UpdateException e) {
				ViewHelper.showException(e);
				return;
			}
		} else {
			displayForm(className, newWrapper, content, -2);
		}
	}


	

}
