package islam.adhanalarm;

import islam.adhanalarm.dialog.AdvancedSettingsDialog;
import islam.adhanalarm.dialog.CalculationSettingsDialog;
import islam.adhanalarm.dialog.NotificationSettingsDialog;
import islam.adhanalarm.dialog.InterfaceSettingsDialog;
import islam.adhanalarm.view.QiblaCompassView;
import islam.adhanalarm.receiver.StartNotificationReceiver;
import islam.adhanalarm.service.FillDailyTimetableService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

public class AdhanAlarm extends Activity {
	private ArrayList<HashMap<String, String>> timetable = new ArrayList<HashMap<String, String>>(7);
	private SimpleAdapter timetableView;

	private static SensorListener orientationListener;
	private static boolean isTrackingOrientation = false;

	@Override
	public void onCreate(Bundle icicle) {
		VARIABLE.mainActivityIsRunning = true;
		VARIABLE.settings = getSharedPreferences("settingsFile", MODE_PRIVATE);
		
		setTheme();
		super.onCreate(icicle);

		setLocale();
		setContentView(R.layout.main);

		for(int i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("time_name", getString(CONSTANT.TIME_NAMES[i]));
			timetable.add(i, map);
		}
		timetableView = new SimpleAdapter(this, timetable, R.layout.timetable_row, new String[]{"mark", "time_name", "time", "time_am_pm"}, new int[]{R.id.mark, R.id.time_name, R.id.time, R.id.time_am_pm}) {
			public boolean areAllItemsEnabled() { return false; } // Disable list's item selection
			public boolean isEnabled(int position) { return false; }
		};
		((ListView)findViewById(R.id.timetable)).setAdapter(timetableView);

		((ListView)findViewById(R.id.timetable)).setOnHierarchyChangeListener(new OnHierarchyChangeListener() { // Set zebra stripes
			private int numChildren = 0;
			public void onChildViewAdded(View parent, View child) {
				int themeIndex = VARIABLE.settings.getInt("themeIndex", CONSTANT.DEFAULT_THEME);
				int alternateRowColor = CONSTANT.ALTERNATE_ROW_COLORS[themeIndex];
				child.setBackgroundResource(++numChildren % 2 == 0 ? alternateRowColor : android.R.color.transparent);
				if(numChildren > CONSTANT.NEXT_FAJR) numChildren = 0; // Last row has been reached, reset for next time
			}
			public void onChildViewRemoved(View parent, View child) {
			}
		});

		double gmtOffset = Schedule.getGMTOffset();
		String plusMinusGMT = gmtOffset < 0 ? "" + gmtOffset : "+" + gmtOffset;
		String daylightTime = Schedule.isDaylightSavings() ? " " + getString(R.string.daylight_savings) : "";
		((TextView)findViewById(R.id.display_time_zone)).setText(getString(R.string.system_time_zone) + ": " + getString(R.string.gmt) + plusMinusGMT + " (" + new GregorianCalendar().getTimeZone().getDisplayName() + daylightTime + ")");

		TabHost tabs = (TabHost)findViewById(R.id.tabs);
		tabs.setup();
		tabs.getTabWidget().setBackgroundResource(CONSTANT.TAB_WIDGET_BACKGROUND_COLORS[VARIABLE.getThemeIndex()]);

		TabHost.TabSpec one = tabs.newTabSpec("one");
		one.setContent(R.id.content1);
		one.setIndicator(getString(R.string.today), getResources().getDrawable(R.drawable.calendar));
		tabs.addTab(one);
		configureCalculationDefaults(); /* End of Tab 1 Items */

		TabHost.TabSpec two = tabs.newTabSpec("two");
		two.setContent(R.id.content2);
		two.setIndicator(getString(R.string.qibla), getResources().getDrawable(R.drawable.compass));
		tabs.addTab(two);

		((QiblaCompassView)findViewById(R.id.qibla_compass)).setConstants(((TextView)findViewById(R.id.bearing_north)), getText(R.string.bearing_north), ((TextView)findViewById(R.id.bearing_qibla)), getText(R.string.bearing_qibla));
		orientationListener = new SensorListener() {
			public void onSensorChanged(int s, float v[]) {
				float northDirection = v[android.hardware.SensorManager.DATA_X];
				((QiblaCompassView)findViewById(R.id.qibla_compass)).setDirections(northDirection, VARIABLE.qiblaDirection);
				
			}
			public void onAccuracyChanged(int s, int a) {
			}
		};
		startTrackingOrientation(); /* End of Tab 2 Items */

		TabHost.TabSpec three = tabs.newTabSpec("three");
		three.setContent(R.id.content3);
		three.setIndicator(getString(R.string.settings), getResources().getDrawable(R.drawable.calculator));
		tabs.addTab(three);

		((Button)findViewById(R.id.set_calculation)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				showSettingsDialog(new CalculationSettingsDialog(v.getContext()), v.getContext());
			}
		});
		((Button)findViewById(R.id.set_notification)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				showSettingsDialog(new NotificationSettingsDialog(v.getContext()), v.getContext());
			}
		});
		((Button)findViewById(R.id.set_interface)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				showSettingsDialog(new InterfaceSettingsDialog(v.getContext()), v.getContext());
			}
		});
		((Button)findViewById(R.id.set_advanced)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				showSettingsDialog(new AdvancedSettingsDialog(v.getContext()), v.getContext());
			}
		});	/* End of Tab 3 Items */
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.layout.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setCancelable(true);
		short time = Schedule.today().nextTimeIndex();
		switch(item.getItemId()) {
		case R.id.menu_previous:
			time--;
			if(time < CONSTANT.FAJR) time = CONSTANT.ISHAA;
			if(time == CONSTANT.SUNRISE && !VARIABLE.alertSunrise()) time = CONSTANT.FAJR;
			Notifier.start(this, time, Schedule.today().getTimes()[time].getTimeInMillis());
			break;
		case R.id.menu_next:
			if(time == CONSTANT.SUNRISE && !VARIABLE.alertSunrise()) time = CONSTANT.DHUHR;
			Notifier.start(this, time, Schedule.today().getTimes()[time].getTimeInMillis());
			break;
		case R.id.menu_stop:
			Notifier.stop();
			break;
		case R.id.menu_help:
			dialogBuilder.setMessage(R.string.help_text);
			dialogBuilder.create().show();
			break;
		case R.id.menu_information:
			dialogBuilder.setMessage(R.string.information_text);
			dialogBuilder.create().show();
			break;
		}
		return true;
	}
	@Override
	public void onStop() {
		super.onStop();
		stopTrackingOrientation();
		VARIABLE.mainActivityIsRunning = false;
	}
	@Override
	public void onResume() {
		super.onResume();
		boolean resetNotification = getIntent().getBooleanExtra("setNotification", true);
		getIntent().removeExtra("setNotification");
		updateTodaysTimetableAndNotification(resetNotification);
		((TabHost)findViewById(R.id.tabs)).setCurrentTab(0);
	}
	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		super.onNewIntent(intent);
	}

	private void startTrackingOrientation() {
		if(!isTrackingOrientation) isTrackingOrientation = ((SensorManager)getSystemService(SENSOR_SERVICE)).registerListener(orientationListener, android.hardware.SensorManager.SENSOR_ORIENTATION);
	}
	private void stopTrackingOrientation() {
		if(isTrackingOrientation) ((SensorManager)getSystemService(SENSOR_SERVICE)).unregisterListener(orientationListener);
		isTrackingOrientation = false;
	}

	private void showSettingsDialog(Dialog d, final Context context) {
		d.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface d) {
				updateTodaysTimetableAndNotification(true);
				if(VARIABLE.settings.contains("latitude") && VARIABLE.settings.contains("longitude")) ((TextView)findViewById(R.id.notes)).setText("");
				if(VARIABLE.themeDirty || VARIABLE.languageDirty) {
					VARIABLE.themeDirty = false;
					VARIABLE.languageDirty = false;
					restart(context);
				}
			}
		});
		d.show();
	}
	private void restart(Context context) {
		long restartTime = Calendar.getInstance().getTimeInMillis() + CONSTANT.RESTART_DELAY;
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, restartTime, PendingIntent.getActivity(context, 0, getIntent(), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT));
		finish();
	}

	private void setTheme() {
		setTheme(CONSTANT.ALL_THEMES[VARIABLE.getThemeIndex()]);
	}
	private void setLocale() {
		String languageKey = VARIABLE.settings.getString("locale", CONSTANT.LANGUAGE_KEYS[CONSTANT.DEFAULT_LANGUAGE]);
		if(languageKey.equals("default")) {
			languageKey = Locale.getDefault().getCountry();
		}
		Locale locale = new Locale(languageKey);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		setTitle(R.string.app_name);
	}
	
	private void configureCalculationDefaults() {
		if(!VARIABLE.settings.contains("latitude") || !VARIABLE.settings.contains("longitude")) {
			Location currentLocation = VARIABLE.getCurrentLocation(this);
			try {
				SharedPreferences.Editor editor = VARIABLE.settings.edit();
				editor.putFloat("latitude", (float)currentLocation.getLatitude());
				editor.putFloat("longitude", (float)currentLocation.getLongitude());
				editor.commit();
			} catch(Exception ex) {
				((TextView)findViewById(R.id.notes)).setText(getString(R.string.location_not_set));
			}
		}
		if(!VARIABLE.settings.contains("calculationMethodsIndex")) {
			try {
				String country = Locale.getDefault().getISO3Country().toUpperCase();
				
				SharedPreferences.Editor editor = VARIABLE.settings.edit();
				for(int i = 0; i < CONSTANT.CALCULATION_METHOD_COUNTRY_CODES.length; i++) {
					if(Arrays.asList(CONSTANT.CALCULATION_METHOD_COUNTRY_CODES[i]).contains(country)) {
						editor.putInt("calculationMethodsIndex", i);
						editor.commit();
						break;
					}
				}
			} catch(Exception ex) {
				// Wasn't set, oh well we'll uses DEFAULT_CALCULATION_METHOD later
			}
		}
	}
	
	private void updateTodaysTimetableAndNotification(boolean resetNotification) {
		if(resetNotification) {
			StartNotificationReceiver.setNext(this);
		}
		FillDailyTimetableService.set(this, new GregorianCalendar(), timetable, timetableView, getString(R.string.next_time_marker), ((TextView)findViewById(R.id.current_latitude_deg)), ((TextView)findViewById(R.id.current_latitude_min)), ((TextView)findViewById(R.id.current_latitude_sec)), ((TextView)findViewById(R.id.current_longitude_deg)), ((TextView)findViewById(R.id.current_longitude_min)), ((TextView)findViewById(R.id.current_longitude_sec)), ((TextView)findViewById(R.id.current_qibla_deg)), ((TextView)findViewById(R.id.current_qibla_min)), ((TextView)findViewById(R.id.current_qibla_sec)), ((TextView)findViewById(R.id.notes)));
	}
}