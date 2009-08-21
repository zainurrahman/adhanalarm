package islam.adhanalarm.dialog;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.R;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class NotificationSettingsDialog extends Dialog {
	
	private static SharedPreferences settings;
	
	public NotificationSettingsDialog(Context context) {
		super(context);
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.settings_notification);
		setTitle(R.string.notification);

		settings = getContext().getSharedPreferences("settingsFile", Context.MODE_PRIVATE);

		Spinner notification_methods = (Spinner)findViewById(R.id.notification_methods);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.notification_methods, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		notification_methods.setAdapter(adapter);
		notification_methods.setSelection(settings.getInt("notificationMethodIndex", CONSTANT.DEFAULT_NOTIFICATION));

		Spinner extra_alerts = (Spinner)findViewById(R.id.extra_alerts);
		adapter = ArrayAdapter.createFromResource(getContext(), R.array.extra_alerts, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		extra_alerts.setAdapter(adapter);
		extra_alerts.setSelection(settings.getInt("extraAlertsIndex", CONSTANT.NO_EXTRA_ALERTS));
		
		((Button)findViewById(R.id.save_and_apply_settings)).setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("extraAlertsIndex", ((Spinner)findViewById(R.id.extra_alerts)).getSelectedItemPosition());
				editor.putInt("notificationMethodIndex", ((Spinner)findViewById(R.id.notification_methods)).getSelectedItemPosition());
				editor.commit();
				dismiss();
			}
		});
		((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				((Spinner)findViewById(R.id.notification_methods)).setSelection(0);
				((Spinner)findViewById(R.id.extra_alerts)).setSelection(0);
			}
		});
	}
}