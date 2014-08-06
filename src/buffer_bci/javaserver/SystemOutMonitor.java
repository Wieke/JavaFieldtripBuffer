package buffer_bci.javaserver;

import java.util.HashMap;

public class SystemOutMonitor implements FieldtripBufferMonitor {
	private final HashMap<Integer, String> adresses = new HashMap<Integer, String>();
	private int count = 0;

	@Override
	public void updateClientActivity(final int clientID, final long time) {
		System.out.println("Activity! Client " + adresses.get(clientID));
	}

	@Override
	public void updateClientError(final int clientID, final int errorType,
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
	public void updateConnectionClosed(final int clientID) {
		System.out.println("Client " + adresses.get(clientID)
				+ " closed connection now " + --count + " connections opened.");
	}

	@Override
	public void updateConnectionOpened(final int clientID, final String adress) {
		System.out.println("Client opened connection at " + adress + " now "
				+ ++count + " connections opened.");
		adresses.put(clientID, adress);
	}

	@Override
	public void updateDataFlushed() {
		System.out.println("Data Flushed.");
	}

	@Override
	public void updateEventCount(final int count) {
		System.out.println("Event Count " + count);
	}

	@Override
	public void updateEventsFlushed() {
		System.out.println("Events Flushed.");

	}

	@Override
	public void updateHeader(final int dataType, final float fSample,
			final int nChannels) {
		System.out.println("Header added, datatype " + dataType + " fSample "
				+ fSample + " nChannels " + nChannels);
	}

	@Override
	public void updateHeaderFlushed() {
		System.out.println("Header Flushed.");
	}

	@Override
	public void updateSampleCount(final int count) {
		System.out.println("Sample Count " + count);
	}

}