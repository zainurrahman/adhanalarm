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

        Spinner calculation_methods = (Spinner)findViewById(R.id.calculation_methods);
        calculation_methods.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.calculation_methods, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calculation_methods.setAdapter(adapter);

        Spinner minutes_offset = (Spinner)findViewById(R.id.minutes_offset);
        minutes_offset.setAllowWrap(true);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.minute_offsets, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minutes_offset.setAdapter(adapter);

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
        Calendar cal = new GregorianCalendar();
        date.setDay(cal.get(Calendar.DAY_OF_MONTH));
        date.setMonth(cal.get(Calendar.MONTH) + 1);
        date.setYear(cal.get(Calendar.YEAR));

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
        
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        currentNotificationTimes[0] = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), dawn.getHour(), dawn.getMinute());
        ((TextView)findViewById(R.id.dawn)).setText(timeFormat.format(currentNotificationTimes[0]));
        currentNotificationTimes[1] = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), ptList[0].getHour(), ptList[0].getMinute());
        ((TextView)findViewById(R.id.fajr)).setText(timeFormat.format(currentNotificationTimes[1]));
        currentNotificationTimes[2] = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), ptList[1].getHour(), ptList[1].getMinute());
        ((TextView)findViewById(R.id.sunrise)).setText(timeFormat.format(currentNotificationTimes[2]));
        currentNotificationTimes[3] = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), ptList[2].getHour(), ptList[2].getMinute());
        ((TextView)findViewById(R.id.dhuhr)).setText(timeFormat.format(currentNotificationTimes[3]));
        currentNotificationTimes[4] = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), ptList[3].getHour(), ptList[3].getMinute());
        ((TextView)findViewById(R.id.asr)).setText(timeFormat.format(currentNotificationTimes[4]));
        currentNotificationTimes[5] = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), ptList[4].getHour(), ptList[4].getMinute());
        ((TextView)findViewById(R.id.maghrib)).setText(timeFormat.format(currentNotificationTimes[5]));
        currentNotificationTimes[6] = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), ptList[5].getHour(), ptList[5].getMinute());
        ((TextView)findViewById(R.id.ishaa)).setText(timeFormat.format(currentNotificationTimes[6]));
        currentNotificationTimes[7] = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, nextDawn.getHour(), nextDawn.getMinute());
        ((TextView)findViewById(R.id.next_dawn)).setText(timeFormat.format(currentNotificationTimes[7]));
        currentNotificationTimes[8] = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, nextFajr.getHour(), nextFajr.getMinute());
        ((TextView)findViewById(R.id.next_fajr)).setText(timeFormat.format(currentNotificationTimes[8]));
        
        // TODO: Add Latitude, Longitude and Qibla DMS location to front tab
        PrayerTimes.DMS dms;
        
        // TODO: Start notification if appropriate
    }
}