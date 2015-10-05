package core;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
	private static final SimpleDateFormat GMDate = new SimpleDateFormat("MM/dd/yyyy hh:mm aa zzz");
	static {
		GMDate.setTimeZone(TimeZone.getTimeZone("EST"));
	}
	public static String getChatTimestamp(long unixtimestamp) {
		return GMDate.format(new Date(unixtimestamp * 1000));
	}
}
