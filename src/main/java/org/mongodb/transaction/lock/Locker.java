package org.mongodb.transaction.lock;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class Locker
{
	private boolean          rollbackable;
	private long             lockExpiredTime;

	private Map<DB, Lock>    _locks                    = new HashMap<DB, Lock>();

	public Locker(boolean rollbackable)
	{
		this(rollbackable, Lock.DEFAULT_LOCK_EXPIRED_TIME);
	}

	public Locker(boolean rollbackable,
	              long expiredTime)
	{
		this.rollbackable = rollbackable;
		this.lockExpiredTime = expiredTime;
	}

	public void lock(DBCollection col, DBObject query) throws DBLockException
	{
		getLock(col).lock(col, query);
	}
	
	public void lockInsert(DBCollection col, Object id) throws DBLockException
	{
		getLock(col).lockInsert(col, id);
	}

	public void unlock()
	{
		for(Lock lock: _locks.values()) {
			lock.unlock();
		}
	}

	public void rollback()
	{
		for(Lock lock: _locks.values()) {
			lock.rollback();
		}
	}

	private Lock getLock(DBCollection col)
	{
		DB db = col.getDB();
		if (!_locks.containsKey(db))
		{
			Lock lock = new Lock(db, rollbackable, lockExpiredTime);
			_locks.put(db, lock);
			return lock;
		}
		return _locks.get(db);
	}
}
