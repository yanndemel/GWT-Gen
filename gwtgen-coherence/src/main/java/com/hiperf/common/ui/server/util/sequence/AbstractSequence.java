package com.hiperf.common.ui.server.util.sequence;

import java.io.IOException;
import java.io.Serializable;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class AbstractSequence implements Serializable, PortableObject {
	
	protected String id;
	
	protected long last;
	
	public AbstractSequence() {
	}

	public AbstractSequence(String id) {
		super();
		this.id = id;
	}

	/**
	 * Allocate a block of sequence numbers, starting from the last allocated
	 * sequence value.
	 * 
	 * @param blockSize
	 *            the number of sequences to allocate
	 * 
	 * @return allocated block of sequential numbers
	 */
	public SequenceBlock allocateBlock(int blockSize) {
		SequenceBlock block = new SequenceBlock(last + 1, last + blockSize);
		last += blockSize;
		return block;
	}

	/**
	 * Return the last allocated sequence number.
	 * 
	 * @return the last allocated sequence number
	 */
	public long peek() {
		return last;
	}

	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getLast() {
		return last;
	}

	public void setLast(long last) {
		this.last = last;
	}

	public void readExternal(PofReader pofReader) throws IOException {
		id = pofReader.readString(0);
		last = pofReader.readLong(1);
	}

	public void writeExternal(PofWriter pofWriter) throws IOException {
		pofWriter.writeString(0, id);
		pofWriter.writeLong(1, last);
	}
}
