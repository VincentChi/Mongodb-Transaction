package org.mongodb.transaction.dao;

import java.io.Serializable;

import org.mongodb.morphia.DAO;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.transaction.entity.CourseEntity;
import org.mongodb.transaction.entity.SchoolEntity;
import org.mongodb.transaction.entity.UserEntity;

import com.mongodb.MongoClient;

public class SchoolDAO extends DAO<SchoolEntity, Serializable>
{

	public SchoolDAO(Class<SchoolEntity> entityClass,
	                 Datastore ds)
	{
		super(entityClass, ds);
		// TODO Auto-generated constructor stub
	}

	public SchoolDAO(Class<SchoolEntity> entityClass,
	                 MongoClient mongoClient,
	                 Morphia morphia,
	                 String dbName)
	{
		super(entityClass, mongoClient, morphia, dbName);
		// TODO Auto-generated constructor stub
	}

}
