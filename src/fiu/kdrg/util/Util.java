package fiu.kdrg.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;



public class Util {
	
	
	public static String rootDir = "/home/zhouwubai/U/workplace/HurricaneStoryline/events/";
//	public static String rootDir = "/home/users/wzhou005/workspace/HurricaneStoryline/";
	
    /**
     * change Date format Aug 12, 2005 to millionseconds
     * @param eventInfo
     * @return
     * @throws ParseException 
     */
	public static Long parseDate2Milionseconds(String date) throws ParseException{
		SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy", Locale.US);
		String strMillionseconds = "";
		
			Date eventDate = formatter.parse(date);
			strMillionseconds = Long.toString(eventDate.getTime(), 10);
		
		return Long.parseLong(strMillionseconds);
	}
	
	
	
	/**
	 * To test a string str whether it can be matched by regular expressions regex
	 *  It will return match string.
	 * If regular expression is not matched, put that element null
	 * @param str
	 * @param regexs
	 * @return
	 */
	public static  String extractStringByRE(String str, String regexs)
	{
		if(null == regexs)
			return null;
		
		String matchedStrings;
		
			Pattern pattern = Pattern.compile(regexs);
			Matcher matcher = pattern.matcher(str);
			
			if(matcher.find())
			{
				matchedStrings = matcher.group().trim();
			}
			else
			{
				matchedStrings = null;
			}
			
		return matchedStrings;
	}
	
	
	public static void main(String[] args) {
		
		String str = "friday mon friday 2007 12";
		String match = extractStringByRE(str, EventUtil.WEEKDAY_REGEX);
		
		System.out.println(match);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		LocalDate ld = null;
		
		try	{
			ld = formatter.parseLocalDate("26/08/2012");
		}catch(IllegalArgumentException ex) {
			
		}
		System.out.println(ld.getMonthOfYear());
	}
	
	
}



