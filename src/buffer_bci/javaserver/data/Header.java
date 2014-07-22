package buffer_bci.javaserver.data;

import buffer_bci.javaserver.exceptions.DataException;

public class Header {
	public final int dataType;
	public final float fSample;
	public final int nChans;
	public final int nSamples;
	public final int nEvents;
	public final Chunk[] chunks;
	public final int nChunks;

	public Header(int nChans, float fSample, int dataType) {
		this.nChans = nChans;
		this.fSample = fSample;
		nSamples = 0;
		nEvents = 0;
		this.dataType = dataType;
		chunks = null;
		nChunks = 0;
	}

	public Header(int nChans, float fSample, int dataType, Chunk[] chunks)
			throws DataException {
		this.nChans = nChans;
		this.fSample = fSample;
		nSamples = 0;
		nEvents = 0;
		this.dataType = dataType;
		this.chunks = chunks;
		nChunks = chunks.length;
	}
}
