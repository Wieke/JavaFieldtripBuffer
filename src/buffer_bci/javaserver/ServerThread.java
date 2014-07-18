package buffer_bci.javaserver;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    private Socket socket = null;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }
    
    public void run() {
        try (
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			DataInputStream input = new DataInputStream(socket.getInputStream());
        ) {
        	while (true){
        		System.out.println(input.readByte());
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}