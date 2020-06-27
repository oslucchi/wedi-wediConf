package it.l_soft.wediConf.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class LanguageResources {
	final static Logger log = Logger.getLogger(LanguageResources.class);
	private static Properties[] resource = new Properties[5];
	private static LanguageResources singletonInstance = null;
	private static int languageCode = Constants.LNG_IT;

	private LanguageResources()
	{
    	try 
    	{
        	InputStream in = LanguageResources.class.getResourceAsStream("/resources/it.properties");
        	resource[Constants.LNG_IT] = new Properties();
        	resource[Constants.LNG_IT].load(in);
	    	in.close();
        	in = LanguageResources.class.getResourceAsStream("/resources/en.properties");
        	resource[Constants.LNG_EN] = new Properties();
        	resource[Constants.LNG_EN].load(in);
        	resource[Constants.LNG_DE] = new Properties();
        	resource[Constants.LNG_DE].load(in);
        	resource[Constants.LNG_FR] = new Properties();
        	resource[Constants.LNG_FR].load(in);
	    	in.close();
		}
    	catch(IOException e) 
    	{
    		log.warn("Exception " + e.getMessage(), e);    		
    		return;
		}
	}
	
	public static String getResource(String errCode)
	{
		if (singletonInstance == null)
		{
			singletonInstance = new LanguageResources();
		}
		return(resource[languageCode].getProperty(errCode, "Unknown error"));
	}

	public static String getResource(int language, String errCode)
	{
		if (singletonInstance == null)
		{
			singletonInstance = new LanguageResources();
		}
		return(resource[language].getProperty(errCode, "Unknown error"));
	}

	public static void setLanguageCode(int languageCode) 
	{
		LanguageResources.languageCode = languageCode;
	}
}
