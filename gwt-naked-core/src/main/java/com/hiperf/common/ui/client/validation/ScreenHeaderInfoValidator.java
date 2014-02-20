package com.hiperf.common.ui.client.validation;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.event.AbstractValidator;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.widget.MessageBox;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.WrapperListValidationResults;
import com.hiperf.common.ui.shared.WrapperValidationResults;
import com.hiperf.common.ui.shared.model.ScreenHeaderInfo;

public class ScreenHeaderInfoValidator extends AbstractValidator {

	@Override
	public void validate(IWrapper w, AsyncCallback<WrapperListValidationResults> tableCallback, 
			AsyncCallback<WrapperValidationResults> formCallback, 
			final WrapperListValidationResults wrapperListResults, 
			final WrapperValidationResults wrapperResults) throws ValidationException {
		ScreenHeaderInfo shi = (ScreenHeaderInfo) w.getContent();
		List<ScreenHeaderInfo> headers = shi.getScreenConfig().getHeaders();
		if(headers != null) {
			int lastDisplayed = -1;
			int i = 0;
			int size = WrapperContext.getFieldInfoByName().get(shi.getScreenConfig().getClassName()).size();
			if(!shi.isDisplayed()) {
				shi.setIndex(size - 1);
			} else if(shi.getIndex() <0 || shi.getIndex() >= size) {
				throw new ValidationException("The index must be between 0 and "+(size-1));
			}
			for(ScreenHeaderInfo h : headers) {
				if(shi.getId() != h.getId()) {
					if(h.getAttribute().equals(shi.getAttribute())) {
						throw new ValidationException("The attribute "+h.getAttribute()+" is already defined in this screen config.\nPlease update the existing element instead of creating a new one.");
					} else if (shi.isDisplayed()) {
						if(h.isDisplayed() && h.getIndex() == shi.getIndex()) {
							MessageBox.alert("The index "+h.getIndex()+" is already mapped by the column "+h.getLabel(NakedConstants.constants.locale())+" (attribute "+h.getAttribute()+")\n Don't forget to change the value of this index after validation.");
						}
						lastDisplayed = i;
					}
				}
				i++;
			}

			if(lastDisplayed > -1 && shi.isDisplayed() && shi.getIndex() > lastDisplayed + 1) {
				MessageBox.alert("The new index is too high : "+shi.getIndex()+". Don't forget to create/update the other indexes.");
			}
		}
		success(w, tableCallback, formCallback, null, wrapperListResults, wrapperResults);
	}

}
