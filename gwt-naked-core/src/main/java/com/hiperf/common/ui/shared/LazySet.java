package com.hiperf.common.ui.shared;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hiperf.common.ui.client.AbstractLazyCollection;
import com.hiperf.common.ui.client.INakedObject;

public class LazySet<T extends INakedObject> extends AbstractLazyCollection<T> implements Set {

	private static final long serialVersionUID = 7086465372460881612L;

	public LazySet() {
		super();
	}

	public LazySet(int originalSize, String description) {
		super(originalSize, description);
	}

	@Override
	public Collection<T> newCollection() {
		return new HashSet<T>();
	}


}
