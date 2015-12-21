package com.mongodb;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.mongodb.transaction.IApplyHandler;

import com.mongodb.MongoAuthority.Type;

public class TransactionalMongoClient extends MongoClient
{
	private final static ConcurrentMap<String, TransactionalDB> _tdbs = new ConcurrentHashMap<String, TransactionalDB>();
	
	private IApplyHandler handler;
	
	public TransactionalMongoClient(ServerAddress addr, List<MongoCredential> credentialsList, MongoClientOptions options, IApplyHandler handler)  throws UnknownHostException{
		super(addr, credentialsList, options);
		this.handler = handler;
	}
	
	public TransactionalMongoClient(List<ServerAddress> seeds, List<MongoCredential> credentialsList, MongoClientOptions options, IApplyHandler handler) {
		super(seeds, credentialsList, options);
		this.handler = handler;
	}
	
	public static boolean isDirect(Mongo mongo) {
		return Type.Direct==mongo.getAuthority().getType();
	}

	@Override
    public DB getDB(String dbname)
    {
		DB tdb = _tdbs.get(dbname);
		if(tdb == null) {
			tdb = new TransactionalDB(this , dbname , _connector, handler);
			DB temp = _dbs.putIfAbsent( dbname , tdb );
	        if ( temp != null )
	            return temp;
		}
	    return tdb;
    }
	
}