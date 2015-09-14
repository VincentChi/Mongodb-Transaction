package org.mongodb.transaction.entity;

/**
 * User: oleksandr.linkevych
 * Date: 2/20/12
 * Time: 3:59 PM
 */

import java.io.Serializable;

import org.mongodb.morphia.annotations.Id;

/**
 *  Abstract base entity, all long-id entities should extends this class
 *  created for extract methods equals and hashCode
 */
public abstract class LongBasedEntity implements Serializable {

    private static final long serialVersionUID = -4608688017013019852L;
    @Id
    protected long id;

    /**
     * Get primary key
     * @return primary key
     */
    public long getId() {
        return id;
    }

    /**
     * Set primary key
     * @param id primary key
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Equals method by id, objects equals if there ids equals
     * @param o another object
     * @return true if this.id equals o.id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LongBasedEntity that = (LongBasedEntity) o;

        return id == that.id;

    }

    /**
     * hashCode method by id, hashCode equals if there ids equals
     * @return hashCode if id
     */
    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}
