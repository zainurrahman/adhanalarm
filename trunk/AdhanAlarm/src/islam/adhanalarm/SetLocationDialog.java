package islam.adhanalarm;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetLocationDialog extends Dialog {
	
	private static SharedPreferences settings;
	
	public SetLocationDialog(Context context) {
		super(context);
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.settings_location);
		setTitle(R.string.location);
		
		settings = getContext().getSharedPreferences("settingsFile", Context.MODE_PRIVATE);

		((EditText)findViewById(R.id.latitude)).setText(Float.toString(settings.getFloat("latitude", 43.67f)));
		((EditText)findViewById(R.id.longitude)).setText(Float.toString(settings.getFloat("longitude", -79.417f)));

		((EditText)findViewById(R.id.pressure)).setText(Float.toString(settings.getFloat("pressure", 1010)));
		((EditText)findViewById(R.id.temperature)).setText(Float.toString(settings.getFloat("temperature", 10)));
		((EditText)findViewById(R.id.altitude)).setText(Float.toString(settings.getFloat("altitude", 0)));

		((Button)findViewById(R.id.lookup_gps)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				criteria.setAltitudeRequired(false);
				criteria.setBearingRequired(false);
				criteria.setCostAllowed(true);
				criteria.setPowerRequirement(Criteria.POWER_LOW);
				
				LocationManager locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
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
				editor.commit();
				dismiss();
			}
		});
		((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				((EditText)findViewById(R.id.pressure)).setText("1010.0");
				((EditText)findViewById(R.id.temperature)).setText("10.0");
				((EditText)findViewById(R.id.altitude)).setText("0.0");
			}
		});
	}
}