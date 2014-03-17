package com.hiperf.common.ui.server.storage.impl;

import java.util.ArrayList;
import java.util.List;

import org.gwtgen.api.shared.INakedObject;

public class ObjectsToAdd {

	private List<INakedObject> firstList;
	private List<INakedObject> mainList;
	private List<INakedObject> lastList;
	
	public ObjectsToAdd() {
		super();
		firstList = new ArrayList<INakedObject>();
		mainList = new ArrayList<INakedObject>();
		lastList = new ArrayList<INakedObject>();
	}

	public List<INakedObject> getFirstList() {
		return firstList;
	}

	public List<INakedObject> getMainList() {
		return mainList;
	}

	public List<INakedObject> getLastList() {
		return lastList;
	}
	
	public boolean isEmpty() {
		return firstList.isEmpty() && mainList.isEmpty() && lastList.isEmpty();
	}

	public boolean contains(INakedObject no) {
		return firstList.contains(no) || mainList.contains(no) || lastList.contains(no);
	}

	public List<INakedObject> getAllObjects() {		
		ArrayList<INakedObject> l = new ArrayList<INakedObject>();
		l.addAll(firstList);
		l.addAll(mainList);
		l.addAll(lastList);
		return l;
	}
	
}
