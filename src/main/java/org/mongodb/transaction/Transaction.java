package org.mongodb.transaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.mongodb.transaction.lock.DBLockException;
import org.mongodb.transaction.lock.Locker;
import org.mongodb.transaction.lock.LockerManager;

import com.mongodb.DB;
import com.mongodb.DBApiLayer;
import com.mongodb.DBCollection;
import com.mongodb.TransactionalDBCollection;

@SuppressWarnings("deprecation")
public class Transaction
{
	private final static ConcurrentMap<DB,ConcurrentMap<String,TransactionalDBCollection>> _dbs = new ConcurrentHashMap<DB,ConcurrentMap<String,TransactionalDBCollection>>();
	private final static ThreadLocal<Locker> lockers = new ThreadLocal<Locker>();
	
	public static TransactionalDBCollection transactinal(DBCollection dbCollection, IApplyHandler handler)
	{
		TransactionalDBCollection col = transactinal(dbCollection);
		col.setApplyHandler(handler);
		return col;
	}
	
	public static TransactionalDBCollection transactinal(DBCollection dbCollection)
	{
		ConcurrentMap<String,TransactionalDBCollection> _collections = _dbs.get(dbCollection.getDB().getName());
		if(_collections !=null) {
			TransactionalDBCollection c = _collections.get(dbCollection.getName());
	        if ( c != null )
	            return c;
		} else {
			_collections = new ConcurrentHashMap<String,TransactionalDBCollection>();
			_dbs.putIfAbsent(dbCollection.getDB(), _collections);
		}
		
		TransactionalDBCollection c = new TransactionalDBCollection(dbCollection);
		TransactionalDBCollection old = _dbs.get(dbCollection.getDB()).putIfAbsent(dbCollection.getName(), c);
		return old != null ? old : c;
	}
	
	/**
     * Lock all DB records used in current thread. <br>
     * The locked data is rollbackable as default. Call {@link #rollback()} if
     * needed.
     */
    public static void start() {
    	start(true);
    }

    /**
     * Lock all DB records used in current thread for a while. <br>
     * The locked data is rollbackable as default. Call {@link #rollback()} if
     * needed.
     */
    public static void start(long lockTime) {
    	Locker locker = LockerManager.getLocker(true, lockTime);
    	lockers.set(locker);
    }

    /**
     * Lock all DB records used in current thread. <br>
     * If rollbackable is true, call {@link #rollback()} when needed.<br>
     * If deleteOldBeforeRollback is true, would delete old values then rollback
     * 
     * @param rollbackable
     * @param deleteOldBeforeRollback
     */
    public static void start(boolean rollbackable) {
    	Locker locker = LockerManager.getLocker(rollbackable);
        lockers.set(locker);
    }
    
    /**
     * Unlock locked data when operation finished. 
     * This method must be called after transaction finished, or the lock would not be removed and be used by other service
     * 
     */
    public static void end() {
    	Locker locker = getLocker();

        if (locker != null) {
            locker.unlock();
            lockers.remove();
        }
    }
    
    /**
     * Rollback transaction to the time before {@link #lock()}.<br> 
     * Could be called when exception caught or other conditions. 
     * @throws DBLockException
     */
    public static void rollback() {
    	Locker locker = getLocker();

        if (locker != null) {
        	locker.rollback();
        	lockers.remove();
        }
    }
    
    public static Locker getLocker() {
    	return lockers.get();
    }
}
