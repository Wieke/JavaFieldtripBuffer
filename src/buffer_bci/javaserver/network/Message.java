package buffer_bci.javaserver.network;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Message {
	public final short version;
	public final short type;
	public final ByteBuffer buffer;
	public final ByteOrder order;

	public Message(short version, short type, ByteBuffer buffer, ByteOrder order) {
		this.version = version;
		this.type = type;
		this.buffer = buffer;
		this.order = order;
	}

	@Override
	public String toString() {
		return "(Version " + Short.toString(version) + ", Type "
				+ Short.toString(type) + ", Size "
				+ Integer.toString(buffer.capacity()) + ")";
	}
}
