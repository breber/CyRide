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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author brianreber
 *
 */
@SuppressWarnings("serial")
public class ParseDataFromXML extends HttpServlet{

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		InputStream is = req.getInputStream();
		Scanner scan = new Scanner(is);
		StringBuilder sb = new StringBuilder();
		while (scan.hasNext()) {
			sb.append(scan.next() + " ");
		}
		String input = sb.toString();
		
		parseData(input);
		
		resp.sendRedirect("/updatecount");
	}
	
	public static void parseData(String input) {

		List<Route> routes = new ArrayList<Route>();
		
		String route = input.substring(input.indexOf("<route>") + 7, input.indexOf("</route>"));
		String dayOfWeek = input.substring(input.indexOf("<dayOfWeek>") + 11, input.indexOf("</dayOfWeek>"));
		
		if (route.endsWith("-")) {
			route = route.substring(0, route.length() - 1);
		}
		
		//Start after </stations>
		String[] stationXml = input.substring(input.indexOf("</stations>")).split("</station>");
		
		for (int stationNumber = 0; stationNumber < stationXml.length; stationNumber++) {
			String currentStation = stationXml[stationNumber];
			if (!currentStation.contains("<name>") || !currentStation.contains("<row>")) {
				continue;
			}
			String station = currentStation.substring(currentStation.indexOf("<name>") + 6, currentStation.indexOf("</name>"));
			
			int stationId = stationNumber;
			
			String[] rows = currentStation.split("</row>");
		
			for (int rowNum = 0; rowNum < rows.length; rowNum++) {
				String currentRow = rows[rowNum];
				String timeOrMessage = "";
				boolean time = false;
				
				if (currentRow.contains("<time>")) {
					timeOrMessage = currentRow.substring(currentRow.indexOf("<time>") + 6, currentRow.indexOf("</time>"));
					time = true;
				} else if (currentRow.contains("<message>")){
					timeOrMessage = currentRow.substring(currentRow.indexOf("<message>") + 9, currentRow.indexOf("</message>"));
				} else {
					continue;
				}

				int timeInt = time ? Utilities.convertTimeStringToMinute(timeOrMessage.substring(0, timeOrMessage.indexOf(":")), 
						timeOrMessage.substring(timeOrMessage.indexOf(":")+1, timeOrMessage.indexOf(" ")), timeOrMessage.contains("AM")) : -1;
				Route r = new Route(route, Utilities.getRouteId(route), station, stationId, timeOrMessage, timeInt, Utilities.getDayOfWeek(dayOfWeek), rowNum);
				
				routes.add(r);
			}
		}
		
		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			for (Route r : routes) {
				pm.makePersistent(r);
			}
		} finally {
			pm.close();
		}
	}
}
