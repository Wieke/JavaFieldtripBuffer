package buffer_bci.javaserver.data;

import buffer_bci.javaserver.exceptions.DataException;
import buffer_bci.javaserver.network.Request;
import buffer_bci.javaserver.network.ServerThread;
import buffer_bci.javaserver.network.WaitRequest;

public class SimpleDataStore extends DataModel {

	@Override
	public void addPollListener(ServerThread listener, WaitRequest request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void flushData() throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void flushEvents() throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void flushHeader() throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public Data getData(Request request) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEventCount() throws DataException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Event[] getEvents(Request request) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Header getHeader() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSampleCount() throws DataException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void putData(Data data) throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void putEvents(Event[] events) throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void putHeader(Header hdr) throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePollListener(ServerThread listener, WaitRequest request) {
		// TODO Auto-generated method stub

	}

}
