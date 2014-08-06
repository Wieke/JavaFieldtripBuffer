package buffer_bci.javaserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import buffer_bci.javaserver.Buffer;
import buffer_bci.javaserver.FieldtripBufferMonitor;
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
public class ConnectionThread extends Thread {
	private final Socket socket;
	private final DataModel dataStore;
	public final String clientAdress;
	private boolean disconnectedOnPurpose = false;
	private FieldtripBufferMonitor monitor;
	public final int clientID;
	private final Buffer buffer;

	/**
	 * Constructor
	 *
	 * @param socket
	 *            The socket for the connection.
	 * @param dataStore
	 *            The storage for all the data implementing the datamodel
	 *            interface.
	 */
	public ConnectionThread(final int clientID, final Socket socket,
			final DataModel dataStore, final Buffer buffer) {
		this.clientID = clientID;
		this.socket = socket;
		this.dataStore = dataStore;
		clientAdress = socket.getInetAddress().toString() + ":"
				+ Integer.toString(socket.getPort());
		this.buffer = buffer;
	}

	/**
	 * Adds a FiedltripBufferMonitor to this thread.
	 *
	 * @param monitor
	 */
	public void addMonitor(final FieldtripBufferMonitor monitor) {
		this.monitor = monitor;

	}

	/**
	 * Disconnects the client connection and stops the thread.
	 */
	public void disconnect() {
		try {
			disconnectedOnPurpose = true;
			socket.close();
		} catch (final IOException e) {
		}
	}

	/**
	 * Removes all data from the store. Returns appropriate response.
	 *
	 * @param message
	 * @return
	 */
	private byte[] handleFlushData(final Message message) {
		try {

			// Remove all data
			dataStore.flushData();
			if (monitor != null) {
				monitor.updateDataFlushed(clientID);
			}
			// Return Okay
			return NetworkProtocol.encodeFlushOkay(message.order);

		} catch (final DataException e) {
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
	private byte[] handleFlushEvents(final Message message) {
		try {

			// Remove all events
			dataStore.flushEvents();
			if (monitor != null) {
				monitor.updateEventsFlushed(clientID);
			}
			// Return Okay
			return NetworkProtocol.encodeFlushOkay(message.order);

		} catch (final DataException e) {
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
	private byte[] handleFlushHeader(final Message message) {
		try {

			// Remove the header (and all the data & events);
			dataStore.flushHeader();
			if (monitor != null) {
				monitor.updateHeaderFlushed(clientID);
			}
			// Return Okay
			return NetworkProtocol.encodeFlushOkay(message.order);

		} catch (final DataException e) {
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
	private byte[] handleGetData(final Message message) {
		try {

			Data data;

			// Check if a request for a specific range has been made.
			if (message.buffer.capacity() > 0) {
				// Get data request from message
				final Request request = NetworkProtocol
						.decodeRequest(message.buffer);

				// Get the requested data
				data = dataStore.getData(request);
			} else {
				data = dataStore.getData();
			}

			// Return message containing requested data
			return NetworkProtocol.encodeData(data, message.order);

		} catch (final DataException e) {
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
	private byte[] handleGetEvent(final Message message) {
		try {

			Event[] events;

			// Check if a request for a specific range has been made.
			if (message.buffer.capacity() > 0) {
				// Get data request from message
				final Request request = NetworkProtocol
						.decodeRequest(message.buffer);

				// Get the requested data
				events = dataStore.getEvents(request);
			} else {
				events = dataStore.getEvents();
			}

			// Return message containing requested data
			return NetworkProtocol.encodeEvents(events, message.order);

		} catch (final DataException e) {
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
	private byte[] handleGetHeader(final Message message) {
		try {

			// Return message containing header
			return NetworkProtocol.encodeHeader(dataStore.getHeader(),
					message.order);

		} catch (final DataException e) {
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
	private byte[] handlePutData(final Message message) {
		try {
			// Get data from message
			final Data data = NetworkProtocol.decodeData(message.buffer);

			// Store data
			final int nSamples = dataStore.putData(data);

			if (monitor != null) {
				monitor.updateSampleCount(nSamples, clientID, data.nSamples);
			}

			// Return okay
			return NetworkProtocol.encodePutOkay(message.order);

		} catch (final ClientException e) {
			// Return error
			return NetworkProtocol.encodeGetError(message.order);

		} catch (final DataException e) {
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
	private byte[] handlePutEvent(final Message message) {
		try {
			// Get the header from the message
			final Event[] events = NetworkProtocol.decodeEvents(message.buffer);

			// Store the header
			final int nEvents = dataStore.putEvents(events);

			if (monitor != null) {
				monitor.updateEventCount(nEvents, clientID, events.length);
			}
			// Return Okay
			return NetworkProtocol.encodePutOkay(message.order);

		} catch (final ClientException e) {
			// Return error
			return NetworkProtocol.encodePutError(message.order);

		} catch (final DataException e) {
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
	private byte[] handlePutHeader(final Message message) {
		try {
			// Get the header from the message
			final Header header = NetworkProtocol.decodeHeader(message.buffer);

			// Store the header
			dataStore.putHeader(header);
			if (monitor != null) {
				monitor.updateHeader(header.dataType, header.fSample,
						header.nChans, clientID);
			}
			// Return Okay
			return NetworkProtocol.encodePutOkay(message.order);

		} catch (final ClientException e) {
			// Return error
			return NetworkProtocol.encodePutError(message.order);

		} catch (final DataException e) {
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
	private byte[] handleWaitData(final Message message) {
		try {
			if (dataStore.headerExists()) {
				// Get wait request
				final WaitRequest request = NetworkProtocol
						.decodeWaitRequest(message.buffer);

				// If timeout is 0 don't bother with the listeners and waiting
				if (request.timeout != 0) {

					// Add this thread to the list of waitlisteners
					dataStore.addWaitRequest(request);

					request.blockUntilSatisfied(request.timeout);
				}

				return NetworkProtocol.encodeWaitResponse(
						dataStore.getSampleCount(), dataStore.getEventCount(),
						message.order);

			} else {
				return NetworkProtocol.encodeWaitError(message.order);
			}
		} catch (final DataException e) {
			// Create error response
			return NetworkProtocol.encodeWaitError(message.order);
		} catch (final InterruptedException e) {
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
			final BufferedOutputStream output = new BufferedOutputStream(
					socket.getOutputStream());
			final BufferedInputStream input = new BufferedInputStream(
					socket.getInputStream());

			boolean run = true;

			while (run) {
				try {
					// Gets the incoming message
					final Message message = NetworkProtocol
							.decodeMessage(input);
					if (monitor != null) {
						monitor.updateClientActivity(clientID,
								System.currentTimeMillis());
					}

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
						break;
					}

					output.write(data);
					output.flush();

				} catch (final ClientException e) {

					if (e.getMessage() == "Client closing connection.") {
						if (monitor != null) {
							monitor.updateConnectionClosed(clientID);
							;
						}
					} else if (e.getMessage().contains("version conflict")) {
						if (monitor != null) {
							monitor.updateClientError(clientID,
									FieldtripBufferMonitor.ERROR_VERSION,
									System.currentTimeMillis());
						}
					} else {
						if (monitor != null) {
							monitor.updateClientError(clientID,
									FieldtripBufferMonitor.ERROR_PROTOCOL,
									System.currentTimeMillis());
						}
					}

					run = false;
				} catch (final SocketException e) {
					if (!disconnectedOnPurpose) {
						socket.close();
						if (monitor != null) {
							monitor.updateClientError(clientID,
									FieldtripBufferMonitor.ERROR_CONNECTION,
									System.currentTimeMillis());
						}
					}
					run = false;
				}
			}
			socket.close();

		} catch (final IOException e) {
			if (!disconnectedOnPurpose) {
				if (monitor != null) {
					monitor.updateClientError(clientID,
							FieldtripBufferMonitor.ERROR_CONNECTION,
							System.currentTimeMillis());
				}
			} else {
				e.printStackTrace();
			}
		}
		buffer.removeConnection(this);
	}
}