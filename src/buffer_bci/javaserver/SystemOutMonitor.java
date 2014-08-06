package buffer_bci.javaserver;

import java.util.HashMap;

public class SystemOutMonitor implements FieldtripBufferMonitor {
	private final HashMap<Integer, String> adresses = new HashMap<Integer, String>();
	private int count = 0;

	@Override
	public void clientClosedConnection(final int clientID) {
		System.out.println("Client " + adresses.get(clientID)
				+ " closed connection now " + --count + " connections opened.");
	}

	@Override
	public void clientContinues(final int clientID) {
		System.out.println("Client " + adresses.get(clientID)
				+ " has continued.");
	}

	@Override
	public void clientError(final int clientID, final int errorType,
			final long time) {
		if (errorType == FieldtripBufferMonitor.ERROR_CONNECTION) {
			System.out.println("Lost client " + adresses.get(clientID)
					+ " connection unexpectidly at " + time);
		} else if (errorType == FieldtripBufferMonitor.ERROR_PROTOCOL) {
			System.out.println("Client " + adresses.get(clientID)
					+ " violates protocl at " + time);
		} else {
			System.out.println("Client " + adresses.get(clientID)
					+ " has wrong version at " + time);
		}
	}

	@Override
	public void clientFlushedData(final int clientID) {
		System.out.println("Data Flushed by " + adresses.get(clientID));
	}

	@Override
	public void clientFlushedEvents(final int clientID) {
		System.out.println("Events Flushed by " + adresses.get(clientID));

	}

	@Override
	public void clientFlushedHeader(final int clientID) {
		System.out.println("Header Flushed by " + adresses.get(clientID));
	}

	@Override
	public void clientGetEvents(final int count, final int clientID) {
		System.out.println("Client " + adresses.get(clientID)
				+ " has been sent " + count + " events.");

	}

	@Override
	public void clientGetHeader(final int clientID) {
		System.out.println("Client " + adresses.get(clientID)
				+ " has been sent the header.");
	}

	@Override
	public void clientGetSamples(final int count, final int clientID) {
		System.out.println("Client " + adresses.get(clientID)
				+ " has been sent " + count + " samples.");
	}

	@Override
	public void clientOpenedConnection(final int clientID, final String adress) {
		System.out.println("Client opened connection at " + adress + " now "
				+ ++count + " connections opened.");
		adresses.put(clientID, adress);
	}

	@Override
	public void clientPutEvents(final int count, final int clientID,
			final int diff) {
		System.out.println("Client " + adresses.get(clientID) + " added "
				+ diff + " events, total now " + count);
	}

	@Override
	public void clientPutHeader(final int dataType, final float fSample,
			final int nChannels, final int clientID) {
		System.out.println("Header added by " + adresses.get(clientID)
				+ " datatype " + dataType + " fSample " + fSample
				+ " nChannels " + nChannels);
	}

	@Override
	public void clientPutSamples(final int count, final int clientID,
			final int diff) {
		System.out.println("Client " + adresses.get(clientID) + " added "
				+ diff + " samples, total now " + count);
	}

	@Override
	public void clientWaits(final int nSamples, final int nEvents,
			final int timeout, final int clientID) {
		System.out.println("Client " + adresses.get(clientID)
				+ " is now waiting for either sample count to reach "
				+ nSamples + " or event count to reach " + nEvents
				+ " with a timout of " + timeout);
	}

}