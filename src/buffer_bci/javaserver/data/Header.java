package buffer_bci.javaserver.data;

import java.nio.ByteOrder;

/**
 * Wrapper class for header information.
 * 
 * @author lkanters
 * 
 */
public class Header {
	public final int dataType;
	public final float fSample;
	public final int nChans;
	public final int nSamples;
	public final int nEvents;
	public final Chunk[] chunks;
	public final int nChunks;
	public final ByteOrder order;

	/**
	 * Constructor
	 * 
	 * @param nChans
	 *            Number of channels
	 * @param fSample
	 *            Sampling frequency
	 * @param dataType
	 *            Datatype of the data
	 * @param order
	 *            Bytorder of the data
	 */
	public Header(int nChans, float fSample, int dataType, ByteOrder order) {
		this.nChans = nChans;
		this.fSample = fSample;
		nSamples = 0;
		nEvents = 0;
		this.dataType = dataType;
		chunks = null;
		nChunks = 0;
		this.order = order;
	}

	/**
	 * Constructor
	 * 
	 * @param nChans
	 *            Number of channels
	 * @param fSample
	 *            Sampling frequency
	 * @param dataType
	 *            Datatype of the data
	 * @param chunks
	 *            Extended header chunks
	 * @param order
	 *            Bytorder of the data
	 */
	public Header(int nChans, float fSample, int dataType, Chunk[] chunks,
			ByteOrder order) {
		this.nChans = nChans;
		this.fSample = fSample;
		nSamples = 0;
		nEvents = 0;
		this.dataType = dataType;
		this.chunks = chunks;
		nChunks = chunks.length;
		this.order = order;
	}

	/**
	 * Constructor
	 * 
	 * @param nChans
	 *            Number of channels
	 * @param nSamples
	 *            Number of samples
	 * @param nEvents
	 *            Number of events
	 * @param fSample
	 *            Sampling frequency
	 * @param dataType
	 *            Datatype of the data
	 * @param order
	 *            Bytorder of the data
	 */
	public Header(int nChans, int nSamples, int nEvents, int fSample,
			int dataType, ByteOrder order) {
		this.nChans = nChans;
		this.fSample = fSample;
		this.nSamples = nSamples;
		this.nEvents = nEvents;
		this.dataType = dataType;
		chunks = null;
		nChunks = 0;
		this.order = order;
	}

	/**
	 * Constructor
	 * 
	 * @param nChans
	 *            Number of channels
	 * @param nSamples
	 *            Number of samples
	 * @param nEvents
	 *            Number of events
	 * @param fSample
	 *            Sampling frequency
	 * @param dataType
	 *            Datatype of the data
	 * @param chunks
	 *            Extended header chunks
	 * @param order
	 *            Bytorder of the data
	 */
	public Header(int nChans, int nSamples, int nEvents, int fSample,
			int dataType, Chunk[] chunks, ByteOrder order) {
		this.nChans = nChans;
		this.fSample = fSample;
		this.nSamples = nSamples;
		this.nEvents = nEvents;
		this.dataType = dataType;
		this.chunks = chunks;
		nChunks = chunks.length;
		this.order = order;
	}
}
