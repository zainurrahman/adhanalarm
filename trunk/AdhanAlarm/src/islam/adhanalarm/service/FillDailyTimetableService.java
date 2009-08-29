package islam.adhanalarm.service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.astro.Dms;
import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Schedule;
import islam.adhanalarm.VARIABLE;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FillDailyTimetableService extends Service {

	private static GregorianCalendar forDay;

	private static ArrayList<HashMap<String, String>> timetable;
	private static SimpleAdapter timetableView;
	private static String marker;

	private static TextView current_latitude_deg;
	private static TextView current_latitude_min;
	private static TextView current_latitude_sec;

	private static TextView current_longitude_deg;
	private static TextView current_longitude_min;
	private static TextView current_longitude_sec;

	private static TextView current_qibla_deg;
	private static TextView current_qibla_min;
	private static TextView current_qibla_sec;

	private static TextView notes;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		try {
			Schedule day = new Schedule(forDay);
			GregorianCalendar[] schedule = day.getTimes();
			SimpleDateFormat timeFormat = new SimpleDateFormat ("h:mm a");
			short nextNotificationTime = day.nextTimeIndex();

			for(short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) {
				String fullTime = timeFormat.format(schedule[i].getTime());
				timetable.get(i).put("time", fullTime.substring(0, fullTime.lastIndexOf(" ")));
				timetable.get(i).put("time_am_pm", fullTime.substring(fullTime.lastIndexOf(" ") + 1, fullTime.length()) + (day.isExtreme(i) ? "*" : ""));
			}
			indicateNotificationTimes(nextNotificationTime);
			timetableView.notifyDataSetChanged();

			// Add Latitude, Longitude and Qibla DMS location
			net.sourceforge.jitl.astro.Location location = new net.sourceforge.jitl.astro.Location(VARIABLE.settings.getFloat("latitude", 43.67f), VARIABLE.settings.getFloat("longitude", -79.417f), Schedule.getGMTOffset(), 0);
			location.setSeaLevel(VARIABLE.settings.getFloat("altitude", 0) < 0 ? 0 : VARIABLE.settings.getFloat("altitude", 0));
			location.setPressure(VARIABLE.settings.getFloat("pressure", 1010));
			location.setTemperature(VARIABLE.settings.getFloat("temperature", 10));

			DecimalFormat df = new DecimalFormat("#.###");
			Dms latitude = new Dms(location.getDegreeLat());
			Dms longitude = new Dms(location.getDegreeLong());
			Dms qibla = Jitl.getNorthQibla(location);
			VARIABLE.qiblaDirection = (float)qibla.getDecimalValue(net.sourceforge.jitl.astro.Direction.NORTH);
			current_latitude_deg.setText(String.valueOf(latitude.getDegree()));
			current_latitude_min.setText(String.valueOf(latitude.getMinute()));
			current_latitude_sec.setText(df.format(latitude.getSecond()));
			current_longitude_deg.setText(String.valueOf(longitude.getDegree()));
			current_longitude_min.setText(String.valueOf(longitude.getMinute()));
			current_longitude_sec.setText(df.format(longitude.getSecond()));
			current_qibla_deg.setText(String.valueOf(qibla.getDegree()));
			current_qibla_min.setText(String.valueOf(qibla.getMinute()));
			current_qibla_sec.setText(df.format(qibla.getSecond()));
		} catch(Exception ex) {
			java.io.StringWriter sw = new java.io.StringWriter();
			java.io.PrintWriter pw = new java.io.PrintWriter(sw, true);
			ex.printStackTrace(pw);
			pw.flush(); sw.flush();
			notes.setText(sw.toString());
		}
	}

	private static void indicateNotificationTimes(short nextNotificationTime) {
		for(short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) {
			timetable.get(i).put("mark", ""); // Clear all existing markers in case it was left from the previous day or while phone was turned off
		}

		int previousNotificationTime = nextNotificationTime - 1 < CONSTANT.FAJR ? CONSTANT.ISHAA : nextNotificationTime - 1;

		if(!VARIABLE.alertSunrise() && nextNotificationTime == CONSTANT.SUNRISE) nextNotificationTime = CONSTANT.DHUHR;
		if(!VARIABLE.alertSunrise() && previousNotificationTime == CONSTANT.SUNRISE) previousNotificationTime = CONSTANT.FAJR;

		timetable.get(nextNotificationTime).put("mark", marker);
	}

	/**
	 * We use this class in a static way by using the following set function.
	 * I'm doing this all in this long way because in the future we may want to be able to display other dates than just today.
	 *  **/
	public static void set(Context context, GregorianCalendar _forDay, ArrayList<HashMap<String, String>> _timetable, SimpleAdapter _timetableView, String _marker, TextView _current_latitude_deg, TextView _current_latitude_min, TextView _current_latitude_sec, TextView _current_longitude_deg, TextView _current_longitude_min, TextView _current_longitude_sec, TextView _current_qibla_deg, TextView _current_qibla_min, TextView _current_qibla_sec, TextView _notes) {
		forDay = _forDay;

		timetable = _timetable;
		timetableView = _timetableView;
		marker = _marker;

		current_latitude_deg = _current_latitude_deg;
		current_latitude_min = _current_latitude_min;
		current_latitude_sec = _current_latitude_sec;

		current_longitude_deg = _current_longitude_deg;
		current_longitude_min = _current_longitude_min;
		current_longitude_sec = _current_longitude_sec;

		current_qibla_deg = _current_qibla_deg;
		current_qibla_min = _current_qibla_min;
		current_qibla_sec = _current_qibla_sec;

		notes = _notes;

		Intent intent = new Intent(context, FillDailyTimetableService.class);
		context.startService(intent);
	}
}