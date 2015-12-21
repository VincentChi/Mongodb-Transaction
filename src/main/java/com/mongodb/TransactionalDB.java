package com.mongodb;

import org.mongodb.transaction.IApplyHandler;
import org.mongodb.transaction.Transaction;

public class TransactionalDB extends DBApiLayer
{
	private IApplyHandler handler;
	
	protected TransactionalDB(Mongo mongo,
                              String name,
                              DBConnector connector,
                              IApplyHandler handler)
    {
	    super(mongo, name, connector);
	    this.handler = handler;
    }

	@Override
    public DBCollection getCollection(String name)
    {
	    return Transaction.transactinal(super.getCollection(name), this.handler);
    }
}
