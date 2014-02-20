package com.hiperf.common.ui.server.tx;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;


public class LocalTransaction implements ITransaction {

	private EntityTransaction tx;

	public LocalTransaction(EntityTransaction tx) {
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
			if(e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
				ConstraintViolationException cve = (ConstraintViolationException)e.getCause();
				Set<String> msgs = new HashSet<String>();
				for(ConstraintViolation cv : cve.getConstraintViolations()) {
					StringBuilder sb = new StringBuilder();
					sb.append(cv.getPropertyPath());
					sb.append(" ");
					sb.append(cv.getMessage());
					msgs.add(sb.toString());
				}
				int i=0;
				StringBuilder sb = new StringBuilder();
				for(String s : msgs) {
					sb.append(s);
					i++;
					if(i<msgs.size())
						sb.append("\n");
				}
				throw new TransactionException(sb.toString(), e);
			} else
				throw new TransactionException(getMessage(e), e);
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
	
	private static String getMessage(Throwable e) {
		StringBuilder msg = new StringBuilder();
		int i = 0;
		if(e.getMessage() != null) {
			msg.append(e.getMessage());
		}
		while((e = e.getCause()) != null && e.getMessage() != null && i < 5) {
			msg.append("\n");
			msg.append(e.getMessage());
			i++;
		}
		return msg.toString();
	}

}
