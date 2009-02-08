package islam.adhanalarm;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sourceforge.jitl.DayPrayers;
import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.Method;
import net.sourceforge.jitl.Prayer;
import net.sourceforge.jitl.astro.Location;

public class DummyJitl extends Jitl {
	private DayPrayers dp = new DayPrayers();
	public DummyJitl(Location loc, Method method) {
		super(loc, method);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 60);
		dp.fajr().setHour(12); dp.fajr().setMinute(12); dp.fajr().setSecond(12);
	}
	public DayPrayers getPrayerTimes(final GregorianCalendar date) {
		return dp;
	}
	public Prayer getImsaak (final GregorianCalendar date) {
		return new Prayer(12, 12, 12, false);
	}

	public Prayer getNextDayImsaak (final GregorianCalendar date) {
		return new Prayer(12, 12, 12, false);
	}

	public Prayer getNextDayFajr (final GregorianCalendar date) {
		return new Prayer(12, 12, 12, false);
	}
}