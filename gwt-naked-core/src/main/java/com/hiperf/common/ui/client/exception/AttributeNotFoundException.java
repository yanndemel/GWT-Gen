package com.hiperf.common.ui.client.exception;

import com.hiperf.common.ui.client.i18n.NakedConstants;

public class AttributeNotFoundException extends RuntimeException {

	public AttributeNotFoundException(String attName) {
		super(NakedConstants.constants.errorAttributeNotFound()+ " : " + attName);
	}

}
