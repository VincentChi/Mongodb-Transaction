package org.mongodb.transaction;

import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryException;
import org.mongodb.transaction.lock.DBLockException;
import org.mongodb.transaction.lock.Lock;
import org.mongodb.transaction.lock.Locker;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class TransactionalDataStoreImpl extends DatastoreImpl
{
	private final static ThreadLocal<Locker> lockers = new ThreadLocal<Locker>();
	
	public TransactionalDataStoreImpl(Morphia morphia,
                                     Mapper mapper,
                                     MongoClient mongoClient,
                                     String dbName)
    {
	    super(morphia, mapper, mongoClient, dbName);
	    // TODO Auto-generated constructor stub
    }

	public TransactionalDataStoreImpl(Morphia morphia,
                                     MongoClient mongoClient,
                                     String dbName)
    {
	    super(morphia, mongoClient, dbName);
	    // TODO Auto-generated constructor stub
    }
	
	@Override
	public <T> WriteResult delete(final Query<T> query, final WriteConcern wc) {

        DBCollection dbColl = query.getCollection();
        //TODO remove this after testing.
        if (dbColl == null) {
            dbColl = getCollection(query.getEntityClass());
        }

        final WriteResult wr;

        if (query.getSortObject() != null || query.getOffset() != 0 || query.getLimit() > 0) {
            throw new QueryException("Delete does not allow sort/offset/limit query options.");
        }

        final DBObject queryObject = query.getQueryObject();
        if (queryObject != null) {
            if (wc == null) {
                wr = dbColl.remove(queryObject);
            } else {
                wr = dbColl.remove(queryObject, wc);
            }
        } else if (wc == null) {
            wr = dbColl.remove(new BasicDBObject());
        } else {
            wr = dbColl.remove(new BasicDBObject(), wc);
        }

        return wr;
    }

	private <T> void lockQuery(final Query<T> query) throws DBLockException {
		Locker lock = lockers.get();
        if (lock != null) {
            lock.lock(query.getCollection(), query.getQueryObject());
        }
    }

}
