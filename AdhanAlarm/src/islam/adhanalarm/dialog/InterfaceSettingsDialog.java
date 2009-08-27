package islam.adhanalarm.dialog;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.R;
import islam.adhanalarm.VARIABLE;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class InterfaceSettingsDialog extends Dialog {
	
	public InterfaceSettingsDialog(Context context) {
		super(context);
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.settings_interface);
		setTitle(R.string.sinterface);

		Spinner themes = (Spinner)findViewById(R.id.themes);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.themes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		themes.setAdapter(adapter);
		themes.setSelection(VARIABLE.settings.getInt("themeIndex", CONSTANT.DEFAULT_THEME));

		Spinner languages = (Spinner)findViewById(R.id.languages);
		adapter = ArrayAdapter.createFromResource(getContext(), R.array.languages, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languages.setAdapter(adapter);
		languages.setSelection(CONSTANT.getLanguageIndex());
		
		((Button)findViewById(R.id.save_and_apply_settings)).setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences.Editor editor = VARIABLE.settings.edit();
				int oldThemeIndex = VARIABLE.settings.getInt("themeIndex", CONSTANT.DEFAULT_THEME);
				int newThemeIndex = ((Spinner)findViewById(R.id.themes)).getSelectedItemPosition();
				if(oldThemeIndex != newThemeIndex) {
					editor.putInt("themeIndex", newThemeIndex);
					VARIABLE.themeDirty = true;
				}
				int newLanguageIndex = ((Spinner)findViewById(R.id.languages)).getSelectedItemPosition();
				if(newLanguageIndex != CONSTANT.getLanguageIndex()) {
					editor.putString("locale", CONSTANT.LANGUAGE_KEYS[newLanguageIndex]);
					VARIABLE.languageDirty = true;
				}
				editor.commit();
				dismiss();
			}
		});
		((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {  
			public void onClick(View v) {
				((Spinner)findViewById(R.id.themes)).setSelection(CONSTANT.DEFAULT_THEME);
			}
		});
	}
}