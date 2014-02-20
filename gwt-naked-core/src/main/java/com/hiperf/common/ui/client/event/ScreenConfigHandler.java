package com.hiperf.common.ui.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.exception.UpdateException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.widget.TablePanel;
import com.hiperf.common.ui.shared.model.ScreenConfig;
import com.hiperf.common.ui.shared.model.ScreenHeaderInfo;

public class ScreenConfigHandler extends AbstractUpdateHandler<ScreenConfig> {


	@Override
	public void beforeSet(INakedObject objToAdd, ScreenConfig target, String viewName, String attribute, AsyncCallback<UpdateResult> callback)
			throws UpdateException {
		if(objToAdd instanceof ScreenConfig) {
			ScreenConfig sc = (ScreenConfig) objToAdd;
			sc.setNbRows(TablePanel.DEFAULT_ROW_NB);
			/*sc.setCreateLabel(NakedConstants.constants.newForm());
			sc.setEditLabel(NakedConstants.constants.editElement());
			sc.setSelectLabel(NakedConstants.constants.select());
			sc.setViewLabel(NakedConstants.constants.viewElement());*/
		}
		else if(objToAdd instanceof ScreenHeaderInfo) {
			throw new UpdateException(NakedConstants.constants.errorCannotAddHeader());
			/*if(target.getClassName() == null || target.getClassName().trim().length() == 0)
				throw new UpdateException(NakedConstants.constants.errorScreenClassName());
			List<ScreenHeaderInfo> headers = target.getHeaders();
			if(headers != null && headers.size() == WrapperContext.getFieldInfoByName().get(target.getClassName()).size()) {
				throw new UpdateException(NakedConstants.constants.errorScreenHeaderBounds());
			}*/
		}
		super.beforeSet(objToAdd, target, viewName, attribute, callback);
	}

	@Override
	public void beforeDelete(String attribute, ScreenConfig target,
			AsyncCallback<String> callback) throws UpdateException {
		if(attribute!=null)
			throw new UpdateException(NakedConstants.constants.errorCannotDeleteHeader());
	}

	@Override
	public boolean mustRedraw(WrapperUpdatedEvent event, ScreenConfig target) {
		// TODO Auto-generated method stub
		return super.mustRedraw(event, target);
	}





}
