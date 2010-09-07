package org.reber.CyRideMobile;


/**
 * @author brianreber
 *
 */
public class Route {

	private String routeName;
	private int routeId;
	private String station;
	private int stationId;
	private String timeString;
	private int time;
	private int day;
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
	

	public String toString() {
		return "{\"routeid\":"+routeId+",\"routename\":\""+routeName+"\",\"station\":\""+station+"\",\"stationid\":"+stationId+",\"timestring\":\""+timeString+"\",\"time\":"+time+",\"dayofweek\":"+day+",\"rownum\":"+rowNum+"}";
	}


	/**
	 * @return the routeName
	 */
	public String getRouteName() {
		return routeName;
	}


	/**
	 * @return the routeId
	 */
	public int getRouteId() {
		return routeId;
	}


	/**
	 * @return the station
	 */
	public String getStation() {
		return station;
	}


	/**
	 * @return the stationId
	 */
	public int getStationId() {
		return stationId;
	}


	/**
	 * @return the timeString
	 */
	public String getTimeString() {
		return timeString;
	}


	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}


	/**
	 * @return the day
	 */
	public int getDay() {
		return day;
	}


	/**
	 * @return the rowNum
	 */
	public int getRowNum() {
		return rowNum;
	}
}
