package fiu.kdrg.test;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonTest {
	public static void main(String[] args) throws JSONException {
		
		String jsonContent = "{'hello':'world','abc':'xyz'}";
		JSONObject jsonObject = new JSONObject(jsonContent);
		
		String str1 = jsonObject.getString("hello");
		String str2 = jsonObject.getString("abc");
		
		System.out.println(str1 +":" +  str2);
		
	}
}
