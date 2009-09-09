package islam.adhanalarm.util;

import islam.adhanalarm.R;
import android.app.Activity;

public class ThemeManager {

	private Activity activity;
	private boolean themeDirty = false;
	private int themeIndex = DEFAULT_THEME;

	public static final short DEFAULT_THEME = 0;

	private static final int[] ALL_THEMES = new int[]{							R.style.DefaultTheme,				R.style.BrownTheme,					R.style.DialogTheme,				R.style.GreenTheme,					R.style.LightTheme};
	private static final int[] ALTERNATE_ROW_COLORS = new int[]{				R.color.semi_transparent_white,		R.color.semi_transparent_black,		R.color.semi_transparent_white,		R.color.semi_transparent_black,		R.color.semi_transparent_black};
	private static final int[] TAB_WIDGET_BACKGROUND_COLORS = new int[]{		android.R.color.black,				R.color.brown_background_color,		android.R.color.transparent,		R.color.green_background_color,		R.color.semi_transparent_black};
	private static final int[] COMPASS_BACKGROUND = new int[]{					R.drawable.compass_background,		R.drawable.compass_background,		R.drawable.compass_background,		R.drawable.compass_background,		R.drawable.compass_background};
	private static final int[] COMPASS_NEEDLE = new int[]{						R.drawable.compass_needle,			R.drawable.compass_needle,			R.drawable.compass_needle,			R.drawable.compass_needle,			R.drawable.compass_needle};

	/**
	 * This class should be instantiated before an activity's super.onCreate() call since the theme cannot be set after that
	 * @param a The activity which will get set to the theme specified in settings
	 */
	public ThemeManager(Activity a) {
		activity = a;

		// Set the theme based on settings
		themeIndex = activity.getSharedPreferences("settingsFile", Activity.MODE_PRIVATE).getInt("themeIndex", DEFAULT_THEME);
		if(themeIndex >= activity.getResources().getTextArray(R.array.themes).length) themeIndex = DEFAULT_THEME;

		activity.setTheme(ALL_THEMES[themeIndex]);
	}

	public int getThemeIndex() {
		return themeIndex;
	}

	public int getAlternateRowColor() {
		return ALTERNATE_ROW_COLORS[themeIndex];
	}

	public int getTabWidgetBackgroundColor() {
		return TAB_WIDGET_BACKGROUND_COLORS[themeIndex];
	}

	public int getCompassBackground() {
		return COMPASS_BACKGROUND[themeIndex];
	}

	public int getCompassNeedle() {
		return COMPASS_NEEDLE[themeIndex];
	}

	public void setDirty() {
		themeDirty = true;
	}

	public boolean isDirty() {
		return themeDirty;
	}
}