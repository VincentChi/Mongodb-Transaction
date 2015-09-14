package org.mongodb.transaction.lock;

@SuppressWarnings("serial")
public class DBLockException extends RuntimeException {
	DBLockErrorCode _code;

	/**
	 * Constructor with DBLockErrorCode (default)
	 * 
	 * @param code
	 *            DBLockErrorCode enum value
	 */
	public DBLockException(DBLockErrorCode code) {
		_code = code;
	}

	/**
	 * Constructor with DBLockErrorCode
	 * 
	 * @param code
	 *            DBLockErrorCode enum value
	 * @param message
	 */
	public DBLockException(DBLockErrorCode code, String message) {
		super(message);
		_code = code;
	}

	/**
	 * Constructor with DBLockErrorCode and inner Exception
	 * 
	 * @param code
	 *            DBLockErrorCode enum value
	 */
	public DBLockException(DBLockErrorCode code, Throwable cause) {
		super(cause);
		_code = code;
	}

	/**
	 * @return reason of exception
	 */
	public DBLockErrorCode getErrorCode() {
		return _code;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder(_code.name());
		String message = super.getMessage();
		if (message != null) {
			sb.append(": ").append(message);
		}
		return sb.toString();
	}
}
