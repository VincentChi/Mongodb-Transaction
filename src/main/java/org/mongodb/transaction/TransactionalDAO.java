package org.mongodb.transaction;


import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.mapping.MappedClass;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.transaction.lock.DBLock;
import org.mongodb.transaction.util.CollectionDelegate;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.TransactionalMongoClient;

/**
 * Wraps Morphia's BasicDAO class to simplify creation of instances and grant that only single
 * instance is created across application - otherwise Morphia dumps some warning logs
 */
public class TransactionalDAO<T, K> extends BasicDAO<T, K> {
	public static final String $ = "$";
    public static final String $inc = "$inc";
    public static final String $set = "$set";
    public static final String $unset = "$unset";
    public static final String $in = "$in";
    public static final String $nin = "$nin";
    public static final String $all = "$all";
    public static final String $exists = "$exists";
    public static final String $pull = "$pull";
    public static final String $push = "$push";
    public static final String $pushAll = "$pushAll";
    public static final String $pullAll = "$pullAll";
    public static final String $or = "$or";
    public static final String $and = "$and";
    public static final String $eq = "$eq";
    public static final String $cond = "$cond";
    public static final String $divide = "$divide";
    public static final String $add = "$add";
    public static final String $regex = "$regex";
    public static final String $options = "$options";
    public static final String $nor = "$nor";
    public static final String $not = "$not";

    /**
     * greater than
     */
    public static final String $gt = "$gt";
    /**
     * greater than or equal
     */
    public static final String $gte = "$gte";
    /**
     * less than
     */
    public static final String $lt = "$lt";
    /**
     * less than or equal
     */
    public static final String $lte = "$lte";
    public static final String $ne="$ne";
    public static final String $size="$size";
    public static final String $addToSet="$addToSet";
    public static final String $each="$each";

    public static final String $match = "$match";
    public static final String $project = "$project";
    public static final String $group = "$group";
    public static final String $sort = "$sort";
    public static final String $limit = "$limit";

    public static final String $sum = "$sum";
    public static final String $first = "$first";
    public static final String $unwind = "$unwind";
    public static final String $elemMatch = "$elemMatch";


    private final static Map<Class, TransactionalDAO> instances =
            Collections.<Class, TransactionalDAO>synchronizedMap(new HashMap<Class, TransactionalDAO>());

    private final static ThreadLocal<DBLock> locks = new ThreadLocal<DBLock>();

    private static Morphia _morphia = new Morphia();
    private Set<String> fieldsName;

    public TransactionalDAO(Class<T> entityClass,
                            MongoClient mongoClient, String dbName) {
	    this(entityClass, _morphia.createDatastore(Transaction.transactinal(mongoClient), dbName));
    }
    
    public TransactionalDAO(Class<T> entityClass,
                            Datastore ds) {
	    super(entityClass, (ds.getMongo() instanceof TransactionalMongoClient)?ds:
	    	 (_morphia.createDatastore(Transaction.transactinal(ds.getMongo()), ds.getDB().getName())));
	    
	    Set<String> fieldsName = new HashSet<String>();
        for (MappedField mappedField : DAOFramework.getInstance().getMorphia().getMapper().getMappedClass(entityClass).getPersistenceFields()) {
            fieldsName.add(mappedField.getNameToStore());
        }
        this.fieldsName = fieldsName;
    }

    public static TransactionalDAO getInstance(Class entity, MongoClient mongoClient, String dbName) {
        return getInstance(entity, _morphia.createDatastore(mongoClient, dbName));
    }
    
	public static TransactionalDAO getInstance(Class entity, Datastore ds) {
		TransactionalDAO ext = instances.get(entity);
        if (ext == null) { //has not been created
            synchronized (instances) { //apply double check pattern, here it is safe
                ext = instances.get(entity);
                if (ext == null) {
                    ext = createInstance(entity, ds);
                    instances.put(entity, ext);
                }
            }
        }
        return ext;
    }

    @SuppressWarnings("unchecked")
    private static TransactionalDAO createInstance(Class entity, Datastore ds) {
    	TransactionalDAO transactionalDAO = new TransactionalDAO(entity, ds);
        return transactionalDAO;
        //locate for @Id annotation
        /*Class<Id> idAnnotation = Id.class;
        for(Field test : entity.getDeclaredFields()){
            Annotation a = test.getAnnotation(idAnnotation);
            if( a != null){
                test.
                break;
            }
        }*/
    }
    
	/**
     * @return Morphia instance for this class. When application uses more than 1 connection, this value can be different for different instances.
     */
    public Morphia getMorphia() {
    	return _morphia;
    }

    /**
     * Allows work with raw data. Deserialize entity from MongoDB
     *
     * @param o mongo presentation
     * @return entity from mongo
     */
    public T fromDBObject(DBObject o) {
        return getMorphia().fromDBObject(getEntityClass(), o);
    }

    /**
     * Allows work with raw data. Serialize entity to MongoDB
     *
     * @param e entity to serialize
     * @return mongo entity
     */
    public DBObject toDBObject(T e) {
        return getMorphia().toDBObject(e);
    }

    /**
     * @return field's names of entity
     */
    public Set<String> getFieldsName() {
        return fieldsName;
    }

    private static final String NO_SUCH_ENTITY = "no such entity";

    /**
     * Update particular fields in database
     *
     * @param testColumn   predicate to evalueat if field must be updated
     * @param entityToSave entity to save
     * @return just saved entity
     */
    public T updateSelectedFields(CollectionDelegate<MappedField, Boolean> testColumn, T entityToSave) {
        Mapper mp = getMorphia().getMapper();
        Object idValue = mp.getId(entityToSave);
        return updateSelectedFields(testColumn, entityToSave, new BasicDBObject(Mapper.ID_KEY, idValue));
    }

    public BasicDBObjectBuilder entityToBasicDbObject(CollectionDelegate<MappedField, Boolean> testColumn, T entityToSave){
        Mapper mp = getMorphia().getMapper();
        MappedClass mappedClass = mp.getMappedClass(entityToSave);

        Object idValue = mp.getId(entityToSave);
        //build result object getPersistenceFields()
        //discover version field
        List<MappedField> versionFields = mappedClass.getFieldsAnnotatedWith(Version.class);
        //remove fields not containing in columnsToUpdate
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (MappedField field : mappedClass.getPersistenceFields()) {
            if (Mapper.ID_KEY.equals(field.getNameToStore()))
                continue; //"_id" is non-editable
            //test against version field that is always updateable
            if (!versionFields.isEmpty() && field.getJavaFieldName().equals(versionFields.get(0).getJavaFieldName()))
                continue; //ol-locking editable flag will be added later
            Boolean evalRes = testColumn.evaluate(field);
            if (evalRes == null)
                throw new IllegalArgumentException("entityToSave contains unclassified column:" + field);
            else if (evalRes)
                builder.add(field.getNameToStore(), getMorphia().getMapper().getConverters().encode(field.getFieldValue(entityToSave)));
        }
        return builder;
    }

    /**
     * Update only semantic (no insert, no remove)
     *
     * @param testColumn   predicate to test if column should be included to update operation.
     *                     Predicate should return TRUE to include column to output.
     *                     Predicate should return FALSE for non-updatable columns
     *                     Predicate must return `null` for unknown columns
     * @param entityToSave entity that is saved
     * @return new instance of 'entityToSave', so if entity supports optimistic locking, then result will contain new version.
     * @throws ConcurrentModificationException
     *                                  when entity tries to override oldest version of entity than exist in database
     * @throws MongoException           for database-level error (one of the possible is entity not exists)
     * @throws IllegalArgumentException when saving entity contains unknown column (testColumn predicate returned `null`)
     */
    public T updateSelectedFields(CollectionDelegate<MappedField, Boolean> testColumn, T entityToSave, DBObject query) {
        Mapper mp = getMorphia().getMapper();
        MappedClass mappedClass = mp.getMappedClass(entityToSave);
        Object idValue = mp.getId(entityToSave);
        List<MappedField> versionFields = mappedClass.getFieldsAnnotatedWith(Version.class);
        DBObject setFields = entityToBasicDbObject(testColumn, entityToSave).get();


        T result;
        if (versionFields.isEmpty()) {//no optimistic-locking version control
            DBObject resultMongo = this.getCollection().findAndModify(
                    query, null, null, false/*remove*/, new BasicDBObject("$set", setFields), true, false);
            if (resultMongo == null)
                throw new MongoException(NO_SUCH_ENTITY);
            result = fromDBObject(resultMongo);
        } else { //apply optimistic locking
            MappedField mfVersion = mappedClass.getFieldsAnnotatedWith(Version.class).get(0);
            String versionKeyName = mfVersion.getNameToStore();
            Long oldVersion = (Long) mfVersion.getFieldValue(entityToSave);
            long newVersion = nextValue(oldVersion);
            setFields.put(versionKeyName, newVersion); //assign new ol-version
            query.put(mfVersion.getNameToStore(), oldVersion); //add ol-version as a search criteria
            DBObject resultMongo = this.getCollection().findAndModify(
                    query, null, null, false/*remove*/, new BasicDBObject("$set", setFields), true, false);
            if (resultMongo == null) {
                if (exists(Mapper.ID_KEY, idValue))
                    throw new ConcurrentModificationException("Entity of class " + entityToSave.getClass().getName()
                            + " (id='" + idValue + "',version='" + oldVersion + "') was concurrently updated.");
                else
                    throw new MongoException(NO_SUCH_ENTITY);
            }
            result = fromDBObject(resultMongo);
        }
        //when everything ok, emulate Morphia post-update lifecycle
        return result;
    }
    
    public static long nextValue(final Long oldVersion) {
        return oldVersion == null ? 1 : oldVersion + 1;
    }

    /**
     * Builds query from map that represent java-bean properties of entity.
     *
     * @param criteria pairs, where key is name of java-bean property and value is quality operation or nested DBObject with more complicated queries
     * @param isUpdate true if the query contains the values for update
     * @return object that can be used as query for find, remove,... methods of Mongo
     * @throws IllegalArgumentException exception when specified invalid (for entity) property name
     */
    public BasicDBObject buildQueryByFieldMap(Map<String, Object> criteria, boolean isUpdate) {
        Mapper mp = getMorphia().getMapper();
        MappedClass mappedClass = mp.getMappedClass(getEntityClass());
        BasicDBObject query = new BasicDBObject();
        //build query
        for (Map.Entry<String, Object> kv : criteria.entrySet()) {
            MappedField dbField = mappedClass.getMappedFieldByJavaField(kv.getKey());
            if (dbField == null) {
                String err = "Unknown field:" + kv.getKey() + " used criteria to identify entity:"+getEntityClass();
                throw new IllegalArgumentException(err);
            }
            Object value = kv.getValue();
            if (value != null && List.class.isAssignableFrom(value.getClass()) && !isUpdate) {
                value = new BasicDBObject(TransactionalDAO.$in, value);
            }
            query.put(dbField.getNameToStore(), value);
        }
        return query;
    }
    
    /*@Override
    public Key<T> save(T entityToSave) {
        K idValue = getEntityId(entityToSave);
        if(idValue!=null) {
        	lockQuery(idValue);
        	lockInsert(idValue);
        	return super.save(entityToSave);
        } else {
        	Key<T> result = super.save(entityToSave);
        	idValue = (K) result.getId();
        	lockInsert(idValue);
        	return result;
        }
    }
    
    private K getEntityId(T entity) {
        Mapper mp = getMorphia().getMapper();
        MappedClass mappedClass = mp.getMappedClass(entity);
        K id = (K) mp.getId(entity);
        return id;
    }

    private void lockQuery(K id) throws DBLockException {
        DBLock lock = locks.get();
        if (lock != null) {
            lock.lock(this, id);
        }
    }

    
    private void lockInsert(K id) throws DBLockException {
        DBLock lock = locks.get();
        if (lock != null) {
            lock.lockInsert(this, id);
        }
    }*/
}
