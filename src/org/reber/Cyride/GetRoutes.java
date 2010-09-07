package org.reber.Cyride;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GetRoutes extends HttpServlet {
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		StringBuilder sb = new StringBuilder();
		try {
				List<Route> records = (List<Route>)pm.newQuery(Route.class).execute();
				sb.append("{");
				sb.append("\"count\":"+records.size()+",");
				sb.append("\"records\":[");
				for (int i = 0; i < records.size(); i++)
				{
					Route r = records.get(i);
					sb.append(r);
					if (i != records.size() - 1) sb.append(",");
				}
				sb.append("]}");
		} finally {
			out.print(sb.toString());
			pm.close();
		}
	}
}
