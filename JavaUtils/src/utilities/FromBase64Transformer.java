package utilities;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class FromBase64Transformer {
	public static void main(String[] args) {
		
		transform("0J/QvtC30LTRgNCw0LLQu9GP0Y4hINCS0Ysg0LHRgNC+0LrQtdGA0YnQuNC6IDgwLdCz0L4g0YPRgNC+0LLQvdGPIQ==");
	}
	
	
	public static String transform(String original) {
		byte[] array = Base64.decodeBase64(original.getBytes());
		String result;
		try {
			result = new String(array, "UTF-8");
			//System.out.println(result);
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return "Error occured while decoding base64 String";
	}
}
