package org.apidb.apicommon.datasetPresenter;

/**
 * An exception caused by factors other than user error.
 * @author steve
 *
 */
public class UnexpectedException extends RuntimeException {

    public static String modelName;

    private static final long serialVersionUID = 442861349675564533L;

    public UnexpectedException() {
        super();
    }

    public UnexpectedException(String message) {
        super(message);
    }

    public UnexpectedException(Throwable cause) {
        super(cause);
    }

    public UnexpectedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
