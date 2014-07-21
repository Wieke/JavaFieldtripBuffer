package buffer_bci.javaserver.network;

import java.net.*;
import java.io.*;

import buffer_bci.javaserver.exceptions.ClientException;

public class ServerThread extends Thread {
    private Socket socket = null;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }
    
    public void run() {
    	System.out.println("Runnig Thread");
        try (
    		BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
    		BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
        ) {
        	while (true){
        		Message m;
				try {
					m = NetworkProtocol.readMessage(input);
					System.out.println(m);
				} catch (ClientException e) {
					System.out.println(socket.getInetAddress().toString() + Integer.toString(socket.getPort()) 
							+ ": " + e.getMessage());
				}
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}