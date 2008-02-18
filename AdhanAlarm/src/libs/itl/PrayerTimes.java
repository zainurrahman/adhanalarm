/************************************************************************
 * $Id: prayer.c,v 1.19 2004/09/15 20:59:06 thamer Exp $
 *
 * ------------
 * Description:
 * ------------
 *  Copyright (c) 2003, Arabeyes, Thamer Mahmoud
 *
 *  A full featured Muslim Prayer Times calculator
 *
 *
 * -----------------
 * Revision Details:    (Updated by Revision Control System)
 * -----------------
 *  $Date: 2004/09/15 20:59:06 $
 *  $Author: thamer $
 *  $Revision: 1.19 $
 *  $Source: /home/arabeyes/cvs/projects/itl/libs/prayertime/src/prayer.c,v $
 *
 * (www.arabeyes.org - under LGPL license - see COPYING file)
 *
 * Ported to Java by Ahmed Talaat (aa_talaat@yahoo.com) 5 Dec 2004
 ************************************************************************/
package libs.itl;

/**
 * @author me
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

public class PrayerTimes {
	/*
	 * Supported methods for Extreme Latitude calculations (Method.extreme):
	 *
	 * 0: none. if unable to calculate, leave as 99:99 1: Nearest Latitude: All
	 * prayers Always 2: Nearest Latitude: Fajr Ishaa Always 3: Nearest Latitude:
	 * Fajr Ishaa if invalid 4: Nearest Good Day: All prayers Always 5: Nearest Good
	 * Day: Fajr Ishaa if invalid (default) 6: 1/7th of Night: Fajr Ishaa Always 7:
	 * 1/7th of Night: Fajr Ishaa if invalid 8: 1/7th of Day: Fajr Ishaa Always 9:
	 * 1/7th of Day: Fajr Ishaa if invalid 10: Half of the Night: Fajr Ishaa Always
	 * 11: Half of the Night: Fajr Ishaa if invalid 12: Minutes from
	 * Shorooq/Maghrib: Fajr Ishaa Always (e.g. Maghrib=Ishaa) 13: Minutes from
	 * Shorooq/Maghrib: Fajr Ishaa If invalid
	 *
	 */

	public class Prayer {
	    private int hour;       /* prayer time hour */
	    private int minute;     /* prayer time minute */
	    private int second;     /* prayer time second */
	    private int isExtreme;  /*
								 * Extreme calculation switch. The 'getPrayerTimes'
								 * function sets this switch to 1 to indicate that
								 * this particular prayer time has been calculated
								 * through extreme latitude methods and NOT by
								 * conventional means of calculation.
								 */
	    public Prayer(){hour=0;minute=0;second=0;isExtreme=0;}
	    public int getHour(){return hour;}
	    public int getMinute(){return minute;}
	    int getSecond(){return second;}
	    int getIsExtreme(){return isExtreme;}

	    void setHour(int aHour){hour = aHour;}
		void setMinute(int aMinute){minute = aMinute;}
		void setSecond(int aSecond){second = aSecond;}
		void setIsExtreme(int aIsExtreme){isExtreme = aIsExtreme;}
		Prayer getPrayer(){
			Prayer tPrayer = new Prayer();
			tPrayer.hour=hour;
			tPrayer.minute=minute;
			tPrayer.second=second;
			tPrayer.isExtreme=isExtreme;
			return tPrayer;
		}
	}

	public class DayInfo {
		private int lastDay;
		private double julianDay;
		public DayInfo(){lastDay=0; julianDay=0;}
		public DayInfo(int aLastDay, double aJulianDay){lastDay=aLastDay; julianDay=aJulianDay;}
		int getLastDay(){return lastDay;}
		double getJulianDay(){return julianDay;}
		void setLastDay(int aLastDay){lastDay=aLastDay;}
		void setJulianDay(double aJulianDay){julianDay=aJulianDay;}
	}

	/*
	 * This class holds the calculation method used. NOTE: Before explicitly setting
	 * any of these values, it is more safe to default initialize them by calling
	 * 'getMethod(0, &method)'
	 */
	public class Method
	{
	    private double fajrAng;     /* Fajr angle */
	    private double ishaaAng;    /* Ishaa angle */
	    private double imsaakAng;   /* Imsaak angle and Fajr difference is 1.5 */
	    private int fajrInv;        /*
									 * Fajr Interval is the amount of minutes
									 * between Fajr and Shurooq (0 if not used)
									 */
	    private int ishaaInv;       /*
									 * Ishaa Interval is the amount if minutes
									 * between Ishaa and Maghrib (0 if not used)
									 */
	    private int imsaakInv;      /*
									 * Imsaak Interval is the amount of minutes
									 * between Imsaak and Fajr. The default is 10
									 * minutes before Fajr if Fajr Interval is set
									 */
	    private int round;          /*
									 * Method used for rounding seconds: 0: No
									 * Rounding. "Prayer.seconds" is set to the
									 * amount of computed seconds. 1: Normal
									 * Rounding. If seconds are equal to 30 or
									 * above, add 1 minute. Sets "Prayer.seconds" to
									 * zero. 2: Special Rounding. Similar to normal
									 * rounding but we always round down for Shurooq
									 * and Imsaak times. (default) 3: Aggressive
									 * Rounding. Similar to Special Rounding but we
									 * add 1 minute if the seconds value are equal
									 * to 1 second or more.
									 */
	    private int mathhab;        /*
									 * Assr prayer shadow ratio: 1: Shaf'i (default)
									 * 2: Hanafi
									 */
	    private double nearestLat;  /*
									 * Latitude Used for the 'Nearest Latitude'
									 * extreme methods. The default is 48.5
									 */
	    private int extreme;        /*
									 * Extreme latitude calculation method (see
									 * below)
									 */
	    private int offset;         /*
									 * Enable Offsets switch (set this to 1 to
									 * activate). This option allows you to add or
									 * subtract any amount of minutes to the daily
									 * computed prayer times based on values (in
									 * minutes) for each prayer in the offList array
									 */
	    private double offList[];  /*
								    * For Example: If you want to add 30 seconds to
								    * Maghrib and subtract 2 minutes from Ishaa:
								    * offset = 1 offList[4] = 0.5 offList[5] = -2
								    * ..and then call getPrayerTimes as usual.
								    */
	    public Method(){
	    	offList = new double[6];
	    	offset=0; extreme=0;nearestLat=0;mathhab=1;round=0;
	    	fajrAng=0;ishaaAng=0;imsaakAng=0;fajrInv=0;ishaaInv=0;imsaakInv=0;
	    }
	    int getOffset(){return offset;}
	    int getExtreme(){return extreme;}
	    double getNearestLat(){return nearestLat;}
	    int getMathhab(){return mathhab;}
	    int getRound(){return round;}
	    double getFajrAng(){return fajrAng;}
	    double getIshaaAng(){return ishaaAng;}
	    double getImsaakAng(){return imsaakAng;}
	    int getFajrInv(){return fajrInv;}
	    int getIshaaInv(){return ishaaInv;}
	    int getImsaakInv(){return imsaakInv;}
	    double getOffList(int i){return offList[i];}

	    void setOffset(int aOffset){offset=aOffset;}
	    void setExtreme(int aExtreme){extreme=aExtreme;}
	    void setNearestLat(double aNearestLat){nearestLat=aNearestLat;}
	    void setMathhab(int aMathhab){mathhab=aMathhab;}
	    public void setRound(int aRound){round=aRound;}
	    void setFajrAng(double aFajrAng){fajrAng=aFajrAng;}
	    void setIshaaAng(double aIshaaAng){ishaaAng=aIshaaAng;}
	    void setImsaakAng(double aImsaakAng){imsaakAng=aImsaakAng;}
	    void setFajrInv(int aFajrInv){fajrInv=aFajrInv;}
	    void setIshaaInv(int aIshaaInv){ishaaInv=aIshaaInv;}
	    void setImsaakInv(int aImsaakInv){imsaakInv=aImsaakInv;}
	    void setOffList(int i, double aOffList){offList[i]=aOffList;}
	    public static final int NONE = 0;
		public static final int EGYPT_SURVEY = 1;
		public static final int KARACHI_SHAF = 2;
		public static final int KARACHI_HANAF = 3;
		public static final int NORTH_AMERICA = 4;
		public static final int MUSLIM_LEAGUE = 5;
		public static final int UMM_ALQURRA = 6;
		public static final int FIXED_ISHAA = 7;

	    Method getMethod(){
	    	Method tMethod = new Method();
	    	tMethod.extreme=extreme;
	    	tMethod.fajrAng = fajrAng;
	    	tMethod.fajrInv = fajrInv;
	    	tMethod.imsaakAng = imsaakAng;
	    	tMethod.imsaakInv = imsaakInv;
	    	tMethod.ishaaAng = ishaaAng;
	    	tMethod.ishaaInv=ishaaInv;
	    	tMethod.mathhab = mathhab;
	    	tMethod.nearestLat = nearestLat;
	    	tMethod.offset = offset;
	    	tMethod.round = round;
	    	for (int i=0;i<offList.length;i++){
	    		tMethod.offList[i]=offList[i];
	    	}
	    	return tMethod;
	    }

	    public void autoFillPrayerMethod(int n){
		    int i;

		    setFajrInv(0);
		    setIshaaInv(0);
		    setImsaakInv(0);
		    setMathhab(1);
		    setRound(2);
		    setNearestLat(PrayerTimes.DEF_NEAREST_LATITUDE);
		    setImsaakAng(PrayerTimes.DEF_IMSAAK_ANGLE);
		    setExtreme(5);
		    setOffset(0);
		    for (int j = 0; j < 6; j++) {
		        setOffList(j,0);
		    }

		    switch(n)
		    {
		    case NONE:
		        setFajrAng(0.0);
		        setIshaaAng(0.0);
		        break;

		    case EGYPT_SURVEY:
		        setFajrAng(20);
		        setIshaaAng(18);
		        break;

		    case KARACHI_SHAF:
		        setFajrAng(18);
		        setIshaaAng(18);
		        break;

		    case KARACHI_HANAF:
		        setFajrAng(18);
		        setIshaaAng(18);
		        setMathhab(2);
		        break;

		    case NORTH_AMERICA:
		        setFajrAng(15);
		        setIshaaAng(15);
		        break;

		    case MUSLIM_LEAGUE:
		        setFajrAng(18);
		        setIshaaAng(17);
		        break;

		    case UMM_ALQURRA:
		        setFajrAng(19);
		        setIshaaAng(0.0);
		        setIshaaInv(90);
		        break;

		    case FIXED_ISHAA:
		        setFajrAng(19.5);
		        setIshaaAng(0.0);
		        setIshaaInv(90);
		        break;
		    }
	   	}

	}


	/* This holds the current date info. structure Date */
	public class PrayerDate {
	    private int day;
	    private int month;
	    private int year;
	    public PrayerDate(){day=0;month=0;year=0;}
	    int getDay(){return day; }
	    int getMonth(){return month;}
	    int getYear(){return year;}
	    public void setDay(int aDay){day = aDay;}
	    public void setMonth(int aMonth){month = aMonth;}
	    public void setYear(int aYear){year = aYear;}
	    PrayerDate getPrayerDate(){
	    	PrayerDate tPrayerDate = new PrayerDate();
	    	tPrayerDate.day=day;
	    	tPrayerDate.month=month;
	    	tPrayerDate.year=year;
	    	return tPrayerDate;
	    }
	}


	public class Location {
	    private double degreeLong;  /* Longitude in decimal degree. */
	    private double degreeLat;   /* Latitude in decimal degree. */
	    private double gmtDiff;     /* GMT difference. */
	    private int dst;            /*
									 * Daylight savings time switch (0 if not used).
									 * Setting this to 1 should add 1 hour to all
									 * the calculated prayer times
									 */
	    private double seaLevel;    /* Height above Sea level in meters */
	    private double pressure;    /*
									 * Atmospheric pressure in millibars (the
									 * astronomical standard value is 1010)
									 */
	    private double temperature; /*
									 * Temperature in Celsius degree (the
									 * astronomical standard value is 10)
									 */
	    public Location(){degreeLong=0;degreeLat=0;gmtDiff=0;dst=0;seaLevel=0;pressure=0;temperature=0;}
	    double getDegreeLong(){return degreeLong;}
	    double getDegreeLat(){return degreeLat;}
	    double getGmtDiff(){return gmtDiff;}
	    int getDst(){return dst;}
	    double getSeaLevel(){return seaLevel;}
	    double getPressure(){return pressure;}
	    double getTemperature(){return temperature;}

	    public void setDegreeLong(double aDegreeLong){degreeLong=aDegreeLong;}
	    public void setDegreeLat(double aDegreeLat){degreeLat=aDegreeLat;}
	    public void setGmtDiff(double aGmtDiff){gmtDiff=aGmtDiff;}
	    public void setDst(int aDst){dst=aDst;}
	    public void setSeaLevel(double aSeaLevel){seaLevel=aSeaLevel;}
	    public void setPressure(double aPressure){pressure=aPressure;}
	    public void setTemperature(double aTemperature){temperature=aTemperature;}
	    Location getLocation() {	//clone the class
	    	Location tLocation=new Location();
	    	tLocation.degreeLat=degreeLat;
	    	tLocation.gmtDiff=gmtDiff;
	    	tLocation.dst=dst;
	    	tLocation.seaLevel=seaLevel;
	    	tLocation.pressure=pressure;
	    	tLocation.temperature=temperature;
	    	return tLocation;
	    }
	}


	public static final double KAABA_LAT = 21.423333;
	public static final double KAABA_LONG = 39.823333;
	public static final double DEF_NEAREST_LATITUDE = 48.5;
	public static final double DEF_IMSAAK_ANGLE = 1.5;
	public static final double DEF_IMSAAK_INTERVAL = 10;
	public static final double DEFAULT_ROUND_SEC = 30;
	public static final double AGGRESSIVE_ROUND_SEC = 1;

	private final double L0[][]={			// L0[64][3]
		    {175347046, 0, 0},
		    {3341656, 4.6692568, 6283.07585},
		    {34894, 4.6261, 12566.1517},
		    {3497, 2.7441, 5753.3849},
		    {3418, 2.8289, 3.5231},
		    {3136, 3.6277, 77713.7715},
		    {2676, 4.4181, 7860.4194},
		    {2343, 6.1352, 3930.2097},
		    {1324, 0.7425, 11506.7698},
		    {1273, 2.0371, 529.691},
		    {1199, 1.1096, 1577.3435},
		    {990, 5.233, 5884.927},
		    {902, 2.045, 26.298},
		    {857, 3.508, 398.149},
		    {780, 1.179, 5223.694},
		    {753, 2.533, 5507.553},
		    {505, 4.583, 18849.228},
		    {492, 4.205, 775.523},
		    {357, 2.92, 0.067},
		    {317, 5.849, 11790.629},
		    {284, 1.899, 796.298},
		    {271, 0.315, 10977.079},
		    {243, 0.345, 5486.778},
		    {206, 4.806, 2544.314},
		    {205, 1.869, 5573.143},
		    {202, 2.4458, 6069.777},
		    {156, 0.833, 213.299},
		    {132, 3.411, 2942.463},
		    {126, 1.083, 20.775},
		    {115, 0.645, 0.98},
		    {103, 0.636, 4694.003},
		    {102, 0.976, 15720.839},
		    {102, 4.267, 7.114},
		    {99, 6.21, 2146.17},
		    {98, 0.68, 155.42},
		    {86, 5.98, 161000.69},
		    {85, 1.3, 6275.96},
		    {85, 3.67, 71430.7},
		    {80, 1.81, 17260.15},
		    {79, 3.04, 12036.46},
		    {71, 1.76, 5088.63},
		    {74, 3.5, 3154.69},
		    {74, 4.68, 801.82},
		    {70, 0.83, 9437.76},
		    {62, 3.98, 8827.39},
		    {61, 1.82, 7084.9},
		    {57, 2.78, 6286.6},
		    {56, 4.39, 14143.5},
		    {56, 3.47, 6279.55},
		    {52, 0.19, 12139.55},
		    {52, 1.33, 1748.02},
		    {51, 0.28, 5856.48},
		    {49, 0.49, 1194.45},
		    {41, 5.37, 8429.24},
		    {41, 2.4, 19651.05},
		    {39, 6.17, 10447.39},
		    {37, 6.04, 10213.29},
		    {37, 2.57, 1059.38},
		    {36, 1.71, 2352.87},
		    {36, 1.78, 6812.77},
		    {33, 0.59, 17789.85},
		    {30, 0.44, 83996.85},
		    {30, 2.74, 1349.87},
		    {25, 3.16, 4690.48}
		};

	private final double L1[][]={		// L1[][3]
		    {628331966747.0, 0, 0},
		    {206059, 2.678235, 6283.07585},
		    {4303, 2.6351, 12566.1517},
		    {425, 1.59, 3.523},
		    {119, 5.796, 26.298},
		    {109, 2.966, 1577.344},
		    {93, 2.59, 18849.23},
		    {72, 1.14, 529.69},
		    {68, 1.87, 398.15},
		    {67, 4.41, 5507.55},
		    {59, 2.89, 5223.69},
		    {56, 2.17, 155.42},
		    {45, 0.4, 796.3},
		    {36, 0.47, 775.52},
		    {29, 2.65, 7.11},
		    {21, 5.34, 0.98},
		    {19, 1.85, 5486.78},
		    {19, 4.97, 213.3},
		    {17, 2.99, 6275.96},
		    {16, 0.03, 2544.31},
		    {16, 1.43, 2146.17},
		    {15, 1.21, 10977.08},
		    {12, 2.83, 1748.02},
		    {12, 3.26, 5088.63},
		    {12, 5.27, 1194.45},
		    {12, 2.08, 4694},
		    {11, 0.77, 553.57},
		    {10, 1.3, 3286.6},
		    {10, 4.24, 1349.87},
		    {9, 2.7, 242.73},
		    {9, 5.64, 951.72},
		    {8, 5.3, 2352.87},
		    {6, 2.65, 9437.76},
		    {6, 4.67, 4690.48}
		};

	private final double L2[][]={		// L2[][3]
		    {52919, 0, 0},
		    {8720, 1.0721, 6283.0758},
		    {309, 0.867, 12566.152},
		    {27, 0.05, 3.52},
		    {16, 5.19, 26.3},
		    {16, 3.68, 155.42},
		    {10, 0.76, 18849.23},
		    {9, 2.06, 77713.77},
		    {7, 0.83, 775.52},
		    {5, 4.66, 1577.34},
		    {4, 1.03, 7.11},
		    {4, 3.44, 5573.14},
		    {3, 5.14, 796.3},
		    {3, 6.05, 5507.55},
		    {3, 1.19, 242.73},
		    {3, 6.12, 529.69},
		    {3, 0.31, 398.15},
		    {3, 2.28, 553.57},
		    {2, 4.38, 5223.69},
		    {2, 3.75, 0.98}
		};

	private final double L3[][]={		// L3[][3
		    {289, 5.844, 6283.076},
		    {35, 0, 0},
		    {17, 5.49, 12566.15},
		    {3, 5.2, 155.42},
		    {1, 4.72, 3.52},
		    {1, 5.3, 18849.23},
		    {1, 5.97, 242.73}
		};

	private final double L4[][]={		// L4[][3]
		    {114.0, 3.142, 0.0},
		    {8.0, 4.13, 6283.08},
		    {1.0, 3.84, 12566.15}
		};

	private final double L5[][]={
		    {1, 3.14, 0}
		};

	private final double B0[][]={

		    {280, 3.199, 84334.662},
		    {102, 5.422, 5507.553},
		    {80, 3.88, 5223.69},
		    {44, 3.7, 2352.87},
		    {32, 4, 1577.34}
		};

	private final double B1[][]={

		    {9, 3.9, 5507.55},
		    {6, 1.73, 5223.69}
		};

	private final double R0[][]={
		    {100013989, 0, 0},
		    {1670700, 3.0984635, 6283.07585},
		    {13956, 3.05525, 12566.1517},
		    {3084, 5.1985, 77713.7715},
		    {1628, 1.1739, 5753.3849},
		    {1576, 2.8469, 7860.4194},
		    {925, 5.453, 11506.77},
		    {542, 4.564, 3930.21},
		    {472, 3.661, 5884.927},
		    {346, 0.964, 5507.553},
		    {329, 5.9, 5223.694},
		    {307, 0.299, 5573.143},
		    {243, 4.273, 11790.629},
		    {212, 5.847, 1577.344},
		    {186, 5.022, 10977.079},
		    {175, 3.012, 18849.228},
		    {110, 5.055, 5486.778},
		    {98, 0.89, 6069.78},
		    {86, 5.69, 15720.84},
		    {86, 1.27, 161000.69},
		    {85, 0.27, 17260.15},
		    {63, 0.92, 529.69},
		    {57, 2.01, 83996.85},
		    {56, 5.24, 71430.7},
		    {49, 3.25, 2544.31},
		    {47, 2.58, 775.52},
		    {45, 5.54, 9437.76},
		    {43, 6.01, 6275.96},
		    {39, 5.36, 4694},
		    {38, 2.39, 8827.39},
		    {37, 0.83, 19651.05},
		    {37, 4.9, 12139.55},
		    {36, 1.67, 12036.46},
		    {35, 1.84, 2942.46},
		    {33, 0.24, 7084.9},
		    {32, 0.18, 5088.63},
		    {32, 1.78, 398.15},
		    {28, 1.21, 6286.6},
		    {28, 1.9, 6279.55},
		    {26, 4.59, 10447.39}
		};

	private final double R1[][]={

		    {103019, 1.10749, 6283.07585},
		    {1721, 1.0644, 12566.1517},
		    {702, 3.142, 0},
		    {32, 1.02, 18849.23},
		    {31, 2.84, 5507.55},
		    {25, 1.32, 5223.69},
		    {18, 1.42, 1577.34},
		    {10, 5.91, 10977.08},
		    {9, 1.42, 6275.96},
		    {9, 0.27, 5486.78}
		};

	private final double R2[][]={

		    {4359, 5.7846, 6283.0758},
		    {124, 5.579, 12566.152},
		    {12, 3.14, 0},
		    {9, 3.63, 77713.77},
		    {6, 1.87, 5573.14},
		    {3, 5.47, 18849}

		};

	private final double R3[][]={
		    {145, 4.273, 6283.076},
		    {7, 3.92, 12566.15}
		};

	private final double R4[][]={
		    {4, 2.56, 6283.08}
		};

	private final double PE[][]={
		    {-171996, -174.2, 92025, 8.9},
		    {-13187, -1.6, 5736, -3.1},
		    {-2274, -0.2, 977, -0.5},
		    {2062, 0.2, -895, 0.5},
		    {1426, -3.4, 54, -0.1},
		    {712, 0.1, -7, 0},
		    {-517, 1.2, 224, -0.6},
		    {-386, -0.4, 200, 0},
		    {-301, 0, 129, -0.1},
		    {217, -0.5, -95, 0.3},
		    {-158, 0, 0, 0},
		    {129, 0.1, -70, 0},
		    {123, 0, -53, 0},
		    {63, 0, 0, 0},
		    {63, 0.1, -33, 0},
		    {-59, 0, 26, 0},
		    {-58, -0.1, 32, 0},
		    {-51, 0, 27, 0},
		    {48, 0, 0, 0},
		    {46, 0, -24, 0},
		    {-38, 0, 16, 0},
		    {-31, 0, 13, 0},
		    {29, 0, 0, 0},
		    {29, 0, -12, 0},
		    {26, 0, 0, 0},
		    {-22, 0, 0, 0},
		    {21, 0, -10, 0},
		    {17, -0.1, 0, 0},
		    {16, 0, -8, 0},
		    {-16, 0.1, 7, 0},
		    {-15, 0, 9, 0},
		    {-13, 0, 7, 0},
		    {-12, 0, 6, 0},
		    {11, 0, 0, 0},
		    {-10, 0, 5, 0},
		    {-8, 0, 3, 0},
		    {7, 0, -3, 0},
		    {-7, 0, 0, 0},
		    {-7, 0, 3, 0},
		    {-7, 0, 3, 0},
		    {6, 0, 0, 0},
		    {6, 0, -3, 0},
		    {6, 0, -3, 0},
		    {-6, 0, 3, 0},
		    {-6, 0, 3, 0},
		    {5, 0, 0, 0},
		    {-5, 0, 3, 0},
		    {-5, 0, 3, 0},
		    {-5, 0, 3, 0},
		    {4, 0, 0, 0},
		    {4, 0, 0, 0},
		    {4, 0, 0, 0},
		    {-4, 0, 0, 0},
		    {-4, 0, 0, 0},
		    {-4, 0, 0, 0},
		    {3, 0, 0, 0},
		    {-3, 0, 0, 0},
		    {-3, 0, 0, 0},
		    {-3, 0, 0, 0},
		    {-3, 0, 0, 0},
		    {-3, 0, 0, 0},
		    {-3, 0, 0, 0},
		    {-3, 0, 0, 0}
		};

	private final int SINCOEFF[][]={
		    {0, 0, 0, 0, 1},
		    {-2, 0, 0, 2, 2},
		    {0, 0, 0, 2, 2},
		    {0, 0, 0, 0, 2},
		    {0, 1, 0, 0, 0},
		    {0, 0, 1, 0, 0},
		    {-2, 1, 0, 2, 2},
		    {0, 0, 0, 2, 1},
		    {0, 0, 1, 2, 2},
		    {-2, -1, 0, 2, 2},
		    {-2, 0, 1, 0, 0},
		    {-2, 0, 0, 2, 1},
		    {0, 0, -1, 2, 2},
		    {2, 0, 0, 0, 0},
		    {0, 0, 1, 0, 1},
		    {2, 0, -1, 2, 2},
		    {0, 0, -1, 0, 1},
		    {0, 0, 1, 2, 1},
		    {-2, 0, 2, 0, 0},
		    {0, 0, -2, 2, 1},
		    {2, 0, 0, 2, 2},
		    {0, 0, 2, 2, 2},
		    {0, 0, 2, 0, 0},
		    {-2, 0, 1, 2, 2},
		    {0, 0, 0, 2, 0},
		    {-2, 0, 0, 2, 0},
		    {0, 0, -1, 2, 1},
		    {0, 2, 0, 0, 0},
		    {2, 0, -1, 0, 1},
		    {-2, 2, 0, 2, 2},
		    {0, 1, 0, 0, 1},
		    {-2, 0, 1, 0, 1},
		    {0, -1, 0, 0, 1},
		    {0, 0, 2, -2, 0},
		    {2, 0, -1, 2, 1},
		    {2, 0, 1, 2, 2},
		    {0, 1, 0, 2, 2},
		    {-2, 1, 1, 0, 0},
		    {0, -1, 0, 2, 2},
		    {2, 0, 0, 2, 1},
		    {2, 0, 1, 0, 0},
		    {-2, 0, 2, 2, 2},
		    {-2, 0, 1, 2, 1},
		    {2, 0, -2, 0, 1},
		    {2, 0, 0, 0, 1},
		    {0, -1, 1, 0, 0},
		    {-2, -1, 0, 2, 1},
		    {-2, 0, 0, 0, 1},
		    {0, 0, 2, 2, 1},
		    {-2, 0, 2, 0, 1},
		    {-2, 1, 0, 2, 1},
		    {0, 0, 1, -2, 0},
		    {-1, 0, 1, 0, 0},
		    {-2, 1, 0, 0, 0},
		    {1, 0, 0, 0, 0},
		    {0, 0, 1, 2, 0},
		    {0, 0, -2, 2, 2},
		    {-1, -1, 1, 0, 0},
		    {0, 1, 1, 0, 0},
		    {0, -1, 1, 2, 2},
		    {2, -1, -1, 2, 2},
		    {0, 0, 3, 2, 2},
		    {2, -1, 0, 2, 2}
		};
	public static final double INVALID_TRIGGER = -.999;
	public static final double DEG_TO_10_BASE =1/15.0;
	public static final double CENTER_OF_SUN_ANGLE = -0.833370;
	public static final double ALTITUDE_REFRACTION = 0.0347;
	public static final double REF_LIMIT = 9999999;
	public static final double PI = 3.1415926535898;

	public static final int NONE_EX = 0;
	public static final int LAT_ALL = 1;
	public static final int LAT_ALWAYS = 2;
	public static final int LAT_INVALID = 3;
	public static final int GOOD_ALL = 4;
	public static final int GOOD_INVALID = 5;
	public static final int SEVEN_NIGHT_ALWAYS = 6;
	public static final int SEVEN_NIGHT_INVALID = 7;
	public static final int SEVEN_DAY_ALWAYS = 8;
	public static final int SEVEN_DAY_INVALID = 9;
	public static final int HALF_ALWAYS = 10;
	public static final int HALF_INVALID = 11;
	public static final int MIN_ALWAYS = 12;
	public static final int MIN_INVALID = 13;
	public static final int GOOD_DIF = 14;

	public static final int FAJR = 0;
	public static final int SHUROOQ = 1;
	public static final int THUHR = 2;
	public static final int ASSR = 3;
	public static final int MAGHRIB = 4;
	public static final int ISHAA = 5;
	public static final int IMSAAK = 6;
	public static final int NEXTFAJR = 7;

	private double DEG_TO_RAD(double A){
    	return (A * (PI/180.0));
    }

    private double RAD_TO_DEG(double A){
    	return (A / (PI/180.0));
    }

    double getRefraction(Location loc, double sunAlt){
        double part1, part2;

        part1 = (loc.getPressure()/1010.0) * (283/(273 + loc.getTemperature()));
        part2 = 1 / Math.tan(DEG_TO_RAD(sunAlt + (7.31/(sunAlt + 4.4)))) + 0.0013515;

        return (part1 * part2) / 60.0;
    }


    double limitAngle(double L){
    	double F;
    	L /= 360.0;
    	F = L - Math.floor(L);
    	if (F > 0)
    		return 360 * F;
    	else if (F < 0)
    		return 360 - 360 * F;
    	else return L;
    }


    double limitAngle180(double L){
		double F;
		L /= 180.0;
		F = L - Math.floor(L);
		if (F > 0)
			return 180 * F;
		else if (F < 0)
			return 180 - 180 * F;
		else return L;
    }

    double limitAngle111(double L){
		double F;
		F = L - Math.floor(L);
		if (F < 0)
			return F += 1;
		return F;
    }

    double limitAngle180between(double L){
		double F;
		L /= 360.0;
		F = (L - Math.floor(L)) * 360.0;
		if  (F < -180)
			F += 360;
		else if  (F > 180)
			F -= 360;
		return F;
    }


	public Prayer[] getPrayerTimes ( Location loc, Method conf, PrayerDate date){
		//the Prayer[] should be Prayer [6]
		DayInfo dayInfo;
		Prayer[] pt;
		dayInfo = getDayInfo( date, loc.getGmtDiff());
		pt = getPrayerTimesByDay(loc, conf, dayInfo.getLastDay(), dayInfo.getJulianDay(),0);
		return pt;
	}

	Prayer[] getPrayerTimesByDay (Location loc, Method conf, int lastDay,
									double julianDay, int type){
		int i, invalid;
		double th, sh, mg, fj, is, ar;
		double lat, lon, dec;
		double tempPrayer[] = new double[6];
		Astro astro = new Astro();
		Prayer[] pt = new Prayer[6];

		for(int j=0;j<6;j++){
			pt[j]= new Prayer();
		}

		getAstroValuesByDay(julianDay, loc, astro);
		dec = DEG_TO_RAD(astro.getDec(1));
		lat = loc.getDegreeLat();
		lon = loc.getDegreeLong();
		invalid = 0;


		/*
		 * First Step: Get Prayer Times results for this day of year and this location.
		 * The results are NOT the actual prayer times
		 */
		fj   = getFajIsh (lat, dec, conf.getFajrAng());
		sh   = getShoMag (loc, astro, SHUROOQ);
		th   = getThuhr (lon, astro);
		ar   = getAssr (lat, dec, conf.getMathhab());
		mg   = getShoMag (loc, astro, MAGHRIB);
		is   = getFajIsh (lat, dec, conf.getIshaaAng());

		/*
		 * Second Step A: Calculate all salat times as Base-10 numbers in Normal
		 * circumstances
		 */

		/* Fajr */
		if (fj == 99) {
			tempPrayer[0] = 99;
			invalid = 1;
		}
		else
			tempPrayer[0] = th - fj;

		if (sh == 99)
			invalid = 1;

		tempPrayer[1] = sh;
		tempPrayer[2] = th;
		tempPrayer[3] = th + ar;
		tempPrayer[4] = mg;

		if (mg == 99)
			invalid = 1;

		/* Ishaa */
		if (is == 99) {
			tempPrayer[5] = 99;
			invalid = 1;
		}
		else tempPrayer[5] = th + is;


		/*
		 * Second Step B: Calculate all salat times as Base-10 numbers in Extreme
		 * Latitudes (if needed)
		 */

		/* Reset status of extreme switches */
		for (i=0; i<6; i++)
			pt[i].setIsExtreme(0);

		if ((conf.getExtreme() != NONE_EX) && !((conf.getExtreme() == GOOD_INVALID ||
		                   conf.getExtreme() == LAT_INVALID ||
		                   conf.getExtreme() == SEVEN_NIGHT_INVALID ||
		                   conf.getExtreme() == SEVEN_DAY_INVALID ||
		                   conf.getExtreme() == HALF_INVALID) &&
		                  (invalid == 0)))
		{
		double exdecPrev, exdecNext;
		double exTh=99, exFj=99, exIs=99, exAr=99, exIm=99, exSh=99, exMg=99;
		double portion = 0;
		double nGoodDay = 0;
		int exinterval = 0;
		Location exLoc = loc.getLocation();		//make a copy
		Astro exAstroPrev;
		Astro exAstroNext;

		switch(conf.getExtreme())
		{
		/* Nearest Latitude (Method.nearestLat) */
		case LAT_ALL:
		case LAT_ALWAYS:
		case LAT_INVALID:

		/*
		 * xxxthamer: we cannot compute this when interval is set because angle==0.
		 * Only the if-invalid methods would work
		 */
		exLoc.setDegreeLat(conf.getNearestLat());
		exFj = getFajIsh(conf.getNearestLat(), dec, conf.getFajrAng());
		exIm = getFajIsh(conf.getNearestLat(), dec, conf.getImsaakAng());
		exIs = getFajIsh(conf.getNearestLat(), dec, conf.getIshaaAng());
		exAr = getAssr(conf.getNearestLat(), dec, conf.getMathhab());
		exSh = getShoMag (exLoc, astro, SHUROOQ);
		exMg = getShoMag (exLoc, astro, MAGHRIB);

		switch(conf.getExtreme())
		{
		case LAT_ALL:
		tempPrayer[0] = th - exFj;
		tempPrayer[1] = exSh;
		tempPrayer[3] = th + exAr;
		tempPrayer[4] = exMg;
		tempPrayer[5] = th + exIs;
		pt[0].setIsExtreme(1);
		pt[1].setIsExtreme(1);
		pt[2].setIsExtreme(1);
		pt[3].setIsExtreme(1);
		pt[4].setIsExtreme(1);
		pt[5].setIsExtreme(1);
		break;

		case LAT_ALWAYS:
		tempPrayer[0] = th - exFj;
		tempPrayer[5] = th + exIs;
		pt[0].setIsExtreme(1);
		pt[5].setIsExtreme(1);
		break;

		case LAT_INVALID:
		if (tempPrayer[0] == 99) {
		tempPrayer[0] = th - exFj;
		pt[0].setIsExtreme(1);
		}
		if (tempPrayer[5] == 99) {
		tempPrayer[5] = th + exIs;
		pt[5].setIsExtreme(1);
		}
		break;
		}
		break;


		/* Nearest Good Day */
		case GOOD_ALL:
		case GOOD_INVALID:
		case GOOD_DIF:

		exAstroPrev = astro.getAstro();
		exAstroNext = astro.getAstro();

		/* Start by getting last or next nearest Good Day */
		for(i=0; i <= lastDay; i++)
		{

		/* last closest day */
		nGoodDay = julianDay - i;
		getAstroValuesByDay(nGoodDay, loc, exAstroPrev);
		exdecPrev = DEG_TO_RAD(exAstroPrev.getDec(1));
		exFj = getFajIsh(lat, exdecPrev, conf.getFajrAng());


		if (exFj != 99)
		{
		exIs = getFajIsh(lat, exdecPrev, conf.getIshaaAng());
		if (exIs != 99)
		{
		  exTh = getThuhr (lon, exAstroPrev);
		  exSh = getShoMag (loc, exAstroPrev, SHUROOQ);
		  exMg = getShoMag (loc, exAstroPrev, MAGHRIB);
		  exAr = getAssr (lat, exdecPrev, conf.getMathhab());
		  break;
		}
		}

		/* Next closest day */
		nGoodDay = julianDay + i;
		getAstroValuesByDay(nGoodDay, loc, exAstroNext);
		exdecNext = DEG_TO_RAD(exAstroNext.getDec(1));
		exFj = getFajIsh(lat, exdecNext, conf.getFajrAng());
		if (exFj != 99)
		{
		exIs = getFajIsh(lat, exdecNext, conf.getIshaaAng());
		if (exIs != 99)
		{
		  exTh = getThuhr (lon, exAstroNext);
		  exSh = getShoMag (loc, exAstroNext, SHUROOQ);
		  exMg = getShoMag (loc, exAstroNext, MAGHRIB);
		  exAr = getAssr (lat, exdecNext, conf.getMathhab());
		  break;
		}
		}
		}

		switch(conf.getExtreme())
		{
		case GOOD_ALL:
			tempPrayer[0] = exTh - exFj;
			tempPrayer[1] = exSh;
			tempPrayer[2] = exTh;
			tempPrayer[3] = exTh + exAr;
			tempPrayer[4] = exMg;
			tempPrayer[5] = exTh + exIs;
			for (i=0; i<6; i++)
				pt[i].setIsExtreme(1);
		break;
		case GOOD_INVALID:
			if (tempPrayer[0] == 99) {
				tempPrayer[0] = exTh - exFj;
				pt[0].setIsExtreme(1);
			}
			if (tempPrayer[5] == 99) {
				tempPrayer[5] = exTh + exIs;
				pt[5].setIsExtreme(1);
			}
		break;

		case GOOD_DIF:
		/*
		 * Nearest Good Day: Differant good days for Fajr and Ishaa (Not implemented)
		 */
		break;
		}
		break;

		case SEVEN_NIGHT_ALWAYS:
		case SEVEN_NIGHT_INVALID:
		case SEVEN_DAY_ALWAYS:
		case SEVEN_DAY_INVALID:
		case HALF_ALWAYS:
		case HALF_INVALID:

		switch(conf.getExtreme())
		{
			case SEVEN_NIGHT_ALWAYS:
			case SEVEN_NIGHT_INVALID:
				portion = (24 - (tempPrayer[4] - tempPrayer[1])) * (1/7.0);
			break;
			case SEVEN_DAY_ALWAYS:
			case SEVEN_DAY_INVALID:
				portion = (tempPrayer[4] - tempPrayer[1]) * (1/7.0);
			break;
			case HALF_ALWAYS:
			case HALF_INVALID:
				portion = (24 - tempPrayer[4] - tempPrayer[1]) * (1/2.0);
			break;
		}

		if (conf.getExtreme() == SEVEN_NIGHT_INVALID ||
		conf.getExtreme() == SEVEN_DAY_INVALID ||
		conf.getExtreme() == HALF_INVALID)
		{
		if (tempPrayer[0] == 99) {
			if  (conf.getExtreme() == HALF_INVALID)
				tempPrayer[0] =  portion + (conf.getFajrInv() / 60.0);
			else tempPrayer[0] = tempPrayer[1] - portion;
			pt[0].setIsExtreme(1);
		}
		if (tempPrayer[5] == 99) {
		if  (conf.getExtreme() == HALF_INVALID)
		  tempPrayer[5] = portion - (conf.getIshaaInv() / 60.0) ;
		else tempPrayer[5] = tempPrayer[4] + portion;
		pt[5].setIsExtreme(1);
		}
		} else {
		if  (conf.getExtreme() == HALF_ALWAYS)
		tempPrayer[0] =   portion + (conf.getFajrInv() / 60.0);
		else tempPrayer[0] = tempPrayer[1] - portion;
		if  (conf.getExtreme() == HALF_ALWAYS)
		tempPrayer[5] = portion - (conf.getIshaaInv() / 60.0) ;
		else tempPrayer[5] = tempPrayer[4] + portion;
		pt[0].setIsExtreme(1);
		pt[5].setIsExtreme(1);
		}
		break;

		case MIN_ALWAYS:
		/*
		 * Do nothing here because this is implemented through fajrInv and ishaaInv
		 * structure members
		 */
		tempPrayer[0] = tempPrayer[1];
		tempPrayer[5] = tempPrayer[4];
		pt[0].setIsExtreme(1);
		pt[5].setIsExtreme(1);
		break;

		case MIN_INVALID:
		if (tempPrayer[0] == 99) {
		exinterval = conf.getFajrInv() / 60;
		tempPrayer[0] = tempPrayer[1] - exinterval;
		pt[0].setIsExtreme(1);
		}
		if (tempPrayer[5] == 99) {
		exinterval = conf.getIshaaInv() / 60;
		tempPrayer[5] = tempPrayer[4] + exinterval;
		pt[5].setIsExtreme(1);
		}
		break;
		} /* end switch */
		} /* end extreme */


		/* Apply intervals if set */
		if (conf.getExtreme() != MIN_INVALID ||
		conf.getExtreme() != HALF_INVALID ||
		conf.getExtreme() != HALF_ALWAYS) {
		if (conf.getFajrInv() != 0)
		tempPrayer[0] = tempPrayer[1] - (conf.getFajrInv() / 60.0);
		if (conf.getIshaaInv() != 0)
		tempPrayer[5] = tempPrayer[4] + (conf.getIshaaInv() / 60.0);
		}


		/*
		 * Third and Final Step: Fill the Prayer array by doing decimal degree to Prayer
		 * structure conversion
		 */
		if (type == IMSAAK || type == NEXTFAJR)
			base6hm(tempPrayer[0], loc, conf, pt[0], type);
		else {
			for (i=0; i<6; i++)
				base6hm(tempPrayer[i], loc, conf, pt[i], i);
		}
		return pt;
	}


	DayInfo getDayInfo ( PrayerDate date, double gmt){
		int ld;
		double jd;
		ld = getDayofYear(date.getYear(), 12, 31);
		jd = getJulianDay(date, gmt);
		DayInfo dayInfo = new DayInfo(ld,jd);
		return dayInfo;
	}

	int getDayofYear(int year, int month, int day)
	{
	    int i;
	    boolean isLeap = ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);

	    char dayList[][] = {
	        {0,31,28,31,30,31,30,31,31,30,31,30,31},
	        {0,31,29,31,30,31,30,31,31,30,31,30,31}
	    };

	    for (i=1; i<month; i++){
	    	if (isLeap==true){
	    		day += dayList[1][i];
	    	}else {
	    		day += dayList[0][i];
	    	}
	    }
	    return day;
	}

	double getJulianDay(PrayerDate date, double gmt){
		double jdB=0, jdY, jdM, JD;

		jdY=date.getYear();
		jdM=date.getMonth();

		if (date.getMonth() <= 2) {
			jdY--;
			jdM+=12;
		}

		if (date.getYear() < 1)
			jdY++;

		if ((date.getYear() > 1582) || ((date.getYear() == 1582) &&
				((date.getMonth() > 10) ||
                                 ((date.getMonth() == 10) && (date.getDay() >= 4)))))
			jdB = 2 - Math.floor(jdY/100.0) + Math.floor((jdY/100.0)/4.0);

		JD = Math.floor(365.25 * (jdY + 4716.0)) + Math.floor(30.6001 * ( jdM + 1))
        + (date.getDay() + (-gmt)/24.0)  + jdB - 1524.5 ;
		return JD;
	}

    void getAstroValuesByDay(double julianDay, Location loc, Astro astro){

   	AstroDay ad = new AstroDay();

   	if (astro.getJd() == (julianDay-1)){

   		astro.setRa(0,astro.getRa(1));
   		astro.setRa(1,astro.getRa(2));

   		astro.setDec(0,astro.getDec(1));
   		astro.setDec(1,astro.getDec(2));

   		astro.setSid(0,astro.getSid(1));
   		astro.setSid(1,astro.getSid(2));

   		astro.setDra(0,astro.getDra(1));
   		astro.setDra(1,astro.getDra(2));

   		computeAstroDay(julianDay+1,loc,ad);
   		astro.setRa(2,ad.getRa());
   		astro.setDec(2,ad.getDec());
   		astro.setSid(2,ad.getSidTime());
   		astro.setDra(2,ad.getDra());
	} else
	if (astro.getJd() == julianDay + 1){
		astro.setRa(2,astro.getRa(1));
   		astro.setRa(1,astro.getRa(0));

   		astro.setDec(2,astro.getDec(1));
   		astro.setDec(1,astro.getDec(0));

   		astro.setSid(2,astro.getSid(1));
   		astro.setSid(1,astro.getSid(0));

   		astro.setDra(2,astro.getDra(1));
   		astro.setDra(1,astro.getDra(0));

		computeAstroDay(julianDay-1, loc, ad);

		astro.setRa(0,ad.getRa());
		astro.setDec(0,ad.getDec());
   		astro.setSid(0,ad.getSidTime());
   		astro.setDra(0,ad.getDra());

	} else if (astro.getJd() != julianDay) {

		computeAstroDay(julianDay-1, loc, ad);
		astro.setRa(0,ad.getRa());
		astro.setDec(0,ad.getDec());
		astro.setSid(0,ad.getSidTime());
		astro.setDra(0,ad.getDra());

		computeAstroDay(julianDay, loc, ad);
		astro.setRa(1,ad.getRa());
		astro.setDec(1,ad.getDec());
		astro.setSid(1,ad.getSidTime());
		astro.setDra(1,ad.getDra());
		computeAstroDay(julianDay+1, loc, ad);
		astro.setRa(2,ad.getRa());
		astro.setDec(2,ad.getDec());
		astro.setSid(2,ad.getSidTime());
		astro.setDra(2,ad.getDra());
	}
   	astro.setJd(julianDay);
    }

    void computeAstroDay(double JD, Location loc, AstroDay astroday){

    	int i =0;
    	double R, Gg, G;
    	double tL, L;
    	double tB, B;
    	double X0, X1, X2, X3, X4;
    	double U, E0, E, lamda, V0, V;
    	double RAn, RAd, RA, DEC;
    	double B0sum=0, B1sum=0;
    	double R0sum=0, R1sum=0, R2sum=0, R3sum=0, R4sum=0;
    	double L0sum=0, L1sum=0, L2sum=0, L3sum=0, L4sum=0, L5sum=0;
    	double xsum=0, psi=0, epsilon=0;
    	double deltaPsi, deltaEps;
    	double lHour, SP;
    	double tU, tCos, tSin, tRA0 ,tRA ,tDEC;
    	double JC = (JD - 2451545)/36525.0;
    	double JM = JC/10.0 ;
    	double JM2 = Math.pow (JM, 2);
    	double JM3 = Math.pow (JM, 3);
    	double JM4 = Math.pow (JM, 4);
    	double JM5 = Math.pow (JM, 5);


    	for(i=0; i<= 63; i++)
    		L0sum += L0[i][0] * Math.cos(L0[i][1] + L0[i][2] * JM);
    	for(i=0; i<= 33; i++)
    		L1sum += L1[i][0] * Math.cos(L1[i][1] + L1[i][2] * JM);
    	for(i=0; i<= 19; i++)
    		L2sum += L2[i][0] * Math.cos(L2[i][1] + L2[i][2] * JM);
    	for(i=0; i<= 6; i++)
    		L3sum += L3[i][0] * Math.cos(L3[i][1] + L3[i][2] * JM);
    	for(i=0; i<= 2; i++)
    		L4sum += L4[i][0] * Math.cos(L4[i][1] + L4[i][2] * JM);
    	for(i=0; i<= 0; i++)
    		L5sum += L5[i][0] * Math.cos(L5[i][1] + L5[i][2] * JM);


    	tL = (L0sum + (L1sum * JM) + (L2sum * JM2)+ (L3sum * JM3) + (L4sum * JM4)
    			+ (L5sum * JM5)) / Math.pow (10, 8);

    	L = limitAngle(RAD_TO_DEG(tL));

    	for(i=0; i<5; i++)
    		B0sum += B0[i][0] * Math.cos(B0[i][1] + B0[i][2] * JM);
    	for(i=0; i<2; i++)
    		B1sum += B1[i][0] * Math.cos(B1[i][1] + B1[i][2] * JM);

    	tB= (B0sum + (B1sum * JM)) / Math.pow (10, 8);
    	B = RAD_TO_DEG(tB);

    	for(i=0; i < 40; i++)
    		R0sum += R0[i][0] * Math.cos(R0[i][1] + R0[i][2] * JM);
    	for(i=0; i < 10; i++)
    		R1sum += R1[i][0] * Math.cos(R1[i][1] + R1[i][2] * JM);
    	for(i=0; i < 6; i++)
    		R2sum += R2[i][0] * Math.cos(R2[i][1] + R2[i][2] * JM);
    	for(i=0; i < 2; i++)
    		R3sum += R3[i][0] * Math.cos(R3[i][1] + R3[i][2] * JM);
    	for(i=0; i < 1; i++)
    		R4sum += R4[i][0] * Math.cos(R4[i][1] + R4[i][2] * JM);

    	R = (R0sum + (R1sum * JM) + (R2sum * JM2)+ (R3sum * JM3) + (R4sum * JM4)) / Math.pow (10, 8);

    	G = limitAngle((L + 180));
    	Gg = -B;

    	X0 = 297.85036 + (445267.111480 * JC) -  (0.0019142 * Math.pow (JC, 2)) +
    			Math.pow (JC, 3)/189474.0;
    	X1 = 357.52772 + (35999.050340 * JC) -  (0.0001603 * Math.pow (JC, 2)) -
    			Math.pow (JC, 3)/300000.0;
    	X2 = 134.96298 + (477198.867398 * JC) +  (0.0086972 * Math.pow (JC, 2)) +
    			Math.pow (JC, 3)/56250.0;
    	X3 = 93.27191 + (483202.017538 * JC) -  ( 0.0036825 * Math.pow (JC, 2)) +
    			Math.pow (JC, 3)/327270.0;
    	X4 = 125.04452 - (1934.136261 * JC) + (0.0020708 * Math.pow (JC, 2)) +
    			Math.pow (JC, 3)/450000.0;

    	for (i=0; i<63; i++) {
    		xsum += X0*SINCOEFF[i][0];
    		xsum += X1*SINCOEFF[i][1];
    		xsum += X2*SINCOEFF[i][2];
    		xsum += X3*SINCOEFF[i][3];
    		xsum += X4*SINCOEFF[i][4];
    		psi     += (PE[i][0] + JC*PE[i][1])*Math.sin(DEG_TO_RAD(xsum));
    		epsilon += (PE[i][2] + JC*PE[i][3])*Math.cos(DEG_TO_RAD(xsum));
    		xsum=0;
    	}

    	deltaPsi = psi/36000000.0;
    	deltaEps = epsilon/36000000.0;

    	U = JM/10.0;
    	E0 = 84381.448 - 4680.93 * U - 1.55 * Math.pow(U,2) + 1999.25 * Math.pow(U,3)
    		- 51.38 * Math.pow(U,4)  - 249.67 * Math.pow(U,5) - 39.05 * Math.pow(U,6) + 7.12
    		* Math.pow(U,7) + 27.87 * Math.pow(U,8) + 5.79 * Math.pow(U,9) + 2.45 * Math.pow(U,10);
    	E = E0/3600.0 + deltaEps;
    	lamda = G + deltaPsi + (-20.4898/(3600.0 * R));

    	V0 = 280.46061837 + 360.98564736629 * ( JD - 2451545) +
    		0.000387933 * Math.pow(JC,2) - Math.pow(JC,3)/ 38710000.0;
    	V = limitAngle(V0) + deltaPsi * Math.cos(DEG_TO_RAD(E));

    	RAn = Math.sin(DEG_TO_RAD(lamda)) * Math.cos(DEG_TO_RAD(E)) -
    			Math.tan(DEG_TO_RAD(Gg)) * Math.sin(DEG_TO_RAD(E));
    	RAd = Math.cos(DEG_TO_RAD(lamda));
    	RA = limitAngle(RAD_TO_DEG(Math.atan2(RAn,RAd)));

    	DEC = Math.sin( Math.sin(DEG_TO_RAD(Gg)) * Math.cos(DEG_TO_RAD(E)) +
               Math.cos(DEG_TO_RAD(Gg)) * Math.sin(DEG_TO_RAD(E)) *
               Math.sin(DEG_TO_RAD(lamda)));


    	lHour = limitAngle(V + loc.getDegreeLong()- RA);

    	SP = 8.794/(3600 * R);

    	tU = Math.atan (0.99664719 * Math.tan(DEG_TO_RAD(loc.getDegreeLat())));

    	tCos = Math.cos(tU) + ( (loc.getSeaLevel())/6378140.0) *
    			Math.cos(DEG_TO_RAD(loc.getDegreeLat()));

    	tSin = 0.99664719 * Math.sin(tU) + ( loc.getSeaLevel()/6378140.0) *
    		Math.sin(DEG_TO_RAD(loc.getDegreeLat()));

    	tRA0 = (((-tCos) * Math.sin(DEG_TO_RAD(SP)) * Math.sin(DEG_TO_RAD(lHour)))
    				/ (Math.cos(DEC) - tCos * Math.sin(DEG_TO_RAD(SP)) * Math.cos(DEG_TO_RAD(lHour))));


    	tRA = RA +  RAD_TO_DEG(tRA0);


    	tDEC = RAD_TO_DEG(Math.atan2((Math.sin(DEC) - tSin * Math.sin(DEG_TO_RAD(SP))) * Math.cos(tRA0),
    			Math.cos(DEC) - tCos * Math.sin(DEG_TO_RAD(SP)) *
    			Math.cos(DEG_TO_RAD(lHour))));

    	astroday.setRa(tRA);
    	astroday.setDec(tDEC);
    	astroday.setSidTime(V);
    	astroday.setDra(tRA0);
    }

    double getThuhr(double lon, Astro astro)
    {

        double M, sidG;
        double ra0=astro.getRa(0), ra2=astro.getRa(2);
        double A, H;

        M = ((astro.getRa(1) - lon - astro.getSid(1)) / 360.0);
        M = limitAngle111(M);
        sidG =  astro.getSid(1) + 360.985647 * M;

        if (astro.getRa(1) > 350 && astro.getRa(2) < 10)
            ra2 += 360;
        if (astro.getRa(0) > 350 && astro.getRa(1) < 10)
            ra0 = 0;

        A = astro.getRa(1) + (M * ((astro.getRa(1) - ra0)
                                 + ( ra2 - astro.getRa(1)) +
                                 (( ra2 - astro.getRa(1)) -
                                  (astro.getRa(1) - ra0)) * M) / 2.0 );

        H =  limitAngle180between(sidG + lon - A);

        return  24.0 * (M - H/360.0);
    }

    double getAssr(double Lat, double dec, int mathhab)
    {
        double part1, part2, part3, part4;

        part1 = mathhab + Math.tan(DEG_TO_RAD(Lat) - dec);
        if (part1 < 1 || Lat < 0)
            part1 = mathhab - Math.tan(DEG_TO_RAD(Lat) - dec);

        part2 = (PI/2.0) - Math.atan(part1);
        part3 = Math.sin(part2) - Math.sin(DEG_TO_RAD(Lat)) * Math.sin(dec);
        part4 = (part3 / (Math.cos(DEG_TO_RAD(Lat)) * Math.cos(dec)));

    /*  if (part4 > 1) */
    /*      return 99; */

        return DEG_TO_10_BASE * RAD_TO_DEG (Math.acos(part4));
    }

    void base6hm(double bs, Location loc, Method conf, Prayer pt, int type){
   	double min, sec;

		if (bs == 99){
			pt.setHour(99);
			pt.setMinute(99);
			pt.setSecond(0);
			return;
		}

		/* Add offsets */
		if (conf.getOffset() == 1) {
			if (type == IMSAAK || type == NEXTFAJR)
				bs += (conf.getOffList(0) / 60.0);
			else
				bs += (conf.getOffList(type) / 60.0);
		}


		/* Fix after minus offsets before midnight */
		if (bs < 0) {
			while (bs < 0)
			    bs = 24 + bs;
		}

		min = (bs - Math.floor(bs)) * 60;
		sec = (min - Math.floor(min)) * 60;


		/* Add rounding minutes */
		if (conf.getRound() == 1)
		{
			if (sec >= DEFAULT_ROUND_SEC)
			    bs += 1/60.0;
			/* compute again */
			min = (bs - Math.floor(bs)) * 60;
			sec = 0;
		} else if (conf.getRound() == 2 || conf.getRound() == 3)
		{
			switch(type){
				case FAJR:
				case THUHR:
				case ASSR:
				case MAGHRIB:
				case ISHAA:
				case NEXTFAJR:

				    if (conf.getRound() == 2) {
				        if (sec >= DEFAULT_ROUND_SEC) {
				            bs += 1/60.0;
				            min = (bs - Math.floor(bs)) * 60;
				        }
				    } else if (conf.getRound() == 3)
				    {
				        if (sec >= AGGRESSIVE_ROUND_SEC) {
				            bs += 1/60.0;
				            min = (bs - Math.floor(bs)) * 60;
				        }
				    }
				    sec = 0;
				    break;

				case SHUROOQ:
				case IMSAAK:
				    sec = 0;
				    break;
			}
		}

		/* Add daylight saving time and fix after midnight times */
		bs += loc.getDst();
		if (bs >= 24)
	//		bs = fmod(bs, 24);
			bs = bs%24;

		pt.setHour((int)bs);
		pt.setMinute((int)min);
		pt.setSecond((int)sec);
    }

    public Prayer getImsaak (Location loc, Method conf, PrayerDate date){

		Method tmpConf;
		int lastDay;
		double julianDay;
		Prayer temp[];
		DayInfo dayInfo;

		temp = new Prayer[6];
		for(int j=0;j<6;j++){
			temp[j]= new Prayer();
		}
		tmpConf = conf.getMethod();

		if (conf.getFajrInv() != 0) {
			if (conf.getImsaakInv() == 0)
				tmpConf.setFajrInv(tmpConf.getFajrInv()+(int)DEF_IMSAAK_INTERVAL);
			else
				tmpConf.setFajrInv(tmpConf.getFajrInv()+conf.getImsaakInv());
		} else if (conf.getImsaakInv() != 0) {
		/* use an inv even if al-Fajr is computed (Indonesia?) */
			tmpConf.setOffList(0,tmpConf.getOffList(0)+(conf.getImsaakInv() * -1));
			tmpConf.setOffset(1);
		} else {
			tmpConf.setFajrAng(tmpConf.getFajrAng()+conf.getImsaakAng());
		}

		dayInfo = getDayInfo ( date, loc.getGmtDiff());

		temp = getPrayerTimesByDay( loc, tmpConf, dayInfo.getLastDay(),
									dayInfo.getJulianDay(),	IMSAAK);

		/* xxxthamer: We probably need to check whether it's possible to compute imsaak
		normally for some extreme methods first */
		/* In case of an extreme Fajr time calculation use intervals for Imsaak and
		* compute again */
		if (temp[0].getIsExtreme() != 0){
			tmpConf = conf.getMethod();
			if ( conf.getImsaakInv() == 0)
			{
			    tmpConf.setOffList(0,tmpConf.getOffList(0)-DEF_IMSAAK_INTERVAL);
			    tmpConf.setOffset(1);
			} else
			{
				tmpConf.setOffList(0,tmpConf.getOffList(0)-conf.getImsaakInv());
				tmpConf.setOffset(1);
		   	}
			temp = getPrayerTimesByDay(loc, tmpConf, dayInfo.getLastDay(),
										dayInfo.getJulianDay(),IMSAAK);
		}
		Prayer tPrayer;
		tPrayer = temp[0].getPrayer();
		return tPrayer;
	}

	public Prayer getNextDayImsaak (Location loc, Method conf, PrayerDate date){
	/* Copy the date structure and increment for next day.  The getImsaak
	* function will handle bad day values */
		Prayer temppt;
		PrayerDate tempd = date.getPrayerDate();

		tempd.setDay(tempd.getDay()+1);

		temppt = getImsaak (loc, conf, tempd);

		return temppt;

	}

	public Prayer getNextDayFajr (Location loc, Method conf, PrayerDate date){
		Prayer[] temp;
		int lastDay;
		double julianDay;
		DayInfo dayInfo;

		temp = new Prayer[6];
		for(int j=0;j<6;j++){
			temp[j]= new Prayer();
		}
		dayInfo = getDayInfo (date, loc.getGmtDiff());
		temp = getPrayerTimesByDay( loc, conf, dayInfo.getLastDay(),
									dayInfo.getJulianDay()+1, NEXTFAJR);

		return temp[0];
	}


	double getFajIsh(double Lat, double dec, double Ang)
	{
		double part1 = Math.cos(DEG_TO_RAD(Lat)) * Math.cos(dec);
		double part2 = -Math.sin(DEG_TO_RAD(Ang)) - Math.sin(DEG_TO_RAD(Lat))
		* Math.sin(dec);
		double part3 = part2 / part1;
		if ( part3 <= INVALID_TRIGGER)
		return 99;
		return DEG_TO_10_BASE * RAD_TO_DEG (Math.acos(part3) );
	}


	double getShoMag (Location loc, Astro astro, int type)
	{
		double lhour, M, sidG, ra0=astro.getRa(0), ra2=astro.getRa(2);
		double A, B, H, sunAlt, R, tH;

		double part1 = Math.sin(DEG_TO_RAD (loc.getDegreeLat()) * Math.sin(DEG_TO_RAD(astro.getDec(1))));
		double part2a =  CENTER_OF_SUN_ANGLE;
		double part2 = Math.sin (DEG_TO_RAD (part2a)) - part1;
		double part3 = Math.cos (DEG_TO_RAD (loc.getDegreeLat()) * Math.cos (DEG_TO_RAD(astro.getDec(1))));
		double part4 = part2 / part3;
		if (part4 <= -1 || part4 >= 1)
		return 99;

		lhour =  limitAngle180 (( RAD_TO_DEG (Math.acos (part4))));
		M = ((astro.getRa(1) - loc.getDegreeLong() - astro.getSid(1)) / 360.0);

		if (type ==  SHUROOQ)
		M = M - (lhour/360.0);
		if (type == MAGHRIB)
		M = M + (lhour/360.0);

		M = limitAngle111(M);

		sidG = limitAngle(astro.getSid(1) + 360.985647 * M);

		ra0 = astro.getRa(0);
		ra2 = astro.getRa(2);

		if (astro.getRa(1) > 350 && astro.getRa(2) < 10)
		ra2 += 360;
		if (astro.getRa(0) > 350 && astro.getRa(1) < 10)
		ra0 = 0;

		A = astro.getRa(1) + (M * (( astro.getRa(1) - ra0) +
		                     (ra2 - astro.getRa(1) ) +
		                     (( ra2 - astro.getRa(1) ) -
		                      ( astro.getRa(1)  -  ra0)) * M) / 2.0 );

		B = astro.getDec(1) + (M * ((astro.getDec(1) - astro.getDec(0)) +
		                      (astro.getDec(2) - astro.getDec(1)) +
		                      ((astro.getDec(2) - astro.getDec(1)) -
		                       (astro.getDec(1) - astro.getDec(0))) * M) / 2.0 );

		H =  limitAngle180between(sidG +  loc.getDegreeLong() -  A);

		tH =  H - RAD_TO_DEG(astro.getDra(1));

		sunAlt = RAD_TO_DEG(Math.asin (Math.sin(DEG_TO_RAD( loc.getDegreeLat())) *
							Math.sin(DEG_TO_RAD (B)) +
							Math.cos(DEG_TO_RAD( loc.getDegreeLat())) *
							Math.cos(DEG_TO_RAD (B)) * Math.cos(DEG_TO_RAD(tH)) ));

		sunAlt += getRefraction(loc, sunAlt);

		R = (M + (( sunAlt - CENTER_OF_SUN_ANGLE+ (ALTITUDE_REFRACTION *
		                                       Math.pow (loc.getSeaLevel(),0.5)))
		      /(360.0 * Math.cos(DEG_TO_RAD (B)) *
		      		Math.cos(DEG_TO_RAD( loc.getDegreeLat())) *
		                                       Math.sin(DEG_TO_RAD (tH)))));

		return  (R * 24.0);

	}

	double dms2Decimal(int deg, int min, double sec, char dir)
	{
	    double sum = deg + ((min/60.0)+(sec/3600.0));
	    if (dir == 'S' || dir == 'W' || dir == 's' || dir == 'w')
	        return sum * (-1.0);
	    return sum;
	}

	DMS decimal2Dms(double decimal){
	    double tempmin, tempsec, n1, n2;

	    tempmin = (decimal-Math.round(decimal)) * 60.0;
	    tempsec = (tempmin-Math.round(tempmin)) * 60.0;
	    DMS tDMS = new DMS();
	    tDMS.set((int)Math.round(decimal), (int) tempmin, (int)tempsec);
	    return tDMS;
	}


	double getNorthQibla(Location loc)	{
	    /* xxxthamer: reduce DEG_TO_RAD usage */
	    double num, denom;
	    num = Math.sin (DEG_TO_RAD (loc.getDegreeLong()) - DEG_TO_RAD (KAABA_LONG));
	    denom = (Math.cos (DEG_TO_RAD (loc.getDegreeLat())) *
	    		Math.tan (DEG_TO_RAD (KAABA_LAT))) -
				(Math.sin (DEG_TO_RAD (loc.getDegreeLat())) *
				((Math.cos ((DEG_TO_RAD (loc.getDegreeLong()) -
                DEG_TO_RAD(KAABA_LONG))))));
	    return RAD_TO_DEG (Math.atan2 (num, denom));
    }

	public class DMS {
		int deg, min, sec;
		public DMS (){deg=min=sec=0;}
		void set(int d,int m, int s){
			deg = d; min = m; sec = s;
		}
		int getDegree(){return deg;}
		int getMinute(){return deg;}
		int getSecond(){return deg;}
	}

	public class Astro {
		private double jd;
	    private double dec[];
	    private double ra[];
	    private double sid[];
	    private double dra[];

	    public Astro(){
	    	dec = new double[3];
	    	ra = new double[3];
	    	sid = new double[3];
	    	dra = new double[3];
	    }
	    double getJd(){return jd;}
	    double getDec(int i){return dec[i];}
	    double getRa(int i){return ra[i];}
	    double getSid(int i){return sid[i];}
	    double getDra(int i){return dra[i];}
	    void setJd(double aJd){jd = aJd;};
	    void setDec(int i, double aDec){dec[i]=aDec;}
	    void setRa(int i, double aRa){ra[i]=aRa;}
	    void setSid(int i, double aSid){sid[i]=aSid;}
	    void setDra(int i, double aDra){dra[i]=aDra;}
	    Astro getAstro(){	//clone the class
	    	Astro tAstro = new Astro();
	    	for(int i=0;i<dec.length;i++){
	    		tAstro.dec[i]=dec[i];
	    	}
	    	for(int i=0;i<ra.length;i++){
	    		tAstro.ra[i]=ra[i];
	    	}
	    	for(int i=0;i<sid.length;i++){
	    		tAstro.sid[i]=sid[i];
	    	}
	    	for(int i=0;i<dra.length;i++){
	    		tAstro.dra[i]=dra[i];
	    	}
	    	return tAstro;
	    }
	}

	public class AstroDay{
		private double dec;
		private double ra;
		private double sidtime;
		private double dra;

		public AstroDay(){
			dec=0;ra=0;sidtime=0;dra=0;
		}
		double getDec(){return dec;}
		double getRa(){return ra;}
		double getSidTime(){return sidtime;}
		double getDra(){return dra;}

		void setDec(double aDec){dec = aDec;}
		void setRa(double aRa){ra = aRa;}
		void setSidTime(double aSidTime){sidtime = aSidTime;}
		void setDra(double aDra){dra = aDra;}
	}

}