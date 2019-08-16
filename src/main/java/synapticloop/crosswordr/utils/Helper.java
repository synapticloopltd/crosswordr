package synapticloop.crosswordr.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Helper {
	public static long getDateDiff(Date dateFrom, Date dateTo, TimeUnit timeUnit) {
		long diffInMillies = dateTo.getTime() - dateFrom.getTime();
		return timeUnit.convert(diffInMillies,TimeUnit.DAYS);
	}
}
