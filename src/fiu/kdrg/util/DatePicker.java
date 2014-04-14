package fiu.kdrg.util;

import java.util.Date;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

public class DatePicker {

	
	public static LocalDate getNearestDayOfWeekBefore(LocalDate t0, int dow) {
		
		LocalDate t1 = t0.withDayOfWeek(dow);
		if(t1.isAfter(t0)){
			return t1.minusWeeks(1);
		}
		
		return t1;
	}
	
	public static LocalDate getNearestDayOfWeekBefore(LocalDate t0, String dow) {
		return getNearestDayOfWeekBefore(t0, t0.dayOfWeek().setCopy(dow).getDayOfWeek());
	}
	
	
	public static void main(String[] args) {
		
		LocalDate today = new LocalDate(2013, 10, 1);
		int dow = DateTimeConstants.THURSDAY;
		System.out.println(getNearestDayOfWeekBefore(today, 1));
		
		Date dt = new Date(1313470800000l);
		System.out.println(dt.toLocaleString());
		
	}
}
