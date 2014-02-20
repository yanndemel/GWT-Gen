package com.hiperf.common.ui.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperValidator;
import com.hiperf.common.ui.client.widget.ViewHelper;
import com.hiperf.common.ui.shared.WrapperListValidationResults;
import com.hiperf.common.ui.shared.WrapperValidationResults;

public abstract class AbstractValidator implements IWrapperValidator {

	protected AsyncCallback<WrapperListValidationResults> onValidate(
			final IWrapper w,
			final AsyncCallback<WrapperListValidationResults> tableCallback,
			final AsyncCallback<WrapperValidationResults> formCallback,
			final WrapperListValidationResults wrapperListResults, 
			final WrapperValidationResults wrapperResults) {
		return new AsyncCallback<WrapperListValidationResults>() {

			@Override
			public void onFailure(Throwable caught) {
				ViewHelper.hideWaitPopup();
				error(tableCallback, formCallback, caught);
			}

			@Override
			public void onSuccess(WrapperListValidationResults result) {
				ViewHelper.hideWaitPopup();
				success(w, tableCallback, formCallback, result, wrapperListResults, wrapperResults);
			}
		};
	}

	protected void success(final IWrapper w,
			final AsyncCallback<WrapperListValidationResults> tableCallback,
			final AsyncCallback<WrapperValidationResults> formCallback,
			final WrapperListValidationResults result, 
			final WrapperListValidationResults wrapperListResults, 
			final WrapperValidationResults wrapperResults) {
		if(tableCallback != null) {
			if(result != null && wrapperListResults != null) {
				wrapperListResults.merge(result);
				tableCallback.onSuccess(result);
			} else if(result == null && wrapperListResults != null)
				tableCallback.onSuccess(wrapperListResults);
			else 
				tableCallback.onSuccess(result);
			
		}
		if(formCallback != null) {
			if(result != null && wrapperResults != null) {
				WrapperValidationResults res = result.getValidationErrorsByClassName().get(w.getWrappedClassName());
				if(res != null)
					wrapperResults.merge(res);
				formCallback.onSuccess(wrapperResults);
			} else if(result == null && wrapperResults != null)
				formCallback.onSuccess(wrapperResults);
			else 
				formCallback.onSuccess(result == null ? null : result.getValidationErrorsByClassName().get(w.getWrappedClassName()));
		}
	}

	protected void error(
			final AsyncCallback<WrapperListValidationResults> tableCallback,
			final AsyncCallback<WrapperValidationResults> formCallback,
			Throwable caught) {
		if(tableCallback != null) {
			tableCallback.onFailure(caught);
		}
		if(formCallback != null) {
			formCallback.onFailure(caught);
		}
	}

}
