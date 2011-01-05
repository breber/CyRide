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
package org.reber.CyRideMobile;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A Database Wrapper for CyRide Scheduling
 * 
 * @author brianreber
 */
public class CyRideDB {
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_ROUTEID = "routeid";
	public static final String KEY_ROUTENAME = "routename";
	public static final String KEY_STATIONNAME = "stationname";
	public static final String KEY_STATIONID = "stationid";
	public static final String KEY_TIMESTRING = "timestring";
	public static final String KEY_TIME = "time";
	public static final String KEY_DAY = "day";
	public static final String KEY_ROWNUM = "rownum";
	private static final String TAG = "CyRideDB";

	private static final String DATABASE_NAME = "cyride.db";
	private static final String DATABASE_TABLE = "cyride";
	private static final int DATABASE_VERSION = 1;

	private int dayOfWeek = 0;

	private static final String DATABASE_CREATE =
		"CREATE TABLE " + DATABASE_TABLE + "(" + KEY_ROWID + " INTEGER PRIMARY KEY autoincrement, " + KEY_ROUTEID + " INTEGER, " 
		+ KEY_ROUTENAME + " TEXT, " + KEY_STATIONNAME + " TEXT, " + KEY_STATIONID + " INTEGER, " + KEY_TIMESTRING + " TEXT, " 
		+ KEY_TIME + " INTEGER, " + KEY_DAY + " INTEGER, " + KEY_ROWNUM + " INTEGER);";

	private final Context context; 

	private DatabaseHelper dbHelper;

	/**
	 * Creates and opens a CyRideDB with the given Context.
	 * 
	 * @param ctx
	 */
	public CyRideDB(Context ctx) {
		this.context = ctx;
		open();
	}

	/**
	 * Opens the database. 
	 */
	public void open() {
		dbHelper = new DatabaseHelper(context);
	}

	/**
	 * Closes the database.
	 */
	public void close() {
		dbHelper.close();
	}

	/**
	 * Sets the day of the week to use in the SQL queries.
	 * 
	 * @param dayOfWeek
	 * 0 = Weekday <br />
	 * 1 = Saturday <br />
	 * 2 = Sunday <br />
	 */
	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	/**
	 * Gets the day of the week
	 * 
	 * @return the day of the week
	 */
	public int getDayOfWeek() {
		return this.dayOfWeek;
	}

	/**
	 * Inserts a Route in the database with the given parameters.
	 * If possible, use insertRoute(List&lt;Route&gt; routes).
	 * 
	 * @param routeId
	 * @param routeName
	 * @param station
	 * @param stationId
	 * @param timeString
	 * @param time
	 * @param dayOfWeek
	 * @param rowNum
	 */
	public void insertRoute(int routeId, String routeName, String station, int stationId, String timeString,
			int time, int dayOfWeek, int rowNum) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_ROUTEID, routeId);
			initialValues.put(KEY_ROUTENAME, routeName);
			initialValues.put(KEY_STATIONNAME, station);
			initialValues.put(KEY_STATIONID, stationId);
			initialValues.put(KEY_TIMESTRING, timeString);
			initialValues.put(KEY_TIME, time);
			initialValues.put(KEY_DAY, dayOfWeek);
			initialValues.put(KEY_ROWNUM, rowNum);
			db.insert(DATABASE_TABLE, null, initialValues);
		} finally {
			db.close();
		}
	}

	/**
	 * Inserts all the Routes in a List into the database.
	 * This makes a batch call into the database, which is much quicker than single calls.
	 * This method is preferred.
	 * 
	 * @param routes
	 * A List of Route objects
	 */
	public void insertRoute(List<Route> routes) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try{
			db.beginTransaction();
			for (Route r : routes) {

				ContentValues initialValues = new ContentValues();
				initialValues.put(KEY_ROUTEID, r.getRouteId());
				initialValues.put(KEY_ROUTENAME, r.getRouteName());
				initialValues.put(KEY_STATIONNAME, r.getStation());
				initialValues.put(KEY_STATIONID, r.getStationId());
				initialValues.put(KEY_TIMESTRING, r.getTimeString());
				initialValues.put(KEY_TIME, r.getTime());
				initialValues.put(KEY_DAY, r.getDay());
				initialValues.put(KEY_ROWNUM, r.getRowNum());
				db.insert(DATABASE_TABLE, null, initialValues);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * Gets all the Route objects in the database.
	 * 
	 * @return
	 * All Route rows in the table
	 */
	public Cursor getAllRoutes() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		try {
			String query = "SELECT * FROM " + DATABASE_TABLE;
			c = db.rawQuery(query, null);
		} finally {
			db.close();
		}

		return c;
	}

	/**
	 * Deletes all rows from the table
	 */
	public void deleteAllRoutes() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL("DROP TABLE " + DATABASE_TABLE);
			db.execSQL(DATABASE_CREATE);
		} finally {
			db.close();
		}
	}

	/**
	 * Gets the number of rows in the table
	 * 
	 * @return
	 * The number of rows in the table
	 */
	public int getCountRoute() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		int temp;
		try {
			String subquery = "SELECT " + KEY_STATIONNAME + " FROM " + DATABASE_TABLE;
			c = db.rawQuery(subquery, null);
			temp = c.getCount();
		} finally {
			c.close();
			db.close();
		}
		return temp;
	}

	/**
	 * Gets the name of a route by its id.
	 * 
	 * @param id
	 * The id of the route
	 * @return
	 * The name of the route with the given id
	 */
	public String getNameOfRouteById(int id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		String name = "";
		try {
			String subquery = "SELECT DISTINCT " + KEY_ROUTENAME + " FROM " + DATABASE_TABLE + " WHERE " + 
			KEY_DAY + " = " + dayOfWeek + " AND " + KEY_ROUTEID + " = " + id;
			c = db.rawQuery(subquery, null);
			c.moveToFirst();
			if (c.getCount() > 1)
				name = c.getString(c.getColumnIndex(KEY_ROUTENAME));
		} finally {
			c.close();
			db.close();
		}
		return name;
	}

	/**
	 * Gets the names of the Routes for the currently set day
	 * 
	 * @return
	 * A List of route names
	 */
	public List<NameIdWrapper> getRouteNames() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		List<NameIdWrapper> list = new ArrayList<NameIdWrapper>();
		Cursor c = null;
		try {
			String subquery = "SELECT DISTINCT " + KEY_ROUTENAME + ", " + KEY_ROUTEID + " FROM " + DATABASE_TABLE + " WHERE " + KEY_DAY + " = " + dayOfWeek
			+ " ORDER BY " + KEY_ROUTEID;
			c = db.rawQuery(subquery, null);
			Log.d("SIZE", c.getCount()+"");
			if (c.getCount() > 1) {
				do {
					c.moveToNext();
					list.add(new NameIdWrapper(c.getString(c.getColumnIndex(KEY_ROUTENAME)).replaceAll("&amp;", "&"),
							c.getInt(c.getColumnIndex(KEY_ROUTEID))));
				} while (!c.isLast());
			}
		} finally {
			if (c != null)
				c.close();
			db.close();
		}

		return list;
	}

	/**
	 * Gets the names of the stations for the currently set day and given routeId
	 * 
	 * @return
	 * A List of station names for the given routeId
	 */
	public List<NameIdWrapper> getStationNamesForRoute(int routeId) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		List<NameIdWrapper> list = new ArrayList<NameIdWrapper>();
		Cursor c = null;
		try {
			String subquery = "SELECT DISTINCT " + KEY_STATIONNAME + ", " + KEY_STATIONID + " FROM " + DATABASE_TABLE + 
			" WHERE " + KEY_DAY + " = " + dayOfWeek + " AND " + KEY_ROUTEID + " = " + routeId + " ORDER BY " + KEY_STATIONID;
			c = db.rawQuery(subquery, null);
			if (c.getCount() > 1) {
				do {
					c.moveToNext();
					list.add(new NameIdWrapper(c.getString(c.getColumnIndex(KEY_STATIONNAME)).replaceAll("&amp;", "&"),
							c.getInt(c.getColumnIndex(KEY_STATIONID))));
				} while (!c.isLast());
			}
		} finally { 
			if (c != null)
				c.close();
			db.close();
		}

		return list;
	}

	/**
	 * Gets the times for the given station for the currently set day
	 * 
	 * @return
	 * A List of times for the given station
	 */
	public List<String> getTimesForStation(int routeId, int stationId) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		List<String> list = new ArrayList<String>();
		Cursor c = null;
		try {
			String subquery = "SELECT " + KEY_TIMESTRING + " FROM " + DATABASE_TABLE + " WHERE " + KEY_DAY + " = " + dayOfWeek + 
			" AND " + KEY_ROUTEID + " = " + routeId + " AND " + KEY_STATIONID + " = " + stationId + " ORDER BY " + KEY_ROWNUM;
			c = db.rawQuery(subquery, null);
			if (c.getCount() > 1) {
				do {
					c.moveToNext();
					list.add(c.getString(c.getColumnIndex(KEY_TIMESTRING)));
				} while (!c.isLast());
			}
		} finally {
			if (c != null)
				c.close();
			db.close();
		}

		return list;
	}

	/**
	 * A helper class for interacting with the Database
	 * 
	 * @author brianreber
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		/**
		 * Creates a new Database with the given context
		 * 
		 * @param context
		 */
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}    
}