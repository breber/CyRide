package org.reber.Cyride;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GetDataFromURL extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/xml");
		PrintWriter out = resp.getWriter();
		
		String route = "", dayOfWeek = "";
		
		String urlString = req.getParameter("url").replaceAll("AND", "&");
		URL url = new URL(urlString);
		Scanner scan = new Scanner(url.openConnection().getInputStream());
		String temp = "";
		StringBuilder temp1 = new StringBuilder();
		while (scan.hasNext()) {
			temp1.append(scan.next() + " ");
		}
		temp = temp1.toString();
		temp = temp.substring(temp.indexOf("<table"),temp.indexOf("</table>")+8);
		
		temp = temp.replaceAll("<table.+?>", "<cyride>");
		temp = temp.replaceAll("</table>", "</cyride>");
		temp = temp.replaceAll("<tr.+?>", "<tr>");
		temp = temp.replaceAll("<td.+?>", "<td>");
		temp = temp.replaceAll("<td>\\s+\\&nbsp;\\s+</td>;", "<td>EMPTY</td>");
		temp = temp.replaceAll("\\s+|\\&nbsp;", " ");
		temp = temp.replaceAll("<strong>|<font.+?>|</font>|</strong>|<a .+?>|</a>|<!--.+?-->|<b>|</b>|<col.+?>|</col.+?>|<br>|<span.+?>|</span>|\\n|\\r|<p.+?>|</p>" +
				"|<img.+?>", "");
		temp = temp.replaceAll(">\\s+", ">");
		temp = temp.replaceAll("<tr></td>", "<tr><td></td>");
		temp = temp.replaceAll("<tr>([^<])", "<tr><td>$1");
		temp = temp.replaceFirst("</tr>", "");
		temp = temp.replaceAll("</td><tr>", "</td></tr><tr>");
		temp = temp.replaceAll(">\\s+<","><");
		
		temp = temp.replaceFirst("<tr><td>(.+?)\\s([^\\s]+?)</td></tr>", "<route>$1</route><dayOfWeek>$2</dayOfWeek>");

		temp = temp.replaceFirst("<tr>(.+?)</tr>", "");
		temp = temp.replaceFirst("<tr>(.+?)</tr>", "<stations>$1</stations>");
		temp = temp.replaceAll("<tr>(<td>\\s*</td>)+</tr>", "");
		
		route = temp.substring(temp.indexOf("<route>") + 7, temp.indexOf("</route>")).trim();
		dayOfWeek = temp.substring(temp.indexOf("<dayOfWeek>") + 11, temp.indexOf("</dayOfWeek>"));
		
		int emptyLocation = -1;
		
		String stationsNode = temp.substring(temp.indexOf("<stations>"), temp.indexOf("</stations>")+11);
		
		ArrayList<String> stations = new ArrayList<String>();
		stationsNode = stationsNode.replaceAll("<td></td>", "EMPTY~");
		stationsNode = stationsNode.replaceAll("<td>(.*?)</td>", "$1~");
		stationsNode = stationsNode.replaceAll("<stations>(.*?)</stations>", "$1");
		
		scan = new Scanner(stationsNode);
		scan.useDelimiter("~");
		while (scan.hasNext()) {
			String v = scan.next();
			stations.add((v.equals("EMPTY")) ? "":v);
		}

		ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < stations.size(); i++) {
			if (stations.get(i).equals("")) emptyLocation = i;
			lists.add(new ArrayList<String>());
		}
		
		temp = temp.replaceFirst("<tr>", "\n<tr>");
		temp = temp.replaceAll("</tr>", "</tr>\n");
		temp = temp.replaceAll("<tr>.+?Map of.+?</tr>", "");
		temp = temp.replaceAll("<tr>.+?All trips have.+?</tr>", "");
		temp = temp.replaceAll("<tr>.+?Shaded lines.+?</tr>\\s+?<tr>.+?</tr>", "");
		temp = temp.replaceAll("<tr>.+?Continues.+?</tr>", "");

		String[] array = temp.substring(temp.indexOf("<tr>")).split("</tr>");
		for (int j = 0; j < array.length; j++) {
			String t = array[j];
			String[] tdArray = t.split("</td>");
			for (int i = 0; i < stations.size(); i++) {
				if (i != emptyLocation) {
					try {
						if (tdArray[i].substring(tdArray[i].indexOf("<td>")+4).trim().matches("^\\d?\\d:\\d\\d$")) {
							lists.get(i).add(tdArray[i].substring(tdArray[i].indexOf("<td>")+4).trim());
						} /*else if (tdArray[i].contains("+") || tdArray[i].contains("Request")) {
							lists.get(i).add(tdArray[i].substring(tdArray[i].indexOf("<td>")+4).trim());
						} */else {
							lists.get(i).add("---");
						}
					} catch (IndexOutOfBoundsException e){lists.get(i).add("---");}
				}
			}
		}

		StringBuilder sb = new StringBuilder(temp.substring(0, temp.indexOf("</stations>")+11));
		ArrayList<Route> routes = new ArrayList<Route>();
		for (int i = 0; i < lists.size(); i++) {
			ArrayList<String> l = lists.get(i);
			sb.append("<station>");
			sb.append("<name>"+stations.get(lists.indexOf(l))+"</name>");
			String name = stations.get(lists.indexOf(l));
			int previousTime = 0;
			for (int j = 0; j < l.size(); j++) {
				String str = l.get(j);
				String s = str.replaceAll("<td>", "").replaceAll("\\s+"," ").trim();
				boolean isMorning = true;
				if (s.contains(":")) {
					if (previousTime > convertTimeStringToMinute(s.substring(0, s.indexOf(":")), 
							s.substring(s.indexOf(":")+1), isMorning))
						isMorning = false;
					int routeId = (emptyLocation == 0) ? lists.indexOf(l) : (1 + lists.indexOf(l));
					Route r = new Route(route, getRouteId(route), name, routeId, s, convertTimeStringToMinute(s.substring(0, s.indexOf(":")), 
							s.substring(s.indexOf(":")+1), isMorning), getDayOfWeek(dayOfWeek), j, "");
					previousTime = convertTimeStringToMinute(s.substring(0, s.indexOf(":")), 
							s.substring(s.indexOf(":")+1), isMorning);
					routes.add(r);
				} /*else if (s.contains("+") || s.contains("Request")) {
					int routeId = (emptyLocation == 0) ? lists.indexOf(l) : (1 + lists.indexOf(l));
					Route r = new Route(route, getRouteId(route), name, routeId, s, previousTime + 1, getDayOfWeek(dayOfWeek), j, "");
					previousTime++;
					routes.add(r);
				}*/
				sb.append("<time><num>"+s+"</num><rownum>"+j+"</rownum></time>");
			}
			sb.append("</station>");
		}
		sb.append("</cyride>");
		
		out.println(sb.toString().replaceAll("<td>", "<station>").replaceAll("</td>", "</station>").replaceAll("\\s+", " "));

		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
//			Query q = pm.newQuery(Route.class);
//			q.setFilter("routeId == 11");
//			q.setFilter("day == 1");
//			q.deletePersistentAll();
			for (Route r : routes) {
				pm.makePersistent(r);
			}
		} finally {
			pm.close();
		}
	}
	
	public static int getDayOfWeek(String day) {
		if (day.contains("Sat")) return 1;
		if (day.contains("Sun")) return 2;
		else return 0;
	}
	
	public static int convertTimeStringToMinute(String hour, String min, boolean isMorning) {
		if (isMorning) {
			return Integer.parseInt(hour.trim()) * 60 + Integer.parseInt(min.trim());
		}
		else return (Integer.parseInt(hour.trim()) + 12) * 60 + Integer.parseInt(min.trim());
	}
	
	private static int getRouteId(String name) {
		if (name.contains("1") && name.contains("West")) return 0;
		if (name.contains("1") && name.contains("East")) return 1;
		if (name.contains("2") && name.contains("West")) return 2;
		if (name.contains("2") && name.contains("East")) return 3;
		if (name.contains("3") && name.contains("South")) return 4;
		if (name.contains("3") && name.contains("North")) return 5;
		if (name.contains("6B") || (name.contains("6") && name.contains("Brown") && name.contains("South"))) return 6;
		if (name.contains("6A") && name.contains("Towers")) return 7;
		if (name.contains("5") && name.contains("Yellow")) return 8;
		if (name.contains("4") && name.contains("Gray")) return 9;
		if (name.contains("6") && name.contains("Brown") && name.contains("North")) return 10;
		if (name.contains("7") && name.contains("Purple")) return 11;
		if (name.contains("10") && name.contains("Pink")) return 12;
		if (name.contains("21") && name.contains("Cardinal")) return 13;
		if (name.contains("22") && name.contains("Gold")) return 14;
		if (name.contains("23") && name.contains("Orange")) return 15;
		if (name.contains("24") && name.contains("Silver")) return 16;
		
		else return -1;
	}
}
