package buffer_bci.javaserver.network;

public class Request {
	public final int begin;
	public final int end;

	public Request(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
}
