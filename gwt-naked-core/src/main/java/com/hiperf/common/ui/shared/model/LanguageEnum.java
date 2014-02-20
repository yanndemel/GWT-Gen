package com.hiperf.common.ui.shared.model;

import com.hiperf.common.ui.client.i18n.NakedConstants;

public enum LanguageEnum {
	
	EN {
		@Override
		public String toString() {
			return NakedConstants.constants.english();
		}

	}, FR {
		@Override
		public String toString() {
			return NakedConstants.constants.french();
		}

	};
	
}
