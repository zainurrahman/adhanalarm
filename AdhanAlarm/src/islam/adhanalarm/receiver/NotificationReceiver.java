package islam.adhanalarm.receiver;

import java.util.Calendar;

import islam.adhanalarm.AdhanAlarm;
import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Schedule;
import islam.adhanalarm.VARIABLE;
import islam.adhanalarm.WakeLock;
import islam.adhanalarm.service.NotificationService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class NotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		WakeLock.acquire(VARIABLE.applicationContext);
		
		setNextNotificationTime(context, intent);
		
		if(VARIABLE.mainActivityIsRunning) {
			Intent i = new Intent(context, AdhanAlarm.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i); // Update the gui marker to show the next prayer
		}
		
		short timeIndex = intent.getShortExtra("timeIndex", (short)-1);
		long actualTime = intent.getLongExtra("actualTime", System.currentTimeMillis());
		intent.removeExtra("timeIndex");
		intent.removeExtra("actualTime");
		NotificationService.start(timeIndex, actualTime); // Notify the user for the current time
	}
	
	private static void setNextNotificationTime(Context context, Intent intent) {
		Schedule today = new Schedule();
		short nextTimeIndex = today.nextTimeIndex();
		setNotificationTime(context, intent, nextTimeIndex, today.getTodaysTimes()[nextTimeIndex]);
	}

	public static void setNotificationTime(Context context, Intent intent, short nextNotificationTime, Calendar actualTime) {
		if(Calendar.getInstance().after(actualTime)) return; // Somehow current time is greater than the prayer time
		
		int notificationMethod = VARIABLE.settings.getInt("notificationMethodIndex", CONSTANT.DEFAULT_NOTIFICATION);
		if(notificationMethod == CONSTANT.NO_NOTIFICATIONS) return;
		
		Intent i = new Intent(context, NotificationReceiver.class);
		i.putExtra("timeIndex", nextNotificationTime);
		i.putExtra("actualTime", actualTime.getTimeInMillis());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, actualTime.getTimeInMillis(), PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
	}
}