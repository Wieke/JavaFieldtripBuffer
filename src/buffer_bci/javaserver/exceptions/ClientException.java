package buffer_bci.javaserver.exceptions;

/**
 * An exception thrown when the client does something it shouldn't.
 * 
 * @author wieke
 *
 */
public class ClientException extends Exception {

	public ClientException(String string) {
		super(string);
	}
}
