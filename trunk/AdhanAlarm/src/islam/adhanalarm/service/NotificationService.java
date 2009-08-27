package islam.adhanalarm.service;

import islam.adhanalarm.AdhanAlarm;
import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.R;
import islam.adhanalarm.VARIABLE;
import islam.adhanalarm.WakeLock;
import islam.adhanalarm.receiver.ClickNotificationReceiver;
import islam.adhanalarm.receiver.ClearNotificationReceiver;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class NotificationService extends Service {
	
	private static Intent notifier;
	private static MediaPlayer mediaPlayer;
	private static Context context;

	public IBinder onBind(Intent i) {
		return null;
	}
	
	@Override
    public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		stopNotification();
		
		short timeIndex = intent.getShortExtra("timeIndex", (short)-1);
		long actualTime = intent.getLongExtra("actualTime", System.currentTimeMillis());
		String notificationTitle = (timeIndex != CONSTANT.SUNRISE ? getString(R.string.allahu_akbar) + ": " : "") + getString(R.string.time_for) + " " + (timeIndex == CONSTANT.NEXT_FAJR ? VARIABLE.TIME_NAMES[CONSTANT.FAJR] : VARIABLE.TIME_NAMES[timeIndex]).toLowerCase();
		Notification notification = new Notification(R.drawable.icon, notificationTitle, actualTime);
		
		int notificationMethod = VARIABLE.settings.getInt("notificationMethodIndex", CONSTANT.DEFAULT_NOTIFICATION);
		if(notificationMethod == CONSTANT.NO_NOTIFICATIONS || (timeIndex == CONSTANT.SUNRISE && !VARIABLE.alertSunrise())) return;
		
		int ringerMode = ((AudioManager)getSystemService(AUDIO_SERVICE)).getRingerMode();
		int callState = ((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).getCallState();
		if(notificationMethod == CONSTANT.RECITE_ADHAN && ringerMode != AudioManager.RINGER_MODE_SILENT && ringerMode != AudioManager.RINGER_MODE_VIBRATE && callState == TelephonyManager.CALL_STATE_IDLE) {
			notificationTitle += " (" + getString(R.string.stop) + ")";
			int alarm = R.raw.beep;
			if(timeIndex == CONSTANT.DHUHR || timeIndex == CONSTANT.ASR || timeIndex == CONSTANT.MAGHRIB || timeIndex == CONSTANT.ISHAA) {
				alarm = R.raw.adhan;
			} else if(timeIndex == CONSTANT.FAJR || timeIndex == CONSTANT.NEXT_FAJR) {
				alarm = R.raw.adhan_fajr;
			}
			mediaPlayer = MediaPlayer.create(NotificationService.this, alarm);
			mediaPlayer.setScreenOnWhilePlaying(true);
			try {
				mediaPlayer.start();
			} catch(Exception ex) {
				notificationTitle += " - " + getString(R.string.error_playing_alert);
			}
			notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
		} else {
			notification.defaults = Notification.DEFAULT_ALL;
		}
		Intent i = new Intent(this, AdhanAlarm.class);
		notification.setLatestEventInfo(this, getString(R.string.app_name), notificationTitle, PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
		notification.contentIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, ClickNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
		notification.deleteIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, ClearNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1, notification);
		try {
			Thread.sleep(CONSTANT.POST_NOTIFICATION_DELAY);
		} catch(Exception ex) {
			// We're just trying to buy a little bit of time to make sure the notification completes.
		}
		WakeLock.release();
	}
	@Override
	public void onDestroy() {
		stopNotification();
		super.onDestroy();
	}
	
	private void stopNotification() {
		if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
	}

	/** We use this class in a static way by using the following start and stop functions **/
	
	public static void start(Context context, short timeIndex, long actualTime) {
		NotificationService.context = context;
		notifier = new Intent(context, NotificationService.class);
		notifier.putExtra("timeIndex", timeIndex);
		notifier.putExtra("actualTime", actualTime);
		notifier.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(notifier);
	}
	public static void start(Context context, Intent intent) {
		short timeIndex = intent.getShortExtra("timeIndex", (short)-1);
		long actualTime = intent.getLongExtra("actualTime", System.currentTimeMillis());
		start(context, timeIndex, actualTime);
	}
	public static void stop() {
		if(notifier != null) context.stopService(notifier);
		notifier = null;
	}
}