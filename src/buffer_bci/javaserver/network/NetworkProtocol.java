package buffer_bci.javaserver.network;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * An impementation of the fieldtrip realtime network protocol. Provides a number of abstract methods that
 * can be used to decode/unwrap incoming communication.
 * @author Wieke Kanters
 *
 */
public class NetworkProtocol {
	public static final short VERSION = 1;
	public static final short GET_HDR = 0x201;
	public static final short GET_DAT = 0x202;
	public static final short GET_EVT = 0x203;
	public static final short GET_OK  = 0x204;
	public static final short GET_ERR = 0x205;

	public static final short PUT_HDR = 0x101;
	public static final short PUT_DAT = 0x102;
	public static final short PUT_EVT = 0x103;
	public static final short PUT_OK  = 0x104;
	public static final short PUT_ERR = 0x105;

	public static final short FLUSH_HDR = 0x301;
	public static final short FLUSH_DAT = 0x302;
	public static final short FLUSH_EVT = 0x303;
	public static final short FLUSH_OK  = 0x304;
	public static final short FLUSH_ERR = 0x305;

	public static final short WAIT_DAT = 0x402;
	public static final short WAIT_OK  = 0x404;
	public static final short WAIT_ERR = 0x405;
	
	public static Message readMessage(BufferedInputStream input) throws IOException{
				
		// First we determine the endianness of the stream.
		byte versionByte1 = (byte) input.read();
		byte versionByte2 = (byte) input.read();
		
		ByteOrder order;
		if (versionByte1 < versionByte2){
			order = ByteOrder.BIG_ENDIAN;
		} else {
			order = ByteOrder.LITTLE_ENDIAN;
		}
		
		// Determine message version
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(order);
		buffer.put(versionByte1);
		buffer.put(versionByte2);
		buffer.rewind();
		short version = buffer.getShort();
		
		// Check if version corresponds otherwise throw IOException
		if (version != VERSION){
			throw new IOException("Client/Server version conflict. "
								+ "\nClient Version " + Short.toString(version) 
								+ "\nServer Version " + Short.toString(VERSION));
		}
		
		// Get Message Type
		buffer.rewind();
		loadBuffer(buffer, input, 2);
		short type = buffer.getShort();
		
		// Get Message Size
		buffer = ByteBuffer.allocate(4);
		loadBuffer(buffer, input, 4);
		int size = buffer.getInt();
		
		// Get buffer.
		buffer = ByteBuffer.allocate(size);
		loadBuffer(buffer, input, size);
		
		return new Message(version, type, buffer);
	}
	
	private static void loadBuffer(ByteBuffer buffer, BufferedInputStream input, int size) throws IOException{
		while (size > 0){
			buffer.put((byte) input.read());
			size--;
		}
		buffer.rewind();
	}
	
	
}
