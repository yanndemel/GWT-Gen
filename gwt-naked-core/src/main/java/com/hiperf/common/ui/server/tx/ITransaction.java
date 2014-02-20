package com.hiperf.common.ui.server.tx;


public interface ITransaction {
	void begin() throws TransactionException;
	void commit() throws TransactionException;
	void rollback() throws TransactionException;
	
}
