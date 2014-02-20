package com.hiperf.common.ui.shared.model;

import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;



public class AbstractEntityListener {

	@PrePersist
	void onPrePersist(Object o) {
		AbstractEntity e = (AbstractEntity)o;
		Date d = new Date();
		e.setCreateDate(d);
		e.setModifyDate(d);
	}



	@PreUpdate
	void onPreUpdate(AbstractEntity o) {
		AbstractEntity e = (AbstractEntity)o;
		e.setModifyDate(new Date());
	}
}
