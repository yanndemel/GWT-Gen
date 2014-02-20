package com.hiperf.common.ui.server.util.sequence;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;

@Entity
@Table(name = "HIP_SEQ")
public class Sequence extends AbstractSequence {
	

	public Sequence() {}

	public Sequence(String id) {
		super();
		this.id = id;
	}

	@Id
	@Column(name="ID", nullable = false)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name="LAST")
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
