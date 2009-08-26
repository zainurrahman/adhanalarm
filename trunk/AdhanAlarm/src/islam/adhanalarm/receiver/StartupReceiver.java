package islam.adhanalarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		NotificationReceiver.setNextNotificationTime(context, intent);
	}
}