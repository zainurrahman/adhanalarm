package islam.adhanalarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.res.Configuration;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sourceforge.jitl.astro.Dms;
import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.Method;
import net.sourceforge.jitl.Rounding;
import net.sourceforge.jitl.Prayer;

public class AdhanAlarm extends Activity {
	public static final boolean DEBUG = false;

	private static final short FAJR = 0, SUNRISE = 1, DHUHR = 2, ASR = 3, MAGHRIB = 4, ISHAA = 5, NEXT_FAJR = 6; // Notification Times
	private static final short DEFAULT_NOTIFICATION = 0, RECITE_ADHAN = 1, NO_NOTIFICATIONS = 2; // Notification Methods
	private static final short NO_EXTRA_ALERTS = 0, ALERT_SUNRISE = 1; // Extra Alerts

	private static final Method[] CALCULATION_METHODS = new Method[]{Method.EGYPT_SURVEY, Method.KARACHI_SHAF, Method.KARACHI_HANAF, Method.NORTH_AMERICA, Method.MUSLIM_LEAGUE, Method.UMM_ALQURRA, Method.FIXED_ISHAA};
	private static final Rounding[] ROUNDING_TYPES = new Rounding[]{Rounding.NONE, Rounding.NORMAL, Rounding.SPECIAL, Rounding.AGRESSIVE};

	private static TextView[] NOTIFICATION_MARKERS;
	private static TextView[] ALARM_TIMES;
	private static TextView[] ALARM_TIMES_AM_PM;
	private static String[] TIME_NAMES;
	
	private static SharedPreferences settings;
	private static MediaPlayer mediaPlayer;
    private static SensorListener orientationListener;
	private static GregorianCalendar[] notificationTimes = new GregorianCalendar[7];
	
	private static float qiblaDirection = 0;
	private static boolean isTrackingOrientation = false;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);

		NOTIFICATION_MARKERS = new TextView[]{(TextView)findViewById(R.id.mark_fajr), (TextView)findViewById(R.id.mark_sunrise), (TextView)findViewById(R.id.mark_dhuhr), (TextView)findViewById(R.id.mark_asr), (TextView)findViewById(R.id.mark_maghrib), (TextView)findViewById(R.id.mark_ishaa), (TextView)findViewById(R.id.mark_next_fajr)};
		ALARM_TIMES = new TextView[]{(TextView)findViewById(R.id.fajr), (TextView)findViewById(R.id.sunrise), (TextView)findViewById(R.id.dhuhr), (TextView)findViewById(R.id.asr), (TextView)findViewById(R.id.maghrib), (TextView)findViewById(R.id.ishaa), (TextView)findViewById(R.id.next_fajr)};
		ALARM_TIMES_AM_PM = new TextView[]{(TextView)findViewById(R.id.fajr_am_pm), (TextView)findViewById(R.id.sunrise_am_pm), (TextView)findViewById(R.id.dhuhr_am_pm), (TextView)findViewById(R.id.asr_am_pm), (TextView)findViewById(R.id.maghrib_am_pm), (TextView)findViewById(R.id.ishaa_am_pm), (TextView)findViewById(R.id.next_fajr_am_pm)};
		TIME_NAMES = new String[]{getString(R.string.fajr), getString(R.string.sunrise), getString(R.string.dhuhr), getString(R.string.asr), getString(R.string.maghrib), getString(R.string.ishaa), getString(R.string.next_fajr)};
		
		settings = getSharedPreferences("settingsFile", MODE_PRIVATE);

		((EditText)findViewById(R.id.latitude)).setText(Float.toString(settings.getFloat("latitude", 43.67f)));
		((EditText)findViewById(R.id.longitude)).setText(Float.toString(settings.getFloat("longitude", -79.417f)));

		Spinner notification_methods = (Spinner)findViewById(R.id.notification_methods);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.notification_methods, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		notification_methods.setAdapter(adapter);
		notification_methods.setSelection(settings.getInt("notificationMethodIndex", DEFAULT_NOTIFICATION));

		Spinner extra_alerts = (Spinner)findViewById(R.id.extra_alerts);
		adapter = ArrayAdapter.createFromResource(this, R.array.extra_alerts, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		extra_alerts.setAdapter(adapter);
		extra_alerts.setSelection(settings.getInt("extraAlertsIndex", NO_EXTRA_ALERTS));

		Spinner calculation_methods = (Spinner)findViewById(R.id.calculation_methods);
		adapter = ArrayAdapter.createFromResource(this, R.array.calculation_methods, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		calculation_methods.setAdapter(adapter);
		calculation_methods.setSelection(settings.getInt("calculationMethodsIndex", 4));

		Spinner rounding_types = (Spinner)findViewById(R.id.rounding_types);
		adapter = ArrayAdapter.createFromResource(this, R.array.rounding_types, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		rounding_types.setAdapter(adapter);
		rounding_types.setSelection(settings.getInt("roundingTypesIndex", 2));

		((EditText)findViewById(R.id.pressure)).setText(Float.toString(settings.getFloat("pressure", 1010)));
		((EditText)findViewById(R.id.temperature)).setText(Float.toString(settings.getFloat("temperature", 10)));
		((EditText)findViewById(R.id.altitude)).setText(Float.toString(settings.getFloat("altitude", 0)));

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

		((ImageButton)findViewById(R.id.previous)).setOnClickListener(new ImageButton.OnClickListener() {
			public void onClick(View v) {
				int time = getNextNotificationTime() - 1;
				if(time < FAJR) time = ISHAA;
				if(time == SUNRISE && !alertSunrise()) time = FAJR;
				playAlertIfAppropriate((short)time);
			}
		});
		((ImageButton)findViewById(R.id.next)).setOnClickListener(new ImageButton.OnClickListener() {
			public void onClick(View v) {
				playAlertIfAppropriate(getNextNotificationTime());
			}
		});
		((ImageButton)findViewById(R.id.clear)).setOnClickListener(new ImageButton.OnClickListener() {
			public void onClick(View v) {
				stopAlert();
				AdhanAlarmWakeLock.release();
			}
		}); /* End of Tab 1 Items */

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
		};
		tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				if(tabId == "two") {
					startTrackingOrientation();
				} else {
					stopTrackingOrientation();
				}
			}
		}); /* End of Tab 2 Items */
		startTrackingOrientation(); // Don't know why I need this but I get a null pointer when I switch to tab 2 otherwise
		
		TabHost.TabSpec three = tabs.newTabSpec("three");
		three.setContent(R.id.content3);
		three.setIndicator(getString(R.string.settings), getResources().getDrawable(R.drawable.calculator));
		tabs.addTab(three);

		((Button)findViewById(R.id.lookup_gps)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				criteria.setAltitudeRequired(false);
				criteria.setBearingRequired(false);
				criteria.setCostAllowed(true);
				criteria.setPowerRequirement(Criteria.POWER_LOW);
				
				LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
				Location currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
				
				if(currentLocation != null) {
					((EditText)findViewById(R.id.latitude)).setText(Double.toString(currentLocation.getLatitude()));
					((EditText)findViewById(R.id.longitude)).setText(Double.toString(currentLocation.getLongitude()));
					((EditText)findViewById(R.id.altitude)).setText(Double.toString(currentLocation.getAltitude()));
				} else {
					((EditText)findViewById(R.id.latitude)).setText("");
					((EditText)findViewById(R.id.longitude)).setText("");
				}
			}
		});
		((Button)findViewById(R.id.save_and_apply_settings)).setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences.Editor editor = settings.edit();
				try {
					editor.putFloat("latitude", Float.parseFloat(((EditText)findViewById(R.id.latitude)).getText().toString()));
				} catch(Exception ex) {
					editor.putFloat("latitude", 43.67f);
					((EditText)findViewById(R.id.latitude)).setText("43.67");
				}
				try {
					editor.putFloat("longitude", Float.parseFloat(((EditText)findViewById(R.id.longitude)).getText().toString()));
				} catch(Exception ex) {
					editor.putFloat("longitude", -79.417f);
					((EditText)findViewById(R.id.longitude)).setText("-79.417");
				}
				try {
					editor.putFloat("altitude", Float.parseFloat(((EditText)findViewById(R.id.altitude)).getText().toString()));
				} catch(Exception ex) {
					editor.putFloat("altitude", 0);
					((EditText)findViewById(R.id.pressure)).setText("0.0");
				}
				editor.putInt("calculationMethodsIndex", ((Spinner)findViewById(R.id.calculation_methods)).getSelectedItemPosition());
				try {
					editor.putFloat("pressure", Float.parseFloat(((EditText)findViewById(R.id.pressure)).getText().toString()));
				} catch(Exception ex) {
					editor.putFloat("pressure", 1010);
					((EditText)findViewById(R.id.pressure)).setText("1010.0");
				}
				try {
					editor.putFloat("temperature", Float.parseFloat(((EditText)findViewById(R.id.temperature)).getText().toString()));
				} catch(Exception ex) {
					editor.putFloat("temperature", 10);
					((EditText)findViewById(R.id.pressure)).setText("10.0");
				}
				editor.putInt("roundingTypesIndex", ((Spinner)findViewById(R.id.rounding_types)).getSelectedItemPosition());
				editor.putInt("notificationMethodIndex", ((Spinner)findViewById(R.id.notification_methods)).getSelectedItemPosition());
				editor.putInt("extraAlertsIndex", ((Spinner)findViewById(R.id.extra_alerts)).getSelectedItemPosition());
				editor.commit();
				updateScheduleAndNotification();
				((TabHost)findViewById(R.id.tabs)).setCurrentTab(0);
			}
		});
		((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				((Spinner)findViewById(R.id.notification_methods)).setSelection(0);
				((Spinner)findViewById(R.id.extra_alerts)).setSelection(0);
				((Spinner)findViewById(R.id.calculation_methods)).setSelection(4);
				((Spinner)findViewById(R.id.rounding_types)).setSelection(2);
				((EditText)findViewById(R.id.pressure)).setText("1010.0");
				((EditText)findViewById(R.id.temperature)).setText("10.0");
				((EditText)findViewById(R.id.altitude)).setText("0.0");
			}
		}); /* End of Tab 3 Items */
		showHideCompass();
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
		switch(item.getItemId()) {
		case R.id.menu_help:
			dialogBuilder.setMessage(R.string.help_text);
			break;
		case R.id.menu_information:
			dialogBuilder.setMessage(R.string.information_text);
			break;
		}
		dialogBuilder.setCancelable(true);
		dialogBuilder.create().show();
		return true;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		showHideCompass();
	}
	
	private void showHideCompass() {
		Configuration config = getResources().getConfiguration();
		int visiblity = config.keyboardHidden == Configuration.KEYBOARDHIDDEN_UNDEFINED || config.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES ? View.VISIBLE : View.INVISIBLE;
		((View)findViewById(R.id.qibla_compass)).setVisibility(visiblity);
		((TextView)findViewById(R.id.compass_disclaimer)).setText(visiblity == View.VISIBLE ? "" : getText(R.string.compass_disclaimer));
	}
	
	private void startTrackingOrientation() {
		if(!isTrackingOrientation) isTrackingOrientation = ((SensorManager)getSystemService(SENSOR_SERVICE)).registerListener(orientationListener, android.hardware.SensorManager.SENSOR_ORIENTATION);
	}
	private void stopTrackingOrientation() {
		if(isTrackingOrientation) ((SensorManager)getSystemService(SENSOR_SERVICE)).unregisterListener(orientationListener);
		isTrackingOrientation = false;
	}

	public void onStop() {
		if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
		stopTrackingOrientation();
		AdhanAlarmWakeLock.release();
		super.onStop();
	}

	public void onResume() {
		short notificationTime = getIntent().getShortExtra("nextNotificationTime", (short)-1);
		if(notificationTime >= FAJR && notificationTime <= NEXT_FAJR) playAlertIfAppropriate(notificationTime);
		updateScheduleAndNotification();
		((TabHost)findViewById(R.id.tabs)).setCurrentTab(0);
		AdhanAlarmWakeLock.release();
		super.onResume();
	}

	public void onNewIntent(Intent intent) {
		setIntent(intent);
		super.onNewIntent(intent);
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

	private float getDSTSavings() {
		return isDaylightSavings() ? new GregorianCalendar().getTimeZone().getDSTSavings() / 3600000 : 0;
	}
	
	private void stopAlert() {
		if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
	}
	
	private boolean alertSunrise() {
		return settings.getInt("extraAlertsIndex", NO_EXTRA_ALERTS) == ALERT_SUNRISE;
	}

	private void playAlertIfAppropriate(short time) {
		stopAlert();
		long timestamp = notificationTimes[time] != null ? notificationTimes[time].getTimeInMillis() : System.currentTimeMillis();
		String notificationTitle = (time != SUNRISE ? getString(R.string.allahu_akbar) + ": " : "") + getString(R.string.time_for) + " " + (time == NEXT_FAJR ? TIME_NAMES[FAJR] : TIME_NAMES[time]).toLowerCase();
		Notification notification = new Notification(R.drawable.icon, notificationTitle, timestamp);

		int notificationMethod = settings.getInt("notificationMethodIndex", DEFAULT_NOTIFICATION);
		int ringerMode = ((AudioManager)getSystemService(AUDIO_SERVICE)).getRingerMode();
		if(notificationMethod == RECITE_ADHAN && ringerMode != AudioManager.RINGER_MODE_SILENT && ringerMode != AudioManager.RINGER_MODE_VIBRATE) {
			int alarm = R.raw.beep;
			if(time == DHUHR || time == ASR || time == MAGHRIB || time == ISHAA || (time == SUNRISE && !alertSunrise())) {
				alarm = R.raw.adhan;
			} else if(time == FAJR || time == NEXT_FAJR) {
				alarm = R.raw.adhan_fajr;
			}
			mediaPlayer = MediaPlayer.create(AdhanAlarm.this, alarm);
			mediaPlayer.setScreenOnWhilePlaying(true);
			try {
				mediaPlayer.start();
			} catch(Exception ex) {
				((TextView)findViewById(R.id.notes)).setText(getString(R.string.error_playing_alert));
			}
			notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
		} else {
			notification.defaults = Notification.DEFAULT_ALL;
		}
		notification.setLatestEventInfo(this, getString(R.string.app_name), notificationTitle, PendingIntent.getActivity(this, 0, new Intent(this, AdhanAlarm.class), 0));
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1, notification);
	}

	private short getNextNotificationTime() {
		for(short i = FAJR; i <= NEXT_FAJR; i++) {
			if(NOTIFICATION_MARKERS[i].getText() == getString(R.string.next_time_marker)) return i;
		}
		return -1;
	}

	private void indicateNotificationTimes(short nextNotificationTime) {
		for(short i = FAJR; i <= NEXT_FAJR; i++) NOTIFICATION_MARKERS[i].setText(""); // Clear all existing markers in case it was left from the previous day or while phone was turned off

		int previousNotificationTime = nextNotificationTime - 1 < FAJR ? ISHAA : nextNotificationTime - 1;
		
		if(!alertSunrise() && nextNotificationTime == SUNRISE) nextNotificationTime = DHUHR;
		if(!alertSunrise() && previousNotificationTime == SUNRISE) previousNotificationTime = FAJR;

		NOTIFICATION_MARKERS[nextNotificationTime].setText(getString(R.string.next_time_marker));
		
		((TextView)findViewById(R.id.notes)).setText(getString(R.string.last_alert) + ": " + TIME_NAMES[previousNotificationTime] + "\n" + getString(R.string.next_alert) + ": " + TIME_NAMES[nextNotificationTime]);
	}

	private void updateScheduleAndNotification() {
		try {
			Method method = CALCULATION_METHODS[settings.getInt("calculationMethodsIndex", 4)].copy();
			method.setRound(ROUNDING_TYPES[settings.getInt("roundingTypesIndex", 2)]);

			net.sourceforge.jitl.astro.Location location = new net.sourceforge.jitl.astro.Location(settings.getFloat("latitude", 43.67f), settings.getFloat("longitude", -79.417f), getGMTOffset(), (int)getDSTSavings());
			location.setSeaLevel(settings.getFloat("altitude", 0));
			location.setPressure(settings.getFloat("pressure", 1010));
			location.setTemperature(settings.getFloat("temperature", 10));

			Jitl itl = DEBUG ? new DummyJitl(location, method) : new Jitl(location, method);
			Calendar currentTime = Calendar.getInstance();
			GregorianCalendar today = new GregorianCalendar();
			GregorianCalendar tomorrow = new GregorianCalendar();
			tomorrow.add(Calendar.DATE, 1);
			Prayer[] dayPrayers = itl.getPrayerTimes(today).getPrayers();
			Prayer[] allTimes = new Prayer[]{dayPrayers[0], dayPrayers[1], dayPrayers[2], dayPrayers[3], dayPrayers[4], dayPrayers[5], itl.getNextDayFajr(today)};

			DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
			short nextNotificationTime = -1;
			for(short i = FAJR; i <= NEXT_FAJR; i++) { // Set the times on the schedule
				if(i == NEXT_FAJR) {
					notificationTimes[i] = new GregorianCalendar(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH), allTimes[i].getHour(), allTimes[i].getMinute(), allTimes[i].getSecond());
				} else {
					notificationTimes[i] = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), allTimes[i].getHour(), allTimes[i].getMinute(), allTimes[i].getSecond());	
				}
				String fullTime = timeFormat.format(notificationTimes[i].getTime());
				ALARM_TIMES[i].setText(fullTime.substring(0, fullTime.lastIndexOf(" ")));
				ALARM_TIMES_AM_PM[i].setText(fullTime.substring(fullTime.lastIndexOf(" ") + 1, fullTime.length()) + (allTimes[i].isExtreme() ? "*" : ""));
				if(nextNotificationTime < 0 && (currentTime.compareTo(notificationTimes[i]) < 0 || i == NEXT_FAJR)) {
					nextNotificationTime = i;
				}
			}
			indicateNotificationTimes(nextNotificationTime);

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
		if(DEBUG) ((TextView)findViewById(R.id.notes)).setText(((TextView)findViewById(R.id.notes)).getText() + ", Debug: " + Math.random());

		getIntent().removeExtra("nextNotificationTime");
		if(Calendar.getInstance().getTimeInMillis() > notificationTimes[nextNotificationTime].getTimeInMillis()) return; // Somehow current time is greater than the prayer time

		int notificationMethod = settings.getInt("notificationMethodIndex", DEFAULT_NOTIFICATION);
		if(notificationMethod == NO_NOTIFICATIONS) return;
		
		if(!alertSunrise() && nextNotificationTime == SUNRISE) nextNotificationTime = DHUHR;

		Intent intent = new Intent(this, WakeUpAndDoSomething.class);
		intent.putExtra("nextNotificationTime", nextNotificationTime);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, notificationTimes[nextNotificationTime].getTimeInMillis(), PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
	}
}