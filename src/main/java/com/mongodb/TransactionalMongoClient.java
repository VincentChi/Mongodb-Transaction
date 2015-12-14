package com.mongodb;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mongodb.MongoAuthority.Type;

public class TransactionalMongoClient extends MongoClient
{
	private final static ConcurrentMap<String, TransactionalDB> _tdbs = new ConcurrentHashMap<String, TransactionalDB>();
		
	public TransactionalMongoClient(ServerAddress addr, List<MongoCredential> credentialsList, MongoClientOptions options)  throws UnknownHostException{
		super(addr, credentialsList, options);
	}
	
	public TransactionalMongoClient(List<ServerAddress> seeds, List<MongoCredential> credentialsList, MongoClientOptions options) {
		super(seeds, credentialsList, options);
	}
	
	public static boolean isDirect(Mongo mongo) {
		return Type.Direct==mongo.getAuthority().getType();
	}

	@Override
    public DB getDB(String dbname)
    {
		DB tdb = _tdbs.get(dbname);
		if(tdb == null) {
			tdb = new TransactionalDB(this , dbname , _connector);
			DB temp = _dbs.putIfAbsent( dbname , tdb );
	        if ( temp != null )
	            return temp;
		}
	    return tdb;
    }
	
}