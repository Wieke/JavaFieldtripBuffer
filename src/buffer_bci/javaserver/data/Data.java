package buffer_bci.javaserver.data;

import java.nio.ByteOrder;

public class Data {
	public final byte[][][] data;
	public final int dataType;
	public final int nChans;
	public final int nSamples;
	public final ByteOrder order;
	public final int nBytes;

	/**
	 * Container for a chunk of data.
	 *
	 * @param data
	 *            The data in bytes [nSamples][nChans][nBytes]
	 * @param dataType
	 *            The data type.
	 * @param dataType2
	 */
	public Data(int nChans, int nSamples, int nBytes, int dataType,
			byte[][][] data, ByteOrder order) {
		this.data = data;
		this.dataType = dataType;
		this.nChans = nChans;
		this.nSamples = nSamples;
		this.order = order;
		this.nBytes = nBytes;
	}
}
