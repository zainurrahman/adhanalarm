package islam.adhanalarm.service;

import islam.adhanalarm.AdhanAlarm;
import islam.adhanalarm.Notifier;
import islam.adhanalarm.VARIABLE;
import islam.adhanalarm.receiver.StartNotificationReceiver;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class NotifyAndSetNextService extends Service {

	public static Context context;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		short timeIndex = intent.getShortExtra("timeIndex", (short)-1);
		long actualTime = intent.getLongExtra("actualTime", (long)0);

		if(VARIABLE.mainActivityIsRunning) {
			Intent i = new Intent(context, AdhanAlarm.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			// Update the gui marker to show the next prayer, have to do this after starting Notifier or intent gets changed
			context.startActivity(i);
		}

		StartNotificationReceiver.setNext(context);
		
		Notifier.start(context, timeIndex, actualTime); // Notify the user for the current time
	}

}
