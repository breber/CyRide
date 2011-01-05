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
package org.reber.CyRideMobile;

import java.util.HashMap;
import java.util.Map;

/**
 * A Utilities class containing constants and
 * basic static methods meant to make the data code
 * easier.
 * 
 * @author brian
 */
public class Utilities {

	public static String PACKAGE = "org.reber.CyRideMobile";
	public static String DATA_URL = "http://cyridesql.appspot.com/getroutes";
	
	private static Map<String, Integer> colors = new HashMap<String, Integer>();
	
	static {
		colors.put("Red", R.drawable.red);
		colors.put("Blue", R.drawable.blue);
		colors.put("Green", R.drawable.green);
		colors.put("Brown", R.drawable.brown);
		colors.put("Towers", R.drawable.brown);
		colors.put("Yellow", R.drawable.yellow);
		colors.put("Orange", R.drawable.orange);
		colors.put("Pink", R.drawable.pink);
		colors.put("Purple", R.drawable.purple);
		colors.put("Towers", R.drawable.darkerbrown);
		colors.put("Silver", R.drawable.silver);
		colors.put("Gray", R.drawable.gray);
		colors.put("Cardinal", R.drawable.cardinal);
		colors.put("Gold", R.drawable.gold);
	}
	
	/**
	 * Gets the image resource that matches the given color/route string
	 * 
	 * @param color
	 * @return
	 * The resource id for the color image
	 */
	public static int getImageResource(String color) {
		for (String s : color.split("\\s|/")) {
			if (colors.containsKey(s)) {
				return colors.get(s);
			}
		}
		
		return R.drawable.clear;
	}
}