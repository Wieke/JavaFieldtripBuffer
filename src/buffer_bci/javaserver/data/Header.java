package buffer_bci.javaserver.data;

public class Header {
	protected int channelNameSize;
	public int dataType;
	public float fSample;
	public int nChans;
	public int nSamples;
	public int nEvents;
	public String[] labels;	
	
	public Header(int nChans, float fSample, int dataType) {
		this.nChans   = nChans;
		this.fSample  = fSample;
		this.nSamples = 0;
		this.nEvents  = 0;
		this.dataType = dataType;
		this.labels   = new String[nChans]; // allocate, but do not fill
	}
	
	public Header(int nChans, float fSample, int dataType, String[] labels) {
		this.nChans   = nChans;
		this.fSample  = fSample;
		this.nSamples = 0;
		this.nEvents  = 0;
		this.dataType = dataType;
		if (labels.length != nChans){
			
		} else {
			this.labels = labels;
		}
	}
}
