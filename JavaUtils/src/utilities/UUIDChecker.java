package utilities;

import java.util.regex.Matcher;  
import java.util.regex.Pattern; 

public class UUIDChecker {
	public static Boolean check(String uuid) {
		System.out.println("Method called");
		Pattern p = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
		Matcher m = p.matcher(uuid);
		
		return Boolean.valueOf(m.matches());
		
	}
}
