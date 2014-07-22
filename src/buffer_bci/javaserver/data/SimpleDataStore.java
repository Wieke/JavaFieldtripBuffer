package buffer_bci.javaserver.data;

import buffer_bci.javaserver.exceptions.DataException;
import buffer_bci.javaserver.network.Request;

public class SimpleDataStore extends DataModel {

	@Override
	public void addPollListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flushData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flushEvents() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flushHeader() {
		// TODO Auto-generated method stub

	}

	@Override
	public Data getData(Request request) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Event[] getEvent(int begin, int end) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Event getEvent(Request request) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEventCount() throws DataException {
		// TODO Auto-generated method stub
		return 0;
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
	public void putEvent(Event event) throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void putHeader(Header hdr) throws DataException {
		// TODO Auto-generated method stub

	}

}
