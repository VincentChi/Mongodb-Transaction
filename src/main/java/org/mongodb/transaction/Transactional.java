package org.mongodb.transaction;

/**
 * 
 * @author Vincent Chi
 * 
 */
public interface Transactional
{
	void setAutoCommit(boolean autoCommit);
}
