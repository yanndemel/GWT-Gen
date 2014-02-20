package com.hiperf.common.ui.client.validation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IValidator;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperValidator;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.widget.MessageBox;
import com.hiperf.common.ui.shared.CommonUtil;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.WrapperListValidationResults;
import com.hiperf.common.ui.shared.WrapperValidationResults;

public class ValidationHelper {

	public static void validateAttributeValue(IWrapper w, String attribute,
			String value, List<IValidator> validators, AsyncCallback<String> callback) throws ValidationException {
		if(validators != null && !validators.isEmpty()) {
			for(IValidator validator : validators) {
				validateWrapper(w, value, callback, validator);
			}
		} else
			callback.onSuccess(null);
	}

	private static void validateWrapper(IWrapper w, Object value,
			AsyncCallback<String> callback, IValidator validator)
			throws ValidationException {
		if(validator instanceof UniqueValidator)
			((UniqueValidator)validator).validate(w, value, callback);
		else
			validator.validate(value, callback);
	}

	public static void validate(final IWrapper w, final WrapperValidationResults wrapperResults,
			final WrapperListValidationResults wrapperListResults,
			final AsyncCallback<WrapperListValidationResults> tableCallback,
			final AsyncCallback<WrapperValidationResults> formCallback) throws ValidationException {
		final String className = w.getWrappedClassName();
		Map<String, IFieldInfo> map = WrapperContext.getFieldInfoByName().get(className);
		final IWrapperValidator specialValidator = ValidatorFactory.getWrapperValidators().get(className);
		boolean found = false;
		for(final String attribute : map.keySet()) {
			IFieldInfo fieldInfo = map.get(attribute);
			List<IValidator> validators = fieldInfo.getValidators();
			if(validators != null && !validators.isEmpty()) {
				found = true;
				for(IValidator validator : validators) {
					try {
						AsyncCallback<String> callback = new AsyncCallback<String>() {

							@Override
							public void onFailure(Throwable caught) {
								wrapperResults.addValidationError(attribute, caught.getMessage());
								if(wrapperListResults != null)
									wrapperListResults.addWrapperValidationResults(className, wrapperResults);
								if(tableCallback != null)
									tableCallback.onSuccess(wrapperListResults);
								if(formCallback != null)
									formCallback.onSuccess(wrapperResults);
							}

							@Override
							public void onSuccess(String result) {
								if(result != null) {
									wrapperResults.addValidationError(attribute,result);
								} else {
									wrapperResults.incrementValidationCount();
									if(wrapperResults.isValidationDone() && !wrapperResults.hasErrors() && specialValidator != null) {
										try {
											specialValidator.validate(w, tableCallback, formCallback, wrapperListResults, wrapperResults);
											return;
										} catch (ValidationException e) {
											wrapperResults.setMessage(e.getMessage());
										}
									}
								}
								if(tableCallback != null)
									tableCallback.onSuccess(wrapperListResults);
								if(formCallback != null)
									formCallback.onSuccess(wrapperResults);
							}
						};
						if(fieldInfo.isEnum() || fieldInfo.isManyToOne() || fieldInfo.isOneToOne()) {
							validateWrapper(w, w.getNakedAttribute(attribute), callback, validator);
						} else if(fieldInfo.isOneToMany() || fieldInfo.isManyToMany() || fieldInfo.isCollection()) {
							validateWrapper(w, w.getWrappedCollection(attribute, true), callback, validator);
						} else  {
							validateWrapper(w, w.getAttribute(attribute), callback, validator);
						}
					} catch(Exception e) {
						GWT.log("Exception",  e);
						wrapperResults.addValidationError(attribute, e.getMessage());
						if(wrapperListResults != null)
							wrapperListResults.addWrapperValidationResults(className, wrapperResults);
						if(tableCallback != null)
							tableCallback.onSuccess(wrapperListResults);
						if(formCallback != null)
							formCallback.onSuccess(wrapperResults);
					}
				}
			}
		}
		if(!found) {
			if(specialValidator != null) {
				try {
					specialValidator.validate(w, tableCallback, formCallback, wrapperListResults, wrapperResults);
					return;
				} catch (ValidationException e) {
					wrapperResults.setMessage(e.getMessage());
				}
			}
			if(tableCallback != null)
				tableCallback.onSuccess(wrapperListResults);
			if(formCallback != null)
				formCallback.onSuccess(wrapperResults);
		}

	}



	public static void validateForm(final IWrappedObjectForm form, AsyncCallback<WrapperValidationResults> callback) {
		//GWT.log("validateForm");
		if(form.hasErrors()) {
			MessageBox.alert(NakedConstants.constants.errorValidate());
		} else {
			form.clearErrors();
			IWrapper w = form.getWrapper();
			final WrapperValidationResults r = new WrapperValidationResults(w.getWrappedClassName());
			try {
				validate(w, r, null, null, callback);
			} catch (ValidationException e) {
				processValidationErrors(form, e.getErrorsByAttribute(), e.getMessage());
			}
		}

	}

	public static void processValidationErrors(IWrappedObjectForm form,
			Map<String, Set<String>> errorsByAttribute, String message) {
		Map<String, IFieldInfo> map = WrapperContext.getFieldInfoByName().get(form.getWrapper().getWrappedClassName());
		boolean alignLeft = false;
		StringBuilder msg = new StringBuilder(NakedConstants.constants.validationPb());
		if(errorsByAttribute != null && !errorsByAttribute.isEmpty()) {
			if(errorsByAttribute.size() > 1) {
				alignLeft = true;
				StringBuilder sb2 = new StringBuilder(NakedConstants.constants.attributes()).append("<br><ul>");
				boolean ok = false;
				for(String att : errorsByAttribute.keySet()) {
					IFieldInfo fi = map.get(att);
					if(fi.isHidden())
						continue;
					ok = true;
					String m = CommonUtil.collectionToString(errorsByAttribute.get(att));
					form.setValueError(att, m);
					sb2.append("<li>").append(fi.getLabel()).append(" : ").append(m).append("</li>");
				}
				if(ok) {
					msg.append(sb2.toString()).append("</ul><br>").append(NakedConstants.constants.errorValidate());
				}
			}
			else {
				String att = errorsByAttribute.keySet().iterator().next();
				IFieldInfo fi = map.get(att);
				if(!fi.isHidden()) {
					String m = CommonUtil.collectionToString(errorsByAttribute.get(att));
					form.setValueError(att, m);
					msg.append(NakedConstants.constants.attributes()).append(" ").append(fi.getLabel()).append(" : ").append(m).append("<br>").append(NakedConstants.constants.errorValidate());
				}
			}
		} else {
			msg.append(" : ").append(message);
		}
		MessageBox.alert(msg.toString(), true, alignLeft);
	}
	
	public static void processValidationErrors(String className, 
			Map<String, Set<String>> errorsByAttribute, String message) {
		Map<String, IFieldInfo> map = WrapperContext.getFieldInfoByName().get(className);
		boolean alignLeft = false;
		StringBuilder msg = new StringBuilder(NakedConstants.constants.validationPb());
		if(errorsByAttribute != null && !errorsByAttribute.isEmpty()) {
			if(errorsByAttribute.size() > 1) {
				alignLeft = true;
				StringBuilder sb2 = new StringBuilder(NakedConstants.constants.attributes()).append("<br><ul>");
				boolean ok = false;
				for(String att : errorsByAttribute.keySet()) {
					IFieldInfo fi = map.get(att);
					if(fi.isHidden())
						continue;
					ok = true;
					sb2.append("<li>").append(fi.getLabel()).append(" : ").append(CommonUtil.collectionToString(errorsByAttribute.get(att))).append("</li>");
				}
				if(ok) {
					msg.append(sb2.toString()).append("</ul><br>").append(NakedConstants.constants.errorValidate());
				}
			}
			else {
				String att = errorsByAttribute.keySet().iterator().next();
				IFieldInfo fi = map.get(att);
				if(!fi.isHidden())
					msg.append(NakedConstants.constants.attributes()).append(" ").append(fi.getLabel()).append(" : ").append(CommonUtil.collectionToString(errorsByAttribute.get(att))).append("<br>").append(NakedConstants.constants.errorValidate());
			}
		} else {
			msg.append(" : ").append(message);
		}
		MessageBox.alert(msg.toString(), true, alignLeft);
	}

	public static void validateTable(Map<IWrapper, Integer> toValidate,
			AsyncCallback<WrapperListValidationResults> asyncCallback) {
		if(toValidate == null || toValidate.isEmpty()) {
			asyncCallback.onSuccess(null);
			return;
		}

		final WrapperListValidationResults res = new WrapperListValidationResults();
		for(IWrapper w : toValidate.keySet()) {
			final WrapperValidationResults r = new WrapperValidationResults(w.getWrappedClassName());
			res.addEmptyWrapperValidationResults(w.getWrappedClassName(), r);
		}
		for(IWrapper w : toValidate.keySet()) {
			WrapperValidationResults r = res.getValidationErrorsByClassName().get(w.getWrappedClassName());
			try {
				validate(w, r, res, asyncCallback, null);
			} catch (ValidationException t) {
				r.getValidationErrorsByAttribute().putAll(t.getErrorsByAttribute());
				res.addWrapperValidationResults(w.getWrappedClassName(), r);
				asyncCallback.onSuccess(res);
			}
		}



	}

}
