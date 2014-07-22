package buffer_bci.javaserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import buffer_bci.javaserver.data.Data;
import buffer_bci.javaserver.data.DataModel;
import buffer_bci.javaserver.data.Header;
import buffer_bci.javaserver.exceptions.ClientException;
import buffer_bci.javaserver.exceptions.DataException;

public class ServerThread extends Thread {
	private final Socket socket;
	private final DataModel dataStore;
	public final String clientAdress;

	public ServerThread(Socket socket, DataModel dataStore) {
		this.socket = socket;
		this.dataStore = dataStore;
		clientAdress = socket.getInetAddress().toString() + ":"
				+ Integer.toString(socket.getPort());
	}

	private byte[] handleFlushData(Message message) throws IOException {
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

	private byte[] handleFlushEvents(Message message) throws IOException {
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

	private byte[] handleFlushHeader(Message message) throws IOException {
		try {

			// Remove all data
			dataStore.flushData();

			// Remove all events
			dataStore.flushEvents();

			// Remove the header
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
	 * @throws IOException
	 */
	private byte[] handleGetData(Message message) throws IOException {
		try {

			// Get data request from message
			Request request = NetworkProtocol.decodeRequest(message.buffer);

			// Get the requested data
			Data data = dataStore.getData(request);

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
	 * Sends the header to the client.
	 * 
	 * @param message
	 * @param output
	 * @throws IOException
	 */
	private byte[] handleGetHeader(Message message) throws IOException {
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
	 * Grabs data from the message and stores it in the dataStore.
	 * 
	 * @param message
	 * @param output
	 * @throws IOException
	 */
	private byte[] handlePutData(Message message) throws IOException {
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
	 * Decodes the header from the message and stores it.
	 * 
	 * @param message
	 * @param output
	 * @throws IOException
	 */
	private byte[] handlePutHeader(Message message) throws IOException {
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
					case NetworkProtocol.FLUSH_DAT:
						data = handleFlushData(message);
						break;
					case NetworkProtocol.FLUSH_EVT:
						data = handleFlushEvents(message);
						break;
					case NetworkProtocol.FLUSH_HDR:
						data = handleFlushHeader(message);
						break;
					default:
						System.out.println(clientAdress + " Message received "
								+ message);
						break;
					}

					output.write(data);
					output.flush();

				} catch (ClientException e) {
					System.out.println(clientAdress + "\n" + e.getMessage());
					socket.close();
					System.out.println(" Connection closed");
					run = false;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}