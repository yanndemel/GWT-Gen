package org.gwtgen.coherence.sequence;

import com.hiperf.common.ui.server.util.sequence.IdGenerator;
import com.tangosol.util.UUID;

public class UuidGenerator implements IdGenerator<String> {

	public static final UuidGenerator INSTANCE = new UuidGenerator(); 
	
	public String generateIdentity() {
		return new UUID().toString();
	}

}
