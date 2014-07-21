package buffer_bci.javaserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import buffer_bci.javaserver.data.DataStore;
import buffer_bci.javaserver.data.Header;
import buffer_bci.javaserver.exceptions.ClientException;
import buffer_bci.javaserver.exceptions.DataException;

public class ServerThread extends Thread {
	private final Socket socket;
	private final DataStore dataStore;
	public final String clientAdress;

	public ServerThread(Socket socket, DataStore dataStore) {
		this.socket = socket;
		this.dataStore = dataStore;
		clientAdress = socket.getInetAddress().toString() + ":"
				+ Integer.toString(socket.getPort());
	}

	/**
	 * Sends the header to the client.
	 *
	 * @param message
	 * @param output
	 * @throws IOException
	 */
	private void handleGetHeader(Message message, BufferedOutputStream output)
			throws IOException {
		try {
			NetworkProtocol.writeHeader(output, dataStore.getHeader(),
					message.order);
		} catch (DataException e) {
			NetworkProtocol.writeGetError(output, message.order);
		}
	}

	/**
	 * Decodes the header from the message and stores it.
	 *
	 * @param message
	 * @param output
	 * @throws IOException
	 */
	private void handlePutHeader(Message message, BufferedOutputStream output)
			throws IOException {
		try {
			Header header = NetworkProtocol.readHeader(message.buffer);
			dataStore.putHeader(header);
			NetworkProtocol.writePutOkay(output, message.order);
		} catch (ClientException | DataException e) {
			NetworkProtocol.writePutError(output, message.order);
		}

	}

	/**
	 * Contains the readMessage/handleMessage loop that handles client/server
	 * communication.
	 */
	@Override
	public void run() {
		try (BufferedOutputStream output = new BufferedOutputStream(
				socket.getOutputStream());
				BufferedInputStream input = new BufferedInputStream(
						socket.getInputStream());) {

			boolean run = true;
			while (run) {
				try {
					// Gets the incoming message
					Message message = NetworkProtocol.readMessage(input);

					// Handles the message using the appropriate function.
					switch (message.type) {
					case NetworkProtocol.PUT_HDR:
						handlePutHeader(message, output);
						break;
					case NetworkProtocol.GET_HDR:
						handleGetHeader(message, output);
						break;
					default:
						System.out.println(clientAdress + " Message received "
								+ message);
						break;
					}

				} catch (ClientException e) {
					System.out.println(clientAdress + "\n" + e.getMessage());
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