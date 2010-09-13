package org.reber.Cyride;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GetDataFromURL extends HttpServlet {
	
	private String[] timesOfDay = {"12", "1", "2", "3", "4", "5", "6", "7", "8", "9","10", "11", "12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/xml");
		PrintWriter out = resp.getWriter();
		
		String doNotPersist = req.getParameter("nopersist");
		String alreadyTransformed = req.getParameter("alreadytransformed");
		
		String urlString = req.getParameter("url").replaceAll("AND", "&");
		
		URL url = new URL(urlString);
		Scanner scan = new Scanner(url.openConnection().getInputStream());
		String temp = "";
		StringBuilder temp1 = new StringBuilder();
		while (scan.hasNext()) {
			temp1.append(scan.next() + " ");
		}
		temp = temp1.toString();
		
		if (doNotPersist == null && alreadyTransformed != null) {
			ParseDataFromXML.parseData(temp.replaceAll("\\s+", " "));
			return;
		}
		
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
		
		temp = temp.replaceAll("[^\\s]\\s+<","<");
//		out.println(temp);
		temp = temp.replaceFirst("<tr><td>(.+?)\\s([^\\s]+?)</td></tr>", "<route>$1</route><dayOfWeek>$2</dayOfWeek>");
		
		String routeAndDay = temp.substring(temp.indexOf("<route>") + 7, temp.indexOf("</dayOfWeek>") + 11);
		temp = temp.replace(routeAndDay, routeAndDay.replaceAll("<td>|</td>", ""));
//		out.println(temp);
		
		temp = temp.replaceFirst("<tr>(.+?)</tr>", "");

		if (temp.substring(temp.indexOf("<tr>"), temp.indexOf("</tr>")+5).matches("<tr><td>.+?\\d\\d\\d\\d\\sto\\s.+?</td></tr>"))
			temp = temp.replaceFirst("<tr>(.+?)</tr>", "");
		
		temp = temp.replaceFirst("<tr>(.+?)</tr>", "<stations>$1</stations>");
		temp = temp.replaceAll("<tr>(<td>\\s*</td>)+</tr>", "");

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
						} else if (tdArray[i].contains("+") || tdArray[i].contains("Request")) {
							lists.get(i).add(tdArray[i].substring(tdArray[i].indexOf("<td>")+4).trim());
						} else if (tdArray[i].contains("The lines below operate") || tdArray[i].contains("The following lines operate")
								|| tdArray[i].contains("The following line operate")) {
							for (int k = i; k < stations.size(); k++) {
								if (k != emptyLocation) {
									lists.get(k).add(tdArray[i].substring(tdArray[i].indexOf("<td>")+4).trim());
								}
							}
						} else {
							lists.get(i).add("---");
						}
					} catch (IndexOutOfBoundsException e){lists.get(i).add("---");}
				}
			}
		}
		
		StringBuilder sb = new StringBuilder(temp.substring(0, temp.indexOf("</stations>")+11));
		for (int i = 0; i < lists.size(); i++) {
			ArrayList<String> l = lists.get(i);
			sb.append("<station>");
			sb.append("<name>"+stations.get(lists.indexOf(l))+"</name>");
			
			int tempIndex = 0;
			for (int j = 0; j < l.size(); j++) {
				String str = l.get(j);
				String s = str.replaceAll("<td>", "").replaceAll("\\s+"," ").trim();

				if (s.contains(":")) {
					int copy = tempIndex;
					tempIndex = figureOutIndex(s.substring(0, s.indexOf(":")), copy);
					if (tempIndex != -1) {
						boolean isMorning = (tempIndex < (timesOfDay.length / 2));

						l.set(j, s + " " + (isMorning ? "AM":"PM"));
						sb.append("<row><time>"+s+" " + (isMorning ? "AM":"PM") +"</time><rownum>"+j+"</rownum></row>"); 
					} else {
						tempIndex = copy;
					}
				} else if (!s.contains("---")) {
					sb.append("<row><message>"+s+"</message><rownum>"+j+"</rownum></row>");
				}
			}
			sb.append("</station>");
		}
		sb.append("</cyride>");
		
		out.println(sb.toString().replaceAll("<td>", "<station>").replaceAll("</td>", "</station>").replaceAll("\\s+", " "));
		
		if (doNotPersist == null) {
			ParseDataFromXML.parseData(sb.toString().replaceAll("<td>", "<station>").replaceAll("</td>", "</station>").replaceAll("\\s+", " "));
		}
	}
	
	private int figureOutIndex(String input, int startingIndex) {
		if (startingIndex >= 0 && startingIndex < timesOfDay.length) {
			for (int i = startingIndex; i < timesOfDay.length; i++) {
				if (timesOfDay[i].equals(input)) {
					return i;
				}
			}
		}
		if (timesOfDay[0].equals(input)) {
			return 0;
		} else {
			return -1;	
		}
	}
}
