package buffer_bci.javaserver;

import java.io.IOException;
import java.net.ServerSocket;

import buffer_bci.javaserver.data.DataModel;
import buffer_bci.javaserver.data.RingDataStore;
import buffer_bci.javaserver.data.SimpleDataStore;
import buffer_bci.javaserver.network.ConnectionThread;

/**
 * Buffer class, a thread that opens a serverSocket to listen for connections
 * and starts a connectionThread to handle them.
 *
 * @author wieke
 *
 */
public class Buffer extends Thread {
	/**
	 * Main method, starts running a server thread in the current thread.
	 * Handles arguments.
	 *
	 * @param args
	 *            <port> or <port> <nSamplesAndEvents> or <port> <nSamples>
	 *            <nEvents>
	 */
	public static void main(final String[] args) {
		if (args.length == 1) {
			new Buffer(Integer.parseInt(args[0])).run();
		} else if (args.length == 2) {
			new Buffer(Integer.parseInt(args[0]), Integer.parseInt(args[1]))
			.run();
		} else if (args.length == 3) {
			new Buffer(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
					Integer.parseInt(args[2])).run();
		} else {
			new Buffer(1972, 10000, 1000).run();
		}
	}

	private final DataModel dataStore;

	private final int portNumber;
	private ServerSocket serverSocket;

	/**
	 * Constructor, creates a simple datastore.
	 *
	 * @param portNumber
	 */
	public Buffer(final int portNumber) {
		this.portNumber = portNumber;
		dataStore = new SimpleDataStore();
		setName("Fieldtrip Buffer Server");
	}

	/**
	 * Constructor, creates a ringbuffer that stores nSamplesEvents number of
	 * samples and events.
	 *
	 * @param portNumber
	 * @param nSamplesEvents
	 */
	public Buffer(final int portNumber, final int nSamplesEvents) {
		this.portNumber = portNumber;
		dataStore = new RingDataStore(nSamplesEvents);
		setName("Fieldtrip Buffer Server");
	}

	/**
	 * Constructor, creates a ringbuffer that stores nSamples of samples and
	 * nEvents of events.
	 *
	 * @param portNumber
	 * @param nSamples
	 * @param nEvents
	 */
	public Buffer(final int portNumber, final int nSamples, final int nEvents) {
		this.portNumber = portNumber;
		dataStore = new RingDataStore(nSamples, nEvents);
		setName("Fieldtrip Buffer Server");
	}

	/**
	 * Attempts to close the current serverSocket.
	 * 
	 * @throws IOException
	 */
	public void closeConnection() throws IOException {
		serverSocket.close();
	}

	/**
	 * Opens a serverSocket and starts listening for connections.
	 */
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(portNumber);
			while (true) {
				new ConnectionThread(serverSocket.accept(), dataStore).start();
			}
		} catch (final IOException e) {
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}
	}
}
