package buffer_bci.javaserver.data;

import nl.fcdonders.fieldtrip.BufferEvent;
import buffer_bci.javaserver.exceptions.DataException;
import buffer_bci.javaserver.network.Request;

public abstract class DataModel {

	public abstract void addPollListener();

	public abstract void flushData() throws DataException;

	public abstract void flushEvents() throws DataException;

	public abstract void flushHeader() throws DataException;

	public abstract Data getData(Request request) throws DataException;

	public abstract BufferEvent[] getEvent(int begin, int end)
			throws DataException;

	public abstract Event getEvent(Request request) throws DataException;

	public abstract int getEventCount() throws DataException;

	public abstract Header getHeader() throws DataException;

	public abstract int getSampleCount() throws DataException;

	public abstract void putData(Data data) throws DataException;

	public abstract void putEvent(Event event) throws DataException;

	public abstract void putHeader(Header hdr) throws DataException;

}
