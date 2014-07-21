package buffer_bci.javaserver.data;

import nl.fcdonders.fieldtrip.BufferEvent;

public abstract class DataModel {

	public abstract void addPollListener();

	public abstract void flushData();

	public abstract void flushEvents();

	public abstract void flushHeader();

	public abstract void getData();

	public abstract BufferEvent[] getEvent(int begin, int end);

	public abstract void getEventCount();

	public abstract Header getHeader();

	public abstract void getSampleCount();

	public abstract void putData();

	public abstract void putEvent(BufferEvent event);

	public abstract void putHeader(Header hdr);

}
