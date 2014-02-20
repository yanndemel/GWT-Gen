package com.hiperf.common.ui.shared.util;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.hiperf.common.ui.shared.PersistenceManager;

public class Id implements IsSerializable {

	private List<String> fieldNames;
	private List<Object> fieldValues;

	public Id() {}

	public Id(List<String> idFields, List<Object> list) {
		super();
		this.fieldNames = idFields;
		this.fieldValues = list;
	}

	public Id(List<Object> ids) {
		super();
		this.fieldValues = ids;
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}
	public List<Object> getFieldValues() {
		return fieldValues;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fieldNames == null) ? 0 : fieldNames.hashCode());
		result = prime * result
				+ ((fieldValues == null) ? 0 : fieldValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Id other = (Id) obj;
		if (fieldNames != null && other.fieldNames != null && !fieldNames.equals(other.fieldNames))
			return false;
		if (fieldValues == null) {
			if (other.fieldValues != null)
				return false;
		} else if (!fieldValues.equals(other.fieldValues))
			return false;
		/*if (fieldNames == null) {
			if (other.fieldNames != null)
				return false;
		} else if (!fieldNames.equals(other.fieldNames))
			return false;*/
		return true;
	}

	public boolean equalsExcludeField(Object obj, String att) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Id other = (Id) obj;
		if (fieldNames == null) {
			if (other.fieldNames != null)
				return false;
		} else if (!fieldNames.equals(other.fieldNames))
			return false;
		if (fieldValues == null) {
			if (other.fieldValues != null)
				return false;
		} else {
			int idx = fieldNames.indexOf(att);
			for(int i =0; i< fieldValues.size(); i++) {
				if(i != idx) {
					if((fieldValues.get(i) == null && other.fieldValues.get(i) != null) || (fieldValues.get(i) != null && other.fieldValues.get(i) == null) || (fieldValues.get(i) != null && other.fieldValues.get(i) != null && !fieldValues.get(i).equals(other.fieldValues.get(i))))
						return false;
				}
			}
			return true;
		}
		return true;
	}

	public boolean isLocal() {
		for(Object o : fieldValues) {
			if(PersistenceManager.isLocal(o))
				return true;
		}
		return false;
	}

	
	public boolean equalsExcludeLocalFields(Id id) {
		int i = 0;
		try {
			for(Object o : fieldValues) {
				if((o instanceof Long && ((Long)o).longValue() > 0 && ((Long)id.getFieldValues().get(id.getFieldNames().indexOf(fieldNames.get(i)))).longValue() !=  ((Long)o).longValue())
						|| (o instanceof String && !((String)o).startsWith(PersistenceManager.SEQ_PREFIX)) && !((String)o).equals((String)id.getFieldValues().get(id.getFieldNames().indexOf(fieldNames.get(i))))) {
					return false;
				}
				i++;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void clearFieldNames() {
		fieldNames = null;
	}

	@Override
	public String toString() {
		return "Id ="+(fieldValues!=null&&fieldValues.size() == 1?fieldValues.get(0):fieldValues);
	}

	public String toStringValue() {
		if(fieldValues != null && fieldValues.size() > 0) {
			StringBuilder sb = new StringBuilder();
			Iterator<Object> it = fieldValues.iterator();
			int i = 0;
			boolean hasNames = fieldNames != null && fieldNames.size() > 0;
			while(it.hasNext()) {
				if(hasNames)
					sb.append(fieldNames.get(i)).append(':').append(it.next().toString());
				else
					sb.append(it.next().toString());
				if(it.hasNext())
					sb.append(',');
				i++;
			}
			return sb.toString();
		}
		return null;
	}
	
	
	
}
