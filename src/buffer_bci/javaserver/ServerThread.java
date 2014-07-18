package buffer_bci.javaserver;

import java.net.*;
import java.io.*;

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
        		Message m = NetworkProtocol.readMessage(input);
        		System.out.println(m);
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}