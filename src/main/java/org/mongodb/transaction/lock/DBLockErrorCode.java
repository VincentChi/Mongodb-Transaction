package org.mongodb.transaction.lock;

import org.mongodb.transaction.ErrorCode;


public enum DBLockErrorCode implements ErrorCode {
	RecordsLocked(100,"org.mongodb.transaction.lock.DBLockErrorCode.RecordsLocked"),
	RetryLockDisabled(200,"org.mongodb.transaction.lock.DBLockErrorCode.RetryLockDisabled"),
	RetryLockTimeOut(300,"org.mongodb.transaction.lock.DBLockErrorCode.RetryLockTimeOut");
	
	private int _code;
    private String _message;
    DBLockErrorCode(int code, String message) {
        _code = code;
        _message = message;
    }

    /**
     * @return associated error code
     */
    @Override
    public int getCode() {
        return _code;
    }

    /**
     *
     * @return associated message that is not localization, but key in *.properties file
     */
    @Override
    public String getMessage() {
        return _message;
    }
}
