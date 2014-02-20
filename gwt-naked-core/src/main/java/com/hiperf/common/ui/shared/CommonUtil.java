package com.hiperf.common.ui.shared;

import java.util.Collection;
import java.util.Iterator;

import com.hiperf.common.ui.shared.model.LanguageEnum;

public class CommonUtil {

	public static final String SEP = ", ";

	public static LanguageEnum getLanguage(String langCode) {
		try {
			String s = langCode.toLowerCase().substring(0, 2);
			if(s.equals("en"))
				return LanguageEnum.EN;
			if(s.equals("fr"))
				return LanguageEnum.FR;
		} catch (Exception e) {}
		return LanguageEnum.EN;
	}

	public static String collectionToString(Collection collection) {
	    StringBuilder sb = new StringBuilder();
	    Iterator it = collection.iterator();
	    while(it.hasNext()) {
	    	sb.append(it.next());
	    	if(it.hasNext())
	    		sb.append(SEP);
	    }
	    return sb.toString();
	}
	
}
