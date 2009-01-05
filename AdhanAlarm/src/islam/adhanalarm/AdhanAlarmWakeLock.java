package islam.adhanalarm;

import android.content.Context;
import android.os.PowerManager;

public class AdhanAlarmWakeLock {

	private static PowerManager.WakeLock wakeLockIndefinite; // Must be released manually
	private static PowerManager.WakeLock wakeLockFinite; // Is acquired only for a finite amount of time, cannot be released manually
	
	static void acquire(Context context) {
		release();
		PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);

        wakeLockIndefinite = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Adhan Alarm Wake Lock Indefinite");
        wakeLockIndefinite.acquire();
	}
	
	static void release() {
        if(wakeLockIndefinite != null && wakeLockIndefinite.isHeld()) wakeLockIndefinite.release();
        wakeLockIndefinite = null;
	}
	
	static void acquireFinite(Context context, long time) {
		PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);

		wakeLockFinite = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Adhan Alarm Wake Lock Finite");
		wakeLockFinite.acquire(time);
	}
}