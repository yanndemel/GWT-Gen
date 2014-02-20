package com.hiperf.common.ui.client.format;

import java.util.ArrayList;
import java.util.Collection;

import com.hiperf.common.ui.client.IFieldInfo;

public class FormatHelper {

	public static Collection<String> formatCollection(IFieldInfo info, Collection c) {
        if(c!=null) {
          Collection<String> l = new ArrayList<String>(c.size());
          for(Object o : c) {
            l.add(info.getFormatter().format(o));
          }
          return l;
        }
        return null;
	}
	
}
