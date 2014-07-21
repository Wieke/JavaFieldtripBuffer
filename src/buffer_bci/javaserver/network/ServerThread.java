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
        	boolean run = true;
        	while (run){
        		Message m;
				try {
					m = NetworkProtocol.readMessage(input);
					System.out.println(m);
				} catch (ClientException e) {
					System.out.println(clientAdress() + " " + e.getMessage());
					if (e.getMessage().startsWith("Client/Server version conflict.")){
						socket.close();
						run = false;
						System.out.println(clientAdress() + " Connection closed");
					}
				}
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String clientAdress(){
    	return socket.getInetAddress().toString() + ":" + Integer.toString(socket.getPort());
    }
}