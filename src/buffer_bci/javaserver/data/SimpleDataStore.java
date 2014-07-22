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
	 * Removes all data.
	 * 
	 * @throws DataException
	 */
	@Override
	public synchronized void flushData() throws DataException {
		// TODO implement flushData
	}

	/**
	 * Removes all events.
	 * 
	 * @throws DataException
	 */
	@Override
	public synchronized void flushEvents() throws DataException {
		// TODO implement flushEvents
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
		// TODO implement getData

		return null;
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
		// TODO implement getEventCount

		return 0;
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
		// TODO implement getEvents;

		return null;
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
		// TODO implement getSampleCount

		return 0;
	}

	/**
	 * Appends the data to the storage. Throws DataException if impossible.
	 * 
	 * @param data
	 * @throws DataException
	 */
	@Override
	public synchronized void putData(Data data) throws DataException {
		// TODO implement putData

	}

	/**
	 * Appends the events to the storage. Throws DataException if impossible.
	 * 
	 * @param events
	 * @throws DataException
	 */
	@Override
	public synchronized void putEvents(Event[] events) throws DataException {
		// TODO implement putEvents
	}

	/**
	 * Adds the header to the storage. Throws DataException if impossible.
	 * 
	 * @param hdr
	 * @throws DataException
	 */
	@Override
	public synchronized void putHeader(Header header) throws DataException {

		// Check if header is in BIG_ENDIAN ByteOrder.
		if (header.order != BIG_ENDIAN) {
			Chunk[] chunks = header.chunks;

			// Check each chunk, if it is a CHUNK_RESOLUTIONS chunk, flip the
			// byte order.
			for (int i = 0; i < chunks.length; i++) {
				if (chunks[i].type == NetworkProtocol.CHUNK_RESOLUTIONS) {
					byte[] data = new byte[chunks[i].data.length];

					for (int j = 0; i < header.nChans; i++) {
						for (int k = 0; j < 8; j++) {
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
