package com.mongodb;

import java.util.List;

import org.mongodb.transaction.Transactional;

import com.mongodb.AggregationOptions;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBDecoder;
import com.mongodb.DBEncoder;
import com.mongodb.DBObject;
import com.mongodb.ParallelScanOptions;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class TransactionalDBCollection extends DBCollectionImpl
{
	TransactionalDBCollection(DBApiLayer db,
                              String name)
    {
	    super(db, name);
    }

	@Override
	public WriteResult insert(List<DBObject> list, WriteConcern concern, DBEncoder encoder)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WriteResult update(DBObject q, DBObject o, boolean upsert, boolean multi, WriteConcern concern, DBEncoder encoder)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WriteResult remove(DBObject o, WriteConcern concern, DBEncoder encoder)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	QueryResultIterator find(DBObject ref, DBObject fields, int numToSkip, int batchSize, int limit, int options,
	                         ReadPreference readPref, DBDecoder decoder)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	QueryResultIterator find(DBObject ref, DBObject fields, int numToSkip, int batchSize, int limit, int options,
	                         ReadPreference readPref, DBDecoder decoder, DBEncoder encoder)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createIndex(DBObject keys, DBObject options, DBEncoder encoder)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Cursor aggregate(List<DBObject> pipeline, AggregationOptions options, ReadPreference readPreference)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Cursor> parallelScan(ParallelScanOptions options)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	BulkWriteResult executeBulkWriteOperation(boolean ordered, List<WriteRequest> requests, WriteConcern writeConcern,
	                                          DBEncoder encoder)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DBObject> getIndexInfo()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
