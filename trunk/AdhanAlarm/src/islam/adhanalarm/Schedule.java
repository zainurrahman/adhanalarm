package islam.adhanalarm;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.Method;
import net.sourceforge.jitl.Prayer;

public class Schedule {

	private static GregorianCalendar[] schedule = new GregorianCalendar[7];
	private static boolean[] extremes = new boolean[7];
	private static Jitl itl;
	
	private static Schedule today;

	public Schedule(GregorianCalendar day) {
		Method method = CONSTANT.CALCULATION_METHODS[VARIABLE.settings.getInt("calculationMethodsIndex", CONSTANT.DEFAULT_CALCULATION_METHOD)].copy();
		method.setRound(VARIABLE.ROUNDING_TYPES[VARIABLE.settings.getInt("roundingTypesIndex", 2)]);

		net.sourceforge.jitl.astro.Location location = new net.sourceforge.jitl.astro.Location(VARIABLE.settings.getFloat("latitude", 43.67f), VARIABLE.settings.getFloat("longitude", -79.417f), getGMTOffset(), 0);
		location.setSeaLevel(VARIABLE.settings.getFloat("altitude", 0) < 0 ? 0 : VARIABLE.settings.getFloat("altitude", 0));
		location.setPressure(VARIABLE.settings.getFloat("pressure", 1010));
		location.setTemperature(VARIABLE.settings.getFloat("temperature", 10));

		itl = CONSTANT.DEBUG ? new DummyJitl(location, method) : new Jitl(location, method);
		Prayer[] dayPrayers = itl.getPrayerTimes(day).getPrayers();
		Prayer[] allTimes = new Prayer[]{dayPrayers[0], dayPrayers[1], dayPrayers[2], dayPrayers[3], dayPrayers[4], dayPrayers[5], itl.getNextDayFajr(day)};

		for(short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) { // Set the times on the schedule
			schedule[i] = new GregorianCalendar(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH), allTimes[i].getHour(), allTimes[i].getMinute(), allTimes[i].getSecond());
			if(i == CONSTANT.NEXT_FAJR && !CONSTANT.DEBUG) {
				schedule[i].add(Calendar.DAY_OF_MONTH, 1); // Next fajr is tomorrow
			}
			schedule[i].add(Calendar.MINUTE, VARIABLE.settings.getInt("offsetMinutes", 0));
			extremes[i] = allTimes[i].isExtreme();
		}
	}
	public GregorianCalendar[] getTimes() {
		return schedule;
	}
	public boolean isExtreme(int i) {
		return extremes[i];
	}
	public short nextTimeIndex() {
		Calendar currentTime = Calendar.getInstance();
		if(currentTime.before(schedule[CONSTANT.FAJR])) return CONSTANT.FAJR;
		for(short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
			if(currentTime.after(schedule[i]) && currentTime.before(schedule[i + 1])) {
				return (short)(i + 1);
			}
		}
		return CONSTANT.NEXT_FAJR;
	}
	public static double getGMTOffset() {
		Calendar currentTime = new GregorianCalendar();
		int gmtOffset = currentTime.getTimeZone().getOffset(currentTime.getTimeInMillis());
		return gmtOffset / 3600000;
	}

	public static boolean isDaylightSavings() {
		Calendar currentTime = new GregorianCalendar();
		return currentTime.getTimeZone().inDaylightTime(currentTime.getTime());
	}
	public static Schedule today() {
		GregorianCalendar now = new GregorianCalendar();
		if(today == null) {
			today = new Schedule(now);
		} else {
			GregorianCalendar fajr = today.getTimes()[CONSTANT.FAJR];
			if(fajr.get(Calendar.YEAR) != now.get(Calendar.YEAR) || fajr.get(Calendar.MONTH) != now.get(Calendar.MONTH) || fajr.get(Calendar.DAY_OF_MONTH) != now.get(Calendar.DAY_OF_MONTH)) {
				today = new Schedule(now);
			}
		}
		return today;
	}
}