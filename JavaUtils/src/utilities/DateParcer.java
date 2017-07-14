package utilities;

import java.util.Date;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateParcer {
	public static void main(String[] args) {
		System.out.println(parce("2017-11-11T13:05:23.001Z"));
	}
	public static String parce(String source) {		
		SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		originalFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat resultFormat = new SimpleDateFormat("yy-MM-ddHH:ss");
		resultFormat.setTimeZone(TimeZone.getTimeZone("GMT-4"));
		try {
			Date date = originalFormat.parse(source);
			return resultFormat.format(date);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}		
		
		return "persing error";
	}
}
