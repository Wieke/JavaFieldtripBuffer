package buffer_bci.javaserver.data;

import nl.fcdonders.fieldtrip.BufferEvent;
import buffer_bci.javaserver.exceptions.DataException;

/**
 * Class responsible for data storage.
 *
 * @author wieke
 *
 */

public class DataStore extends DataModel {

	@Override
	public void addPollListener() {
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
	public void getData() throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public BufferEvent[] getEvent(int begin, int end) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getEventCount() throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public Header getHeader() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getSampleCount() throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void putData() throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void putEvent(BufferEvent event) throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void putHeader(Header hdr) throws DataException {
		// TODO Auto-generated method stub

	}

	// Use synchronized keyword for pretty much all functions, in order to
	// guarantee thread safety.

}
