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
import android.view.View;

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
        		TabHost tabs = (TabHost)findViewById(R.id.tabs);
                tabs.setCurrentTab(0);
        	}
        });

        Button saveAndApplyLocation = (Button)findViewById(R.id.save_and_apply_location);
        saveAndApplyLocation.setOnClickListener(new Button.OnClickListener() {  
        	public void onClick(View v) {
        		TabHost tabs = (TabHost)findViewById(R.id.tabs);
                tabs.setCurrentTab(0);
        	}
        });

        Button resetAdvanced = (Button)findViewById(R.id.reset_advanced);
        resetAdvanced.setOnClickListener(new Button.OnClickListener() {  
        	public void onClick(View v) {
        	}
        });

        Button saveAndApplyAdvanced = (Button)findViewById(R.id.save_and_apply_advanced);
        saveAndApplyAdvanced.setOnClickListener(new Button.OnClickListener() {  
        	public void onClick(View v) {
        		TabHost tabs = (TabHost)findViewById(R.id.tabs);
                tabs.setCurrentTab(0);
        	}
        });
    }
}