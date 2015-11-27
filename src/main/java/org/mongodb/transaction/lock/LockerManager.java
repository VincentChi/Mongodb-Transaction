package org.mongodb.transaction.lock;


public class LockerManager {
    public static Locker getLocker(boolean rollbackable) {
        return new Locker(rollbackable);
    }
    
    public static Locker getLocker(boolean rollbackable, long lockTime) {
        return new Locker(rollbackable, lockTime);
    }
}
