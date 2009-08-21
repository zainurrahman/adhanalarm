package islam.adhanalarm.service;

import islam.adhanalarm.AdhanAlarm;
import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.R;
import islam.adhanalarm.VARIABLE;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class NotifierService extends Service {
	
	private static Intent notifier;
	private static Context context;

	private static SharedPreferences settings;
	private static MediaPlayer mediaPlayer;

	public IBinder onBind(Intent i) {
		return null;
	}
	
	@Override
    public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		settings = getSharedPreferences("settingsFile", MODE_PRIVATE);
		stopNotification();
		
		short time = intent.getShortExtra("time", (short)-1);
		long timestamp = VARIABLE.schedule[time] != null ? VARIABLE.schedule[time].getTimeInMillis() : System.currentTimeMillis();
		String notificationTitle = (time != CONSTANT.SUNRISE ? getString(R.string.allahu_akbar) + ": " : "") + getString(R.string.time_for) + " " + (time == CONSTANT.NEXT_FAJR ? VARIABLE.TIME_NAMES[CONSTANT.FAJR] : VARIABLE.TIME_NAMES[time]).toLowerCase();
		Notification notification = new Notification(R.drawable.icon, notificationTitle, timestamp);

		int notificationMethod = settings.getInt("notificationMethodIndex", CONSTANT.DEFAULT_NOTIFICATION);
		int ringerMode = ((AudioManager)getSystemService(AUDIO_SERVICE)).getRingerMode();
		int callState = ((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).getCallState();
		if(notificationMethod == CONSTANT.RECITE_ADHAN && ringerMode != AudioManager.RINGER_MODE_SILENT && ringerMode != AudioManager.RINGER_MODE_VIBRATE && callState == TelephonyManager.CALL_STATE_IDLE) {
			int alarm = R.raw.beep;
			if(time == CONSTANT.DHUHR || time == CONSTANT.ASR || time == CONSTANT.MAGHRIB || time == CONSTANT.ISHAA || (time == CONSTANT.SUNRISE && !alertSunrise())) {
				alarm = R.raw.adhan;
			} else if(time == CONSTANT.FAJR || time == CONSTANT.NEXT_FAJR) {
				alarm = R.raw.adhan_fajr;
			}
			mediaPlayer = MediaPlayer.create(NotifierService.this, alarm);
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
		i.putExtra("clearNotification", true);
		notification.setLatestEventInfo(this, getString(R.string.app_name), notificationTitle, PendingIntent.getActivity(this, 0, i, 0));
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1, notification);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopNotification();
	}
	
	private void stopNotification() {
		if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
	}
	private boolean alertSunrise() {
		return settings.getInt("extraAlertsIndex", CONSTANT.NO_EXTRA_ALERTS) == CONSTANT.ALERT_SUNRISE;
	}

	/** We use this class in a static way by setting the context and using the subsequent start and stop functions **/
	
	public static void setContext(Context c) {
		context = c;
	}
	public static void start(short time) {
		stop();
		notifier = new Intent(context, NotifierService.class);
		notifier.putExtra("time", time);
		notifier.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(notifier);
	}
	public static void stop() {
		if(notifier != null) context.stopService(notifier);
		notifier = null;
	}
}
