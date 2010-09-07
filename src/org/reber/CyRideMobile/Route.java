package org.reber.CyRideMobile;

/**
 * An implementation of a Route that can be persisted into our databases.
 * 
 * 
 * @author brianreber
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
	 * Creates a Route with the given parameters.
	 * 
	 * @param routeName
	 * @param routeId
	 * @param station
	 * @param stationId
	 * @param timeString
	 * @param time
	 * @param day
	 * @param rowNum
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