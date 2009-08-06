package islam.adhanalarm;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SetCalculationDialog extends Dialog {
	
	private static SharedPreferences settings;
	
	public SetCalculationDialog(Context context) {
		super(context);
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.settings_calculation);
		setTitle(R.string.calculation);

		settings = getContext().getSharedPreferences("settingsFile", Context.MODE_PRIVATE);

		Spinner calculation_methods = (Spinner)findViewById(R.id.calculation_methods);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.calculation_methods, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		calculation_methods.setAdapter(adapter);
		calculation_methods.setSelection(settings.getInt("calculationMethodsIndex", 4));

		Spinner rounding_types = (Spinner)findViewById(R.id.rounding_types);
		adapter = ArrayAdapter.createFromResource(getContext(), R.array.rounding_types, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		rounding_types.setAdapter(adapter);
		rounding_types.setSelection(settings.getInt("roundingTypesIndex", 2));
		
		((Button)findViewById(R.id.save_and_apply_settings)).setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("calculationMethodsIndex", ((Spinner)findViewById(R.id.calculation_methods)).getSelectedItemPosition());
				editor.putInt("roundingTypesIndex", ((Spinner)findViewById(R.id.rounding_types)).getSelectedItemPosition());
				editor.commit();
				dismiss();
			}
		});
		((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				((Spinner)findViewById(R.id.calculation_methods)).setSelection(4);
				((Spinner)findViewById(R.id.rounding_types)).setSelection(2);
			}
		});
	}
}