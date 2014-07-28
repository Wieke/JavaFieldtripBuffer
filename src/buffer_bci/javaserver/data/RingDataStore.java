package buffer_bci.javaserver.data;

import java.nio.ByteOrder;
import java.util.ArrayList;

import buffer_bci.javaserver.exceptions.DataException;
import buffer_bci.javaserver.network.NetworkProtocol;
import buffer_bci.javaserver.network.Request;
import buffer_bci.javaserver.network.WaitRequest;

public class RingDataStore extends DataModel {
	private final ArrayList<WaitRequest> requests = new ArrayList<WaitRequest>();
	private DataRingBuffer dataBuffer;
	private final EventRingBuffer eventBuffer;
	private int nChans;
	private int nBytes;
	private int dataType;
	private Header header = null;
	private final static ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();
	private final int dataBufferSize;

	/**
	 * Constructor
	 * 
	 * @param nBuffer
	 *            Capacit of sample and event buffers.
	 */
	public RingDataStore(int nBuffer) {
		eventBuffer = new EventRingBuffer(nBuffer);
		dataBufferSize = nBuffer;
	}

	/**
	 * Constructor
	 * 
	 * @param nSamples
	 *            Capacity of the sample ringbuffer.
	 * @param nEvents
	 *            Capacity of the event ringbuffer.
	 */
	public RingDataStore(int nSamples, int nEvents) {
		eventBuffer = new EventRingBuffer(nEvents);
		dataBufferSize = nSamples;
	}

	/**
	 * Adds a thread, with corresponding request, to the list of listeners of
	 * this dataStore. Once the threshold, as defined in request, had been met
	 * the threads waitOver() function will be called.
	 * 
	 * @param thread
	 * @param request
	 */
	@Override
	public synchronized void addWaitRequest(WaitRequest request) {
		requests.add(request);
	}

	/**
	 * Checks for all the listeners, if the conditions have been met, if so
	 * calls the appropriate waitOver function.
	 * 
	 * @throws DataException
	 */
	private synchronized void checkListeners() throws DataException {
		for (int i = 0; i < requests.size(); i++) {
			if (requests.get(i).nEvents < getEventCount()
					|| requests.get(i).nSamples < getSampleCount()) {
				requests.get(i).satisfied();
				requests.remove(i);
				i--;
			}
		}
	}

	/**
	 * Removes all data.
	 * 
	 * @throws DataException
	 */
	@Override
	public synchronized void flushData() throws DataException {
		dataBuffer.clear();
	}

	/**
	 * Removes all events.
	 * 
	 * @throws DataException
	 */
	@Override
	public synchronized void flushEvents() throws DataException {
		eventBuffer.clear();
	}

	/**
	 * Removes the header, and all data & events.
	 * 
	 * @throws DataException
	 */
	@Override
	public synchronized void flushHeader() throws DataException {
		flushData();
		flushEvents();
		dataBuffer = null;
		header = null;
	}

	/**
	 * Returns all data
	 * 
	 * @return
	 * @throws DataException
	 */
	@Override
	public synchronized Data getData() throws DataException {
		if (dataBuffer.size() == 0) {
			throw new DataException("No data stored.");
		}

		int nSamples = dataBuffer.size();

		byte[][][] data = new byte[nSamples][nChans][nBytes];

		int j = 0;
		for (int i = dataBuffer.indexOfOldest(); i < dataBuffer.size(); i++) {
			data[j++] = dataBuffer.get(i);
		}

		return new Data(nChans, nSamples, dataType, data, NATIVE_ORDER);
	}

	/**
	 * Returns the requested data. Throws DataException if impossible.
	 * 
	 * @param request
	 *            Start index and end index of the range requested.
	 * @return
	 * @throws DataException
	 */
	@Override
	public synchronized Data getData(Request request) throws DataException {
		if (dataBuffer.size() == 0) {
			throw new DataException("No data stored.");
		}

		if (request.begin < 0) {
			throw new DataException("Requesting samples with start index < 0.");
		}

		if (request.end < 0) {
			throw new DataException("Requesting samples with end index < 0.");
		}

		if (request.end < request.begin) {
			throw new DataException(
					"Requesting samples with start index > end index.");
		}

		if (request.end >= dataBuffer.size()) {
			throw new DataException(
					"Requesting samples that do not exist (end index >= sample count).");
		}

		if (request.begin >= dataBuffer.size()) {
			throw new DataException(
					"Requesting samples that do not exist (begin index >= sample count).");
		}

		if (request.end < dataBuffer.indexOfOldest()) {
			throw new DataException(
					"Requested samples that do not exist (end index < index of oldest sample in ring)");
		}

		if (request.begin < dataBuffer.indexOfOldest()) {
			throw new DataException(
					"Requested samples that do not exist (begin index < index of oldest sample in ring)");
		}

		int nSamples = request.end - request.begin + 1;

		byte[][][] data = new byte[nSamples][nChans][nBytes];

		int i = 0;
		for (int j = request.begin; j <= request.end; j++) {
			data[i++] = dataBuffer.get(j);
		}

		return new Data(nChans, nSamples, dataType, data, NATIVE_ORDER);
	}

	/**
	 * Returns the number of events currently stored. Throws DataException if
	 * impossible.
	 * 
	 * @return
	 * @throws DataException
	 */
	@Override
	public synchronized int getEventCount() throws DataException {
		return eventBuffer.size();
	}

	/**
	 * Returns all events
	 * 
	 * @return
	 * @throws DataException
	 */
	@Override
	public synchronized Event[] getEvents() throws DataException {
		if (eventBuffer.size() == 0) {
			throw new DataException("No events stored.");
		}

		Event[] events = new Event[eventBuffer.size()
				- eventBuffer.indexOfOldest()];

		int j = 0;
		for (int i = eventBuffer.indexOfOldest(); i < eventBuffer.size(); i++) {
			events[j++] = eventBuffer.get(i);
		}

		return events;
	}

	/**
	 * Returns the requested events. Throws DataException if impossible.
	 * 
	 * @param request
	 *            Start index and end index of the range requested.
	 * @return
	 * @throws DataException
	 */
	@Override
	public synchronized Event[] getEvents(Request request) throws DataException {
		if (eventBuffer.size() == 0) {
			throw new DataException("No events stored.");
		}

		if (request.begin < 0) {
			throw new DataException("Requesting events with start index < 0.");
		}

		if (request.end < 0) {
			throw new DataException("Requesting events with end index < 0.");
		}

		if (request.end < request.begin) {
			throw new DataException(
					"Requesting events with start index > end index.");
		}

		if (request.end >= eventBuffer.size()) {
			throw new DataException(
					"Requesting events that do not exist (end index >= events count).");
		}

		if (request.begin >= eventBuffer.size()) {
			throw new DataException(
					"Requesting events that do not exist (begin index >= events count).");
		}

		if (request.end < eventBuffer.indexOfOldest()) {
			throw new DataException(
					"Requested samples that do not exist (end index < index of oldest sample in ring)");
		}

		if (request.begin < eventBuffer.indexOfOldest()) {
			throw new DataException(
					"Requested samples that do not exist (begin index < index of oldest sample in ring)");
		}

		int nEvents = request.end - request.begin + 1;

		Event[] events = new Event[nEvents];

		int j = 0;
		for (int i = request.begin; i <= request.end; i++) {
			events[j++] = eventBuffer.get(i);
		}

		return events;
	}

	/**
	 * Returns the header currently stored. Throws DataException if impossible.
	 * 
	 * @return
	 * @throws DataException
	 */
	@Override
	public synchronized Header getHeader() throws DataException {

		// Check if header exists
		if (header == null) {
			throw new DataException("No header.");
		}

		// Return header with correct sample/event counts
		return new Header(header, getSampleCount(), getEventCount());
	}

	/**
	 * Returns the number of currently stored samples. Throws DataException if
	 * impossible.
	 * 
	 * @return
	 * @throws DataException
	 */
	@Override
	public synchronized int getSampleCount() throws DataException {
		return dataBuffer.size();
	}

	/**
	 * Returns true if a header has been initialised.
	 * 
	 * @return
	 */
	@Override
	public synchronized boolean headerExists() {
		return header != null;
	}

	/**
	 * Appends the data to the storage. Throws DataException if impossible.
	 * 
	 * @param data
	 * @throws DataException
	 */
	@Override
	public synchronized void putData(Data data) throws DataException {
		if (data.dataType != dataType) {
			throw new DataException("Trying to append data of wrong dataType.");
		}
		if (data.nChans != nChans) {
			throw new DataException(
					"Trying to append data with wrong number of channels");
		}

		// Check if byte order needs to be flipped
		if (data.order != NATIVE_ORDER && nBytes != 1) {
			for (int i = 0; i < data.nSamples; i++) {
				byte[][] sample = new byte[nChans][nBytes];

				for (int j = 0; j < nChans; j++) {
					for (int k = 0; k < nBytes; k++) {
						sample[j][k] = data.data[i][j][nBytes - k - 1];
					}
				}

				dataBuffer.add(sample);
			}
		} else {
			for (int i = 0; i < data.nSamples; i++) {
				dataBuffer.add(data.data[i]);
			}
		}
		checkListeners();
	}

	/**
	 * Appends the events to the storage. Throws DataException if impossible.
	 * 
	 * @param events
	 * @throws DataException
	 */
	@Override
	public synchronized void putEvents(Event[] events) throws DataException {
		for (Event event : events) {
			if (event.order != NATIVE_ORDER) {
				int typeNBytes = NetworkProtocol.dataTypeSize(event.typeType);

				byte[][] type = event.type.clone();
				if (typeNBytes > 1) {
					for (int i = 0; i < event.typeSize; i++) {
						for (int j = 0; j < typeNBytes; j++) {
							type[i * typeNBytes + j] = event.type[i
									* typeNBytes + typeNBytes - j - 1];
						}
					}
				}

				int valueNBytes = NetworkProtocol.dataTypeSize(event.valueType);

				byte[][] value = event.value.clone();
				if (valueNBytes > 1) {
					for (int i = 0; i < event.valueSize; i++) {
						for (int j = 0; j < valueNBytes; j++) {
							value[i * valueNBytes + j] = event.value[i
									* valueNBytes + valueNBytes - j - 1];
						}
					}
				}

				eventBuffer.add(new Event(event, type, value, NATIVE_ORDER));
			} else {
				eventBuffer.add(event);
			}
		}
		checkListeners();
	}

	/**
	 * Adds the header to the storage. Throws DataException if impossible.
	 * 
	 * @param hdr
	 * @throws DataException
	 */
	@Override
	public synchronized void putHeader(Header header) throws DataException {

		boolean newHeader = header == null;

		// Check if header is in BIG_ENDIAN ByteOrder.
		if (header.order != NATIVE_ORDER) {
			Chunk[] chunks = header.chunks;

			// Check each chunk, if it is a CHUNK_RESOLUTIONS chunk, flip the
			// byte order.
			for (int i = 0; i < chunks.length; i++) {
				if (chunks[i].type == NetworkProtocol.CHUNK_RESOLUTIONS) {
					byte[] data = new byte[chunks[i].data.length];

					for (int j = 0; j < header.nChans; j++) {
						for (int k = 0; k < 8; k++) {
							data[j * 8 + k] = chunks[i].data[j * 8 + 7 - k];
						}
					}

					// Replace chunk.
					chunks[i] = new Chunk(chunks[i].type, chunks[i].size, data);
				}
			}

			// Create new header with BIG_ENDIAN ByteOrder
			header = new Header(header, chunks, NATIVE_ORDER);
		}

		if (newHeader) {
			if (nChans != header.nChans) {
				throw new DataException(
						"Replacing header has different number of channels");
			}
			if (dataType != header.dataType) {
				throw new DataException(
						"Replacing header has different data type");
			}
		} else {
			nChans = header.nChans;
			dataType = header.dataType;
			nBytes = NetworkProtocol.dataTypeSize(dataType);
		}

		this.header = header;
		dataBuffer = new DataRingBuffer(dataBufferSize, nChans, nBytes);
	}
}
