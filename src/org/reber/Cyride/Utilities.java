package org.reber.Cyride;

/**
 * @author brianreber
 *
 */
public class Utilities {
	
	private Utilities() { }

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
	
	public static int getRouteId(String name) {
		if (name.contains("1") && name.contains("West")) return 0;
		if (name.contains("1") && name.contains("East")) return 1;
		if (name.contains("2") && name.contains("West")) return 2;
		if (name.contains("2") && name.contains("East")) return 3;
		if (name.contains("3") && name.contains("South")) return 4;
		if (name.contains("3") && name.contains("North")) return 5;
		if ((name.contains("4") && !name.contains("4A")) && name.contains("Gray")) return 6;
		if (name.contains("4A") && name.contains("Gray")) return 7;
		if (name.contains("5") && name.contains("Yellow")) return 8;
		if (name.contains("6") && name.contains("Brown") && name.contains("North")) return 9;
		if (name.contains("6A") && name.contains("Towers")) return 10;
		if (name.contains("6B") || (name.contains("6") && name.contains("Brown") && name.contains("South"))) return 11;
		if (name.contains("7") && name.contains("Purple")) return 12;
		if (name.contains("10") && name.contains("Pink")) return 13;
		if (name.contains("21") && name.contains("Cardinal")) return 14;
		if (name.contains("22") && name.contains("Gold")) return 15;
		if (name.contains("23") && name.contains("Orange")) return 16;
		if (name.contains("24") && name.contains("Silver")) return 17;
		
		else return -1;
	}
	
}
