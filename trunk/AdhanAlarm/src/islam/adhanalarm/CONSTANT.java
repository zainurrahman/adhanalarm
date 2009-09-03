package islam.adhanalarm;

import net.sourceforge.jitl.Method;

public class CONSTANT {

	public static final boolean DEBUG = false;

	public static final long RESTART_DELAY = 1000; // 1 second
	public static final long POST_NOTIFICATION_DELAY = 5000; // 5 seconds

	public static final short DEFAULT_THEME = 0;
	public static final int[] ALL_THEMES = new int[]{						R.style.DefaultTheme,				R.style.BrownTheme,					R.style.DialogTheme,				R.style.GreenTheme,					R.style.LightTheme};
	public static final int[] ALTERNATE_ROW_COLORS = new int[]{				R.color.semi_transparent_white,		R.color.semi_transparent_black,		R.color.semi_transparent_white,		R.color.semi_transparent_black,		R.color.semi_transparent_black};
	public static final int[] TAB_WIDGET_BACKGROUND_COLORS = new int[]{		android.R.color.black,				R.color.brown_background_color,		android.R.color.transparent,		R.color.green_background_color,		android.R.color.transparent};

	public static final String[] LANGUAGE_KEYS = new String[]{"default", "en", "de", "fr", "ru", "tr"};
	public static final short DEFAULT_LANGUAGE = 0; // LANGUAGE_KEYS[0] represents the default system language (i.e. not necessarily English)

	public static final String[][] CALCULATION_METHOD_COUNTRY_CODES = new String[][]{

		/** METHOD_EGYPT_SURVEY:	Africa, Syria, Iraq, Lebanon, Malaysia, Parts of the USA **/
		new String[]{
				// Africa
				"AGO", "BDI", "BEN", "BFA", "BWA", "CAF", "CIV", "CMR", "COG", "COM", "CPV", "DJI", "DZA", "EGY", "ERI", "ESH", "ETH", "GAB", "GHA", "GIN", "GMB", "GNB", "GNQ", "KEN", "LBR", "LBY", "LSO", "MAR", "MDG", "MLI", "MOZ", "MRT", "MUS", "MWI", "MYT", "NAM", "NER", "NGA", "REU", "RWA", "SDN", "SEN", "SLE", "SOM", "STP", "SWZ", "SYC", "TCD", "TGO", "TUN", "TZA", "UGA", "ZAF", "ZAR", "ZWB", "ZWE",
				// Syria, Iraq, Lebanon, Malaysia
				"IRQ", "LBN", "MYS", "SYR"
		},

		/** METHOD_KARACHI_SHAF:		____ **/
		new String[]{},

		/** METHOD_KARACHI_HANAF:	Pakistan, Bangladesh, India, Afghanistan, Parts of Europe **/
		new String[]{"AFG", "BGD", "IND", "PAK"},

		/** METHOD_NORTH_AMERICA:	Parts of the USA, Canada, Parts of the UK **/
		new String[]{"USA", "CAN"},

		/** METHOD_MUSLIM_LEAGUE:	Europe, The Far East, Parts of the USA **/
		new String[]{
				// Europe
				"AND", "AUT", "BEL", "DNK", "FIN", "FRA", "DEU", "GIB", "IRL", "ITA", "LIE", "LUX", "MCO", "NLD", "NOR", "PRT", "SMR", "ESP", "SWE", "CHE", "GBR", "VAT",
				// Far East
				"CHN", "JPN", "KOR", "PRK", "TWN"
		},

		/** METHOD_UMM_ALQURRA:		The Arabian Peninsula **/
		new String[]{"BHR", "KWT", "OMN", "QAT", "SAU", "YEM"},

		/** METHOD_FIXED_ISHAA:		___ **/
		new String[]{}

	};
	public static final Method[] CALCULATION_METHODS = new Method[]{Method.EGYPT_SURVEY, Method.KARACHI_SHAF, Method.KARACHI_HANAF, Method.NORTH_AMERICA, Method.MUSLIM_LEAGUE, Method.UMM_ALQURRA, Method.FIXED_ISHAA};
	public static final short DEFAULT_CALCULATION_METHOD = 4; // MUSLIM_LEAGUE

	public static final short FAJR = 0, SUNRISE = 1, DHUHR = 2, ASR = 3, MAGHRIB = 4, ISHAA = 5, NEXT_FAJR = 6; // Notification Times
	public static int[] TIME_NAMES = new int[]{R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.maghrib, R.string.ishaa, R.string.next_fajr};
	public static final short DEFAULT_NOTIFICATION = 0, RECITE_ADHAN = 1, NO_NOTIFICATIONS = 2; // Notification Methods
	public static final short NO_EXTRA_ALERTS = 0, ALERT_SUNRISE = 1; // Extra Alerts
	public static final short DEFAULT_ROUNDING_TYPE = 2; // Special

	private CONSTANT() {
		// Private constructor to enforce un-instantiability.
	}
}