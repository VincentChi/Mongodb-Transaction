package org.mongodb.transaction;

import com.mongodb.DBObject;

/**
 * Apply handler would be invoked each time DBObject inserted or updated.<br>
 * This handler could be added to any DBCollection using Transaction util.
 * @author Vincent Chi
 *
 */
public interface IApplyHandler
{
	void apply(DBObject obj);
}
