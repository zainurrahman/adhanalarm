package islam.adhanalarm;

import net.sourceforge.jitl.Method;
import net.sourceforge.jitl.Rounding;

public class CONSTANT {
	public static final boolean DEBUG = false;
	public static final long RESTART_DELAY = 250; // Milliseconds
	public static final long POST_NOTIFICATION_DELAY = 2000; // Milliseconds
	
	public static final short DEFAULT_THEME = 0;
	public static final int[] ALL_THEMES = new int[]{R.style.DefaultTheme, R.style.GreenTheme};
	public static final int[] ALTERNATE_ROW_COLORS = new int[]{R.color.semi_transparent_white, R.color.semi_transparent_black};

	public static final String[] LANGUAGE_KEYS = new String[]{"default", "en", "de", "fr", "ru", "tr"};
	
	public static final short FAJR = 0, SUNRISE = 1, DHUHR = 2, ASR = 3, MAGHRIB = 4, ISHAA = 5, NEXT_FAJR = 6; // Notification Times
	public static final short DEFAULT_NOTIFICATION = 0, RECITE_ADHAN = 1, NO_NOTIFICATIONS = 2; // Notification Methods
	public static final short NO_EXTRA_ALERTS = 0, ALERT_SUNRISE = 1; // Extra Alerts
	public static final short DEFAULT_CALCULATION_METHOD = 4; // Muslim World League
	public static final short DEFAULT_ROUNDING_TYPE = 2; // Special
	
	public static final Method[] CALCULATION_METHODS = new Method[]{Method.EGYPT_SURVEY, Method.KARACHI_SHAF, Method.KARACHI_HANAF, Method.NORTH_AMERICA, Method.MUSLIM_LEAGUE, Method.UMM_ALQURRA, Method.FIXED_ISHAA};
	public static final Rounding[] ROUNDING_TYPES = new Rounding[]{Rounding.NONE, Rounding.NORMAL, Rounding.SPECIAL, Rounding.AGRESSIVE};
	
	public static int getLanguageIndex() {
		if(VARIABLE.settings == null) return 0;
		String languageKey = VARIABLE.settings.getString("locale", CONSTANT.LANGUAGE_KEYS[0]);
		for(int i = 0; i < LANGUAGE_KEYS.length; i++) {
			if(languageKey.equals(LANGUAGE_KEYS[i])) {
				return i;
			}
		}
		return 0;
	}
	
	private CONSTANT() {
		// Private constructor to enforce un-instantiability.
	}
}