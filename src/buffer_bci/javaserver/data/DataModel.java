package buffer_bci.javaserver.data;

import nl.fcdonders.fieldtrip.BufferEvent;
import buffer_bci.javaserver.exceptions.DataException;

public abstract class DataModel {

	public abstract void addPollListener();

	public abstract void flushData() throws DataException;

	public abstract void flushEvents() throws DataException;

	public abstract void flushHeader() throws DataException;

	public abstract void getData() throws DataException;

	public abstract BufferEvent[] getEvent(int begin, int end)
			throws DataException;

	public abstract void getEventCount() throws DataException;

	public abstract Header getHeader() throws DataException;

	public abstract void getSampleCount() throws DataException;

	public abstract void putData() throws DataException;

	public abstract void putEvent(BufferEvent event) throws DataException;

	public abstract void putHeader(Header hdr) throws DataException;

}
