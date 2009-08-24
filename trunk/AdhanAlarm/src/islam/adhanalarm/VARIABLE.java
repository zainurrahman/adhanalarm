package islam.adhanalarm;

import android.content.Context;
import android.content.SharedPreferences;

public class VARIABLE {
	
	public static String[] TIME_NAMES;
	public static SharedPreferences settings;
	public static Context applicationContext;
	public static boolean mainActivityIsRunning = false;
	
	private VARIABLE() {
		// Private constructor to enforce un-instantiability.
	}
	
	public static boolean alertSunrise() {
		if(settings == null) return false;
		return settings.getInt("extraAlertsIndex", CONSTANT.NO_EXTRA_ALERTS) == CONSTANT.ALERT_SUNRISE;
	}
}
