package org.reber.Cyride;

import java.io.IOException;
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
public class UpdateCount  extends HttpServlet {
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws IOException {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		int routesCount = ((List<Route>) pm.newQuery(Route.class).execute()).size();
		List<Count> c = (List<Count>) pm.newQuery(Count.class).execute();
		try {
			if (c.size() == 0) {
				pm.makePersistent(new Count(routesCount));
			} else {
				c.get(0).setCount(routesCount);
			}
		} finally {
			pm.close();
		}
	}
}
