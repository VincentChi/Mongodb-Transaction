package com.mongodb;

import org.mongodb.transaction.Transaction;

public class TransactionalDB extends DBApiLayer
{
	protected TransactionalDB(Mongo mongo,
                              String name,
                              DBConnector connector)
    {
	    super(mongo, name, connector);
    }

	@Override
    public DBCollection getCollection(String name)
    {
	    return Transaction.transactinal(super.getCollection(name));
    }
}
