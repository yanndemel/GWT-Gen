package com.hiperf.common.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import com.hiperf.common.ui.client.AbstractLazyCollection;
import com.hiperf.common.ui.client.INakedObject;

public class LazyList<T extends INakedObject> extends AbstractLazyCollection<T> implements List {

	public LazyList() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LazyList(int originalSize, String description) {
		super(originalSize, description);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean addAll(int index, Collection c) {
		if(newItems == null)
			newItems = newCollection();
		return ((List)newItems).addAll(index, c);
	}


	@Override
	public T get(int index) {
		if(newItems == null)
			newItems = newCollection();
		return ((List<T>)newItems).get(index);
	}

	@Override
	public Object set(int index, Object element) {
		if(newItems == null)
			newItems = newCollection();
		return ((List<T>)newItems).set(index, (T)element);
	}

	@Override
	public void add(int index, Object element) {
		if(newItems == null)
			newItems = newCollection();
		((List<T>)newItems).add(index, (T)element);
	}

	@Override
	public Object remove(int index) {
		if(newItems == null)
			return null;
		return ((List<T>)newItems).remove(index);
	}

	@Override
	public int indexOf(Object o) {
		if(newItems == null)
			return -1;
		return ((List<T>)newItems).indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		if(newItems == null)
			return -1;
		return ((List<T>)newItems).lastIndexOf(o);
	}

	public ListIterator listIterator() {
		if(newItems == null)
			return null;
		return null;
	}

	public ListIterator listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> newCollection() {
		return new ArrayList<T>();
	}


}
