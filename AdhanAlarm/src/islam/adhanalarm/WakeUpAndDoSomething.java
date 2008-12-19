package islam.adhanalarm;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class WakeUpAndDoSomething extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, AdhanAlarm.class);
		i.putExtra("nextNotificationTime", intent.getShortExtra("nextNotificationTime", (short)-1));
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i); // Simply calls the AdhanAlarm activity which may play an alarm in onResume
	}
}