package islam.adhanalarm;

import android.content.Context;
import android.os.PowerManager;

public class AdhanAlarmWakeLock {

	private static PowerManager.WakeLock wakeLock;
	private static long wakeTime = 180000;
	
	static void setShortWakeTime(boolean shortWake) {
		wakeTime = shortWake ? 20000 : 180000; // 20 seconds is for a default notification, 3 minutes gives enough time for an adhan
	}
	
	static void acquire(Context context) {
		release();
		PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);

        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Adhan Alarm Wake Lock");
        wakeLock.acquire(wakeTime);
	}
	
	static void release() {
        if(wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        wakeLock = null;
	}
}