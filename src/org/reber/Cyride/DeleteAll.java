package org.reber.Cyride;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.apphosting.api.DeadlineExceededException;

/**
 * @author brianreber
 *
 */
@SuppressWarnings("serial")
public class DeleteAll extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(Route.class);
			q.setFilter("routeId == 0 OR routeId == 1 OR routeId == 3 OR routeId == 4");
			q.setFilter("day == 0");
			try {
				q.deletePersistentAll();
			} catch (DeadlineExceededException e) {
				q.deletePersistentAll();
			}

		} finally {
			pm.close();
		}
	}

}
