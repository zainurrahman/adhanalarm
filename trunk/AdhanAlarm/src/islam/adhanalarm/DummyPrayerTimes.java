package islam.adhanalarm;

import java.util.Calendar;

import libs.itl.PrayerTimes;

public class DummyPrayerTimes extends PrayerTimes {
	private Prayer[] pt = new Prayer[9];
	public DummyPrayerTimes() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		for(int i = 0; i < pt.length; i++) {
			calendar.add(Calendar.SECOND, 60);
			pt[i] = new Prayer();
			pt[i].setHour(calendar.getTime().getHours());
			pt[i].setMinute(calendar.getTime().getMinutes());
			pt[i].setSecond(calendar.getTime().getSeconds());
			pt[i].setIsExtreme(0);
		}
	}
	public Prayer[] getPrayerTimes ( Location loc, Method conf, PrayerDate date) {
		return new Prayer[] {pt[1], pt[2], pt[3], pt[4], pt[5], pt[6]};
	}

	public Prayer getImsaak (Location loc, Method conf, PrayerDate date) {
		return pt[0];
	}

	public Prayer getNextDayImsaak (Location loc, Method conf, PrayerDate date) {
		return pt[7];
	}

	public Prayer getNextDayFajr (Location loc, Method conf, PrayerDate date) {
		return pt[8];
	}
}