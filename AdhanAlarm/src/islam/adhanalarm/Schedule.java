package islam.adhanalarm;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.Method;
import net.sourceforge.jitl.Prayer;

public class Schedule {

	private static GregorianCalendar[] schedule = new GregorianCalendar[7];
	private static boolean[] extremes = new boolean[7];
	private Jitl itl = null;

	public Schedule() {
		Method method = CONSTANT.CALCULATION_METHODS[VARIABLE.settings.getInt("calculationMethodsIndex", 4)].copy();
		method.setRound(CONSTANT.ROUNDING_TYPES[VARIABLE.settings.getInt("roundingTypesIndex", 2)]);
		
		net.sourceforge.jitl.astro.Location location = new net.sourceforge.jitl.astro.Location(VARIABLE.settings.getFloat("latitude", 43.67f), VARIABLE.settings.getFloat("longitude", -79.417f), getGMTOffset(), 0);
		location.setSeaLevel(VARIABLE.settings.getFloat("altitude", 0) < 0 ? 0 : VARIABLE.settings.getFloat("altitude", 0));
		location.setPressure(VARIABLE.settings.getFloat("pressure", 1010));
		location.setTemperature(VARIABLE.settings.getFloat("temperature", 10));
		
		itl = CONSTANT.DEBUG ? new DummyJitl(location, method) : new Jitl(location, method);
		GregorianCalendar today = new GregorianCalendar();
		GregorianCalendar tomorrow = new GregorianCalendar();
		tomorrow.add(Calendar.DATE, 1);
		Prayer[] dayPrayers = itl.getPrayerTimes(today).getPrayers();
		Prayer[] allTimes = new Prayer[]{dayPrayers[0], dayPrayers[1], dayPrayers[2], dayPrayers[3], dayPrayers[4], dayPrayers[5], itl.getNextDayFajr(today)};
		
		for(short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) { // Set the times on the schedule
			if(i == CONSTANT.NEXT_FAJR) {
				schedule[i] = new GregorianCalendar(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DAY_OF_MONTH), allTimes[i].getHour(), allTimes[i].getMinute(), allTimes[i].getSecond());
			} else {
				schedule[i] = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), allTimes[i].getHour(), allTimes[i].getMinute(), allTimes[i].getSecond());	
			}
			extremes[i] = allTimes[i].isExtreme();
		}
	}
	public GregorianCalendar[] getTodaysTimes() {
		return schedule;
	}
	public boolean isExtreme(int i) {
		return extremes[i];
	}
	public short nextTimeIndex() {
		GregorianCalendar today = new GregorianCalendar();
		Calendar currentTime = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.HOUR), today.get(Calendar.MINUTE), today.get(Calendar.SECOND));
		if(currentTime.before(schedule[CONSTANT.FAJR])) return CONSTANT.FAJR;
		for(short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
			if(currentTime.compareTo(schedule[i]) <= 0 && currentTime.before(schedule[i + 1])) {
				return i;
			}
		}
		return -1;
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
}