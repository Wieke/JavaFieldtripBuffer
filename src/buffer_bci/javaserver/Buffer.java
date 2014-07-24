package buffer_bci.javaserver;

import java.io.IOException;
import java.net.ServerSocket;

import buffer_bci.javaserver.data.DataModel;
import buffer_bci.javaserver.data.RingDataStore;
import buffer_bci.javaserver.data.SimpleDataStore;
import buffer_bci.javaserver.network.ServerThread;

/**
 * Main program. Sets everything up and runs it.
 * 
 * @author wieke
 * 
 */

public class Buffer {

	public static void main(String[] args) {
		int portNumber;
		DataModel dataStore;

		if (args.length == 1) {
			portNumber = Integer.parseInt(args[0]);
			dataStore = new SimpleDataStore();
		} else if (args.length == 2) {
			portNumber = Integer.parseInt(args[0]);
			dataStore = new RingDataStore(Integer.parseInt(args[1]));
		} else if (args.length == 3) {
			portNumber = Integer.parseInt(args[0]);
			dataStore = new RingDataStore(Integer.parseInt(args[1]),
					Integer.parseInt(args[2]));
		} else {
			portNumber = 1972;
			dataStore = new RingDataStore(10000, 1000);
		}

		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			while (true) {
				new ServerThread(serverSocket.accept(), dataStore).start();
			}
		} catch (IOException e) {
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}
	}

}
