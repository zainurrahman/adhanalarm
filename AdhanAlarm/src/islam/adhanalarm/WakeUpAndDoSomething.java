package islam.adhanalarm;

import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;

public class WakeUpAndDoSomething extends IntentReceiver {
	@Override
	public void onReceiveIntent(Context context, Intent intent) {
        Intent i = new Intent(context, AdhanAlarm.class);
        // TODO: This should be removed in the next SDK
        i.putExtra("islam.adhanalarm.nextNotificationTime", intent.getIntExtra("islam.adhanalarm.nextNotificationTime", -1));
        // TODO: The following should work in the next SDK and would be better than above since it pushes parameters even when the application is open
        ////context.getApplication().getIntent().putExtra("islam.adhanalarm.nextNotificationTime", intent.getIntExtra("islam.adhanalarm.nextNotificationTime", -1));
        i.setLaunchFlags(Intent.NEW_TASK_LAUNCH);
        context.startActivity(i); // Simply calls the AdhanAlarm activity which may play an alarm in onResume
    }
}
