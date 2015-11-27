package com.mongodb;

import java.net.UnknownHostException;

public class Fixture
{
    public static final String DEFAULT_URI = "mongodb://localhost:27017";
    public static final String MONGODB_URI_SYSTEM_PROPERTY_NAME = "org.mongodb.test.uri";
    private static final String DEFAULT_DATABASE_NAME = "JavaDriverTest";

    private static MongoClient mongoClient;
    private static MongoClientURI mongoClientURI;
    private static DB defaultDatabase;

    private Fixture() {
    }

    public static synchronized MongoClient getMongoClient() {
        if (mongoClient == null) {
            MongoClientURI mongoURI = getMongoClientURI();
            try
            {
	            mongoClient = new MongoClient(mongoURI);
            }
            catch (UnknownHostException e)
            {
	            e.printStackTrace();
            }
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        }
        return mongoClient;
    }

    @SuppressWarnings("deprecation") // This is for access to the old API, so it will use deprecated methods
    public static synchronized DB getDefaultDatabase() {
        if (defaultDatabase == null) {
            defaultDatabase = getMongoClient().getDB(getDefaultDatabaseName());
        }
        return defaultDatabase;
    }

    public static String getDefaultDatabaseName() {
        return DEFAULT_DATABASE_NAME;
    }
    
    static class ShutdownHook extends Thread {
        @Override
        public void run() {
            synchronized (Fixture.class) {
                if (mongoClient != null) {
                    if (defaultDatabase != null) {
                        defaultDatabase.dropDatabase();
                    }
                    mongoClient.close();
                    mongoClient = null;
                }
            }
        }
    }

    public static synchronized String getMongoClientURIString() {
        String mongoURIProperty = System.getProperty(MONGODB_URI_SYSTEM_PROPERTY_NAME);
        return mongoURIProperty == null || mongoURIProperty.isEmpty()
               ? DEFAULT_URI : mongoURIProperty;
    }

    public static synchronized MongoClientURI getMongoClientURI() {
        if (mongoClientURI == null) {
            mongoClientURI = getMongoClientURI(MongoClientOptions.builder());
        }
        return mongoClientURI;
    }
    
    public static synchronized MongoClientURI getMongoClientURI(final MongoClientOptions.Builder builder) {
        MongoClientURI mongoClientURI = null;
        String mongoURIString = getMongoClientURIString();
        if (System.getProperty("java.version").startsWith("1.6.")) {
            //builder.sslInvalidHostNameAllowed(true); TODO
        }

        mongoClientURI = new MongoClientURI(mongoURIString, builder);
        return mongoClientURI;
    }
}
