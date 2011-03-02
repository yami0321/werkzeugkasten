package org.handwerkszeug.dns.record;

import java.net.InetAddress;

import org.handwerkszeug.dns.NameCompressor;
import org.handwerkszeug.dns.RRType;
import org.handwerkszeug.dns.ResourceRecord;
import org.handwerkszeug.util.AddressUtil;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 3.4.1. A RDATA format
 * 
 * @author taichi
 */
public class ARecord extends AbstractRecord {

	/**
	 * A 32 bit Internet address.
	 */
	protected long address;

	public ARecord() {
		super(RRType.A);
	}

	public ARecord(ARecord from) {
		super(from);
		this.address = from.address;
	}

	@Override
	protected void parseRDATA(ChannelBuffer buffer) {
		this.address = buffer.readUnsignedInt();
	}

	@Override
	protected void writeRDATA(ChannelBuffer buffer, NameCompressor compressor) {
		buffer.writeInt((int) this.address);
	}

	@Override
	protected ResourceRecord copy() {
		return new ARecord(this);
	}

	public InetAddress address() {
		return AddressUtil.getByAddress(this.address);
	}

	public void address(InetAddress v4address) {
		this.address = AddressUtil.toLong(v4address);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (this.address ^ (this.address >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ARecord other = (ARecord) obj;
		if (this.address != other.address) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append(super.toString());
		stb.append(' ');
		stb.append(this.address().getHostAddress());
		return stb.toString();
	}
}
