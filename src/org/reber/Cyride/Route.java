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
	@Persistent
	private String specialInstruction;
	
	/**
	 * @param routeName
	 * @param routeId
	 * @param station
	 * @param time
	 */
	public Route(String routeName, int routeId, String station, int stationId, String timeString, int time, int day, int rowNum, String specialInstruction) {
		this.routeName = routeName;
		this.routeId = routeId;
		this.station = station;
		this.stationId = stationId;
		this.timeString = timeString;
		this.time = time;
		this.day = day;
		this.rowNum = rowNum;
		this.specialInstruction = specialInstruction;
	}
	
	public String getStation() {
		return station;	
	}
	
	public void setStationId(int temp) {
		this.stationId = temp;
	}

	public String toString() {
		return "{\"routeid\":"+routeId+",\"routename\":\""+routeName+"\",\"station\":\""+station+"\",\"stationid\":"+stationId+",\"timestring\":\""+timeString+"\",\"time\":"+time+",\"dayofweek\":"+day+",\"rownum\":"+rowNum+",\"specialinstruction\":\""+specialInstruction+"\"}";
	}
}
