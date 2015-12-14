package org.mongodb.transaction;

import junit.framework.Assert;

import org.junit.Test;
import org.mongodb.morphia.query.Query;
import org.mongodb.transaction.entity.SchoolEntity;
import org.mongodb.transaction.entity.UserEntity;

import com.mongodb.DBObject;

public class TrasactionalDAOTest extends MorphiaTest
{
	@Test
	public void testRollback() {
		TransactionalDAO<UserEntity, DBObject> dao = TransactionalDAO.getInstance(UserEntity.class, getDatastore());
		// The lock must be after at least one DAO created
		Transaction.start();;
		
		UserEntity user = new UserEntity();
		user.setName("VincentChi");
		SchoolEntity school = new SchoolEntity();
		school.setName("Flower School");
		user.setSchool(school);
		dao.save(user);
		
		Query query = dao.createQuery().field("name").equal("VincentChi");
		UserEntity saved=dao.findOne(query);
		Assert.assertNotNull(saved);

		Transaction.rollback();
		saved=dao.findOne(query);
		Assert.assertNull(saved);
		
		Transaction.end();// Not necessary
	}
}
