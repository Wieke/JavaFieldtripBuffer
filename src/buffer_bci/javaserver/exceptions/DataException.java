package buffer_bci.javaserver.exceptions;

/**
 * An exception thrown when there is an error/inconsistency within the data.
 * @author wieke
 *
 */
public class DataException extends Exception {

	public DataException(String string) {
		super(string);
	}

}
