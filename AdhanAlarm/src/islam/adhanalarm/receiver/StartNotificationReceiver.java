package islam.adhanalarm.receiver;

import java.util.Calendar;

import islam.adhanalarm.Schedule;
import islam.adhanalarm.WakeLock;
import islam.adhanalarm.service.NotifyAndSetNextService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class StartNotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		WakeLock.acquire(context);
		Intent i = new Intent(context, NotifyAndSetNextService.class);
		i.putExtra("timeIndex", intent.getShortExtra("timeIndex", (short)-1));
		i.putExtra("actualTime", intent.getLongExtra("actualTime", 0));
		context.startService(i);
	}

	public static void setNext(Context context) {
		Schedule today = Schedule.today();
		short nextTimeIndex = today.nextTimeIndex();
		set(context, nextTimeIndex, today.getTimes()[nextTimeIndex]);
	}

	private static void set(Context context, short timeIndex, Calendar actualTime) {
		if(Calendar.getInstance().after(actualTime)) return; // Somehow current time is greater than the prayer time

		Intent i = new Intent(context, StartNotificationReceiver.class);
		i.putExtra("timeIndex", timeIndex);
		i.putExtra("actualTime", actualTime.getTimeInMillis());

		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, actualTime.getTimeInMillis(), PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT));
	}
}