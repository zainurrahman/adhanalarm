package islam.adhanalarm;

import net.sourceforge.jitl.Rounding;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

public class VARIABLE {

	public static SharedPreferences settings;
	public static boolean mainActivityIsRunning = false;

	public static float qiblaDirection = 0;

	public static final Rounding[] ROUNDING_TYPES = new Rounding[]{Rounding.NONE, Rounding.NORMAL, Rounding.SPECIAL, Rounding.AGRESSIVE};

	public static Location getCurrentLocation(Context context) {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		Location currentLocation = null;
		try {
			currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
		} catch(Exception ex) {
			// GPS or wireless networks is disabled
		}
		return currentLocation;
	}

	private VARIABLE() {
		// Private constructor to enforce un-instantiability.
	}

	public static boolean alertSunrise() {
		if(settings == null) return false;
		return settings.getInt("extraAlertsIndex", CONSTANT.NO_EXTRA_ALERTS) == CONSTANT.ALERT_SUNRISE;
	}
}