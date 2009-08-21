package islam.adhanalarm;

import islam.adhanalarm.dialog.AdvancedSettingsDialog;
import islam.adhanalarm.dialog.CalculationSettingsDialog;
import islam.adhanalarm.dialog.NotificationSettingsDialog;
import islam.adhanalarm.view.QiblaCompassView;
import islam.adhanalarm.service.NotifierService;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Calendar;
import java.util.GregorianCalendar;
import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.Method;
import net.sourceforge.jitl.Prayer;
import net.sourceforge.jitl.astro.Dms;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
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
	
	private static SharedPreferences settings;
	
    private static SensorListener orientationListener;
	private static float qiblaDirection = 0;
	private static boolean isTrackingOrientation = false;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		
		NotifierService.setContext(this);
		settings = getSharedPreferences("settingsFile", MODE_PRIVATE);
		
		VARIABLE.TIME_NAMES = new String[]{getString(R.string.fajr), getString(R.string.sunrise), getString(R.string.dhuhr), getString(R.string.asr), getString(R.string.maghrib), getString(R.string.ishaa), getString(R.string.next_fajr)};
		
		for(int i = 0; i < VARIABLE.TIME_NAMES.length; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("time_name", VARIABLE.TIME_NAMES[i]);
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
        		child.setBackgroundResource(++numChildren % 2 == 0 ? R.color.semi_transparent : R.color.transparent);
        		if(numChildren == VARIABLE.TIME_NAMES.length) numChildren = 0; // Last row has been reached, reset for next time
        	}
        	public void onChildViewRemoved(View parent, View child) {
        	}
		});

		double gmtOffset = getGMTOffset();
		String plusMinusGMT = gmtOffset < 0 ? "" + gmtOffset : "+" + gmtOffset;
		String daylightTime = isDaylightSavings() ? " " + getString(R.string.daylight_savings) : "";
		((TextView)findViewById(R.id.display_time_zone)).setText(getString(R.string.system_time_zone) + ": " + getString(R.string.gmt) + plusMinusGMT + " (" + new GregorianCalendar().getTimeZone().getDisplayName() + daylightTime + ")");

		TabHost tabs = (TabHost)findViewById(R.id.tabs);
		tabs.setup();

		TabHost.TabSpec one = tabs.newTabSpec("one");
		one.setContent(R.id.content1);
		one.setIndicator(getString(R.string.today), getResources().getDrawable(R.drawable.calendar));
		tabs.addTab(one);
		if(!settings.contains("latitude") || !settings.contains("longitude")) {
			((TextView)findViewById(R.id.notes)).setText(getString(R.string.location_not_set));
		} /* End of Tab 1 Items */

		TabHost.TabSpec two = tabs.newTabSpec("two");
		two.setContent(R.id.content2);
		two.setIndicator(getString(R.string.qibla), getResources().getDrawable(R.drawable.compass));
		tabs.addTab(two);
		
		((QiblaCompassView)findViewById(R.id.qibla_compass)).setConstants(((TextView)findViewById(R.id.bearing_north)), getText(R.string.bearing_north), ((TextView)findViewById(R.id.bearing_qibla)), getText(R.string.bearing_qibla));
		orientationListener = new SensorListener() {
        	public void onSensorChanged(int s, float v[]) {
        		float northDirection = v[android.hardware.SensorManager.DATA_X];
        		((QiblaCompassView)findViewById(R.id.qibla_compass)).setDirections(northDirection, qiblaDirection);
        	}
        	public void onAccuracyChanged(int s, int a) {
        	}
		}; /* End of Tab 2 Items */
		
		TabHost.TabSpec three = tabs.newTabSpec("three");
		three.setContent(R.id.content3);
		three.setIndicator(getString(R.string.settings), getResources().getDrawable(R.drawable.calculator));
		tabs.addTab(three);

		((Button)findViewById(R.id.set_advanced)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				showSettingsDialog(new AdvancedSettingsDialog(v.getContext()));
			}
		});
		((Button)findViewById(R.id.set_notification)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				showSettingsDialog(new NotificationSettingsDialog(v.getContext()));
			}
		});
		((Button)findViewById(R.id.set_calculation)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				showSettingsDialog(new CalculationSettingsDialog(v.getContext()));
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
		switch(item.getItemId()) {
		case R.id.menu_previous:
			int time = getNextNotificationTime() - 1;
			if(time < CONSTANT.FAJR) time = CONSTANT.ISHAA;
			if(time == CONSTANT.SUNRISE && !alertSunrise()) time = CONSTANT.FAJR;
			NotifierService.start((short)time);
			break;
		case R.id.menu_next:
			NotifierService.start(getNextNotificationTime());
			break;
		case R.id.menu_stop:
			NotifierService.stop();
			WakeLock.release();
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
		NotifierService.stop();
		stopTrackingOrientation();
		WakeLock.release();
		super.onStop();
	}
	@Override
	public void onResume() {
		if(getIntent().getBooleanExtra("clearNotification", false)) {
			NotifierService.stop(); // TODO: at WakeUpAndDoSomething should simply start notifier service, only if activity is open should it update gui
		}
		short notificationTime = getIntent().getShortExtra("nextNotificationTime", (short)-1);
		if(notificationTime >= CONSTANT.FAJR && notificationTime <= CONSTANT.NEXT_FAJR) NotifierService.start(notificationTime);
		updateScheduleAndNotification();
		((TabHost)findViewById(R.id.tabs)).setCurrentTab(0);
		startTrackingOrientation();
		WakeLock.release();
		super.onResume();
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

	private void showSettingsDialog(Dialog d) {
		d.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface d) {
				updateScheduleAndNotification();
				if(settings.contains("latitude") && settings.contains("longitude")) ((TextView)findViewById(R.id.notes)).setText("");
			}
		});
		d.show();
	}

	private double getGMTOffset() {
		Calendar currentTime = new GregorianCalendar();
		int gmtOffset = currentTime.getTimeZone().getOffset(currentTime.getTimeInMillis());
		return gmtOffset / 3600000;
	}

	private boolean isDaylightSavings() {
		Calendar currentTime = new GregorianCalendar();
		return currentTime.getTimeZone().inDaylightTime(currentTime.getTime());
	}
	
	private boolean alertSunrise() {
		return settings.getInt("extraAlertsIndex", CONSTANT.NO_EXTRA_ALERTS) == CONSTANT.ALERT_SUNRISE;
	}

	private short getNextNotificationTime() {
		for(short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) {
			if(timetable.get(i).get("mark") == getString(R.string.next_time_marker)) return i;
		}
		return -1;
	}

	private void indicateNotificationTimes(short nextNotificationTime) {
		for(short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) {
			timetable.get(i).put("mark", ""); // Clear all existing markers in case it was left from the previous day or while phone was turned off
		}

		int previousNotificationTime = nextNotificationTime - 1 < CONSTANT.FAJR ? CONSTANT.ISHAA : nextNotificationTime - 1;
		
		if(!alertSunrise() && nextNotificationTime == CONSTANT.SUNRISE) nextNotificationTime = CONSTANT.DHUHR;
		if(!alertSunrise() && previousNotificationTime == CONSTANT.SUNRISE) previousNotificationTime = CONSTANT.FAJR;
		
		timetable.get(nextNotificationTime).put("mark", getString(R.string.next_time_marker));
	}

	private void updateScheduleAndNotification() {
		try {
			Method method = CONSTANT.CALCULATION_METHODS[settings.getInt("calculationMethodsIndex", 4)].copy();
			method.setRound(CONSTANT.ROUNDING_TYPES[settings.getInt("roundingTypesIndex", 2)]);

			net.sourceforge.jitl.astro.Location location = new net.sourceforge.jitl.astro.Location(settings.getFloat("latitude", 43.67f), settings.getFloat("longitude", -79.417f), getGMTOffset(), 0);
			location.setSeaLevel(settings.getFloat("altitude", 0) < 0 ? 0 : settings.getFloat("altitude", 0));
			location.setPressure(settings.getFloat("pressure", 1010));
			location.setTemperature(settings.getFloat("temperature", 10));

			Jitl itl = CONSTANT.DEBUG ? new DummyJitl(location, method) : new Jitl(location, method);
			Calendar currentTime = Calendar.getInstance();
			GregorianCalendar today = new GregorianCalendar();
			GregorianCalendar tomorrow = new GregorianCalendar();
			tomorrow.add(Calendar.DATE, 1);
			Prayer[] dayPrayers = itl.getPrayerTimes(today).getPrayers();
			Prayer[] allTimes = new Prayer[]{dayPrayers[0], dayPrayers[1], dayPrayers[2], dayPrayers[3], dayPrayers[4], dayPrayers[5], itl.getNextDayFajr(today)};

			SimpleDateFormat timeFormat = new SimpleDateFormat ("h:mm a");
			short nextNotificationTime = -1;
			for(short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) { // Set the times on the schedule
				if(i == CONSTANT.NEXT_FAJR) {
					VARIABLE.schedule[i] = new GregorianCalendar(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH), allTimes[i].getHour(), allTimes[i].getMinute(), allTimes[i].getSecond());
				} else {
					VARIABLE.schedule[i] = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), allTimes[i].getHour(), allTimes[i].getMinute(), allTimes[i].getSecond());	
				}
				String fullTime = timeFormat.format(VARIABLE.schedule[i].getTime());
				timetable.get(i).put("time", fullTime.substring(0, fullTime.lastIndexOf(" ")));
				timetable.get(i).put("time_am_pm", fullTime.substring(fullTime.lastIndexOf(" ") + 1, fullTime.length()) + (allTimes[i].isExtreme() ? "*" : ""));
				if(nextNotificationTime < 0 && (currentTime.compareTo(VARIABLE.schedule[i]) < 0 || i == CONSTANT.NEXT_FAJR)) {
					nextNotificationTime = i;
				}
			}
			indicateNotificationTimes(nextNotificationTime);
			timetableView.notifyDataSetChanged();

			// Add Latitude, Longitude and Qibla DMS location
			DecimalFormat df = new DecimalFormat("#.###");
			Dms latitude = new Dms(location.getDegreeLat());
			Dms longitude = new Dms(location.getDegreeLong());
			Dms qibla = itl.getNorthQibla();
			qiblaDirection = (float)qibla.getDecimalValue(net.sourceforge.jitl.astro.Direction.NORTH);
			((TextView)findViewById(R.id.current_latitude_deg)).setText(String.valueOf(latitude.getDegree()));
			((TextView)findViewById(R.id.current_latitude_min)).setText(String.valueOf(latitude.getMinute()));
			((TextView)findViewById(R.id.current_latitude_sec)).setText(df.format(latitude.getSecond()));
			((TextView)findViewById(R.id.current_longitude_deg)).setText(String.valueOf(longitude.getDegree()));
			((TextView)findViewById(R.id.current_longitude_min)).setText(String.valueOf(longitude.getMinute()));
			((TextView)findViewById(R.id.current_longitude_sec)).setText(df.format(longitude.getSecond()));
			((TextView)findViewById(R.id.current_qibla_deg)).setText(String.valueOf(qibla.getDegree()));
			((TextView)findViewById(R.id.current_qibla_min)).setText(String.valueOf(qibla.getMinute()));
			((TextView)findViewById(R.id.current_qibla_sec)).setText(df.format(qibla.getSecond()));

			setNextNotificationTime(nextNotificationTime);
		} catch(Exception ex) {
			java.io.StringWriter sw = new java.io.StringWriter();
			java.io.PrintWriter pw = new java.io.PrintWriter(sw, true);
			ex.printStackTrace(pw);
			pw.flush(); sw.flush();
			((TextView)findViewById(R.id.notes)).setText(sw.toString());
		}
	}

	private void setNextNotificationTime(short nextNotificationTime) {
		if(CONSTANT.DEBUG) ((TextView)findViewById(R.id.notes)).setText(((TextView)findViewById(R.id.notes)).getText() + ", Debug: " + Math.random());

		getIntent().removeExtra("nextNotificationTime");
		if(Calendar.getInstance().getTimeInMillis() > VARIABLE.schedule[nextNotificationTime].getTimeInMillis()) return; // Somehow current time is greater than the prayer time

		int notificationMethod = settings.getInt("notificationMethodIndex", CONSTANT.DEFAULT_NOTIFICATION);
		if(notificationMethod == CONSTANT.NO_NOTIFICATIONS) return;
		
		if(!alertSunrise() && nextNotificationTime == CONSTANT.SUNRISE) nextNotificationTime = CONSTANT.DHUHR;

		Intent intent = new Intent(this, WakeUpAndDoSomething.class);
		intent.putExtra("nextNotificationTime", nextNotificationTime);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, VARIABLE.schedule[nextNotificationTime].getTimeInMillis(), PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
	}
}