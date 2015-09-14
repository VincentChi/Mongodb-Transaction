package org.mongodb.transaction.lock;

import com.mongodb.MongoClient;

public class DBLockManager {
    public static DBLock getLock(MongoClient mongoClient, String dbName) {
        return new DBLock(mongoClient, dbName);
    }

    public static DBLock getLock(MongoClient mongoClient, String dbName, boolean rollbackable) {
        return new DBLock(mongoClient, dbName, rollbackable);
    }

    public static DBLock getLock(MongoClient mongoClient, String dbName, boolean rollbackable, long lockTime) {
        return new DBLock(mongoClient, dbName, rollbackable, lockTime);
    }
}
