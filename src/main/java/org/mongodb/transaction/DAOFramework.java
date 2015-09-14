package org.mongodb.transaction;

import java.util.concurrent.atomic.AtomicReference;

import org.mongodb.morphia.Morphia;

public class DAOFramework
{
	private static DAOFramework instance = new DAOFramework();
	
	private AtomicReference<Morphia> _morphia = new AtomicReference<Morphia>();
	
	private DAOFramework() {
		
	}
	
	public static DAOFramework getInstance() {
		return instance;
	}
	
	
	/**
     * Get single instance of Morphia. It is thread-safe per google comment (http://groups.google.com/group/morphia/browse_thread/thread/bc981e664b9f04d5)
     * @return application-wide single instance of Morphia
     */
    public Morphia getMorphia() {
        if( _morphia.get() == null ){//no morphia initialized yet
            Morphia morphia = new Morphia();
            //first time need assign entities mappings
            /*for(String pack : getEntityPackages()){//iterate all packages to locate entities
                for(Class<?> c : EntityDiscover.getClassesInPackage(pack, ".+Entity$")){
                    morphia.map(c);
                }
            }*/
            //assign only thread-safe way
            _morphia.compareAndSet(null, morphia);
        }
        /**/
        return _morphia.get();
    }
    
    public void mapEntityClass(Class entityClasses) {
    	getMorphia().map(entityClasses);
    	
    }
}
