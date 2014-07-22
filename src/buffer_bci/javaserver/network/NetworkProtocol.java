package buffer_bci.javaserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import buffer_bci.javaserver.data.Chunk;
import buffer_bci.javaserver.data.Data;
import buffer_bci.javaserver.data.Event;
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
	public static final int CHUNK_NEUROMAG_HEADER = 8;
	public static final int CHUNK_NEUROMAG_ISOTRAK = 9;

	public static final int CHUNK_NEUROMAG_HPIRESULT = 10;
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

	/**
	 * Returns the number of bytes in a particular data type.
	 * 
	 * @param dataType
	 * @return
	 */
	public static int dataTypeSize(int dataType) {
		switch (dataType) {
		case CHAR:
		case UINT8:
		case INT8:
			return 1;
		case UINT16:
		case INT16:
			return 2;
		case UINT32:
		case INT32:
		case FLOAT32:
			return 4;
		case UINT64:
		case INT64:
		case FLOAT64:
			return 8;
		}
		return -1;
	}

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
	 * Decodes a single extended header chunk from the bytebuffer
	 * 
	 * @param buffer
	 * @return
	 * @throws ClientException
	 */
	private static Chunk readChunk(ByteBuffer buffer) throws ClientException {
		// Get extended header type
		int type = buffer.getInt();

		// Get extended header size;
		int size = buffer.getInt();

		// Check if there are enough bytes remaining
		if (buffer.capacity() - buffer.position() < size) {
			throw new ClientException("Malformed header message.");
		}

		// Grab the remaining bytes in the chunk.

		byte[] data = new byte[size];
		buffer.get(data);

		return new Chunk(type, size, data);
	}

	/**
	 * Decodes a series of extended header chunks from the bytebuffer.
	 * 
	 * @param buffer
	 * @return
	 * @throws ClientException
	 */
	private static Chunk[] readChunks(ByteBuffer buffer) throws ClientException {
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		int nChunks = 0;

		// Read events while bytes remain in the buffer.
		try {
			while (buffer.position() < buffer.capacity()) {
				chunks.add(readChunk(buffer));
				nChunks++;
			}
		} catch (BufferUnderflowException e) {
			throw new ClientException("Malformed header message");
		}

		return chunks.toArray(new Chunk[nChunks]);
	}

	/**
	 * Decodes the data from the message. Handles all data as groups of bytes,
	 * does not convert to java primitives.
	 * 
	 * @param buf
	 * @return
	 * @throws ClientException
	 */
	public static Data readData(ByteBuffer buffer) throws ClientException {
		// Get number of channels
		int nChans = buffer.getInt();

		// Get number of samples, should be 0
		int nSamples = buffer.getInt();

		// Get data type
		int dataType = buffer.getInt();

		// Determine the number of bytes per datapoint.
		int nBytes = dataTypeSize(dataType);

		// Get size of remaining message.
		int size = buffer.getInt();

		// Check if size and the number of bytes in the buffer match

		if (buffer.capacity() - buffer.position() != size) {
			throw new ClientException(
					"Defined size of data and actual size do not match.");
		}

		// Check if the number of bytes left in the buffer corresponds to what
		// we expect.
		if (buffer.capacity() - buffer.position() < nSamples * nChans * nBytes) {
			throw new ClientException(
					"Recieved less bytes of data than expected.");
		} else if (buffer.capacity() - buffer.position() > nSamples * nChans
				* nBytes) {
			throw new ClientException(
					"Recieved more bytes of data than expected.");
		}

		// Transfer bytes from the buffer into a nSamples*nChans*nBytes array;
		byte[][][] data = new byte[nSamples][nChans][nBytes];

		for (int x = 0; x < nSamples; x++) {
			for (int y = 0; y < nChans; y++) {
				for (int z = 0; z < nBytes; z++) {
					data[x][y][z] = buffer.get();
				}
			}
		}

		return new Data(nChans, nSamples, dataType, data, buffer.order());
	}

	/**
	 * Partially decodes a single event from a bytebuffer. Handles type and
	 * value of events as arrays of bytes.
	 * 
	 * @param buffer
	 * @return
	 * @throws ClientException
	 */
	private static Event readEvent(ByteBuffer buffer) throws ClientException,
			BufferUnderflowException {
		// Get data type of event type
		int typeType = buffer.getInt();
		int typeNBytes = dataTypeSize(typeType);

		// Get number of elements in event type
		int typeSize = buffer.getInt();

		// Get data type of event value
		int valueType = buffer.getInt();
		int valueNBytes = dataTypeSize(valueType);

		// Get number of elements in event value
		int valueSize = buffer.getInt();

		// Get associated sample
		int sample = buffer.getInt();

		// Get offset
		int offset = buffer.getInt();

		// Get duration
		int duration = buffer.getInt();

		// Get size of remaining data
		int size = buffer.getInt();

		// Check if size and predicted size are consistent

		if (size != typeSize * typeNBytes + valueSize * valueNBytes) {
			throw new ClientException(
					"Given size and actual size of value and type do not match or malformed event message.");
		}

		if (typeNBytes == -1) {
			throw new ClientException(
					"Wrong type type or malformed event message.");
		}

		if (valueNBytes == -1) {
			throw new ClientException(
					"Wrong value type or malformed event message.");
		}

		// Transfer the remaining bytes in type[][] and value[][]
		byte[][] type = new byte[typeSize][typeNBytes];

		for (int x = 0; x < typeSize; x++) {
			for (int y = 0; y < typeNBytes; y++) {
				type[x][y] = buffer.get();
			}
		}

		byte[][] value = new byte[valueSize][valueNBytes];

		for (int x = 0; x < valueSize; x++) {
			for (int y = 0; y < valueNBytes; y++) {
				value[x][y] = buffer.get();
			}
		}

		return new Event(typeType, typeSize, valueType, valueSize, sample,
				offset, duration, type, value, buffer.order());

	}

	/**
	 * Decodes a series of events from the ByteBuffer. Handles event values and
	 * types as bytes.
	 * 
	 * @param buffer
	 * @return
	 * @throws ClientException
	 */
	public static Event[] readEvents(ByteBuffer buffer) throws ClientException {
		ArrayList<Event> events = new ArrayList<Event>();
		int nEvents = 0;

		// Read events while bytes remain in the buffer.
		try {
			while (buffer.position() < buffer.capacity()) {
				events.add(readEvent(buffer));
				nEvents++;
			}
		} catch (BufferUnderflowException e) {
			throw new ClientException("Malformed event message");
		}

		return events.toArray(new Event[nEvents]);
	}

	/**
	 * Decodes a header from a bytebuffer.
	 * 
	 * @param buf
	 * @return the header object
	 * @throws ClientException
	 *             Thrown if the number of samples/events is higher than 0.
	 */
	public static Header readHeader(ByteBuffer buffer) throws ClientException {
		// Get number of channels
		int nChans = buffer.getInt();

		// Get number of samples, should be 0
		int nSamples = buffer.getInt();

		if (nSamples != 0) {
			throw new ClientException(
					"Recieved header with more than 0 samples.");
		}

		// Get number of events, should be 0
		int nEvents = buffer.getInt();

		if (nEvents != 0) {
			throw new ClientException(
					"Recieved header with more than 0 events.");
		}

		// Get sample frequency
		float fSample = buffer.getFloat();

		// Get data type
		int dataType = buffer.getInt();

		// Get size of remaining message.
		int size = buffer.getInt();

		// Check if size matches the remaining bytes

		if (buffer.capacity() - buffer.position() != size) {
			throw new ClientException(
					"Defined size of header chunks and actual size do not match.");
		}

		Chunk[] chunks = readChunks(buffer);

		return new Header(nChans, fSample, dataType, chunks, buffer.order());
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
	public static Request readRequest(ByteBuffer buffer) {
		// Read begin
		int begin = buffer.getInt();

		// Read end
		int end = buffer.getInt();

		return new Request(begin, end);
	}

	/**
	 * Decodes a WaitRequest from the ByteBuffer.
	 * 
	 * @param buffer
	 * @return
	 */
	public static WaitRequest readWaitRequest(ByteBuffer buffer) {
		int nSamples = buffer.getInt();
		int nEvents = buffer.getInt();
		int timeout = buffer.getInt();
		return new WaitRequest(nSamples, nEvents, timeout);
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
		int nBytes = dataTypeSize(data.dataType);

		ByteBuffer buffer = ByteBuffer.allocate(8 + 16 + data.size() * nBytes);
		buffer.order(order);

		// Add standard message opening
		buffer.putShort(VERSION);
		buffer.putShort(GET_OK);
		buffer.putInt(16 + data.size() * nBytes);

		// Add number of channels
		buffer.putInt(data.nChans);

		// Add number of samples
		buffer.putInt(data.nSamples);

		// Add data type
		buffer.putInt(data.dataType);

		// Add data

		buffer.putInt(data.size() * nBytes);

		boolean flipOrder = order != data.order && nBytes > 1;

		for (int x = 0; x < data.nSamples; x++) {
			for (int y = 0; y < data.nChans; y++) {
				for (int z = 0; z < nBytes; z++) {
					if (flipOrder) {
						buffer.put(data.data[x][y][nBytes - z - 1]);
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
	 * Write an Event to the BufferedOutputStream.
	 * 
	 * @param output
	 * @param event
	 * @param order
	 * @throws IOException
	 */
	public static void writeEvents(BufferedOutputStream output, Event[] events,
			ByteOrder order) throws IOException {

		// Determine total message size
		int totalBufferSize = 8;
		for (Event event : events) {
			totalBufferSize += 32;
			totalBufferSize += event.typeSize * dataTypeSize(event.typeType);
			totalBufferSize += event.valueSize * dataTypeSize(event.valueType);
		}

		// Create ByteBuffer
		ByteBuffer buffer = ByteBuffer.allocate(totalBufferSize);
		buffer.order(order);

		// Add standard message opening
		buffer.putShort(VERSION);
		buffer.putShort(GET_OK);
		buffer.putInt(totalBufferSize - 8);

		// Loop through all evens and add them to the buffer.

		for (Event event : events) {
			// Add event type data type
			buffer.putInt(event.typeType);

			// Add number of elements in event type
			buffer.putInt(event.typeSize);

			// Add event value data type
			buffer.putInt(event.valueType);

			// Add number of elements in event value
			buffer.putInt(event.valueType);

			// Add associated sample
			buffer.putInt(event.sample);

			// Add offset
			buffer.putInt(event.offset);

			// Add duration
			buffer.putInt(event.duration);

			// Add size of remaining value and type bytes

			int typeNBytes = dataTypeSize(event.typeType);
			int valueNBytes = dataTypeSize(event.valueType);

			buffer.putInt(event.typeSize * typeNBytes + event.valueSize
					* valueNBytes);

			// Add type bytes
			boolean flipOrder = order != event.order && typeNBytes > 1;

			for (int x = 0; x < event.typeSize; x++) {
				for (int y = 0; y < typeNBytes; y++) {
					if (flipOrder) {
						buffer.put(event.type[x][typeNBytes - y - 1]);
					} else {
						buffer.put(event.type[x][y]);
					}
				}
			}

			// Add value bytes
			flipOrder = order != event.order && valueNBytes > 1;

			for (int x = 0; x < event.valueSize; x++) {
				for (int y = 0; y < valueNBytes; y++) {
					if (flipOrder) {
						buffer.put(event.value[x][valueNBytes - y - 1]);
					} else {
						buffer.put(event.value[x][y]);
					}
				}
			}
		}

		output.write(buffer.array());
		output.flush();
	}

	/**
	 * Write a FLUSH_OK to the BufferedOutputStream
	 * 
	 * @param output
	 * @param order
	 * @throws IOException
	 */
	public static void writeFlushOkay(BufferedOutputStream output,
			ByteOrder order) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);

		buffer.putShort(VERSION);
		buffer.putShort(FLUSH_OK);
		buffer.putInt(0);

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

		// Write extended header chunks

		if (hdr.nChunks > 0) {
			for (Chunk chunk : hdr.chunks) {
				// Write chunk type
				buf.putInt(chunk.type);

				// Write chunk size
				buf.putInt(chunk.size);

				// Writ chunk data.
				// In case of Resolutions chunk flip order if necessary.

				boolean flipOrder = order != hdr.order;

				if (chunk.type == CHUNK_RESOLUTIONS && flipOrder) {
					for (int i = 0; i < hdr.nChans; i++) {
						for (int j = 7; j >= 0; j--) {
							buf.put(chunk.data[i * 8 + j]);
						}
					}
				} else {
					buf.put(chunk.data);
				}
			}
		}

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

	/**
	 * Write a WAIT_ERR to the BufferedOutputStream
	 * 
	 * @param output
	 * @param order
	 * @throws IOException
	 */
	public static void writeWaitError(BufferedOutputStream output,
			ByteOrder order) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(order);

		buffer.putShort(VERSION);
		buffer.putShort(WAIT_ERR);
		buffer.putInt(0);

		output.write(buffer.array());
		output.flush();
	}

	/**
	 * Writes a WaitRequest to the BufferOutputStream given the ByteOrder
	 * 
	 * @param output
	 * @param waitRequest
	 * @param order
	 * @throws IOException
	 */
	public static void writeWaitResponse(BufferedOutputStream output,
			WaitRequest waitRequest, ByteOrder order) throws IOException {

		// Create ByteBuffer
		ByteBuffer buffer = ByteBuffer.allocate(16);

		// Add standard message opening
		buffer.putShort(VERSION);
		buffer.putShort(WAIT_OK);
		buffer.putInt(8);

		// Add nSamples
		buffer.putInt(waitRequest.nSamples);

		// Add nEvents
		buffer.putInt(waitRequest.nEvents);

		output.write(buffer.array());
		output.flush();
	}

}
