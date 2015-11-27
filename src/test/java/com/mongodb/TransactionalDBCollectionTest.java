package com.mongodb;

import static org.junit.Assert.*;

import static com.mongodb.Fixture.getMongoClient;

import java.net.UnknownHostException;

import org.junit.Test;
import org.mongodb.transaction.Transaction;

public class TransactionalDBCollectionTest extends DBTestcase
{
	@Test
	public void testInsert() throws UnknownHostException {
		DBObject object = new BasicDBObject("_id", "_id01");
		object.put("TestMethod", "TestTransactionalDBCollection.testInsert()");
		object.put("value", "hello");

		DBCollection col = getCollection();
		col.remove(object);
		
		Transaction.start();
		col = Transaction.transactinal(col);
		
		//col.save(object);
		col.insert(object);
		
		DBCursor cursor = col.find(new BasicDBObject("_id", "_id01"));
		assertEquals(cursor.hasNext(),true);
		
		Transaction.rollback();
		cursor = col.find(object);
		assertEquals(cursor.hasNext(),false);
		
		Transaction.end();
	}
	
	@Test
	public void testUpdate() throws UnknownHostException {
		DBObject object = new BasicDBObject("_id", "_id01");
		object.put("TestMethod", "TestTransactionalDBCollection.testUpdate()");
		object.put("value", "value01");

		DBCollection col = getCollection();
		col.remove(new BasicDBObject("_id", "_id01"));
		col.insert(object);
		
		Transaction.start();
		col = Transaction.transactinal(col);
		col.update(new BasicDBObject("_id", "_id01"), new BasicDBObject("$set", new BasicDBObject("value", "value02")));
		
		DBCursor cursor = col.find(new BasicDBObject("_id", "_id01"));
		assertEquals(cursor.next().get("value"),"value02");
		
		Transaction.rollback();
		cursor = col.find(new BasicDBObject("_id", "_id01"));
		assertEquals(cursor.next().get("value"),"value01");
		
		Transaction.end();
		
		col.remove(new BasicDBObject("_id", "_id01"));
	}
	
	private DBCollection getCollection() {
		String databaseName = "trasactionTest";
		DB db = getMongoClient().getDB(databaseName);
		DBCollection col = db.getCollection("test");
		return col;
	}
}
