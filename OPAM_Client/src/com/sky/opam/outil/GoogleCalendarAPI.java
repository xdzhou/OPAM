package com.sky.opam.outil;

import java.util.Calendar;
import java.util.TimeZone;

import com.sky.opam.entity.Cours;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.widget.Toast;

@SuppressLint("NewApi")
public class GoogleCalendarAPI {
	private static String calanderURL = "";
	private static String calanderEventURL = "";
	private static String calanderRemiderURL = "";
	private String calId = "";
	private Context context;
	private String userName = "";
	private Boolean highSDK = false;

	static {
		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			calanderURL = "content://com.android.calendar/calendars";
			calanderEventURL = "content://com.android.calendar/events";
			calanderRemiderURL = "content://com.android.calendar/reminders";

		} else {
			calanderURL = "content://calendar/calendars";
			calanderEventURL = "content://calendar/events";
			calanderRemiderURL = "content://calendar/reminders";
		}
	}

	public GoogleCalendarAPI(Context context) {
		if (Integer.parseInt(Build.VERSION.SDK) >= 14) highSDK = true;
		this.context = context;

		Cursor userCursor;
		if(highSDK) userCursor = context.getContentResolver().query(Calendars.CONTENT_URI, null, null, null, null);
		else userCursor = context.getContentResolver().query(Uri.parse(calanderURL), null, null, null, null);
		for (userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor
				.moveToNext()) {
			userName = userCursor.getString(userCursor.getColumnIndex("name"));
			if (userName.contains("@gmail.com")) {
				calId = userCursor.getString(userCursor.getColumnIndex("_id"));
				break;
			}
		}
		userCursor.close();
	}

	@SuppressLint("SimpleDateFormat")
	public long addCourse2Calendar(Cours c) {
		if(calId.equals("") || userName.equals("")) return 0;
		
		ContentValues event = new ContentValues();
		event.put("calendar_id", calId);
		event.put("title", c.getCalendarTitle());
		if (!c.salle.equals(""))
			event.put("eventLocation", "room:" + c.salle);
		event.put("eventTimezone", "Europe/Paris");
		event.put("description", c.getCalendarDescription());
		if (Integer.parseInt(Build.VERSION.SDK) < 14)
			event.put("visibility", 0);
		// if(c.type.contains("CF1")) event.put("eventColor",Color.RED);
		// if(c.type.contains("CF2")) event.put("eventColor",Color.YELLOW);

		int numweek, numday, hour, min, year;
		String nums[];

		nums = c.debut.split(":");
		hour = Integer.parseInt(nums[0]);
		min = Integer.parseInt(nums[1]);

		nums = c.position.split("_");
		numweek = Integer.parseInt(nums[0]);
		numday = Integer.parseInt(nums[1]);

		Calendar calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		calendar.clear();
		calendar.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, min);
		calendar.set(Calendar.WEEK_OF_YEAR, numweek);
		calendar.set(Calendar.DAY_OF_WEEK, numday + 1);
		event.put("dtstart", calendar.getTime().getTime());

		nums = c.fin.split(":");
		hour = Integer.parseInt(nums[0]);
		min = Integer.parseInt(nums[1]);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, min);
		event.put("dtend", calendar.getTime().getTime());
		calendar = null;
		event.put("hasAlarm", 1);

		Uri newEvent;
		if(highSDK) newEvent = context.getContentResolver().insert(Events.CONTENT_URI, event);
		else newEvent = context.getContentResolver().insert(Uri.parse(calanderEventURL), event);
		
		long id = Long.parseLong(newEvent.getLastPathSegment());
		ContentValues values = new ContentValues();
		values.put("event_id", id);
		values.put("minutes", 15);
		values.put("method", 1); // Alert(1), Email(2), SMS(3)

		if(highSDK) context.getContentResolver().insert(Reminders.CONTENT_URI,values);
		else context.getContentResolver().insert(Uri.parse(calanderRemiderURL),values);

		values = new ContentValues();
		values.put("event_id", id);
		values.put("minutes", 10);
		values.put("method", 3); // Alert(1), Email(2), SMS(3)
		if(highSDK) context.getContentResolver().insert(Reminders.CONTENT_URI,values);
		else context.getContentResolver().insert(Uri.parse(calanderRemiderURL),values);

		return id;
	}

	public void delEvent(long eventid) {
		//Uri eventUri = ContentUris.withAppendedId(Uri.parse(calanderEventURL),eventid);
		Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI,eventid);
		context.getContentResolver().delete(eventUri, null, null);
	}

	public void showInfo(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

	}

}
