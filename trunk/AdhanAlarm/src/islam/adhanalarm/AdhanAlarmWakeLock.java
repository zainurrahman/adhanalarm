package islam.adhanalarm;

import android.content.Context;
import android.os.PowerManager;

public class AdhanAlarmWakeLock {
	
	private static PowerManager.WakeLock wakeLock;
	
	static void acquire(Context context) {
		release();
		PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);

        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Adhan Alarm Wake Lock");
        wakeLock.acquire(180000); // Be awake for at least 3 minutes, gives the notification time to play the full adhan
	}
	
	static void release() {
        if(wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        wakeLock = null;
	}
}
