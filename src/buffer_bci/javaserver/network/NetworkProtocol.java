package buffer_bci.javaserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import buffer_bci.javaserver.data.Header;
import buffer_bci.javaserver.exceptions.ClientException;
import buffer_bci.javaserver.exceptions.DataException;

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
	
	public static final int CHUNK_UNKNOWN = 0;
	public static final int CHUNK_CHANNEL_NAMES = 1;
	public static final int CHUNK_CHANNEL_FLAGS = 2;
	public static final int CHUNK_RESOLUTIONS = 3;
	public static final int CHUNK_ASCII_KEYVAL = 4;
	public static final int CHUNK_NIFTI1 = 5;
	public static final int CHUNK_SIEMENS_AP = 6;
	public static final int CHUNK_CTF_RES4 = 7;
	
	/**
	 * Reads an incoming message and prepares it for further processing.
	 * @param input
	 * @return A message object containing the version, type and remaining bytes.
	 * @throws IOException - Passed on from input.
	 * @throws ClientException - Thrown if a version conflict exists between client/server
	 */
	public static Message readMessage(BufferedInputStream input) throws IOException, ClientException{
				
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
			throw new ClientException("Client/Server version conflict. "
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
	
	/**
	 * Decodes a header from a bytebuffer.
	 * @param buf
	 * @return - the header object
	 * @throws ClientException - Thrown if the number of samples/events is higher than 0.
	 */
	public static Header readHeader(ByteBuffer buf) throws ClientException{
		//Get number of channels
		int nChans   = buf.getInt();
		String[] labels   = new String[nChans];
		
		//Get number of samples, should be 0
		int nSamples = buf.getInt();
		
		if (nSamples != 0){
			throw new ClientException("Recieved header with more than 0 samples.");
		}
		
		//Get number of events, should be 0
		int nEvents  = buf.getInt();
		
		if (nEvents != 0){
			throw new ClientException("Recieved header with more than 0 events.");
		}
		
		//Get sample frequency
		float fSample  = buf.getFloat();
		
		//Get data type
		int dataType = buf.getInt();
		
		//Get size of remaining message.
		int size = buf.getInt();
	
		
		
		while (size > 0) {
			int chunkType = buf.getInt();
			int chunkSize = buf.getInt();
			byte[] bs = new byte[chunkSize];
			buf.get(bs);
			
			if (chunkType == CHUNK_CHANNEL_NAMES) {
				int n = 0, len = 0;
				for (int pos = 0;pos<chunkSize;pos++) {
					if (bs[pos]==0) {
						if (len>0) {
							labels[n] = new String(bs, pos-len, len);
						}
						len = 0;
						if (++n == nChans) break;
					} else {
						len++;
					}
				}
			} else {
				// ignore all other chunks for now
			}
			size -= 8 + chunkSize;
		}
		
		try {
			return new Header(nChans, fSample, dataType, labels);
		} catch (DataException e) {
			throw new ClientException("Number of channels and labels does not match.");
		}
	}
	
	/**
	 * Writes the Header to the BufferedOutputStream using the given ByteOrder.
	 * @param output
	 * @param hdr
	 * @param order
	 * @throws IOException
	 */
	public void writeHeader(BufferedOutputStream output, Header hdr, ByteOrder order) throws IOException{
		
		// Determine size
		int size = 24;
		
		int channelNameSize = 0;
		if (hdr.labels.length == hdr.nChans) {
			channelNameSize = 0;
			for (int i=0;i<hdr.nChans;i++) {
				channelNameSize++;
				if (hdr.labels[i] != null) {
					channelNameSize += hdr.labels[i].getBytes().length;
				}
			}
			if (channelNameSize > hdr.nChans) {
				// we've got more than just empty string
				size += 8 + channelNameSize;
			}
		}
		
		// Create a byte buffer.
		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.order(order);
		
		buf.putInt(hdr.nChans);
		buf.putInt(hdr.nSamples);
		buf.putInt(hdr.nEvents);
		buf.putFloat(hdr.fSample);
		buf.putInt(hdr.dataType);
		if (channelNameSize <= hdr.nChans) {
			// channel names are all empty or array length does not match
			buf.putInt(0);
		} else {
			buf.putInt(8 + channelNameSize);	// 8 bytes for chunk def
			buf.putInt(CHUNK_CHANNEL_NAMES);
			buf.putInt(channelNameSize);
			for (int i=0;i<hdr.nChans;i++) {
				if (hdr.labels[i] != null) buf.put(hdr.labels[i].getBytes());
				buf.put((byte) 0);
			}
		}
		
		output.write(buf.array());
	}
	
	/**
	 * Writes the response to the client for a successful put.
	 * @param output
	 * @param order
	 * @throws IOException
	 */
	public static void writePutOkay(BufferedOutputStream output, ByteOrder order) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);
		
		buffer.putShort(VERSION);
		buffer.putShort(PUT_OK);
		buffer.putInt(0);
		
		output.write(buffer.array());
	}
	
	/**
	 * Writes the response to the client for a put error.
	 * @param output
	 * @param order
	 * @throws IOException
	 */
	public static void writePutError(BufferedOutputStream output, ByteOrder order) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);
		
		buffer.putShort(VERSION);
		buffer.putShort(PUT_ERR);
		buffer.putInt(0);
		
		output.write(buffer.array());
	}
		
	/**
	 * Loads a number of bytes from the BufferedInputStream into the ByteBuffer.
	 * @param buffer
	 * @param input
	 * @param size - The number of bytes to read.
	 * @throws IOException
	 */
	private static void loadBuffer(ByteBuffer buffer, BufferedInputStream input, int size) throws IOException{
		while (size > 0){
			buffer.put((byte) input.read());
			size--;
		}
		buffer.rewind();
	}
	
	
}
