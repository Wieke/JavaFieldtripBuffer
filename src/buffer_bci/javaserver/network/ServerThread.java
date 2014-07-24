package buffer_bci.javaserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import buffer_bci.javaserver.data.Data;
import buffer_bci.javaserver.data.DataModel;
import buffer_bci.javaserver.data.Event;
import buffer_bci.javaserver.data.Header;
import buffer_bci.javaserver.exceptions.ClientException;
import buffer_bci.javaserver.exceptions.DataException;

/**
 * Thread for handling a single connection. Uses NetworkProtocol to
 * encode/decode messages. Uses a shared dataModel object for storing data.
 * 
 * @author Wieke Kanters
 * 
 */
public class ServerThread extends Thread {
	private final Socket socket;
	private final DataModel dataStore;

	public final String clientAdress;

	/**
	 * Constructor
	 * 
	 * @param socket
	 *            The socket for the connection.
	 * @param dataStore
	 *            The storage for all the data implementing the datamodel
	 *            interface.
	 */
	public ServerThread(Socket socket, DataModel dataStore) {
		this.socket = socket;
		this.dataStore = dataStore;
		clientAdress = socket.getInetAddress().toString() + ":"
				+ Integer.toString(socket.getPort());
	}

	/**
	 * Removes all data from the store. Returns appropriate response.
	 * 
	 * @param message
	 * @return
	 */
	private byte[] handleFlushData(Message message) {
		System.out.println(clientAdress + " Flushing data.");
		try {

			// Remove all data
			dataStore.flushData();

			// Return Okay
			return NetworkProtocol.encodeFlushOkay(message.order);

		} catch (DataException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodeFlushError(message.order);

		}
	}

	/**
	 * Removes all events from the store. Returns appropriate response.
	 * 
	 * @param message
	 * @return
	 */
	private byte[] handleFlushEvents(Message message) {
		System.out.println(clientAdress + " Flushing events.");
		try {

			// Remove all events
			dataStore.flushEvents();

			// Return Okay
			return NetworkProtocol.encodeFlushOkay(message.order);

		} catch (DataException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodeFlushError(message.order);

		}
	}

	/**
	 * Removes all data from the store. Returns appropriate response.
	 * 
	 * @param message
	 * @return
	 */
	private byte[] handleFlushHeader(Message message) {
		System.out.println(clientAdress + " Flushing header.");
		try {

			// Remove the header (and all the data & events);
			dataStore.flushHeader();

			// Return Okay
			return NetworkProtocol.encodeFlushOkay(message.order);

		} catch (DataException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodeFlushError(message.order);

		}
	}

	/**
	 * Gets begin/end from the message and returns the appropriate data.
	 * 
	 * @param message
	 * @param input
	 * @param output
	 *            @
	 */
	private byte[] handleGetData(Message message) {
		System.out.println(clientAdress + " Get data.");
		try {

			Data data;

			// Check if a request for a specific range has been made.
			if (message.buffer.capacity() > 0) {
				// Get data request from message
				Request request = NetworkProtocol.decodeRequest(message.buffer);

				// Get the requested data
				data = dataStore.getData(request);
			} else {
				data = dataStore.getData();
			}

			// Return message containing requested data
			return NetworkProtocol.encodeData(data, message.order);

		} catch (DataException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodeGetError(message.order);
		}
	}

	/**
	 * Encodes the requested events for sending it to the client.
	 * 
	 * @param message
	 * @return
	 */
	private byte[] handleGetEvent(Message message) {
		System.out.println(clientAdress + " Get event.");
		try {

			Event[] events;

			// Check if a request for a specific range has been made.
			if (message.buffer.capacity() > 0) {
				// Get data request from message
				Request request = NetworkProtocol.decodeRequest(message.buffer);

				// Get the requested data
				events = dataStore.getEvents(request);
			} else {
				events = dataStore.getEvents();
			}

			// Return message containing requested data
			return NetworkProtocol.encodeEvents(events, message.order);

		} catch (DataException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodeGetError(message.order);
		}
	}

	/**
	 * Encodes the header for sending it to the client.
	 * 
	 * @param message
	 * @param output
	 *            @
	 */
	private byte[] handleGetHeader(Message message) {
		System.out.println(clientAdress + " Get header.");
		try {

			// Return message containing header
			return NetworkProtocol.encodeHeader(dataStore.getHeader(),
					message.order);

		} catch (DataException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodeGetError(message.order);

		}
	}

	/**
	 * Grabs data from the message and stores it in the dataStore. Returns
	 * appropriate response.
	 * 
	 * @param message
	 * @param output
	 *            @
	 */
	private byte[] handlePutData(Message message) {
		System.out.println(clientAdress + " Put data.");
		try {
			// Get data from message
			Data data = NetworkProtocol.decodeData(message.buffer);

			// Store data
			dataStore.putData(data);

			// Return okay
			return NetworkProtocol.encodePutOkay(message.order);

		} catch (ClientException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodeGetError(message.order);

		} catch (DataException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodeGetError(message.order);

		}

	}

	/**
	 * Decodes the events from the message and stores them. Returns appropriate
	 * response.
	 * 
	 * @param message
	 * @return
	 */
	private byte[] handlePutEvent(Message message) {
		System.out.println(clientAdress + " Put event.");
		try {
			// Get the header from the message
			Event[] events = NetworkProtocol.decodeEvents(message.buffer);

			// Store the header
			dataStore.putEvents(events);

			// Return Okay
			return NetworkProtocol.encodePutOkay(message.order);

		} catch (ClientException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodePutError(message.order);

		} catch (DataException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodePutError(message.order);
		}
	}

	/**
	 * Decodes the header from the message and stores it. Returns appropriate
	 * response.
	 * 
	 * @param message
	 * @param output
	 *            @
	 */
	private byte[] handlePutHeader(Message message) {
		System.out.println(clientAdress + " Put header.");
		try {
			// Get the header from the message
			Header header = NetworkProtocol.decodeHeader(message.buffer);

			// Store the header
			dataStore.putHeader(header);

			// Return Okay
			return NetworkProtocol.encodePutOkay(message.order);

		} catch (ClientException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodePutError(message.order);

		} catch (DataException e) {

			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Return error
			return NetworkProtocol.encodePutError(message.order);
		}
	}

	/**
	 * Decodes the WaitRequest from the message. Adds this thread to the
	 * WaitListeners of the dataStore. Launches a countdown thread.
	 * 
	 * @param message
	 * @return
	 */
	private byte[] handleWaitData(Message message) {
		try {
			System.out.println(clientAdress + " Wait for data.");

			// Get wait request
			WaitRequest request = NetworkProtocol
					.decodeWaitRequest(message.buffer);

			// If timeout is 0 don't bother with the listeners and timeout
			// and send a response immediately.
			if (request.timeout == 0) {

				return NetworkProtocol.encodeWaitResponse(
						dataStore.getSampleCount(), dataStore.getEventCount(),
						message.order);

			}

			long start = System.currentTimeMillis();

			// Check the thresholds every 10 ms, stop checking if thresholds
			// are met.
			while (System.currentTimeMillis() - start < request.timeout) {
				sleep(10);

				if (request.nEvents < dataStore.getEventCount()
						|| request.nSamples < dataStore.getSampleCount()) {
					break;
				}
			}

			// Return response
			return NetworkProtocol.encodeWaitResponse(
					dataStore.getSampleCount(), dataStore.getEventCount(),
					message.order);

		} catch (DataException e) {
			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Create error response
			return NetworkProtocol.encodeWaitError(message.order);
		} catch (InterruptedException e) {
			// Print error message
			System.out.println(clientAdress + " " + e.getMessage());

			// Create error response
			return NetworkProtocol.encodeWaitError(message.order);
		}

	}

	/**
	 * Contains the readMessage/handleMessage loop that handles client/server
	 * communication.
	 */
	@Override
	public void run() {
		try {
			BufferedOutputStream output = new BufferedOutputStream(
					socket.getOutputStream());
			BufferedInputStream input = new BufferedInputStream(
					socket.getInputStream());

			boolean run = true;

			while (run) {
				try {
					// Gets the incoming message
					Message message = NetworkProtocol.decodeMessage(input);

					byte[] data = null;

					// Handles the message using the appropriate function.
					switch (message.type) {
					case NetworkProtocol.PUT_HDR:
						data = handlePutHeader(message);
						break;
					case NetworkProtocol.GET_HDR:
						data = handleGetHeader(message);
						break;
					case NetworkProtocol.PUT_DAT:
						data = handlePutData(message);
						break;
					case NetworkProtocol.GET_DAT:
						data = handleGetData(message);
						break;
					case NetworkProtocol.GET_EVT:
						data = handleGetEvent(message);
						break;
					case NetworkProtocol.PUT_EVT:
						data = handlePutEvent(message);
						break;
					case NetworkProtocol.FLUSH_DAT:
						data = handleFlushData(message);
						break;
					case NetworkProtocol.FLUSH_EVT:
						data = handleFlushEvents(message);
						break;
					case NetworkProtocol.FLUSH_HDR:
						data = handleFlushHeader(message);
						break;
					case NetworkProtocol.WAIT_DAT:
						data = handleWaitData(message);
						if (data == null) {
							return;
						}
						break;
					}

					output.write(data);
					output.flush();

				} catch (ClientException e) {
					System.out.println(clientAdress + " " + e.getMessage());
					socket.close();
					System.out.println(clientAdress + " Connection closed");
					run = false;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}