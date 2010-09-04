package org.reber.CyRideMobile;

/**
 * @author brianreber
 *
 */
public class NameIdWrapper {

	private int id;
	private String name;
	
	public NameIdWrapper(String name, int id) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
}
