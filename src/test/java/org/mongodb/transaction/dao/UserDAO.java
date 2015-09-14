package org.mongodb.transaction.dao;

import java.io.Serializable;

import org.mongodb.morphia.DAO;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.transaction.entity.UserEntity;

import com.mongodb.MongoClient;

public class UserDAO extends DAO<UserEntity, Serializable>
{

	public UserDAO(Class<UserEntity> entityClass,
                   Datastore ds)
    {
	    super(entityClass, ds);
	    // TODO Auto-generated constructor stub
    }

	public UserDAO(Class<UserEntity> entityClass,
                   MongoClient mongoClient,
                   Morphia morphia,
                   String dbName)
    {
	    super(entityClass, mongoClient, morphia, dbName);
	    // TODO Auto-generated constructor stub
    }
	

}
