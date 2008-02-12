package adhanalarm.settings;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AdhanSettings extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        TextView tv = new TextView(this);
        tv.setText("Welcome to Adhan Alarm");
        setContentView(tv);
    }
}