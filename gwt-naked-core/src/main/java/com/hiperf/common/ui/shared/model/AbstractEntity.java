package com.hiperf.common.ui.shared.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hiperf.common.ui.client.IAuditable;

@EntityListeners(AbstractEntityListener.class)
@MappedSuperclass
public abstract class AbstractEntity implements IAuditable {

	@Column(name="CREATE_USER")
    protected String createUser;

    @Column(name="CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date createDate;

    @Column(name="MODIFY_USER")
    protected String modifyUser;

    @Column(name="MODIFY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modifyDate;

	@Override
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Override
	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	
}
