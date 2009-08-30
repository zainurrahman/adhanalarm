package islam.adhanalarm;

import islam.adhanalarm.receiver.ClickNotificationReceiver;
import islam.adhanalarm.receiver.ClearNotificationReceiver;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.telephony.TelephonyManager;

public class Notifier {
	
	private static MediaPlayer mediaPlayer;
	private static Context context;
	private static Notification notification;
	
	public static void start(Context context, short timeIndex, long actualTime) {
		Notifier.context = context;
		stopNotification();

		notification = new Notification(R.drawable.icon, "", actualTime);
		notification.tickerText = (timeIndex != CONSTANT.SUNRISE ? context.getString(R.string.allahu_akbar) + ": " : "") + context.getString(R.string.time_for) + " " + (timeIndex == CONSTANT.NEXT_FAJR ? context.getString(CONSTANT.TIME_NAMES[CONSTANT.FAJR]) : context.getString(CONSTANT.TIME_NAMES[timeIndex])).toLowerCase();

		int notificationMethod = VARIABLE.settings.getInt("notificationMethodIndex", CONSTANT.DEFAULT_NOTIFICATION);
		if(notificationMethod == CONSTANT.NO_NOTIFICATIONS || (timeIndex == CONSTANT.SUNRISE && !VARIABLE.alertSunrise())) return;
		
		int ringerMode = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
		int callState = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
		if(notificationMethod == CONSTANT.RECITE_ADHAN && ringerMode != AudioManager.RINGER_MODE_SILENT && ringerMode != AudioManager.RINGER_MODE_VIBRATE && callState == TelephonyManager.CALL_STATE_IDLE) {
			notification.tickerText = notification.tickerText + " (" + context.getString(R.string.stop) + ")";
			int alarm = R.raw.beep;
			if(timeIndex == CONSTANT.DHUHR || timeIndex == CONSTANT.ASR || timeIndex == CONSTANT.MAGHRIB || timeIndex == CONSTANT.ISHAA) {
				alarm = R.raw.adhan;
			} else if(timeIndex == CONSTANT.FAJR || timeIndex == CONSTANT.NEXT_FAJR) {
				alarm = R.raw.adhan_fajr;
			}
			mediaPlayer = MediaPlayer.create(context, alarm);
			mediaPlayer.setScreenOnWhilePlaying(true);
			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					notification.tickerText = notification.tickerText.toString().replace(" (" + Notifier.context.getString(R.string.stop) + ")", "");
					notification.defaults = 0;
					startNotification();
				}
			});
			try {
				mediaPlayer.start();
			} catch(Exception ex) {
				notification.tickerText = notification.tickerText + " - " + context.getString(R.string.error_playing_alert);
			}
			notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
		} else {
			notification.defaults = Notification.DEFAULT_ALL;
		}
		startNotification();
	}
	public static void stop() {
		stopNotification();
		WakeLock.release();
	}
	
	private static void stopNotification() {
		if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
		if(context != null) ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
	}
	private static void startNotification() {
		Intent i = new Intent(context, AdhanAlarm.class);
		notification.setLatestEventInfo(context, context.getString(R.string.app_name), notification.tickerText, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
		notification.contentIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, ClickNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
		notification.deleteIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, ClearNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
		((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, notification);
		if(mediaPlayer == null || !mediaPlayer.isPlaying()) {
			try {
				Thread.sleep(CONSTANT.POST_NOTIFICATION_DELAY);	
			} catch(Exception ex) {
				// Just trying to make sure notification completes before phone falls asleep again
			}
			WakeLock.release();
		}
	}
}