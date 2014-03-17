package org.gwtgen.coherence.sequence;

import com.hiperf.common.ui.server.util.sequence.IdGenerator;
import com.tangosol.net.CacheFactory;

public class SequenceGenerator implements IdGenerator<Long> {

	/**
	 * Default sequence block size.
	 */
	private static final int DEFAULT_BLOCK_SIZE = 20;

	private String cacheName;
	
	private String sequenceClassName;

	/**
	 * Sequence name.
	 */
	private String name;

	/**
	 * Sequence block size.
	 */
	private int blockSize;

	/**
	 * Currently allocated block of sequences.
	 */
	private SequenceBlock allocatedSequences;


	/**
	 * Construct sequence generator.
	 * 
	 * @param sequenceName
	 *            a sequence name
	 * @param blockSize
	 *            the size of the sequence block to allocate at once
	 */
	public SequenceGenerator(String sequenceCacheName, String sequenceClassName, String sequenceName, int blockSize) {
		this.cacheName = sequenceCacheName;
		this.name = sequenceName;
		this.blockSize = blockSize;
		this.sequenceClassName = sequenceClassName;
	}

	/**
	 * Return the next number in the sequence.
	 * 
	 * @return the next number in the sequence
	 */
	public synchronized Long generateIdentity() {
		if (allocatedSequences == null || !allocatedSequences.hasNext()) {
			allocatedSequences = allocateSequenceBlock();
		}
		return allocatedSequences.next();
	}

	/**
	 * Allocate a new sequence block.
	 * 
	 * @return block of sequential numbers
	 */
	protected SequenceBlock allocateSequenceBlock() {
		return (SequenceBlock) CacheFactory.getCache(cacheName).invoke(name,
				new SequenceBlockAllocator(sequenceClassName, blockSize));
	}
}
