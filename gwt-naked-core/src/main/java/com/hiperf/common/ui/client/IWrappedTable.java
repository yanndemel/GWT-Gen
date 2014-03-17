package com.hiperf.common.ui.client;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.hiperf.common.ui.client.exception.PendingModificationsException;
import com.hiperf.common.ui.client.util.PersistenceResult;
import com.hiperf.common.ui.shared.util.Id;

public interface IWrappedTable extends IsWidget {

	void validate(AsyncCallback<PersistenceResult> callback);

	void validateAndRefresh();

	void removeRows();

	void insertNewRow(IWrapper w);

	void refresh(int page, int rows) throws PendingModificationsException;

	boolean isPersistent();

	boolean isEditable();

	Widget getWidget(int row, int col);

	boolean clearCell(int row, int col);

	void setWidget(int row, int col, Widget w);

	void clearSortColumns(Widget cellToKeep);

	IWrapper findWrapper(INakedObject oldValue);

	int findRow(IWrapper parentWrapper);

	RowFormatter getRowFormatter();

	void redrawRow(int row, Id id);

	IPanelMediator getMediator();

	ColumnFormatter getColumnFormatter();

	CellFormatter getCellFormatter();

	String getViewName();

	String getFormTitle();
}
