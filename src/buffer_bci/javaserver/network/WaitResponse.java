package buffer_bci.javaserver.network;

public class WaitResponse {
	public final int nSamples;
	public final int nEvents;

	public WaitResponse(int nSamples, int nEvents) {
		this.nSamples = nSamples;
		this.nEvents = nEvents;
	}
}
