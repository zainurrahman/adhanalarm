Adhan Alarm is an open source project.  Code is available at http://code.google.com/p/adhanalarm/

Introduction

In a busy world where there are many distractions, it would be nice if your cellphone could alert you when it's time to pray. Especially in western countries where the call to prayer is not publicly announced, this application can help Muslims establish prayer. The times for the five daily prayers are determined by the position of the sun, for example the noon prayer is established when the sun produces the shortest shadows of the day. Algorithms exist that can determine these times based on the latitude, longitude and a few other input parameters.

How To

Once you have installed the AdhanAlarm.apk file on your Android phone, start up the application.  First you will need to set up the program with your alarm, location and calculation preference settings.  These can be done by visiting the "Alert", "Place" and "Extra" tabs respectively.  Choose your settings and click "Save And Apply" for each.  Once settings are saved, you can view the "Today" tab which is updated to indicate the next prayer and alarm times as well as the direction to pray based on your location (i.e. Qibla).  The call to prayer will sound at prayer times (or beep which is the default behavior).  Also the Today panel will be updated to reflect next prayer times.

Known Bugs

The alarm does not sound if the application is open.  It only sounds when the application is minimized.  This is due to an issue with the SDK and can easily be fixed in the next SDK.  The onResume function of the Activity is called by an IntentReceiver which sets the NEW_TASK_LAUCH flag which does not reset the nextNotificationTime variable when the activity is already open.  If I could access the application from the IntentReceiver, I could do this in one line so I didn't bother hacking around this and would rather wait for the next SDK when a fix is promised.

A couple of the prayer times are inaccurate, this needs a fix on the calculation algorithm.