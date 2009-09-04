package islam.adhanalarm.service;

import islam.adhanalarm.AdhanAlarm;
import islam.adhanalarm.Notifier;
import islam.adhanalarm.VARIABLE;
import islam.adhanalarm.WakeLock;
import islam.adhanalarm.receiver.StartNotificationReceiver;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SetNextAndPossiblyNotifyCurrentService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		StartNotificationReceiver.setNext(this);

		if(VARIABLE.mainActivityIsRunning) {
			Intent i = new Intent(this, AdhanAlarm.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("setNotification", false); // Already did this at the top
			startActivity(i); // Update the gui marker to show the next prayer
		}

		short timeIndex = intent.getShortExtra("timeIndex", (short)-1);
		long actualTime = intent.getLongExtra("actualTime", (long)0);
		if(timeIndex == -1) {
			WakeLock.release(); // Got here from boot so no need to notify current			
		} else {
			Notifier.start(this, timeIndex, actualTime); // Notify the user for the current time, need to do this last since it releases the WakeLock
		}
	}

}