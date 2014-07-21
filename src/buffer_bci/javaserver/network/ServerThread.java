package buffer_bci.javaserver.network;

import java.net.*;
import java.io.*;

import buffer_bci.javaserver.exceptions.ClientException;

public class ServerThread extends Thread {
    private final Socket socket;
    public final String clientAdress;

    public ServerThread(Socket socket) {
        this.socket = socket;
        this.clientAdress = socket.getInetAddress().toString() + ":" + Integer.toString(socket.getPort());
    }
    
    public void run() {
        try (
    		BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
    		BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
        ) {
        	boolean run = true;
        	while (run){
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