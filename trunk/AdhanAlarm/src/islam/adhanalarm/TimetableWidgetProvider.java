package islam.adhanalarm;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class TimetableWidgetProvider extends AppWidgetProvider {
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		setLatestTimetable(context, appWidgetManager, appWidgetIds);
	}

	public static void setLatestTimetable(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TimetableWidgetProvider.class));
		setLatestTimetable(context, appWidgetManager, appWidgetIds);
	}
	private static void setLatestTimetable(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int[] times = new int[]{R.id.fajr, R.id.sunrise, R.id.dhuhr, R.id.asr, R.id.maghrib, R.id.ishaa, R.id.next_fajr};
		final GregorianCalendar[] schedule = Schedule.today().getTimes();
		final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
		for(int i = 0; i < appWidgetIds.length; i++) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_widget);

			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, AdhanAlarm.class), 0);
			views.setOnClickPendingIntent(R.id.timetable_widget, pendingIntent);

			for(int j = 0; j < times.length; j++) {
				String time = timeFormat.format(schedule[j].getTime());
				if(j == Schedule.today().nextTimeIndex()) {
					time += " " + context.getString(R.string.next_time_marker_reverse);
				}
				views.setTextViewText(times[j], time);
			}
			appWidgetManager.updateAppWidget(appWidgetIds[i], views);
		}
	}
}
