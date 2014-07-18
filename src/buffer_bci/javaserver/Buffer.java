package buffer_bci.javaserver;

import java.io.IOException;
import java.net.ServerSocket;


/**
 * Main program. Sets everything up and runs it.
 * @author wieke
 *
 */

public class Buffer {

	public static void main(String[] args) {
		int portNumber;
		if (args.length == 1){
			portNumber = Integer.parseInt(args[1]);
		} else {
			portNumber = 1988;
		}
        
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (true) {
	            new ServerThread(serverSocket.accept()).start();
	        }
	    } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
	}

}
