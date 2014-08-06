package buffer_bci.javaserver;

public interface FieldtripBufferMonitor {

	public void updateClientActivity(int clientID, long time);

	public void updateConnectionClosed(int clientID);

	public void updateConnectionOpened(int clientID, String adress, int count);

	public void updateDataFlushed();

	public void updateEventCount(int count);

	public void updateEventsFlushed();

	public void updateHeader(int dataType, float fSample, int nChannels);

	public void updateHeaderFlushed();

	public void updateSampleCount(int count);
}
