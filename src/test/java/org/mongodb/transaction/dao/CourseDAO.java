package org.mongodb.transaction.dao;

import java.io.Serializable;

import org.mongodb.morphia.DAO;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.transaction.entity.CourseEntity;
import org.mongodb.transaction.entity.UserEntity;

import com.mongodb.MongoClient;

public class CourseDAO extends DAO<CourseEntity, Serializable>
{

	public CourseDAO(Class<CourseEntity> entityClass,
	                 Datastore ds)
	{
		super(entityClass, ds);
		// TODO Auto-generated constructor stub
	}

	public CourseDAO(Class<CourseEntity> entityClass,
	                 MongoClient mongoClient,
	                 Morphia morphia,
	                 String dbName)
	{
		super(entityClass, mongoClient, morphia, dbName);
		// TODO Auto-generated constructor stub
	}

}
