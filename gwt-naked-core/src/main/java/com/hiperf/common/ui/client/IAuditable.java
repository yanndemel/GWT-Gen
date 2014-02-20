package com.hiperf.common.ui.client;

import java.util.Date;

/*
 * Implement this interface if you want your POJO to maintain an audit log info (at each insert / update)
 * */
public interface IAuditable extends INakedObject {

	void setCreateDate(Date date);

	void setModifyDate(Date date);

	void setModifyUser(String modifyUser);

	void setCreateUser(String createUser);

}
