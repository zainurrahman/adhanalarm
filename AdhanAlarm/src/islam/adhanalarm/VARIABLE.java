package islam.adhanalarm;

import net.sourceforge.jitl.Rounding;
import android.content.SharedPreferences;

public class VARIABLE {

	public static SharedPreferences settings;
	public static boolean mainActivityIsRunning = false;

	public static float qiblaDirection = 0;

	public static boolean themeDirty = false;
	public static boolean languageDirty = false;
	public static final Rounding[] ROUNDING_TYPES = new Rounding[]{Rounding.NONE, Rounding.NORMAL, Rounding.SPECIAL, Rounding.AGRESSIVE};

	public static int getLanguageIndex() {
		if(settings == null) return CONSTANT.DEFAULT_LANGUAGE;
		String languageKey = settings.getString("locale", CONSTANT.LANGUAGE_KEYS[CONSTANT.DEFAULT_LANGUAGE]);
		for(int i = 0; i < CONSTANT.LANGUAGE_KEYS.length; i++) {
			if(languageKey.equals(CONSTANT.LANGUAGE_KEYS[i])) {
				return i;
			}
		}
		return 0;
	}
	public static int getThemeIndex() {
		if(settings == null) return CONSTANT.DEFAULT_THEME;
		return settings.getInt("themeIndex", CONSTANT.DEFAULT_THEME);
	}

	private VARIABLE() {
		// Private constructor to enforce un-instantiability.
	}

	public static boolean alertSunrise() {
		if(settings == null) return false;
		return settings.getInt("extraAlertsIndex", CONSTANT.NO_EXTRA_ALERTS) == CONSTANT.ALERT_SUNRISE;
	}
}
