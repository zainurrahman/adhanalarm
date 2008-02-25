package islam.adhanalarm;

import android.app.Activity;
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
import android.view.View;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import libs.itl.PrayerTimes;

public class AdhanAlarm extends Activity {
	public static final String PREFS_NAME = "settingsFile";
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
        notification_methods.setSelection(settings.getInt("notificationMethodIndex", 0));

        Spinner extra_alerts = (Spinner)findViewById(R.id.extra_alerts);
        extra_alerts.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.extra_alerts, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        extra_alerts.setAdapter(adapter);
        extra_alerts.setSelection(settings.getInt("extraAlertsIndex", 0));

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

        tabs.setCurrentTab(0);

        Button playStop = (Button)findViewById(R.id.play_stop);
        playStop.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            		((Button)findViewById(R.id.play_stop)).setText(getString(R.string.preview));
            		mediaPlayer.stop();
            		//((Vibrator)getSystemService("vibrator")).cancel();
            	} else {
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            		int notificationMethodIndex = settings.getInt("notificationMethodIndex", 0);
            		if(notificationMethodIndex == 1 || notificationMethodIndex == 2) {
            			((Vibrator)getSystemService("vibrator")).vibrate(new long[]{0, 1000}, 15000);
            		}
            		if(notificationMethodIndex != 1) {
            			int alarm = R.raw.beep;
            			if(notificationMethodIndex == 3) {
            				alarm = R.raw.adhan;
            			}
                        mediaPlayer = MediaPlayer.create(AdhanAlarm.this, alarm);
                        try {
                    		((Button)findViewById(R.id.play_stop)).setText(getString(R.string.stop));
                        	mediaPlayer.start();
                    		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    			public void onCompletion(MediaPlayer mp) {
                    				((Button)findViewById(R.id.play_stop)).setText(getString(R.string.preview));
                    			}
                    		});
                        } catch(Exception ex) {
                        	((TextView)findViewById(R.id.notes)).setText(getString(R.string.error_playing_alert) + "2");
                        }
                	}
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
    
    private double getGMTOffset() {
        Calendar currentTime = new GregorianCalendar();
        TimeZone timeZone = currentTime.getTimeZone();
        int gmtOffset = timeZone.getOffset(currentTime.getTimeInMillis());
        return gmtOffset / 3600000;
    }
    
    private boolean isDaylightSavings() {
        Calendar currentTime = new GregorianCalendar();
        TimeZone timeZone = currentTime.getTimeZone();
        return timeZone.inDaylightTime(currentTime.getTime());
    }
    
    private void clearAllMarkers() {
        ((TextView)findViewById(R.id.mark_next_fajr)).setText("");
        ((TextView)findViewById(R.id.mark_dawn)).setText("");
        ((TextView)findViewById(R.id.mark_fajr)).setText("");
        ((TextView)findViewById(R.id.mark_sunrise)).setText("");
        ((TextView)findViewById(R.id.mark_dhuhr)).setText("");
        ((TextView)findViewById(R.id.mark_asr)).setText("");
        ((TextView)findViewById(R.id.mark_maghrib)).setText("");
        ((TextView)findViewById(R.id.mark_ishaa)).setText("");
        ((TextView)findViewById(R.id.mark_next_dawn)).setText("");
    }
    
    // The notification times for the following: Dawn, Fajr, Sunrise, Dhuhr, Asr, Maghrib, Ishaa, Next Dawn, Next Fajr
    // Note 1: User may not choose to notify for Dawn and Sunrise
    // Note 2: We never actually reach "Next Dawn".  Instead after Ishaa we set to wake up at midnight and then update schedule for next day which again starts at Dawn
    private GregorianCalendar[] currentNotificationTimes = new GregorianCalendar[9];
    
    private void updateScheduleAndNotification() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        PrayerTimes prayerTimes = new PrayerTimes();

        PrayerTimes.PrayerDate date= prayerTimes.new PrayerDate();
        Calendar currentTime = new GregorianCalendar();
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
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        currentNotificationTimes[0] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), dawn.getHour(), dawn.getMinute(), dawn.getSecond());
        ((TextView)findViewById(R.id.dawn)).setText(timeFormat.format(currentNotificationTimes[0].getTime()) + (dawn.getIsExtreme() == 1 ? "*" : ""));
        currentNotificationTimes[1] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[0].getHour(), ptList[0].getMinute(), ptList[0].getSecond());
        ((TextView)findViewById(R.id.fajr)).setText(timeFormat.format(currentNotificationTimes[1].getTime()) + (ptList[0].getIsExtreme() == 1 ? "*" : ""));
        currentNotificationTimes[2] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[1].getHour(), ptList[1].getMinute(),ptList[1] .getSecond());
        ((TextView)findViewById(R.id.sunrise)).setText(timeFormat.format(currentNotificationTimes[2].getTime()) + (ptList[1].getIsExtreme() == 1 ? "*" : ""));
        currentNotificationTimes[3] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[2].getHour(), ptList[2].getMinute(), ptList[2].getSecond());
        ((TextView)findViewById(R.id.dhuhr)).setText(timeFormat.format(currentNotificationTimes[3].getTime()) + (ptList[2].getIsExtreme() == 1 ? "*" : ""));
        currentNotificationTimes[4] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[3].getHour(), ptList[3].getMinute(), ptList[3].getSecond());
        ((TextView)findViewById(R.id.asr)).setText(timeFormat.format(currentNotificationTimes[4].getTime()) + (ptList[3].getIsExtreme() == 1 ? "*" : ""));
        currentNotificationTimes[5] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[4].getHour(), ptList[4].getMinute(), ptList[4].getSecond());
        ((TextView)findViewById(R.id.maghrib)).setText(timeFormat.format(currentNotificationTimes[5].getTime()) + (ptList[4].getIsExtreme() == 1 ? "*" : ""));
        currentNotificationTimes[6] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[5].getHour(), ptList[5].getMinute(), ptList[5].getSecond());
        ((TextView)findViewById(R.id.ishaa)).setText(timeFormat.format(currentNotificationTimes[6].getTime()) + (ptList[5].getIsExtreme() == 1 ? "*" : ""));
        currentNotificationTimes[7] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH) + 1, nextDawn.getHour(), nextDawn.getMinute(), nextDawn.getSecond());
        ((TextView)findViewById(R.id.next_dawn)).setText(timeFormat.format(currentNotificationTimes[7].getTime()) + (nextDawn.getIsExtreme() == 1 ? "*" : ""));
        currentNotificationTimes[8] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH) + 1, nextFajr.getHour(), nextFajr.getMinute(), nextFajr.getSecond());
        ((TextView)findViewById(R.id.next_fajr)).setText(timeFormat.format(currentNotificationTimes[8].getTime()) + (nextFajr.getIsExtreme() == 1 ? "*" : ""));

        // Set the marker indicating the next time (remove existing markers)
        clearAllMarkers();
        if(currentTime.compareTo(currentNotificationTimes[0]) < 0) {
            ((TextView)findViewById(R.id.mark_dawn)).setText(getString(R.string.next_prayer_marker));
            ((TextView)findViewById(R.id.notes)).setText(getString(R.string.next_alert) + ": " + getString(R.string.dawn));
        } else if(currentTime.compareTo(currentNotificationTimes[1]) < 0) {
            ((TextView)findViewById(R.id.mark_fajr)).setText(getString(R.string.next_prayer_marker));
            ((TextView)findViewById(R.id.notes)).setText(getString(R.string.next_alert) + ": " + getString(R.string.fajr));
        } else if(currentTime.compareTo(currentNotificationTimes[2]) < 0) {
            ((TextView)findViewById(R.id.mark_sunrise)).setText(getString(R.string.next_prayer_marker));
            ((TextView)findViewById(R.id.notes)).setText(getString(R.string.next_alert) + ": " + getString(R.string.sunrise));
        } else if(currentTime.compareTo(currentNotificationTimes[3]) < 0) {
            ((TextView)findViewById(R.id.mark_dhuhr)).setText(getString(R.string.next_prayer_marker));
            ((TextView)findViewById(R.id.notes)).setText(getString(R.string.next_alert) + ": " + getString(R.string.dhuhr));
        } else if(currentTime.compareTo(currentNotificationTimes[4]) < 0) {
            ((TextView)findViewById(R.id.mark_asr)).setText(getString(R.string.next_prayer_marker));
            ((TextView)findViewById(R.id.notes)).setText(getString(R.string.next_alert) + ": " + getString(R.string.asr));
        } else if(currentTime.compareTo(currentNotificationTimes[5]) < 0) {
            ((TextView)findViewById(R.id.mark_maghrib)).setText(getString(R.string.next_prayer_marker));
            ((TextView)findViewById(R.id.notes)).setText(getString(R.string.next_alert) + ": " + getString(R.string.maghrib));
        } else if(currentTime.compareTo(currentNotificationTimes[6]) < 0) {
            ((TextView)findViewById(R.id.mark_ishaa)).setText(getString(R.string.next_prayer_marker));
            ((TextView)findViewById(R.id.notes)).setText(getString(R.string.next_alert) + ": " + getString(R.string.ishaa));
        } else if(currentTime.compareTo(currentNotificationTimes[7]) < 0) {
            ((TextView)findViewById(R.id.mark_next_dawn)).setText(getString(R.string.next_prayer_marker));
            ((TextView)findViewById(R.id.notes)).setText(getString(R.string.next_alert) + ": " + getString(R.string.next_dawn));
        } else if(currentTime.compareTo(currentNotificationTimes[8]) < 0) {
            ((TextView)findViewById(R.id.mark_next_fajr)).setText(getString(R.string.next_prayer_marker));
            ((TextView)findViewById(R.id.notes)).setText(getString(R.string.next_alert) + ": " + getString(R.string.next_fajr));
        }
        
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

        // TODO: Cancel existing notification if exists
	    
        // TODO: Start new notification if appropriate
    }
}