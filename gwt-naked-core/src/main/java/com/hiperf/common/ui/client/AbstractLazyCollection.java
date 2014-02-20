package com.hiperf.common.ui.client;

import java.util.Collection;
import java.util.Iterator;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class AbstractLazyCollection<T> implements ILazy, Collection, IsSerializable {

	protected int originalSize;
	protected String description;
	protected Collection<T> newItems;
	protected Collection<T> removedItems;
	protected boolean initialized;



	public AbstractLazyCollection() {
		super();
		this.originalSize = 0;
		this.description = null;
		this.newItems = null;
		this.removedItems = null;
		this.initialized = false;
	}


	public AbstractLazyCollection(int originalSize, String description) {
		super();
		this.originalSize = originalSize;
		this.description = description;
		this.newItems = null;
		this.removedItems = null;
		this.initialized = true;
	}


	@Override
	public boolean add(Object e) {
		if(newItems == null)
			newItems = newCollection();
		return newItems.add((T)e);
	}


	public abstract Collection<T> newCollection();

	@Override
	public boolean addAll(Collection c) {
		if(newItems == null)
			newItems = newCollection();
		return newItems.addAll(c);
	}

	@Override
	public void clear() {
		if(newItems != null)
			newItems.clear();
		if(removedItems != null)
			removedItems.clear();
	}

	@Override
	public boolean contains(Object o) {
		if(newItems != null)
			return newItems.contains(o);
		return false;
	}

	@Override
	public boolean containsAll(Collection c) {
		if(newItems != null)
			return newItems.containsAll(c);
		return false;
	}

	@Override
	public boolean isEmpty() {
		if(newItems != null)
			return newItems.isEmpty();
		return true;
	}

	@Override
	public Iterator iterator() {
		if(newItems != null)
			return newItems.iterator();
		return EMPTY_IT;
	}

	@Override
	public boolean remove(Object o) {
		if(newItems != null) {
			if(!newItems.remove(o)) {
				addRemoved(o);
			}
		} else {
			addRemoved(o);
		}
		return true;
	}

	public void addRemoved(Object o) {
		if(removedItems == null)
			removedItems = newCollection();
		removedItems.add((T)o);
	}

	@Override
	public boolean removeAll(Collection c) {
		if(newItems != null) {
			if(!newItems.removeAll(c)) {
				addAllRemoved(c);
			}
		} else {
			addAllRemoved(c);
		}
		return true;
	}

	public void addAllRemoved(Collection c) {
		if(removedItems == null)
			removedItems = newCollection();
		removedItems.addAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
		if(newItems != null)
			return newItems.retainAll(c);
		return false;
	}

	@Override
	public int size() {
		int s = originalSize;
		if(removedItems != null)
			s -= removedItems.size();
		if(newItems != null)
			s += newItems.size();
		return s;
	}

	public Object[] toArray() {
		return null;
	}

	public  Object[] toArray(Object[] a) {
		return null;
	}


	@Override
	public String toString() {
		if(description == null && newItems != null && newItems.size() == 1)
			return newItems.iterator().next().toString();
		return description;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}


	@Override
	public void setInitialized(boolean b) {
		initialized = b;
	}


	@Override
	public void setOriginalSize(int originalSize) {
		this.originalSize = originalSize;
	}


	@Override
	public void setDescription(String description) {
		this.description = description;
	}



}
