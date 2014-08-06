package buffer_bci.javaserver;

public interface FieldtripBufferMonitor {
	public static final int ERROR_PROTOCOL = 0;
	public static final int ERROR_CONNECTION = 1;
	public static final int ERROR_VERSION = 2;

	public void updateClientActivity(int clientID, long time);

	public void updateClientError(int clientID, int errorType, long time);

	public void updateConnectionClosed(int clientID);

	public void updateConnectionOpened(int clientID, String adress);

	public void updateDataFlushed(int clientID);

	public void updateEventCount(int count, int clientID, int diff);

	public void updateEventsFlushed(int clientID);

	public void updateHeader(int dataType, float fSample, int nChannels,
			int clientID);

	public void updateHeaderFlushed(int clientID);

	public void updateSampleCount(int count, int clientID, int diff);
}
