package buffer_bci.javaserver;

public interface FieldtripBufferMonitor {
	public static final int ERROR_PROTOCOL = 0;
	public static final int ERROR_CONNECTION = 1;
	public static final int ERROR_VERSION = 2;

	public void clientClosedConnection(int clientID);

	public void clientContinues(int clientID);

	public void clientError(int clientID, int errorType, long time);

	public void clientFlushedData(int clientID);

	public void clientFlushedEvents(int clientID);

	public void clientFlushedHeader(int clientID);

	public void clientGetEvents(int count, int clientID);

	public void clientGetHeader(int clientID);

	public void clientGetSamples(int count, int clientID);

	public void clientOpenedConnection(int clientID, String adress);

	public void clientPutEvents(int count, int clientID, int diff);

	public void clientPutHeader(int dataType, float fSample, int nChannels,
			int clientID);

	public void clientPutSamples(int count, int clientID, int diff);

	public void clientWaits(int nSamples, int nEvents, int timeout, int clientID);
}
