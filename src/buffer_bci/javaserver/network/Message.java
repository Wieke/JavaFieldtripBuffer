package buffer_bci.javaserver.network;

import java.nio.ByteBuffer;

public class Message {
	public short version;
	public short type;
	public ByteBuffer buffer;
	
	public Message(short version, short type, ByteBuffer buffer){
		this.version = version;
		this.type = type;
		this.buffer = buffer;
	}
	
	@Override
	public String toString() {
	    return "(Version " + Short.toString(version) 
	    		+ ", Type " + Short.toString(type) 
	    		+ ", Size " + Integer.toString(buffer.capacity())
	    		+ ")";
	} 
}
