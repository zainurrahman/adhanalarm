package islam.adhanalarm.util;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import islam.adhanalarm.R;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.res.XmlResourceParser;
import android.widget.TextView;

public class ThemeManager {

	private Activity activity;
	private boolean themeDirty = false;
	private int themeIndex = DEFAULT_THEME;

	public static final short DEFAULT_THEME = 0;

	private ArrayList<HashMap<String, Integer>> allThemes = new ArrayList<HashMap<String, Integer>>();

	/**
	 * This class should be instantiated before an activity's super.onCreate() call since the theme cannot be set after that
	 * @param a The activity which will get set to the theme specified in settings
	 */
	public ThemeManager(Activity a) {
		activity = a;
		fillAllThemes();

		// Set the theme based on settings
		themeIndex = activity.getSharedPreferences("settingsFile", Activity.MODE_PRIVATE).getInt("themeIndex", DEFAULT_THEME);
		if(themeIndex >= allThemes.size()) themeIndex = DEFAULT_THEME;

		activity.setTheme(allThemes.get(themeIndex).get("style"));
	}
	private void fillAllThemes() {
		ArrayList<Integer> themeManifestFileIds = new ArrayList<Integer>();
		List<PackageInfo> packages = activity.getPackageManager().getInstalledPackages(0);
		for(int i = 0; i < packages.size(); i++) {
			int resId = activity.getResources().getIdentifier("adhan_alarm_theme_manifest", "xml", packages.get(i).packageName);
			if(resId != 0) themeManifestFileIds.add(resId);
		}
		for(int i = 0; i < themeManifestFileIds.size(); i++) {
			final XmlResourceParser parser = activity.getResources().getXml(themeManifestFileIds.get(i));
			try {
				int type = parser.next();
				while((type = parser.next()) != XmlResourceParser.END_DOCUMENT) {
					if(type == XmlResourceParser.START_TAG && "theme".equalsIgnoreCase(parser.getName())) {
						HashMap<String, Integer> theme = new HashMap<String, Integer>();
						theme.put("style", parser.getAttributeResourceValue(null,					"style",					R.style.DefaultTheme));
						theme.put("alternateRowColor", parser.getAttributeResourceValue(null,		"alternateRowColor",		R.color.semi_transparent_white));
						theme.put("tabWidgetBackgroundColor", parser.getAttributeResourceValue(null,"tabWidgetBackgroundColor",	android.R.color.transparent));
						theme.put("compassBackground", parser.getAttributeResourceValue(null,		"compassBackground",		R.drawable.compass_background));
						theme.put("compassNeedle", parser.getAttributeResourceValue(null,			"compassNeedle",			R.drawable.compass_needle));
						theme.put("name", parser.getAttributeResourceValue(null,					"name",						R.string.sdefault));
						allThemes.add(theme);
					}
				}
			} catch(Exception ex) {
				java.io.StringWriter sw = new java.io.StringWriter();
				java.io.PrintWriter pw = new java.io.PrintWriter(sw, true);
				ex.printStackTrace(pw);
				pw.flush(); sw.flush();
				((TextView)activity.findViewById(R.id.notes)).setText(sw.toString());
			}
		}
	}

	public int getThemeIndex() {
		return themeIndex;
	}

	public int getAlternateRowColor() {
		return allThemes.get(themeIndex).get("alternateRowColor");
	}

	public int getTabWidgetBackgroundColor() {
		return allThemes.get(themeIndex).get("tabWidgetBackgroundColor");
	}

	public int getCompassBackground() {
		return allThemes.get(themeIndex).get("compassBackground");
	}

	public int getCompassNeedle() {
		return allThemes.get(themeIndex).get("compassNeedle");
	}

	public void setDirty() {
		themeDirty = true;
	}

	public boolean isDirty() {
		return themeDirty;
	}
	
	public String[] getAllThemeNames() {
		String[] allThemeNames = new String[allThemes.size()];
		for(int i = 0; i < allThemes.size(); i++) {
			allThemeNames[i] = activity.getResources().getString(allThemes.get(i).get("name"));
		}
		return allThemeNames;
	}
}