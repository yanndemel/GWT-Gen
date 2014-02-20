package com.hiperf.common.ui.client;

import java.util.Collection;

import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.exception.ParseException;

/**
 * All wrapped objects implements this interface. For each object implementing {@link com.hiperf.common.ui.client.INakedObject},
 * the generator configured in the file MyModule.gwt.xml will generate the wrapper
 * <p>
 * <b>example of MyModule.gwt.xml :</b><br>
 * {@code <inherits name="com.hiperf.common.ui.Naked"/>}<br>
 * {@code ...} <br>
 * {@code <generate-with class="com.hiperf.common.rebind.WrapperGenerator">}<br>
 * {@code      <when-type-assignable class="com.hiperf.common.ui.client.INakedObject"/>}<br>
 * {@code </generate-with>}
 * */
public interface IWrapper {
	void setContent(INakedObject content);
	INakedObject getContent();
	void setLazy(boolean lazy);
	boolean isLazy();
	Object getNakedAttribute(String attr) throws AttributeNotFoundException;
	String getAttribute(String attr) throws AttributeNotFoundException;
	void setObjectAttribute(String attr, Object value) throws AttributeNotFoundException;
	void setObjectAttribute(String attr, Object value, boolean fireEvents) throws AttributeNotFoundException;
	void setAttribute(String attr, String value) throws ParseException, AttributeNotFoundException;
	void setAttribute(String attr, String value, boolean fireEvents) throws ParseException, AttributeNotFoundException;
	void setNakedObjectAttribute(String attribute, INakedObject result) throws AttributeNotFoundException;
	void setNakedObjectAttribute(String attribute, INakedObject result, boolean fireEvents) throws AttributeNotFoundException;
	Collection getCollection(String attr) throws AttributeNotFoundException;
	IWrapperListModel getWrappedCollection(String attr, boolean persistent) throws AttributeNotFoundException;
	Object addToCollection(String attr, String value) throws AttributeNotFoundException, ParseException;
	void addObjectToCollection(String attr, INakedObject o) throws AttributeNotFoundException;
	void addObjectToCollection(String attr, INakedObject o, boolean redrawLinkedCollectionCells) throws AttributeNotFoundException;
	void removeFromCollection(String attr, String value) throws AttributeNotFoundException, ParseException;
	void removeObjectFromCollection(String attr, INakedObject object) throws AttributeNotFoundException;
	void removeAllObjectsFromCollection(String attr) throws AttributeNotFoundException;
	IWrapper getWrappedAttribute(String attr) throws AttributeNotFoundException;
	INakedObject newNakedObject();
	IWrapper newWrapper();
	String getWrappedClassName();
	String getWrappedCollectionClassName(String attr) throws AttributeNotFoundException;
	void setEnumAttribute(String attribute, String value) throws AttributeNotFoundException;
	void setEnumAttribute(String attribute, String value, boolean fireEvents) throws AttributeNotFoundException;
	void removeLazyAttribute(String attribute);
	void setLazyTime(String attribute, Long time);
	void setCollection(String attr, Collection c) throws AttributeNotFoundException;
}

