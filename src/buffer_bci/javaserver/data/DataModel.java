package buffer_bci.javaserver.data;

import nl.fcdonders.fieldtrip.BufferEvent;
import nl.fcdonders.fieldtrip.Header;

public abstract class DataModel {

	public abstract void putHeader(Header hdr);
	
	public abstract Header getHeader();
	
	public abstract void flushHeader();
	
	public abstract void putData();
	
	public abstract void getData();
	
	public abstract void flushData();
	
	public abstract void putEvent(BufferEvent event);
	
	public abstract BufferEvent[] getEvent(int begin, int end);
	
	public abstract void flushEvents();
	
	public abstract void getSampleCount();
	
	public abstract void getEventCount();
	
	public abstract void addPollListener();
	
}
