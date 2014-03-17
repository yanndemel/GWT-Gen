package org.gwtgen.coherence.sequence;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * An entry processor that allocates a block of sequential number from a named
 * sequence.
 * <p/>
 * If the sequence entry for the given name does not already exist in the cache,
 * it will be created automatically.
 * 
 */
public class SequenceBlockAllocator extends AbstractProcessor implements
		PortableObject {
	
	private static final Logger logger = Logger.getLogger(SequenceBlockAllocator.class.getName());

	/**
	 * The size of the sequence block to allocate.
	 */
	private int blockSize;
	
	/**
	 * The sequence class name to put in sequence cache (null for default class : com.hiperf.common.ui.server.util.sequence.Sequence)
	 * */
	private String sequenceClassName;
	

	// ---- constructors ----------------------------------------------------

	/**
	 * For internal use only.
	 */
	public SequenceBlockAllocator() {
	}

	/**
	 * Construct new sequence block allocator.
	 * 
	 * @param blockSize
	 *            the size of the sequence block to allocate
	 */
	public SequenceBlockAllocator(String sequenceClassName, int blockSize) {
		this.sequenceClassName = sequenceClassName;
		this.blockSize = blockSize;
		
	}

	// ---- EntryProcessor implementation -----------------------------------

	/**
	 * Allocates a block of sequences from a target entry.
	 * <p/>
	 * If the target entry for the given name does not already exist in a cache,
	 * it will be created automatically.
	 * 
	 * @param entry
	 *            target entry to allocate sequence block from
	 * 
	 * @return allocated sequence block
	 */
	public Object process(InvocableMap.Entry entry) {
		try {
			AbstractSequence sequence = entry.isPresent() ? (AbstractSequence) entry.getValue()
					: newSequence((String)entry.getKey());
			SequenceBlock block = sequence.allocateBlock(blockSize);
			entry.setValue(sequence);
			return block;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception while creating Sequence", e);
		}
		return null;
	}

	public void readExternal(PofReader pofReader) throws IOException {
		blockSize = pofReader.readInt(0);
		sequenceClassName = pofReader.readString(1);
	}

	public void writeExternal(PofWriter pofWriter) throws IOException {
		pofWriter.writeInt(0, blockSize);
		pofWriter.writeString(1, sequenceClassName);
	}
	
	private AbstractSequence newSequence(String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if(sequenceClassName == null)
			return new Sequence(name);
		else {
			AbstractSequence seq = (AbstractSequence)(Class.forName(sequenceClassName).newInstance());
			seq.setId(name);
			return seq;
		}
	}


	/**
	 * Test specified object for equality.
	 * 
	 * @param o
	 *            object to test
	 * 
	 * @return <tt>true</tt> if the specified object is equal to this one,
	 *         <tt>false</tt> otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SequenceBlockAllocator that = (SequenceBlockAllocator) o;
		return blockSize == that.blockSize;
	}

	/**
	 * Return a hash code for this object.
	 * 
	 * @return a hash code for this object
	 */
	@Override
	public int hashCode() {
		return blockSize;
	}

	/**
	 * Return string representation of this object.
	 * 
	 * @return string representation of this object
	 */
	@Override
	public String toString() {
		return "SequenceBlockAllocator{" + "blockSize=" + blockSize + '}';
	}
}
