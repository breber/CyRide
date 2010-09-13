package org.reber.Cyride;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author brianreber
 *
 */
@SuppressWarnings("serial")
public class GetCount extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		PrintWriter out = resp.getWriter();
		
		@SuppressWarnings("unchecked")
		Count count = ((List<Count>) pm.newQuery(Count.class).execute()).get(0);
		
		out.println(count.getCount());
	}
}
