package islam.adhanalarm.receiver;

import java.util.Calendar;

import islam.adhanalarm.AdhanAlarm;
import islam.adhanalarm.Notifier;
import islam.adhanalarm.Schedule;
import islam.adhanalarm.VARIABLE;
import islam.adhanalarm.WakeLock;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class StartNotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		WakeLock.acquire(context);
		
		short timeIndex = intent.getShortExtra("timeIndex", (short)-1);
		long actualTime = intent.getLongExtra("actualTime", (long)0);
		Notifier.start(context, timeIndex, actualTime); // Notify the user for the current time
		
		if(VARIABLE.mainActivityIsRunning) {
			Intent i = new Intent(context, AdhanAlarm.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			// Update the gui marker to show the next prayer, have to do this after starting Notifier or intent gets changed
			context.startActivity(i);
		}
		
		setNext(context);
	}
	
	public static void setNext(Context context) {
		Schedule today = new Schedule();
		short nextTimeIndex = today.nextTimeIndex();
		set(context, nextTimeIndex, today.getTodaysTimes()[nextTimeIndex]);
	}

	public static void set(Context context, short timeIndex, Calendar actualTime) {
		if(Calendar.getInstance().after(actualTime)) return; // Somehow current time is greater than the prayer time
		
		Intent intent = new Intent(context, StartNotificationReceiver.class);
		intent.putExtra("timeIndex", timeIndex);
		intent.putExtra("actualTime", actualTime.getTimeInMillis());
		
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, actualTime.getTimeInMillis(), PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_ONE_SHOT));
	}
}