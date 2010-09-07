package org.reber.Cyride;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author brianreber
 *
 */
public class ParseDataFromXML {

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PrintWriter out = resp.getWriter();
		
		InputStream is = req.getInputStream();
		Scanner scan = new Scanner(is);
		StringBuilder sb = new StringBuilder();
		while (scan.hasNext()) {
			sb.append(scan.next() + " ");
		}
		out.print(sb.toString());
		
//		Route r = new Route(route, getRouteId(route), name, routeId, s, convertTimeStringToMinute(s.substring(0, s.indexOf(":")), 
//				s.substring(s.indexOf(":")+1), isMorning), getDayOfWeek(dayOfWeek), j);
	}
	
}
