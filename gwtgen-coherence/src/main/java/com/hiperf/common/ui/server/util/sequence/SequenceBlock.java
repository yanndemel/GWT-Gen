package com.hiperf.common.ui.server.util.sequence;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
 
public class SequenceBlock implements Serializable, PortableObject {

	/**
	 * The next assignable number within this sequence block.
	 */
	private AtomicLong next;

	/**
	 * The last assignable number within this sequence block.
	 */
	private long last;

	public SequenceBlock() {
	}

	/**
	 * @param l    last number in a sequence
	 */
	public SequenceBlock(long first, long l) {
		next = new AtomicLong(first);
		last = l;
	}

	/**
	 * Return <tt>true</tt> if there are avialable numbers in this sequence
	 * block, <tt>false</tt> otherwise.
	 * 
	 * @return <tt>true</tt> if there are avialable numbers in this sequence
	 *         block, <tt>false</tt> otherwise
	 */
	public boolean hasNext() {
		return next.longValue() <= last;
	}

	/**
	 * Return the next available number in this sequence block.
	 * 
	 * @return the next available number in this sequence block
	 */
	public long next() {
		return next.getAndIncrement();
	}

	public void readExternal(PofReader pofReader) throws IOException {
		next = new AtomicLong(pofReader.readLong(0));
		last = pofReader.readLong(1);
	}

	public void writeExternal(PofWriter pofWriter) throws IOException {
		pofWriter.writeLong(0, next.longValue());
		pofWriter.writeLong(1, last);
	}
}
