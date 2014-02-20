package com.hiperf.common.ui.server.tx;

import javax.transaction.UserTransaction;

public class JtaTransaction implements ITransaction {

	private UserTransaction tx;
	
	public JtaTransaction(UserTransaction tx) {
		this.tx = tx;
	}

	@Override
	public void begin() throws TransactionException {
		try {
			tx.begin();
		} catch (Exception e) {
			throw new TransactionException(e);
		}
	}

	@Override
	public void commit() throws TransactionException {
		try {
			tx.commit();
		} catch (Exception e) {
			throw new TransactionException(e);
		}

	}

	@Override
	public void rollback() throws TransactionException {
		try {
			tx.rollback();
		} catch (Exception e) {
			throw new TransactionException(e);
		}

	}

}
