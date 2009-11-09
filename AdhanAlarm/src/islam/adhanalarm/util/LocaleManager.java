package islam.adhanalarm.util;

import java.util.Locale;

import android.app.Activity;
import android.content.res.Configuration;

public class LocaleManager {

	private Activity activity;
	private boolean languageDirty = false;
	private int languageIndex = DEFAULT_LANGUAGE;

	private static final short DEFAULT_LANGUAGE = 0; // LANGUAGE_KEYS[0] == "default" (represents the default system language (i.e. not necessarily English))

	public static final String[] LANGUAGE_KEYS = new String[]{"default", "en", "de", "fr", "ru", "tr"};

	/**
	 * This class should be instantiated after an activity's super.onCreate() call but before setContentView()
	 * @param a The activity which will get set to the language specified in settings
	 */
	public LocaleManager(Activity a) {
		activity = a;

		// Set the language based on settings
		String languageKey = activity.getSharedPreferences("settingsFile", Activity.MODE_PRIVATE).getString("locale", LANGUAGE_KEYS[DEFAULT_LANGUAGE]);
		if(languageKey.equals("default")) {
			languageKey = Locale.getDefault().getCountry();
		}
		String country = Locale.getDefault().getISO3Country().toUpperCase();
		Locale locale = new Locale(languageKey, country);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		
		if(!android.os.Build.VERSION.RELEASE.equals("2.0")) { // Breaks on Eclair due to restart bug
			activity.getBaseContext().getResources().updateConfiguration(config, activity.getBaseContext().getResources().getDisplayMetrics());
		}

		// Set the language index into the local LANGUAGE_KEYS array
		for(int i = 0; i < LANGUAGE_KEYS.length; i++) {
			if(languageKey.equals(LANGUAGE_KEYS[i])) {
				languageIndex = i;
				break;
			}
		}
	}
	public int getLanguageIndex() {
		return languageIndex;
	}

	public void setDirty() {
		languageDirty = true;
	}

	public boolean isDirty() {
		return languageDirty;
	}
}