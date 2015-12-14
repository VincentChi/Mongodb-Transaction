package com.mongodb;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.transaction.IApplyHandler;
import org.mongodb.transaction.Transaction;
import org.mongodb.transaction.lock.Locker;

public class TransactionalDBCollection extends DBCollectionImpl
{
	public static final String ID_FIELD_NAME = "_id";
	public static final String $ = "$";
	
	private DBCollection col; //original collection
	private IApplyHandler applyHandler;
	
	@SuppressWarnings("deprecation")
    public TransactionalDBCollection(DBCollection col)
    {
	    super((DBApiLayer) col.getDB(),
              col.getName());
	    this.col = col;
    }
	
	@Override
	protected WriteResult insert(List<DBObject> list, boolean shouldApply , WriteConcern concern, DBEncoder encoder ){
		return super.insert(list, true , concern, encoder );// set shouldApply=true
	}
	
	@Override
    public WriteResult update( DBObject query , DBObject o , boolean upsert , boolean multi , WriteConcern concern,
                               DBEncoder encoder ) {
		getLocker().lock(col, query);
		WriteResult result = super.update(query, o, upsert, multi, concern, encoder);
		if(upsert) {
			lockInsert(query, result);
		}
		return result;
	}
	
	@Override
	public DBObject findAndModify(final DBObject query, final DBObject fields, final DBObject sort,
                                  final boolean remove, final DBObject update,
                                  final boolean returnNew, final boolean upsert,
                                  final long maxTime, final TimeUnit maxTimeUnit) {
		getLocker().lock(col, query);// TODO not sure modify all or modify first

		// To make sure the return fields must contains id
		if (fields != null && !fields.keySet().isEmpty() && !fields.containsField(Mapper.ID_KEY))
		{
			fields.put(ID_FIELD_NAME, 1);
		}
		DBObject result = super.findAndModify(query, fields, sort, remove, update, returnNew, upsert, maxTime, maxTimeUnit);
		if(upsert){
			getLocker().lockInsert(col, result.get(ID_FIELD_NAME));// single id
    	}
		return result;
	}
    
	/**
	 * Lock new insert documents
	 */
	@SuppressWarnings("deprecation")
    @Override
	public Object apply(final DBObject document) {
        Object id = super.apply(document);
        getLocker().lockInsert(col, id);
        if(applyHandler!=null) {
        	applyHandler.apply(document);
        }
        return id;
    }
	
	public void setApplyHandler(IApplyHandler applyHandler)
	{
		this.applyHandler = applyHandler;
	}

	@SuppressWarnings("deprecation")
    private void lockInsert(DBObject q, WriteResult result) {
    	if(result==null) return;
    	CommandResult lastError = result.getLastError();
    	if(q.containsField(ID_FIELD_NAME)) {// If inserted, the DBObject id will be auto generated if not manually set
    		Object id = q.containsField(ID_FIELD_NAME);
    		if(id instanceof DBObject) {
    			if(!id.toString().contains($)) { // TODO This could not happen, because such $eq/$in would be locked in query part
    				if(lastError.getException()==null) {// If the writeConcern is unacknowledge/none, will get an exception such like duplicate key
    					getLocker().lockInsert(col, q.get(ID_FIELD_NAME));
    				}
    			}
    		} else {
    			if(lastError.getException()==null) {
    				getLocker().lockInsert(col, q.get(ID_FIELD_NAME));
				}
    		}
		} else {// update with upsert=true
			if (lastError.ok() || lastError.getErrorMessage().equals( "No matching object found" )) {
    			Object id = lastError.get( "upserted" );// if lastError.getException() is "duplicated key", id==null
    			if(id!=null) {
    				getLocker().lockInsert(col, q.get(ID_FIELD_NAME));// single id
    			}
            }
		}
    }
	
	private Locker getLocker() {
		return Transaction.getLocker();
	}
}
