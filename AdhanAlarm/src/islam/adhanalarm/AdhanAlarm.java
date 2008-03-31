package islam.adhanalarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import libs.itl.PrayerTimes;

public class AdhanAlarm extends Activity {
	public static final String PREFS_NAME = "settingsFile";

	private static final short DAWN = 0, FAJR = 1, SUNRISE = 2, DHUHR = 3, ASR = 4, MAGHRIB = 5, ISHAA = 6, NEXT_DAWN = 7, NEXT_FAJR = 8; // Notification Times
	private static final short DISPLAY_ONLY = 0, VIBRATE = 1, BEEP = 2, BEEP_AND_VIBRATE = 3, RECITE_ADHAN = 4; // Notification Methods
	private static final short ALERT_PRAYERS_ONLY = 0, ALERT_DAWN = 1, ALERT_SUNRISE = 2, ALERT_DAWN_AND_SUNRISE = 3; // Extra Alerts
	
	private MediaPlayer mediaPlayer = null;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Spinner notification_methods = (Spinner)findViewById(R.id.notification_methods);
        notification_methods.setAllowWrap(true);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.notification_methods, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notification_methods.setAdapter(adapter);
        notification_methods.setSelection(settings.getInt("notificationMethodIndex", BEEP));

        Spinner extra_alerts = (Spinner)findViewById(R.id.extra_alerts);
        extra_alerts.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.extra_alerts, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        extra_alerts.setAdapter(adapter);
        extra_alerts.setSelection(settings.getInt("extraAlertsIndex", ALERT_PRAYERS_ONLY));

        // TODO: Make all edit texts single line and numeric from main.xml in next SDK
        ((EditText)findViewById(R.id.latitude)).setText(settings.getString("latitude", "51.477222"));
        ((EditText)findViewById(R.id.longitude)).setText(settings.getString("longitude", "0"));
        ((EditText)findViewById(R.id.altitude)).setText(Float.toString(settings.getFloat("altitude", 0)));
        
        if(settings.getString("latitude", "") != "" && settings.getString("longitude", "") != "") {
        	updateScheduleAndNotification();
        }

        Spinner calculation_methods = (Spinner)findViewById(R.id.calculation_methods);
        calculation_methods.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.calculation_methods, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calculation_methods.setAdapter(adapter);
        calculation_methods.setSelection(settings.getInt("calculationMethodsIndex", 0));

        Spinner rounding_types = (Spinner)findViewById(R.id.rounding_types);
        rounding_types.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.rounding_types, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       rounding_types.setAdapter(adapter);
       rounding_types.setSelection(settings.getInt("roundingTypesIndex", 1));

       ((EditText)findViewById(R.id.pressure)).setText(Float.toString(settings.getFloat("pressure", 1010)));
       ((EditText)findViewById(R.id.temperature)).setText(Float.toString(settings.getFloat("temperature", 10)));

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

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.content2);
        two.setIndicator(getString(R.string.alert), getResources().getDrawable(R.drawable.volume));
        tabs.addTab(two);

        TabHost.TabSpec three = tabs.newTabSpec("three");
        three.setContent(R.id.content3);
        three.setIndicator(getString(R.string.place), getResources().getDrawable(R.drawable.globe));
        tabs.addTab(three);

        TabHost.TabSpec four = tabs.newTabSpec("four");
        four.setContent(R.id.content4);
        four.setIndicator(getString(R.string.extra), getResources().getDrawable(R.drawable.calculator));
        tabs.addTab(four);

        Button playStop = (Button)findViewById(R.id.play_stop);
        playStop.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            		((Button)findViewById(R.id.play_stop)).setText(getString(R.string.preview_next_alarm));
            		mediaPlayer.stop();
            		//((Vibrator)getSystemService("vibrator")).cancel();
            	} else {
    				playAlertIfAppropriate(getNextNotificationTime());
            	}
            }
        });

        Button lookupGPS = (Button)findViewById(R.id.lookup_gps);
        lookupGPS.setOnClickListener(new Button.OnClickListener() {  
            public void onClick(View v) {
                EditText latitude = (EditText)findViewById(R.id.latitude);
                EditText longitude = (EditText)findViewById(R.id.longitude);

                LocationManager locationMananager = (LocationManager)getSystemService(LOCATION_SERVICE);
                 Location location = locationMananager.getCurrentLocation("gps");

                 if(location != null) {
                    latitude.setText(Double.toString(location.getLatitude()));
                    longitude.setText(Double.toString(location.getLongitude()));
                 } else {
                    latitude.setText("");
                    longitude.setText("");
                 }
            }
        });

        Button saveAndApplyAlert = (Button)findViewById(R.id.save_and_apply_alert);
        saveAndApplyAlert.setOnClickListener(new Button.OnClickListener() {  
            public void onClick(View v) {
                updateScheduleAndNotification();
                TabHost tabs = (TabHost)findViewById(R.id.tabs);
                tabs.setCurrentTab(0);
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("notificationMethodIndex", ((Spinner)findViewById(R.id.notification_methods)).getSelectedItemPosition());
                editor.putInt("extraAlertsIndex", ((Spinner)findViewById(R.id.extra_alerts)).getSelectedItemPosition());
                editor.commit();
            }
        });

        Button saveAndApplyPlace = (Button)findViewById(R.id.save_and_apply_place);
        saveAndApplyPlace.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                updateScheduleAndNotification();
                TabHost tabs = (TabHost)findViewById(R.id.tabs);
                tabs.setCurrentTab(0);
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("latitude", ((EditText)findViewById(R.id.latitude)).getText().toString());
                editor.putString("longitude", ((EditText)findViewById(R.id.longitude)).getText().toString());
                editor.putFloat("altitude", Float.parseFloat(((EditText)findViewById(R.id.altitude)).getText().toString()));
                editor.commit();
            }
        });

        Button resetExtra = (Button)findViewById(R.id.reset_extra);
        resetExtra.setOnClickListener(new Button.OnClickListener() {  
            public void onClick(View v) {
                ((Spinner)findViewById(R.id.calculation_methods)).setSelection(0);
                ((Spinner)findViewById(R.id.rounding_types)).setSelection(1);
                ((EditText)findViewById(R.id.pressure)).setText("1010.0");
                ((EditText)findViewById(R.id.temperature)).setText("10.0");
            }
        });

        Button saveAndApplyExtra = (Button)findViewById(R.id.save_and_apply_extra);
        saveAndApplyExtra.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                updateScheduleAndNotification();
                TabHost tabs = (TabHost)findViewById(R.id.tabs);
                tabs.setCurrentTab(0);
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("calculationMethodsIndex", ((Spinner)findViewById(R.id.calculation_methods)).getSelectedItemPosition());
                editor.putInt("roundingTypesIndex", ((Spinner)findViewById(R.id.rounding_types)).getSelectedItemPosition());
                editor.putFloat("pressure", Float.parseFloat(((EditText)findViewById(R.id.pressure)).getText().toString()));
                editor.putFloat("temperature", Float.parseFloat(((EditText)findViewById(R.id.temperature)).getText().toString()));
                editor.commit();
            }
        });
    }
    
    public void onResume() {
    	int notificationTime = getIntent().getIntExtra("islam.adhanalarm.nextNotificationTime", -1);
    	if(notificationTime > 0) {
            Toast.makeText(this, getString(R.string.time_for) + " " + getTimeName(notificationTime), Toast.LENGTH_LONG).show();
            playAlertIfAppropriate(notificationTime);
    	}
    	getIntent().removeExtra("islam.adhanalarm.nextNotificationTime");
    	updateScheduleAndNotification();
    	((TabHost)findViewById(R.id.tabs)).setCurrentTab(0);
    	super.onResume();
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
    
    private TextView[] getAllNotificationMarkers() {
    	return new TextView[]{(TextView)findViewById(R.id.mark_dawn), (TextView)findViewById(R.id.mark_fajr), (TextView)findViewById(R.id.mark_sunrise), (TextView)findViewById(R.id.mark_dhuhr), (TextView)findViewById(R.id.mark_asr), (TextView)findViewById(R.id.mark_maghrib), (TextView)findViewById(R.id.mark_ishaa), (TextView)findViewById(R.id.mark_next_dawn), (TextView)findViewById(R.id.mark_next_fajr)};
    }
    
    private String getTimeName(int time) {
    	int[] timeNames = new int[]{R.string.dawn, R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.maghrib, R.string.ishaa, R.string.next_dawn, R.string.next_fajr};
    	return getString(timeNames[time]);
    }
    
    private void playAlertIfAppropriate(int time) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		int notificationMethod = settings.getInt("notificationMethodIndex", BEEP);
    	if(notificationMethod == DISPLAY_ONLY || time < DAWN || time > NEXT_FAJR) return;
		if(notificationMethod == VIBRATE || notificationMethod == BEEP_AND_VIBRATE) {
			//((Vibrator)getSystemService("vibrator")).vibrate(new long[]{0, 1000}, 15000);
		}
		if(notificationMethod != VIBRATE) {
			int alarm = R.raw.beep;
			int extraAlerts = settings.getInt("extraAlertsIndex", ALERT_PRAYERS_ONLY);
			if(notificationMethod == RECITE_ADHAN) {
				if((time == DAWN || time == NEXT_DAWN) && (extraAlerts == ALERT_DAWN || extraAlerts == ALERT_DAWN_AND_SUNRISE)) {
					alarm = R.raw.beep;
				} else if(time == FAJR || time == NEXT_FAJR) {
					alarm = R.raw.adhan_fajr;
				} else if(time == SUNRISE && (extraAlerts == ALERT_SUNRISE || extraAlerts == ALERT_DAWN_AND_SUNRISE)) {
					alarm = R.raw.beep;
				} else if(time == DHUHR || time == ASR || time == MAGHRIB || time == ISHAA) {
					alarm = R.raw.adhan;
				} else if(extraAlerts == ALERT_PRAYERS_ONLY && (time == DAWN || time == NEXT_DAWN)) {
					alarm = R.raw.adhan_fajr;
				} else if(extraAlerts == ALERT_PRAYERS_ONLY && time == SUNRISE) {
					alarm = R.raw.adhan;
				}
			}
	        mediaPlayer = MediaPlayer.create(AdhanAlarm.this, alarm);
	        try {
	    		((Button)findViewById(R.id.play_stop)).setText(getString(R.string.stop));
	        	mediaPlayer.start();
	    		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
	    			public void onCompletion(MediaPlayer mp) {
	    				((Button)findViewById(R.id.play_stop)).setText(getString(R.string.preview_next_alarm));
	    			}
	    		});
	        } catch(Exception ex) {
	        	((TextView)findViewById(R.id.notes)).setText(getString(R.string.error_playing_alert));
	        }
    	}
    }
    
    private int getNextNotificationTime() {
    	TextView[] markers = getAllNotificationMarkers();
    	for(int i = DAWN; i < NEXT_FAJR; i++) {
    		if(markers[i].getText() == getString(R.string.next_time_marker)) return i;
    	}
    	return -1;
    }
    
    private void indicateNextNotificationAndAlarmTimes(int nextNotificationTime) {
    	TextView[] markers = getAllNotificationMarkers();
    	TextView note = (TextView)findViewById(R.id.notes);
    	// Clear all existing markers in case it was left from the previous day or while phone was turned off
    	for(int i = DAWN; i < NEXT_FAJR; i++) {
    		markers[i].setText("");
    	}
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		int extraAlerts = settings.getInt("extraAlertsIndex", ALERT_PRAYERS_ONLY);
        if(nextNotificationTime == DAWN) {
            markers[DAWN].setText(getString(R.string.next_time_marker));
            if(extraAlerts == ALERT_DAWN || extraAlerts == ALERT_DAWN_AND_SUNRISE) { // Only alert dawn if the user has specified
                note.setText(getString(R.string.next_alert) + ": " + getString(R.string.dawn));
            } else {
            	note.setText(getString(R.string.next_alert) + ": " + getString(R.string.fajr));
            }
        } else if(nextNotificationTime == FAJR) {
            markers[FAJR].setText(getString(R.string.next_time_marker));
            note.setText(getString(R.string.next_alert) + ": " + getString(R.string.fajr));
        } else if(nextNotificationTime == SUNRISE) {
        	markers[SUNRISE].setText(getString(R.string.next_time_marker));
            if(extraAlerts == ALERT_SUNRISE || extraAlerts == ALERT_DAWN_AND_SUNRISE) { // Only alert sunrise if the user has specified
            	note.setText(getString(R.string.next_alert) + ": " + getString(R.string.sunrise));
            } else {
                note.setText(getString(R.string.next_alert) + ": " + getString(R.string.dhuhr));
            }
        } else if(nextNotificationTime == DHUHR) {
        	markers[DHUHR].setText(getString(R.string.next_time_marker));
        	note.setText(getString(R.string.next_alert) + ": " + getString(R.string.dhuhr));
        } else if(nextNotificationTime == ASR) {
        	markers[ASR].setText(getString(R.string.next_time_marker));
        	note.setText(getString(R.string.next_alert) + ": " + getString(R.string.asr));
        } else if(nextNotificationTime == MAGHRIB) {
        	markers[MAGHRIB].setText(getString(R.string.next_time_marker));
        	note.setText(getString(R.string.next_alert) + ": " + getString(R.string.maghrib));
        } else if(nextNotificationTime == ISHAA) {
        	markers[ISHAA].setText(getString(R.string.next_time_marker));
        	note.setText(getString(R.string.next_alert) + ": " + getString(R.string.ishaa));
        } else if(nextNotificationTime == NEXT_DAWN) {
        	markers[NEXT_DAWN].setText(getString(R.string.next_time_marker));
            if(extraAlerts == ALERT_DAWN || extraAlerts == ALERT_DAWN_AND_SUNRISE) { // Only alert dawn if the user has specified
            	note.setText(getString(R.string.next_alert) + ": " + getString(R.string.next_dawn));
            } else {
            	note.setText(getString(R.string.next_alert) + ": " + getString(R.string.next_fajr));
            }
        } else if(nextNotificationTime == NEXT_FAJR) {
        	markers[NEXT_FAJR].setText(getString(R.string.next_time_marker));
        	note.setText(getString(R.string.next_alert) + ": " + getString(R.string.next_fajr));
        }
    }
    
    private void updateScheduleAndNotification() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        PrayerTimes prayerTimes = new PrayerTimes();

        PrayerTimes.PrayerDate date= prayerTimes.new PrayerDate();
        Calendar currentTime = Calendar.getInstance();
        date.setDay(currentTime.get(Calendar.DAY_OF_MONTH));
        date.setMonth(currentTime.get(Calendar.MONTH) + 1);
        date.setYear(currentTime.get(Calendar.YEAR));

        PrayerTimes.Location loc = prayerTimes.new Location();
        loc.setDegreeLat(Float.parseFloat(settings.getString("latitude", "51.477222"))); // default greenwich
        loc.setDegreeLong(Float.parseFloat(settings.getString("longitude", "0")));
        loc.setGmtDiff(getGMTOffset());
        loc.setDst(isDaylightSavings() ? 1 : 0);
        //loc.setGmtDiff(-5);
        //loc.setDst(0);
        loc.setSeaLevel(settings.getFloat("altitude", 0));
        loc.setPressure(settings.getFloat("pressure", 1010));
        loc.setTemperature(settings.getFloat("temperature", 10));

        PrayerTimes.Method conf = prayerTimes.new Method();
        conf.autoFillPrayerMethod(settings.getInt("calculationMethodsIndex", 0));
        conf.setRound(settings.getInt("roundingTypesIndex", 1));

        PrayerTimes.Prayer[] ptList = prayerTimes.getPrayerTimes(loc, conf, date);
        PrayerTimes.Prayer dawn = prayerTimes.getImsaak(loc, conf, date);
        PrayerTimes.Prayer nextDawn = prayerTimes.getNextDayImsaak(loc, conf, date);
        PrayerTimes.Prayer nextFajr = prayerTimes.getNextDayFajr(loc, conf, date);
        
        // Set the times on the schedule
        GregorianCalendar[] notificationTimes = new GregorianCalendar[9];
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        notificationTimes[DAWN] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), dawn.getHour(), dawn.getMinute(), dawn.getSecond());
        ((TextView)findViewById(R.id.dawn)).setText(timeFormat.format(notificationTimes[0].getTime()) + (dawn.getIsExtreme() == 1 ? "*" : ""));
        notificationTimes[FAJR] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[0].getHour(), ptList[0].getMinute(), ptList[0].getSecond());
        ((TextView)findViewById(R.id.fajr)).setText(timeFormat.format(notificationTimes[1].getTime()) + (ptList[0].getIsExtreme() == 1 ? "*" : ""));
        notificationTimes[SUNRISE] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[1].getHour(), ptList[1].getMinute(),ptList[1] .getSecond());
        ((TextView)findViewById(R.id.sunrise)).setText(timeFormat.format(notificationTimes[2].getTime()) + (ptList[1].getIsExtreme() == 1 ? "*" : ""));
        notificationTimes[DHUHR] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[2].getHour(), ptList[2].getMinute(), ptList[2].getSecond());
        ((TextView)findViewById(R.id.dhuhr)).setText(timeFormat.format(notificationTimes[3].getTime()) + (ptList[2].getIsExtreme() == 1 ? "*" : ""));
        notificationTimes[ASR] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[3].getHour(), ptList[3].getMinute(), ptList[3].getSecond());
        ((TextView)findViewById(R.id.asr)).setText(timeFormat.format(notificationTimes[4].getTime()) + (ptList[3].getIsExtreme() == 1 ? "*" : ""));
        notificationTimes[MAGHRIB] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[4].getHour(), ptList[4].getMinute(), ptList[4].getSecond());
        ((TextView)findViewById(R.id.maghrib)).setText(timeFormat.format(notificationTimes[5].getTime()) + (ptList[4].getIsExtreme() == 1 ? "*" : ""));
        notificationTimes[ISHAA] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[5].getHour(), ptList[5].getMinute(), ptList[5].getSecond());
        ((TextView)findViewById(R.id.ishaa)).setText(timeFormat.format(notificationTimes[6].getTime()) + (ptList[5].getIsExtreme() == 1 ? "*" : ""));
        notificationTimes[NEXT_DAWN] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH) + 1, nextDawn.getHour(), nextDawn.getMinute(), nextDawn.getSecond());
        ((TextView)findViewById(R.id.next_dawn)).setText(timeFormat.format(notificationTimes[7].getTime()) + (nextDawn.getIsExtreme() == 1 ? "*" : ""));
        notificationTimes[NEXT_FAJR] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH) + 1, nextFajr.getHour(), nextFajr.getMinute(), nextFajr.getSecond());
        ((TextView)findViewById(R.id.next_fajr)).setText(timeFormat.format(notificationTimes[8].getTime()) + (nextFajr.getIsExtreme() == 1 ? "*" : ""));

        int nextNotificationTime;
        for(nextNotificationTime = DAWN; nextNotificationTime < NEXT_FAJR; nextNotificationTime++) {
        	if(currentTime.compareTo(notificationTimes[nextNotificationTime]) < 0) break;
        }
        indicateNextNotificationAndAlarmTimes(nextNotificationTime);
        
        // Add Latitude, Longitude and Qibla DMS location to front panel
        PrayerTimes.DMS dms = prayerTimes.decimal2Dms(loc.getDegreeLat());
        double qibla = prayerTimes.getNorthQibla(loc);
	    String current_latitude = Math.abs(dms.getDegree()) + "° " + Math.abs(dms.getMinute())+ "' " + Math.abs(dms.getSecond()) + "\" " + ((loc.getDegreeLat() >= 0) ? getString(R.string.north) : getString(R.string.south));
	    ((TextView)findViewById(R.id.current_latitude)).setText(current_latitude);
	    dms = prayerTimes.decimal2Dms(loc.getDegreeLong());
	    String current_longitude = Math.abs(dms.getDegree()) + "° " + Math.abs(dms.getMinute()) + "' " + Math.abs(dms.getSecond()) + "\" " + ((loc.getDegreeLong() >= 0) ? getString(R.string.east) : getString(R.string.west));
	    ((TextView)findViewById(R.id.current_longitude)).setText(current_longitude);
	    dms = prayerTimes.decimal2Dms(qibla);
	    String current_qibla = Math.abs(dms.getDegree()) + "° " + Math.abs(dms.getMinute()) + "' " + Math.abs(dms.getSecond()) + "\" " + ((qibla >= 0) ? getString(R.string.west) : getString(R.string.east));
	    ((TextView)findViewById(R.id.current_qibla)).setText(current_qibla);

        // Cancel existing notification if it exists
	    Intent intent = new Intent(AdhanAlarm.this, WakeUpAndDoSomething.class);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(intent);

        // Schedule the alarm! (Pass the next notification time so we know which alarm to sound when nudged by WakeUpAndDoSomething)
        intent.putExtra("islam.adhanalarm.nextNotificationTime", nextNotificationTime);
        am.set(AlarmManager.RTC_WAKEUP, notificationTimes[nextNotificationTime].getTimeInMillis(), intent);
	    
        // TODO: The following is just for testing so I don't have to actually wait for the real time
        /*am.cancel(intent);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);
        intent.putExtra("islam.adhanalarm.nextNotificationTime", nextNotificationTime);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
        ((TextView)findViewById(R.id.notes)).setText("Alarm" + Math.random());*/
        // TODO: Remove the above test code when ready
    }
}