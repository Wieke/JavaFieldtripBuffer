package buffer_bci.javaserver.network;

public class WaitRequest {
	public final int nSamples;
	public final int nEvents;
	public final int timeout;

	public WaitRequest(int nSamples, int nEvents, int timeout) {
		this.nSamples = nSamples;
		this.nEvents = nEvents;
		this.timeout = timeout;
	}

	public synchronized void blockUntilSatisfied(long timeout)
			throws InterruptedException {
		wait(timeout);
	}

	public synchronized void satisfied() {
		notifyAll();
	}

}
