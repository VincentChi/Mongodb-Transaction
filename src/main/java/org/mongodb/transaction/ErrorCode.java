package org.mongodb.transaction;

/**
 * Interface for all error code enumerations accessible from exceptions inherited from {@link CumulusBusinessException}
 * 
 * @author andrii.petrenko
 */
public interface ErrorCode {
	
	/**
	 * @return integer representation of error code
	 */
    int getCode();

    /**
    * @return associated message that is not localization, but key in *.properties file
    */
    String getMessage();

}
