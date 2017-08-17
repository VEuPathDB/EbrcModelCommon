package org.apidb.apicommon.datasetPresenter;

/**
 * An exception caused by a user error, such as an incorrect file name or missing property
 * @author steve
 *
 */
public class UserException extends RuntimeException {

    public static String modelName;

    private static final long serialVersionUID = 442861349675564533L;

    public UserException() {
        super();
    }

    public UserException(String message) {
        super(message);
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    public UserException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
