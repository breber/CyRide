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

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class CreateRoute extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
		String routeName = req.getParameter("routename");
		String routeId = req.getParameter("routeid");
		String station = req.getParameter("station");
		String stationId = req.getParameter("stationid");
		String day = req.getParameter("day");
		String hour = req.getParameter("hour");
		String minute = req.getParameter("minute");
		String timeString = hour + ":" + minute;

		Route record;
		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			int time = convertTimeStringToMinute(hour, minute);
			record = new Route(routeName, Integer.parseInt(routeId), station, Integer.parseInt(stationId), timeString, time, Integer.parseInt(day), 0);
			pm.makePersistent(record);
		} finally {
			pm.close();
		}
	}

	public static int convertTimeStringToMinute(String hour, String min) {		
		return Integer.parseInt(hour) * 60 + Integer.parseInt(min);
	}
}
