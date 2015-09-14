package org.mongodb.transaction.lock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.transaction.DAOFramework;
import org.mongodb.transaction.TransactionalDAO;
import org.mongodb.transaction.entity.LongBasedEntity;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

public class DBLock {
	public static final String FIELD_ID = "_id";
	public static final String FIELD_LOCK_TIME = "_lockTime";
	public static final String FIELD_TRANSACTION_ID = "_transactionId";
	public static final String FIELD_OL_VERSION = "olVersion";

	public static final String ROLL_BACK_COLLECTION_NAME = "transactionBackup";
	public static final String ROLL_BACK_FIELD_ID = "_id";
	public static final String ROLL_BACK_FIELD_DATA = "data";
	public static final String ROLL_BACK_FIELD_DATA_ID = "dataId";
	public static final String ROLL_BACK_FIELD_COLLECTION = "collection";
	public static final String ROLL_BACK_FIELD_TID = "transactionId";
	public static final String ROLL_BACK_FIELD_IS_INSERT = "isInsert";

	private static final DBObject DB_SELECT_ID = new BasicDBObject(FIELD_ID, 1);
	
	private MongoClient mongoClient;
	private String dbName;
	
	private static TransactionalDAO<TransactionBackupEntity, Long> backupDao;

	private static final boolean retryLock = true;
	private static final boolean deleteOldBeforeRollback = true;
	private static final int DEFAULT_LOCK_EXPIRED_TIME = 10*1000;
	private static final int minRetryLockInterval = 100;
	private long lockExpiredTime = DEFAULT_LOCK_EXPIRED_TIME;

	private String transactionId;
	private boolean rollbackable;
	private boolean commited;
	private long    firstLockTime = 0;
	
	private Map<TransactionalDAO, List<DBObject>> daoQueriesMap = new HashMap<TransactionalDAO, List<DBObject>>();
	private Map<TransactionalDAO, Set<Object>> daoBackupIdsMap = new HashMap<TransactionalDAO, Set<Object>>();

	private DBObject lockUnsetObject;
	
	// In debug mode, the lock will never expire and never throw RetryLockTimeOut exception
	private boolean debugMode = false;

	public DBLock(MongoClient mongoClient, String dbName) {
		this(mongoClient, dbName, false);
	}

	public DBLock(MongoClient mongoClient, String dbName, boolean rollbackable) {
		this(mongoClient, dbName, rollbackable, DEFAULT_LOCK_EXPIRED_TIME);
	}

	public DBLock(MongoClient mongoClient, String dbName, boolean rollbackable, long expiredTime) {
		this.transactionId = generateTransactionId();
		this.rollbackable = rollbackable;
		this.lockExpiredTime = expiredTime > DEFAULT_LOCK_EXPIRED_TIME ? expiredTime : DEFAULT_LOCK_EXPIRED_TIME;
		this.backupDao = TransactionalDAO.getInstance(TransactionBackupEntity.class, mongoClient, dbName);

		DBObject unsetFields = new BasicDBObject();
		unsetFields.put(FIELD_LOCK_TIME, 1);
		unsetFields.put(FIELD_TRANSACTION_ID, 1);
		this.lockUnsetObject = new BasicDBObject(TransactionalDAO.$unset,
				unsetFields);
	}

	public void lock(TransactionalDAO dao, Collection<Object> ids)
			throws DBLockException {
		DBObject query = new BasicDBObject(FIELD_ID, new BasicDBObject(
				TransactionalDAO.$in, ids));
		lock(dao, query);
	}

	public void lock(TransactionalDAO dao, Object id) throws DBLockException {
		lock(dao, new BasicDBObject(FIELD_ID, id));
	}
	
	public void lockInsert(TransactionalDAO dao, Object id) throws DBLockException {
		Set<Object> backupIds = daoBackupIdsMap.get(dao);
		if(backupIds==null) {
			backupIds=new HashSet<Object>();
			daoBackupIdsMap.put(dao, backupIds);
		}
		if(id!=null) {
			if (!backupIds.contains(id)) {
				DBObject dest = new BasicDBObject();
				//dest.put(ROLL_BACK_FIELD_ID, DAOUtils.generateLongID(backupDao));
				//dest.put(ROLL_BACK_FIELD_DATA, record);
				dest.put(ROLL_BACK_FIELD_DATA_ID, id);
				dest.put(ROLL_BACK_FIELD_COLLECTION, dao.getCollection().getName());
				dest.put(ROLL_BACK_FIELD_TID, transactionId);
				dest.put(ROLL_BACK_FIELD_IS_INSERT, true);
				// ROLL_BACK_FIELD_ROLLBACKED //TODO rollback status
				backupDao.getCollection().save(dest);
				backupIds.add(id);
			}
		}
	}

	public void lock(TransactionalDAO dao, DBObject query) throws DBLockException {
		if (lock(dao, query, false)) {
			backupRecord(dao, query);
		} else {
			if (retryLock) {
				retryLock();
			} else {
				rollback();
				throw new DBLockException(DBLockErrorCode.RetryLockDisabled);
			}
		}
	}

	private boolean lock(TransactionalDAO dao, DBObject query, boolean relock) {
		long count = dao.getCollection().getCount(query);
		long currentTime = currentTimeMillis();

        if (!daoQueriesMap.containsKey(dao)) {
            daoQueriesMap.put(dao, new ArrayList<DBObject>());
        }
        if (!relock) {
            List<DBObject> dbObjects = dao.getCollection().find(query, DB_SELECT_ID).toArray();
            List<Object> existsIds = new ArrayList<Object>(dbObjects.size());
            for (DBObject dbObject : dbObjects) {
                existsIds.add(dbObject.get(FIELD_ID));
            }

            DBObject newQuery = new BasicDBObject(FIELD_ID, new BasicDBObject(
                    TransactionalDAO.$in, existsIds));

            daoQueriesMap.get(dao).add(newQuery);
        }

		DBObject updateQuery = new BasicDBObject();
		updateQuery.putAll(query);
		QueryBuilder updateQueryBuilder = new QueryBuilder();
		updateQueryBuilder.or(new BasicDBObject(FIELD_TRANSACTION_ID, null),
				new BasicDBObject(FIELD_TRANSACTION_ID, transactionId),
				new BasicDBObject(FIELD_LOCK_TIME, new BasicDBObject(
						TransactionalDAO.$lt, currentTime - lockExpiredTime)));
		updateQuery.putAll(updateQueryBuilder.get());

		DBObject update = new BasicDBObject();
		update.put(FIELD_LOCK_TIME, currentTimeMillis());
		update.put(FIELD_TRANSACTION_ID, transactionId);
		DBObject updateSet = new BasicDBObject(TransactionalDAO.$set, update);

		WriteResult result = dao.getCollection().updateMulti(updateQuery, updateSet);
		if (result.getN() < count) {
			return false;
        } else {
            return true;
        }
	}

	private void backupRecord(TransactionalDAO dao, DBObject query)
			throws DBLockException {
		if (!rollbackable || ROLL_BACK_COLLECTION_NAME.equals(dao.getCollection().getName()))
			return;

		if (!daoBackupIdsMap.containsKey(dao)) {
			daoBackupIdsMap.put(dao, new HashSet<Object>());
		}
		Set<Object> backupIds = daoBackupIdsMap.get(dao);

		DBCursor cursor = dao.getCollection().find(query);
		/*if(!cursor.hasNext()) {// Insert new object
			Object id = query.get(FIELD_ID);
			if(id!=null) {
				if (!backupIds.contains(id)) {
					DBObject dest = new BasicDBObject();
					dest.put(ROLL_BACK_FIELD_ID, DAOUtils.generateLongID(backupDao));
					//dest.put(ROLL_BACK_FIELD_DATA, record);
					dest.put(ROLL_BACK_FIELD_DATA_ID, id);
					dest.put(ROLL_BACK_FIELD_COLLECTION, dao.getCollection().getName());
					dest.put(ROLL_BACK_FIELD_TID, transactionId);
					dest.put(ROLL_BACK_FIELD_IS_INSERT, true);
					// ROLL_BACK_FIELD_ROLLBACKED //TODO rollback status
					backupDao.getCollection().save(dest);
					backupIds.add(id);
				}
			}
		}*/
		while (cursor.hasNext()) {
			DBObject record = cursor.next();
			Object id = record.get(FIELD_ID);
			if (backupIds.contains(id)) {
				continue;
			}
			// Remove lock marker
			record.removeField(FIELD_LOCK_TIME);
			record.removeField(FIELD_TRANSACTION_ID);

			DBObject dest = new BasicDBObject();
			//dest.put(ROLL_BACK_FIELD_ID, DAOUtils.generateLongID(backupDao));
			dest.put(ROLL_BACK_FIELD_DATA, record);
			dest.put(ROLL_BACK_FIELD_DATA_ID, id);
			dest.put(ROLL_BACK_FIELD_COLLECTION, dao.getCollection().getName());
			dest.put(ROLL_BACK_FIELD_TID, transactionId);
			dest.put(ROLL_BACK_FIELD_IS_INSERT, false);
			// ROLL_BACK_FIELD_ROLLBACKED //TODO rollback status
			backupDao.getCollection().save(dest);
			backupIds.add(id);
		}
	}

	public void commit() {//TODO
		commited = true;
	}
	
	public boolean commited() {
		return commited;
	}
	
	public boolean isDebugMode()
	{
		return debugMode;
	}

	public void setDebugMode(boolean debugMode)
	{
		this.debugMode = debugMode;
	}

	public void unlock() {
		for (TransactionalDAO dao : daoQueriesMap.keySet()) {
			List<DBObject> queries = daoQueriesMap.get(dao);
			for (DBObject query : queries) {
				DBObject unsetQuery = new BasicDBObject();
				unsetQuery.putAll(query);
				unsetQuery.put(FIELD_TRANSACTION_ID, transactionId);

				dao.getCollection().updateMulti(unsetQuery, lockUnsetObject);
			}
		}
		clearBackups();
	}

	public void rollback() {
		if (!rollbackable) {
			return;
		}

		for (TransactionalDAO dao : daoBackupIdsMap.keySet()) {
			//Set<Object> ids = daoBackupIdsMap.get(dao);
            DBObject backupQuery = QueryBuilder.start(ROLL_BACK_FIELD_TID).is(transactionId)
                    .and(ROLL_BACK_FIELD_COLLECTION).is(dao.getCollection().getName()).get();
			DBCursor backups = backupDao.getCollection().find(backupQuery);
			while (backups.hasNext()) {
				DBObject rollback = backups.next();
				Boolean isInsert = (Boolean) rollback.get(ROLL_BACK_FIELD_IS_INSERT);
				isInsert = (isInsert==null)?false:isInsert;
				if(deleteOldBeforeRollback||isInsert) {
				    dao.getCollection().remove(new BasicDBObject(FIELD_ID, rollback.get(ROLL_BACK_FIELD_DATA_ID)));
				}
				
				if(!isInsert) {
					dao.getCollection().save((DBObject)rollback.get(ROLL_BACK_FIELD_DATA)); //A dao.save() must not be override
				}
			}
		}
		clearBackups();
	}

	private void retryLock() throws DBLockException {
        waitToRetryLock();
        while( !relock() ) {
            waitToRetryLock();
        }
	}

	private void waitToRetryLock() {
		try {
			Thread.sleep((long) (minRetryLockInterval * (1 + Math.random())));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private boolean relock() {
		for (TransactionalDAO dao : daoQueriesMap.keySet()) {
			List<DBObject> queries = daoQueriesMap.get(dao);
			for (DBObject query : queries) {
				if (!lock(dao, query, true)) {
					return false;
				}
			}
		}
		return true;
	}

	private long currentTimeMillis() {
		long currentTimeMillis = ((Double) backupDao.getCollection().getDB()
				.eval("new Date().getTime()")).longValue();
		
		//TODO comment 'checkTimeout' for DEBUG
		checkTimeout(currentTimeMillis);
		return currentTimeMillis;
	}
	
	private void checkTimeout(long currentTimeMillis) {
	    if (firstLockTime == 0) {
            firstLockTime = currentTimeMillis;
        } else if (currentTimeMillis > firstLockTime + lockExpiredTime) {
            throw new DBLockException(DBLockErrorCode.RetryLockTimeOut);
        }
	}
	
	private void clearBackups() {
		if (rollbackable) {
			DBObject clearObject = new BasicDBObject(ROLL_BACK_FIELD_TID, transactionId);
			backupDao.getCollection().remove(clearObject);
		}
	}
	
    private String generateTransactionId() {
        return Thread.currentThread().getName()+"_"+System.currentTimeMillis() + "_" + (int) ( Math.random() * 1000 );
    }
}

@SuppressWarnings("serial")
@Entity(value=DBLock.ROLL_BACK_COLLECTION_NAME, noClassnameStored=true)
class TransactionBackupEntity extends LongBasedEntity{
	
}