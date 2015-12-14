package org.mongodb.transaction;

import java.net.UnknownHostException;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class MongoTest
{
	@Test
	public void testMongo() throws UnknownHostException
	{
		Mongo mongo = new Mongo();
		DB db = mongo.getDB("test");
		DBCollection conn = db.getCollection("test");
		DBCursor cursor = conn.find();
		while(cursor.hasNext()) {
			DBObject object = cursor.next();
			System.out.println(object);
		}
		cursor.close();
		//Collections.synchronizedCollection(c);
		WriteResult result = conn.update(new BasicDBObject("count",100), new BasicDBObject("$inc",new BasicDBObject("count",1)),true,false);
		CommandResult res = result.getLastError();
		Object value = res.get("upserted");
		System.out.println(value);
		
		result = conn.update(new BasicDBObject("_id",123), new BasicDBObject("$inc",new BasicDBObject("count",1)),true,false);
		res = result.getLastError();
		if (res.ok() || res.getErrorMessage().equals( "No matching object found" )) {
			value = res.get("upserted");
			System.out.println(value);
		}
		
		DBObject fields =new BasicDBObject("name",1);
		if (fields != null && !fields.keySet().isEmpty() && !fields.containsField("_id")) {
    		fields.put("_id", 1);
    	}
		System.out.println(fields);
		
		DBObject obj=new BasicDBObject();
//		obj.put("_id", "234");
		obj.put("key", "key");
//		WriteResult rr=conn.insert(obj,WriteConcern.ACKNOWLEDGED);
//		WriteResult rr=conn.update(new BasicDBObject("_id","hhhhh"), obj, true, false);
		WriteResult rr=conn.update(new BasicDBObject("_id","hhhhh"), obj, true, false,WriteConcern.UNACKNOWLEDGED);
		System.out.println(rr.getLastError());
		System.out.println(rr.getLastError().ok());
	}
	
	public MongoClient getMongo() {
		MongoClient mongo = null;
        try
        {
	        mongo = new MongoClient();
        }
        catch (UnknownHostException e)
        {
	        e.printStackTrace();
        }
		return mongo;
	}
	
	public String getDbName() {
		return "trasactionTest";
	}
}
