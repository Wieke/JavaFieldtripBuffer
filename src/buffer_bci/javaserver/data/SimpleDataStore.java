package buffer_bci.javaserver.data;

import java.nio.ByteOrder;
import java.util.ArrayList;

import buffer_bci.javaserver.exceptions.DataException;
import buffer_bci.javaserver.network.NetworkProtocol;
import buffer_bci.javaserver.network.Request;
import buffer_bci.javaserver.network.ServerThread;
import buffer_bci.javaserver.network.WaitRequest;

public class SimpleDataStore extends DataModel {
	private class Listener {
		public final ServerThread thread;
		public final int nSamples;
		public final int nEvents;

		public Listener(ServerThread thread, WaitRequest request) {
			this.thread = thread;
			nSamples = request.nSamples;
			nEvents = request.nEvents;
		}
	}

	private final ArrayList<Listener> listeners = new ArrayList<Listener>();
	private final ArrayList<byte[][]> dataArray = new ArrayList<byte[][]>();
	private final ArrayList<Event> eventArray = new ArrayList<Event>();
	private int nChans;
	private int nBytes;
	private int dataType;
	private Header header = null;
	private final static ByteOrder BIG_ENDIAN = ByteOrder.BIG_ENDIAN;

	/**
	 * Adds a thread, with corresponding request, to the list of listeners of
	 * this dataStore. Once the threshold, as defined in request, had been met
	 * the threads waitOver() function will be called.
	 * 
	 * @param thread
	 * @param request
	 */
	@Override
	public synchronized void addWaitListener(ServerThread thread,
			WaitRequest request) {
		listeners.add(new Listener(thread, request));
	}

	/**
	 * Checks for all the listeners, if the conditions have been met, if so
	 * calls the appropriate waitOver function.
	 * 
	 * @throws DataException
	 */
	private void checkListeners() throws DataException {
		for (Listener listener : listeners) {
			if (listener.nEvents < getEventCount()
					|| listener.nSamples < getSampleCount()) {
				listener.thread.waitOver(false);
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
		dataArray.clear();
	}

	/**
	 * Removes all events.
	 * 
	 * @throws DataException
	 */
	@Override
	public synchronized void flushEvents() throws DataException {
		eventArray.clear();
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
		header = null;
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

		if (request.end >= dataArray.size()) {
			throw new DataException(
					"Requesting samples that do not exist (end index >= sample count).");
		}

		if (request.begin >= dataArray.size()) {
			throw new DataException(
					"Requesting samples that do not exist (begin index >= sample count).");
		}

		int nSamples = request.end - request.begin + 1;

		byte[][][] data = new byte[nSamples][nChans][nBytes];

		int i = 0;
		for (byte[][] sample : dataArray
				.subList(request.begin, request.end + 1)) {
			data[i++] = sample;
		}

		return new Data(nChans, nSamples, dataType, data, BIG_ENDIAN);
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
		return eventArray.size();
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

		if (request.end >= dataArray.size()) {
			throw new DataException(
					"Requesting events that do not exist (end index >= events count).");
		}

		if (request.begin >= dataArray.size()) {
			throw new DataException(
					"Requesting events that do not exist (begin index >= events count).");
		}

		int nEvents = request.end - request.begin + 1;

		return eventArray.subList(request.begin, request.end + 1).toArray(
				new Event[nEvents]);
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
		return dataArray.size();
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
		if (data.order != BIG_ENDIAN && nBytes != 1) {
			for (int i = 0; i < data.nSamples; i++) {
				byte[][] sample = new byte[nChans][nBytes];

				for (int j = 0; j < nChans; j++) {
					for (int k = 0; k < nBytes; k++) {
						sample[j][k] = data.data[i][j][nBytes - k - 1];
					}
				}

				dataArray.add(sample);
			}
		} else {
			for (int i = 0; i < data.nSamples; i++) {
				dataArray.add(data.data[i]);
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
			if (event.order != BIG_ENDIAN) {
				int typeNBytes = NetworkProtocol.dataTypeSize(event.typeType);
				int valueNBytes = NetworkProtocol.dataTypeSize(event.valueType);

				byte[][] type = new byte[event.typeSize][typeNBytes];
				for (int i = 0; i < event.typeSize; i++) {
					for (int j = 0; j < typeNBytes; j++) {
						type[i * typeNBytes + j] = event.type[i * typeNBytes
								+ typeNBytes - j - 1];
					}
				}

				byte[][] value = new byte[event.valueSize][valueNBytes];
				for (int i = 0; i < event.valueSize; i++) {
					for (int j = 0; j < valueNBytes; j++) {
						value[i * valueNBytes + j] = event.value[i
								* valueNBytes + valueNBytes - j - 1];
					}
				}

				eventArray.add(new Event(event, type, value, BIG_ENDIAN));
			} else {
				eventArray.add(event);
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
		if (header.order != BIG_ENDIAN) {
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
			header = new Header(header, chunks, BIG_ENDIAN);
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
	}

	/**
	 * Removes a thread from the list of listeners.
	 * 
	 * @param listener
	 */
	@Override
	public synchronized void removeWaitListener(ServerThread thread) {
		for (int i = 0; i < listeners.size(); i++) {
			if (listeners.get(i).thread.equals(thread)) {
				listeners.remove(i);
				break;
			}
		}
	}

}
