package org.reber.Cyride;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author brianreber
 *
 */
@SuppressWarnings("unused")
@PersistenceCapable
public class Count {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long identifier;

	@Persistent
	private int count;
	
	public Count(int count) {
		this.count = count;
	}
	
	public int getCount() {
		return count;	
	}
	
	public void setCount(int count) {
		this.count = count;	
	}

	public String toString() {
		return "{\"count\":"+count+"}";
	}
}
