package it.l_soft.wediConf.utils;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerationException;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class JavaJSONMapper {
	public static void JavaToJSON(Object object, Class<?> objClass)
	{
	}
	
	public static JsonObject StringToJSON(String jsonString)
	{
		JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
		JsonObject jObj = jsonReader.readObject();
		jsonReader.close();
		return jObj; 
	}
	
	public static Object JSONToJava(JsonObject jsonIn, Class<?> objClass)
	{
		Object object = null;
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			//	    	  PrintWriter out = new PrintWriter("./tmpjson");
			//	    	  out.println(jsonIn.toString());
			//	    	  out.close();
			//		         object =  mapper.readValue(new File("./tmpjson"), objectClass);
			object =  mapper.readValue(jsonIn.toString(), objClass);
		} catch (JsonGenerationException e)
		{
			e.printStackTrace();
		} catch (JsonMappingException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return(object);
	}
}
