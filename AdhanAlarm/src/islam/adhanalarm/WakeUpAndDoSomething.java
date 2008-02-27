package islam.adhanalarm;

import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;

public class WakeUpAndDoSomething extends IntentReceiver {
	@Override
	public void onReceiveIntent(Context context, Intent intent) {
        Intent i = new Intent(context, AdhanAlarm.class);
        i.setLaunchFlags(Intent.NEW_TASK_LAUNCH);
        context.startActivity(i); // Simply calls the AdhanAlarm activity which may play an alarm in onResume
    }
}
