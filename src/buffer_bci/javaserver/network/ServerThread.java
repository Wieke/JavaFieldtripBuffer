package buffer_bci.javaserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import buffer_bci.javaserver.exceptions.ClientException;

public class ServerThread extends Thread {
	private final Socket socket;
	public final String clientAdress;

	public ServerThread(Socket socket) {
		this.socket = socket;
		clientAdress = socket.getInetAddress().toString() + ":"
				+ Integer.toString(socket.getPort());
	}

	@Override
	public void run() {
		try (BufferedOutputStream output = new BufferedOutputStream(
				socket.getOutputStream());
				BufferedInputStream input = new BufferedInputStream(
						socket.getInputStream());) {
			boolean run = true;
			while (run) {
				Message m;
				try {
					m = NetworkProtocol.readMessage(input);
					System.out.println(clientAdress + " Message received " + m);
				} catch (ClientException e) {
					System.out.println(clientAdress + " " + e.getMessage());
					socket.close();
					run = false;
					System.out.println(clientAdress + " Connection closed");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}