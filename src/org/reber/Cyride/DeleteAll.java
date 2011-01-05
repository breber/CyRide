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
//			q.setFilter("routeId == 0 OR routeId == 1 OR routeId == 2");
//			q.setFilter("day == 0");
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
