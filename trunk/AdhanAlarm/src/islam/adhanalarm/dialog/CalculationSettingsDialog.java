package islam.adhanalarm.dialog;

import islam.adhanalarm.R;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class CalculationSettingsDialog extends Dialog {
	
	private static SharedPreferences settings;
	
	public CalculationSettingsDialog(Context context) {
		super(context);
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.settings_calculation);
		setTitle(R.string.calculation);

		settings = getContext().getSharedPreferences("settingsFile", Context.MODE_PRIVATE);

		((EditText)findViewById(R.id.latitude)).setText(Float.toString(settings.getFloat("latitude", 43.67f)));
		((EditText)findViewById(R.id.longitude)).setText(Float.toString(settings.getFloat("longitude", -79.417f)));

		Spinner calculation_methods = (Spinner)findViewById(R.id.calculation_methods);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.calculation_methods, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		calculation_methods.setAdapter(adapter);
		calculation_methods.setSelection(settings.getInt("calculationMethodsIndex", 4));

		((Button)findViewById(R.id.lookup_gps)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				criteria.setAltitudeRequired(false);
				criteria.setBearingRequired(false);
				criteria.setCostAllowed(true);
				criteria.setPowerRequirement(Criteria.POWER_LOW);
				
				LocationManager locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
				Location currentLocation = null;
				try {
					currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
				} catch(Exception ex) {
					// GPS or wireless networks is disabled
				}
				
				if(currentLocation != null) {
					((EditText)findViewById(R.id.latitude)).setText(Double.toString(currentLocation.getLatitude()));
					((EditText)findViewById(R.id.longitude)).setText(Double.toString(currentLocation.getLongitude()));
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
					// Invalid latitude
				}
				try {
					editor.putFloat("longitude", Float.parseFloat(((EditText)findViewById(R.id.longitude)).getText().toString()));
				} catch(Exception ex) {
					// Invalid longitude
				}
				editor.putInt("calculationMethodsIndex", ((Spinner)findViewById(R.id.calculation_methods)).getSelectedItemPosition());
				editor.commit();
				dismiss();
			}
		});
		((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				((Spinner)findViewById(R.id.calculation_methods)).setSelection(4);
			}
		});
	}
}