package buffer_bci.javaserver.data;

import buffer_bci.javaserver.exceptions.DataException;
import buffer_bci.javaserver.network.Request;
import buffer_bci.javaserver.network.ServerThread;
import buffer_bci.javaserver.network.WaitRequest;

public abstract class DataModel {

	public abstract void addWaitListener(ServerThread listener,
			WaitRequest request);

	public abstract void flushData() throws DataException;

	public abstract void flushEvents() throws DataException;

	public abstract void flushHeader() throws DataException;

	public abstract Data getData(Request request) throws DataException;

	public abstract int getEventCount() throws DataException;

	public abstract Event[] getEvents(Request request) throws DataException;

	public abstract Header getHeader() throws DataException;

	public abstract int getSampleCount() throws DataException;

	public abstract void putData(Data data) throws DataException;

	public abstract void putEvents(Event[] events) throws DataException;

	public abstract void putHeader(Header hdr) throws DataException;

	public abstract void removeWaitListener(ServerThread listener);

}
