/*
 * Copyright (C) 2011 Brian Reber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by Brian Reber.  
 * THIS SOFTWARE IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
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
public class Route {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long identifier;

	@Persistent
	private String routeName;
	@Persistent
	private int routeId;
	@Persistent
	private String station;
	@Persistent
	private int stationId;
	@Persistent
	private String timeString;
	@Persistent
	private int time;
	@Persistent
	private int day;
	@Persistent
	private int rowNum;
	
	/**
	 * @param routeName
	 * @param routeId
	 * @param station
	 * @param time
	 */
	public Route(String routeName, int routeId, String station, int stationId, String timeString, int time, int day, int rowNum) {
		this.routeName = routeName;
		this.routeId = routeId;
		this.station = station;
		this.stationId = stationId;
		this.timeString = timeString;
		this.time = time;
		this.day = day;
		this.rowNum = rowNum;
	}
	
	public String getStation() {
		return station;	
	}
	
	public void setStationId(int temp) {
		this.stationId = temp;
	}

	public String toString() {
		return "{\"routeid\":"+routeId+",\"routename\":\""+routeName+"\",\"station\":\""+station+"\",\"stationid\":"+stationId+",\"timestring\":\""+timeString+"\",\"time\":"+time+",\"dayofweek\":"+day+",\"rownum\":"+rowNum+"}";
	}
}
