package buffer_bci.javaserver.data;

import buffer_bci.javaserver.exceptions.DataException;

public class Header {
	protected int channelNameSize;
	public int dataType;
	public float fSample;
	public int nChans;
	public int nSamples;
	public int nEvents;
	public String[] labels;

	public Header(int nChans, float fSample, int dataType) {
		this.nChans = nChans;
		this.fSample = fSample;
		nSamples = 0;
		nEvents = 0;
		this.dataType = dataType;
		labels = new String[nChans]; // allocate, but do not fill
	}

	public Header(int nChans, float fSample, int dataType, String[] labels)
			throws DataException {
		this.nChans = nChans;
		this.fSample = fSample;
		nSamples = 0;
		nEvents = 0;
		this.dataType = dataType;

		if (labels.length != nChans) {
			throw new DataException(
					"Number of channels and number of labels do not match.");
		} else {
			this.labels = labels;
		}
	}
}
