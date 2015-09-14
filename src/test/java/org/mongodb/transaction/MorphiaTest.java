package org.mongodb.transaction;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class MorphiaTest extends MongoTest
{	
	public Datastore getDatastore() {
		Morphia morphia = new Morphia();
		Datastore store = morphia.createDatastore(getMongo(), getDbName());
		return store;
	}
}
