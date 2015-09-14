# Mongodb-Transaction
This project is used to implement mongodb transaction using java codes.

Mongodb doesn't support transaction, and the official totorial suggested to use [two-phase-commits](http://docs.mongodb.org/manual/tutorial/perform-two-phase-commits/) as workaround. This project referenced the idea and also used the Paxos algorithm. 

**Download**
Grab via maven
```xml
<dependency>
	<groupId>org.mongodb</groupId>
	<artifactId>mongodb-transaction</artifactId>
	<version>(insert latest version)</version>
</dependency>
```

**Usage**
If we want to make your codes support transaction, we should follow these steps
1) Use morphia
```Java
		TransactionalDAO<UserEntity, DBObject> dao = TransactionalDAO.getInstance(UserEntity.class, getDatastore());
		// The lock must be after at least one DAO created
		TransactionalDAO.lock();
		
		UserEntity user = new UserEntity();
		user.setName("VincentChi");
		dao.save(user);
		
		Query query = dao.createQuery().field("name").equal("VincentChi");
		UserEntity saved=dao.findOne(query);
		Assert.assertNotNull(saved);

		TransactionalDAO.rollback();
		saved=dao.findOne(query);
		Assert.assertNull(saved);
```
 
2) Use mongo java driver
```Java
TODO
```
