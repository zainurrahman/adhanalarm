package islam.adhanalarm;

import android.content.Context;
import android.os.PowerManager;

public class WakeLock {

	private static PowerManager.WakeLock wakeLock;
	
	public static void acquire(Context context) {
		release();
		PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);

        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Adhan Alarm Wake Lock");
        wakeLock.acquire();
	}
	
	public static void release() {
        if(wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        wakeLock = null;
	}
}