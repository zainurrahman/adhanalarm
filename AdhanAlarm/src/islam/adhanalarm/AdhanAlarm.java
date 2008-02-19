package islam.adhanalarm;

import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import libs.itl.PrayerTimes;

public class AdhanAlarm extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        Spinner notification_methods = (Spinner)findViewById(R.id.notification_methods);
        notification_methods.setAllowWrap(true);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.notification_methods, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notification_methods.setAdapter(adapter);

        Spinner minutes_offset = (Spinner)findViewById(R.id.minutes_offset);
        minutes_offset.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.minute_offsets, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minutes_offset.setAdapter(adapter);

        Spinner extra_alerts = (Spinner)findViewById(R.id.extra_alerts);
        extra_alerts.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.extra_alerts, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        extra_alerts.setAdapter(adapter);

        Spinner gmt_difference = (Spinner)findViewById(R.id.gmt_difference);
        gmt_difference.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.gmt_differences, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gmt_difference.setAdapter(adapter);

        Spinner calculation_methods = (Spinner)findViewById(R.id.calculation_methods);
        calculation_methods.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.calculation_methods, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calculation_methods.setAdapter(adapter);

        Spinner rounding_types = (Spinner)findViewById(R.id.rounding_types);
        rounding_types.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.rounding_types, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       rounding_types.setAdapter(adapter);

        TabHost tabs = (TabHost)findViewById(R.id.tabs);
        tabs.setup();

        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.content1);
        one.setIndicator(getString(R.string.schedule), getResources().getDrawable(R.drawable.calendar));
        tabs.addTab(one);

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.content2);
        two.setIndicator(getString(R.string.alert), getResources().getDrawable(R.drawable.volume));
        tabs.addTab(two);

        TabHost.TabSpec three = tabs.newTabSpec("three");
        three.setContent(R.id.content3);
        three.setIndicator(getString(R.string.location), getResources().getDrawable(R.drawable.globe));
        tabs.addTab(three);

        TabHost.TabSpec four = tabs.newTabSpec("four");
        four.setContent(R.id.content4);
        four.setIndicator(getString(R.string.advanced), getResources().getDrawable(R.drawable.calculator));
        tabs.addTab(four);

        tabs.setCurrentTab(0);

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
            }
        });

        Button saveAndApplyLocation = (Button)findViewById(R.id.save_and_apply_location);
        saveAndApplyLocation.setOnClickListener(new Button.OnClickListener() {  
            public void onClick(View v) {
                updateScheduleAndNotification();
                TabHost tabs = (TabHost)findViewById(R.id.tabs);
                tabs.setCurrentTab(0);
            }
        });

        Button resetAdvanced = (Button)findViewById(R.id.reset_advanced);
        resetAdvanced.setOnClickListener(new Button.OnClickListener() {  
            public void onClick(View v) {
                ((Spinner)findViewById(R.id.calculation_methods)).setSelection(0);
                ((Spinner)findViewById(R.id.rounding_types)).setSelection(1);
                ((EditText)findViewById(R.id.sea_level)).setText("0");
                ((EditText)findViewById(R.id.pressure)).setText("1010");
                ((EditText)findViewById(R.id.temperature)).setText("10");
            }
        });

        Button saveAndApplyAdvanced = (Button)findViewById(R.id.save_and_apply_advanced);
        saveAndApplyAdvanced.setOnClickListener(new Button.OnClickListener() {  
            public void onClick(View v) {
                updateScheduleAndNotification();
                TabHost tabs = (TabHost)findViewById(R.id.tabs);
                tabs.setCurrentTab(0);
            }
        });
    }
    
    // The notification times for the following: Dawn, Fajr, Sunrise, Dhuhr, Asr, Maghrib, Ishaa, Next Dawn, Next Fajr
    // Note 1: User may not choose to notify for Dawn and Sunrise
    // Note 2: We never actually reach "Next Dawn".  Instead after Ishaa we set to wake up at midnight and then update schedule for next day which again starts at Dawn
    private GregorianCalendar[] currentNotificationTimes = new GregorianCalendar[9];
    
    private void updateScheduleAndNotification() {
        
        // TODO: Cancel notification if exists

        PrayerTimes prayerTimes = new PrayerTimes();

        PrayerTimes.PrayerDate date= prayerTimes.new PrayerDate();
        Calendar currentTime = new GregorianCalendar();
        date.setDay(currentTime.get(Calendar.DAY_OF_MONTH));
        date.setMonth(currentTime.get(Calendar.MONTH) + 1);
        date.setYear(currentTime.get(Calendar.YEAR));

        // TODO: Set these values from stored data
        PrayerTimes.Location loc = prayerTimes.new Location();
        loc.setDegreeLat(25.25);
        loc.setDegreeLong(55.35);
        loc.setGmtDiff(4);
        loc.setDst(0);
        loc.setSeaLevel(0);
        loc.setPressure(1010);
        loc.setTemperature(10);

        PrayerTimes.Method conf = prayerTimes.new Method();
        conf.autoFillPrayerMethod(((Spinner)findViewById(R.id.calculation_methods)).getSelectedItemPosition());
        conf.setRound(0);

        PrayerTimes.Prayer[] ptList = prayerTimes.getPrayerTimes(loc, conf, date);
        PrayerTimes.Prayer dawn = prayerTimes.getImsaak(loc, conf, date);
        PrayerTimes.Prayer nextDawn = prayerTimes.getNextDayImsaak(loc, conf, date);
        PrayerTimes.Prayer nextFajr = prayerTimes.getNextDayFajr(loc, conf, date);
        
        // Set the times on the schedule
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        currentNotificationTimes[0] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), dawn.getHour(), dawn.getMinute(), dawn.getSecond());
        ((TextView)findViewById(R.id.dawn)).setText(timeFormat.format(currentNotificationTimes[0].getTime()));
        currentNotificationTimes[1] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[0].getHour(), ptList[0].getMinute(), ptList[0].getSecond());
        ((TextView)findViewById(R.id.fajr)).setText(timeFormat.format(currentNotificationTimes[1].getTime()));
        currentNotificationTimes[2] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[1].getHour(), ptList[1].getMinute(),ptList[1] .getSecond());
        ((TextView)findViewById(R.id.sunrise)).setText(timeFormat.format(currentNotificationTimes[2].getTime()));
        currentNotificationTimes[3] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[2].getHour(), ptList[2].getMinute(), ptList[2].getSecond());
        ((TextView)findViewById(R.id.dhuhr)).setText(timeFormat.format(currentNotificationTimes[3].getTime()));
        currentNotificationTimes[4] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[3].getHour(), ptList[3].getMinute(), ptList[3].getSecond());
        ((TextView)findViewById(R.id.asr)).setText(timeFormat.format(currentNotificationTimes[4].getTime()));
        currentNotificationTimes[5] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[4].getHour(), ptList[4].getMinute(), ptList[4].getSecond());
        ((TextView)findViewById(R.id.maghrib)).setText(timeFormat.format(currentNotificationTimes[5].getTime()));
        currentNotificationTimes[6] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH), ptList[5].getHour(), ptList[5].getMinute(), ptList[5].getSecond());
        ((TextView)findViewById(R.id.ishaa)).setText(timeFormat.format(currentNotificationTimes[6].getTime()));
        currentNotificationTimes[7] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH) + 1, nextDawn.getHour(), nextDawn.getMinute(), nextDawn.getSecond());
        ((TextView)findViewById(R.id.next_dawn)).setText(timeFormat.format(currentNotificationTimes[7].getTime()));
        currentNotificationTimes[8] = new GregorianCalendar(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH) + 1, nextFajr.getHour(), nextFajr.getMinute(), nextFajr.getSecond());
        ((TextView)findViewById(R.id.next_fajr)).setText(timeFormat.format(currentNotificationTimes[8].getTime()));
        
        // Set the marker indicating the next time (remove for previous time)
        if(currentTime.compareTo(currentNotificationTimes[0]) < 0) {
            ((TextView)findViewById(R.id.mark_next_fajr)).setText("");
            ((TextView)findViewById(R.id.mark_dawn)).setText("<");
        } else if(currentTime.compareTo(currentNotificationTimes[1]) < 0) {
            ((TextView)findViewById(R.id.mark_dawn)).setText("");
            ((TextView)findViewById(R.id.mark_fajr)).setText("<");
        } else if(currentTime.compareTo(currentNotificationTimes[2]) < 0) {
            ((TextView)findViewById(R.id.mark_fajr)).setText("");
            ((TextView)findViewById(R.id.mark_sunrise)).setText("<");
        } else if(currentTime.compareTo(currentNotificationTimes[3]) < 0) {
            ((TextView)findViewById(R.id.mark_sunrise)).setText("");
            ((TextView)findViewById(R.id.mark_dhuhr)).setText("<");
        } else if(currentTime.compareTo(currentNotificationTimes[4]) < 0) {
            ((TextView)findViewById(R.id.mark_dhuhr)).setText("");
            ((TextView)findViewById(R.id.mark_asr)).setText("<");
        } else if(currentTime.compareTo(currentNotificationTimes[5]) < 0) {
            ((TextView)findViewById(R.id.mark_asr)).setText("");
            ((TextView)findViewById(R.id.mark_maghrib)).setText("<");
        } else if(currentTime.compareTo(currentNotificationTimes[6]) < 0) {
            ((TextView)findViewById(R.id.mark_maghrib)).setText("");
            ((TextView)findViewById(R.id.mark_ishaa)).setText("<");
        } else if(currentTime.compareTo(currentNotificationTimes[7]) < 0) {
            ((TextView)findViewById(R.id.mark_ishaa)).setText("");
            ((TextView)findViewById(R.id.mark_next_dawn)).setText("<");
        } else if(currentTime.compareTo(currentNotificationTimes[8]) < 0) {
            ((TextView)findViewById(R.id.mark_next_dawn)).setText("");
            ((TextView)findViewById(R.id.mark_next_fajr)).setText("<");
        }
        
        // Add Latitude, Longitude and Qibla DMS location to front panel
        PrayerTimes.DMS dms = prayerTimes.decimal2Dms(loc.getDegreeLat());
        double qibla = prayerTimes.getNorthQibla(loc);
	    String current_latitude = Math.abs(dms.getDegree()) + " " + Math.abs(dms.getMinute())+ " " + Math.abs(dms.getSecond()) + " " + ((loc.getDegreeLat() >= 0) ? getString(R.string.north) : getString(R.string.south));
	    ((TextView)findViewById(R.id.current_latitude)).setText(current_latitude);
	    dms = prayerTimes.decimal2Dms(loc.getDegreeLong());
	    String current_longitude = Math.abs(dms.getDegree()) + " " + Math.abs(dms.getMinute()) + " " + Math.abs(dms.getSecond()) + " " + ((loc.getDegreeLong() >= 0) ? getString(R.string.east) : getString(R.string.west));
	    ((TextView)findViewById(R.id.current_longitude)).setText(current_longitude);
	    dms = prayerTimes.decimal2Dms(qibla);
	    String current_qibla = Math.abs(dms.getDegree()) + " " + Math.abs(dms.getMinute()) + " " + Math.abs(dms.getSecond()) + " " + ((qibla >= 0) ? getString(R.string.west) : getString(R.string.east));
	    ((TextView)findViewById(R.id.current_qibla)).setText(current_qibla);
        
        // TODO: Start notification if appropriate
    }
}