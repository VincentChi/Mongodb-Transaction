package com.mongodb;

import org.junit.After;
import org.junit.Before;

import static com.mongodb.Fixture.getDefaultDatabaseName;
import static com.mongodb.Fixture.getMongoClient;

public class DBTestcase
{
    //For ease of use and readability, in this specific case we'll allow protected variables
    //CHECKSTYLE:OFF
    protected DB database;
    protected DBCollection collection;
    protected String collectionName;
    //CHECKSTYLE:ON

    @Before
    @SuppressWarnings("deprecation") // This is for testing the old API, so it will use deprecated methods
    public void setUp() {
        database = getMongoClient().getDB(getDefaultDatabaseName());

        //create a brand new collection for each test
        collectionName = getClass().getName() + System.nanoTime();
        collection = database.getCollection(collectionName);
    }

    @After
    public void tearDown() {
        collection.drop();
    }

    public MongoClient getClient() {
        return getMongoClient();
    }
}
