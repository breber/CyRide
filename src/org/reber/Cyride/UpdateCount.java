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
