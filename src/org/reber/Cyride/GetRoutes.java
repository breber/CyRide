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
		PrintWriter out = resp.getWriter();
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
				List<Route> records = (List<Route>)pm.newQuery(Route.class).execute();
				out.println("{");
				out.println("\"count\":"+records.size()+",");
				out.println("\"records\":[");
				for (int i = 0; i < records.size(); i++)
				{
					Route r = records.get(i);
					out.print(r);
					if (i != records.size() - 1) out.println(",");
					else out.println();
				}
				out.println("]}");
		} finally {
			pm.close();
		}
	}
}
