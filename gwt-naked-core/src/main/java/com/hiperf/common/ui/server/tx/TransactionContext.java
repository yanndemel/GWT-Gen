package com.hiperf.common.ui.server.tx;

import javax.persistence.EntityManager;

public class TransactionContext {
	private ITransaction tx;
	private EntityManager em;
	
	public TransactionContext(ITransaction tx, EntityManager em) {
		super();
		this.tx = tx;
		this.em = em;
	}
	public ITransaction getTx() {
		return tx;
	}
	public EntityManager getEm() {
		return em;
	}
	
	public void close() {
		if(em.isOpen())
			em.close();
	}
	
	public void rollback() throws TransactionException {
		tx.rollback();
	}
	
}
