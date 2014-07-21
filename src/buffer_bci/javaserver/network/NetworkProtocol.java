package buffer_bci.javaserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import buffer_bci.javaserver.data.Data;
import buffer_bci.javaserver.data.Header;
import buffer_bci.javaserver.exceptions.ClientException;

/**
 * An impementation of the fieldtrip realtime network protocol. Provides a
 * number of abstract methods that can be used to decode/unwrap incoming
 * communication. And a number of functions that can be used to send outgoing
 * data.
 *
 * @author Wieke Kanters
 *
 */
public class NetworkProtocol {
	/**
	 * Loads a number of bytes from the BufferedInputStream into the ByteBuffer.
	 *
	 * @param buffer
	 * @param input
	 * @param size
	 *            The number of bytes to read.
	 * @throws IOException
	 */
	private static void loadBuffer(ByteBuffer buffer,
			BufferedInputStream input, int size) throws IOException {
		while (size > 0) {
			buffer.put((byte) input.read());
			size--;
		}
		buffer.rewind();
	}

	/**
	 * Decodes the data from the message. Handles all data as groups of bytes,
	 * does not convert to java primitives.
	 *
	 * @param buf
	 * @return
	 * @throws ClientException
	 */
	public static Data readData(ByteBuffer buf) throws ClientException {
		// Get number of channels
		int nChans = buf.getInt();

		// Get number of samples, should be 0
		int nSamples = buf.getInt();

		// Get data type
		int dataType = buf.getInt();

		// Determine the number of bytes per datapoint.
		int nBytes = 1;

		switch (dataType) {
		case CHAR:
		case UINT8:
		case INT8:
			nBytes = 1;
			break;
		case UINT16:
		case INT16:
			nBytes = 2;
			break;
		case UINT32:
		case INT32:
		case FLOAT32:
			nBytes = 4;
			break;
		case UINT64:
		case INT64:
		case FLOAT64:
			nBytes = 8;
			break;
		}

		// Get size of remaining message.
		int size = buf.getInt();

		// Check if the number of bytes left in the buffer corresponds to what
		// we expect.
		if (buf.capacity() - buf.position() < nSamples * nChans * nBytes) {
			throw new ClientException(
					"Recieved less bytes of data than expected.");
		} else if (buf.capacity() - buf.position() > nSamples * nChans * nBytes) {
			throw new ClientException(
					"Recieved more bytes of data than expected.");
		}

		// Transfer bytes from the buffer into a nSamples*nChans*nBytes array;
		byte[][][] data = new byte[nSamples][nChans][nBytes];

		for (int x = 0; x < nSamples; x++) {
			for (int y = 0; y < nChans; y++) {
				for (int z = 0; z < nBytes; z++) {
					data[x][y][z] = buf.get();
				}
			}
		}

		return new Data(nChans, nSamples, nBytes, dataType, data, buf.order());
	}

	/**
	 * Decodes a header from a bytebuffer.
	 *
	 * @param buf
	 * @return the header object
	 * @throws ClientException
	 *             Thrown if the number of samples/events is higher than 0.
	 */
	public static Header readHeader(ByteBuffer buf) throws ClientException {
		// Get number of channels
		int nChans = buf.getInt();

		// Get number of samples, should be 0
		int nSamples = buf.getInt();

		if (nSamples != 0) {
			throw new ClientException(
					"Recieved header with more than 0 samples.");
		}

		// Get number of events, should be 0
		int nEvents = buf.getInt();

		if (nEvents != 0) {
			throw new ClientException(
					"Recieved header with more than 0 events.");
		}

		// Get sample frequency
		float fSample = buf.getFloat();

		// Get data type
		int dataType = buf.getInt();

		// Get size of remaining message.
		int size = buf.getInt();

		/*
		 * // Initialize the labels. String[] labels = new String[nChans];
		 *
		 * while (size > 0) { int chunkType = buf.getInt(); int chunkSize =
		 * buf.getInt(); byte[] bs = new byte[chunkSize]; buf.get(bs);
		 *
		 * if (chunkType == CHUNK_CHANNEL_NAMES) { int n = 0, len = 0; for (int
		 * pos = 0; pos < chunkSize; pos++) { if (bs[pos] == 0) { if (len > 0) {
		 * labels[n] = new String(bs, pos - len, len); } len = 0; if (++n ==
		 * nChans) { break; } } else { len++; } } } else { // ignore all other
		 * chunks for now } size -= 8 + chunkSize; }
		 *
		 * try { return new Header(nChans, fSample, dataType); } catch
		 * (DataException e) { throw new ClientException(
		 * "Number of channels and labels does not match."); }
		 */

		return new Header(nChans, fSample, dataType);
	}

	/**
	 * Reads an incoming message and prepares it for further processing.
	 *
	 * @param input
	 * @return A message object containing the version, type and remaining
	 *         bytes.
	 * @throws IOException
	 *             Passed on from input.
	 * @throws ClientException
	 *             Thrown if a version conflict exists between client/server
	 */
	public static Message readMessage(BufferedInputStream input)
			throws IOException, ClientException {

		// First we determine the endianness of the stream.
		byte versionByte1 = (byte) input.read();
		byte versionByte2 = (byte) input.read();

		ByteOrder order;
		if (versionByte1 < versionByte2) {
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
		if (version != VERSION) {
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

		// Get Message body.
		buffer = ByteBuffer.allocate(size);
		loadBuffer(buffer, input, size);

		return new Message(version, type, buffer, order);
	}

	/**
	 * Decodes a event/data request.
	 * 
	 * @param buf
	 * @return
	 */
	public static Request readRequest(ByteBuffer buf) {
		// Read begin
		int begin = buf.getInt();

		// Read end
		int end = buf.getInt();

		return new Request(begin, end);
	}

	/**
	 * Writes the Data to the BufferOutputStream given the ByteOrder.
	 *
	 * @param output
	 * @param data
	 * @param order
	 * @throws IOException
	 */
	public static void writeData(BufferedOutputStream output, Data data,
			ByteOrder order) throws IOException {

		// Create ByteBuffer
		ByteBuffer buffer = ByteBuffer.allocate(8 + 16 + data.size());
		buffer.order(order);

		// Add standard message opening
		buffer.putShort(VERSION);
		buffer.putShort(GET_OK);
		buffer.putInt(16 + data.size());

		// Add number of channels
		buffer.putInt(data.nChans);

		// Add number of samples
		buffer.putInt(data.nSamples);

		// Add data type
		buffer.putInt(data.dataType);

		// Add data
		buffer.putInt(data.size());

		boolean flipOrder = order != data.order && data.nBytes > 1;

		for (int x = 0; x < data.nSamples; x++) {
			for (int y = 0; y < data.nChans; y++) {
				for (int z = 0; z < data.nBytes; z++) {
					if (flipOrder) {
						buffer.put(data.data[x][y][data.nBytes - z - 1]);
					} else {
						buffer.put(data.data[x][y][z]);
					}
				}
			}
		}

		output.write(buffer.array());
		output.flush();
	}

	/**
	 * Write a GET_ERR to the BufferedOutputStream
	 *
	 * @param output
	 * @param order
	 * @throws IOException
	 */
	public static void writeGetError(BufferedOutputStream output,
			ByteOrder order) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);

		buffer.putShort(VERSION);
		buffer.putShort(GET_ERR);
		buffer.putInt(0);

		output.write(buffer.array());
		output.flush();
	}

	/**
	 * Writes the Header to the BufferedOutputStream using the given ByteOrder.
	 *
	 * @param output
	 * @param hdr
	 * @param order
	 * @throws IOException
	 */
	public static void writeHeader(BufferedOutputStream output, Header hdr,
			ByteOrder order) throws IOException {

		// Create a byte buffer.
		ByteBuffer buf = ByteBuffer.allocate(24 + 8);
		buf.order(order);

		// Add standard message opening
		buf.putShort(VERSION);
		buf.putShort(GET_OK);
		buf.putInt(24);

		// Add header information
		buf.putInt(hdr.nChans);
		buf.putInt(hdr.nSamples);
		buf.putInt(hdr.nEvents);
		buf.putFloat(hdr.fSample);
		buf.putInt(hdr.dataType);
		buf.putInt(0);

		// Send data
		output.write(buf.array());
		output.flush();
	}

	/**
	 * Writes the response to the client for a put error.
	 *
	 * @param output
	 * @param order
	 * @throws IOException
	 */
	public static void writePutError(BufferedOutputStream output,
			ByteOrder order) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);

		buffer.putShort(VERSION);
		buffer.putShort(PUT_ERR);
		buffer.putInt(0);

		output.write(buffer.array());
		output.flush();
	}

	/**
	 * Writes the response to the client for a successful put.
	 *
	 * @param output
	 * @param order
	 * @throws IOException
	 */
	public static void writePutOkay(BufferedOutputStream output, ByteOrder order)
			throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);

		buffer.putShort(VERSION);
		buffer.putShort(PUT_OK);
		buffer.putInt(0);

		output.write(buffer.array());
		output.flush();
	}

	public static final short VERSION = 1;

	public static final short GET_HDR = 0x201;
	public static final short GET_DAT = 0x202;
	public static final short GET_EVT = 0x203;
	public static final short GET_OK = 0x204;
	public static final short GET_ERR = 0x205;

	public static final short PUT_HDR = 0x101;
	public static final short PUT_DAT = 0x102;
	public static final short PUT_EVT = 0x103;
	public static final short PUT_OK = 0x104;
	public static final short PUT_ERR = 0x105;

	public static final short FLUSH_HDR = 0x301;
	public static final short FLUSH_DAT = 0x302;
	public static final short FLUSH_EVT = 0x303;
	public static final short FLUSH_OK = 0x304;
	public static final short FLUSH_ERR = 0x305;

	public static final short WAIT_DAT = 0x402;
	public static final short WAIT_OK = 0x404;
	public static final short WAIT_ERR = 0x405;

	public static final int CHUNK_UNKNOWN = 0;
	public static final int CHUNK_CHANNEL_NAMES = 1;
	public static final int CHUNK_CHANNEL_FLAGS = 2;
	public static final int CHUNK_RESOLUTIONS = 3;
	public static final int CHUNK_ASCII_KEYVAL = 4;
	public static final int CHUNK_NIFTI1 = 5;
	public static final int CHUNK_SIEMENS_AP = 6;
	public static final int CHUNK_CTF_RES4 = 7;

	public static final int CHAR = 0;
	public static final int UINT8 = 1;
	public static final int UINT16 = 2;
	public static final int UINT32 = 3;
	public static final int UINT64 = 4;
	public static final int INT8 = 5;
	public static final int INT16 = 6;
	public static final int INT32 = 7;
	public static final int INT64 = 8;
	public static final int FLOAT32 = 9;
	public static final int FLOAT64 = 10;

}
