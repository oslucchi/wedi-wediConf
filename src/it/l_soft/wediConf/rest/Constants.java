package it.l_soft.wediConf.rest;

public class Constants {
	public static final String STATUS_ACTIVE = "A";
	public static final String STATUS_COMPLETED = "C";
	public static final String STATUS_D = "D";
	public static final String STATUS_EXPIRED = "E";
	public static final String STATUS_PENDING_APPROVAL = "P";
	public static final String STATUS_REJECTED = "R";
	public static final String STATUS_PENDING_CONFIRM_TO_USER = "S";
	public static final String STATUS_WAITING_FOR_TRANSACTION = "W";

	public static final int LNG_IT	= 1;
	public static final int LNG_EN	= 2;
	public static final int LNG_FR	= 3;
	public static final int LNG_DE	= 4;
	public static final int LNG_ES	= 5;
	public static final String LOCALE_IT = "it_IT";
	public static final String LOCALE_EN = "en_US";
	public static final String LOCALE_FR = "fr_FR";
	public static final String LOCALE_DE = "de_DE";
	public static final String LOCALE_ES = "es_ES";
	
	public static int getLanguageCode(String language)
	{
		switch(language.substring(0,2).toUpperCase())
		{
		case "IT":
			return(LNG_IT);

		case "EN":
			return(LNG_EN);

		case "FR":
			return(LNG_FR);

		case "DE":
			return(LNG_DE);

		case "ES":
			return(LNG_ES);

		default:
			return(LNG_EN);
		}
	}
	
	public static int getAlternativeLanguage(String language)
	{
		switch(language.substring(0,2).toUpperCase())
		{
		case "IT":
			return(LNG_EN);

		case "EN":
			return(LNG_IT);

		case "FR":
			return(LNG_IT);

		case "DE":
			return(LNG_IT);

		case "ES":
			return(LNG_IT);

		default:
			return(LNG_IT);
		}
	}

	public static int getAlternativeLanguage(int language)
	{
		switch(language)
		{
		case LNG_IT:
			return(LNG_EN);

		case LNG_EN:
			return(LNG_IT);

		case LNG_FR:
			return(LNG_IT);

		case LNG_DE:
			return(LNG_IT);

		case LNG_ES:
			return(LNG_IT);

		default:
			return(LNG_IT);
		}
	}

	public static String getLocale(int languageId)
	{
		switch(languageId)
		{
		case LNG_IT:
			return(LOCALE_IT);

		case LNG_EN:
			return(LOCALE_EN);

		case LNG_FR:
			return(LOCALE_FR);

		case LNG_DE:
			return(LOCALE_DE);

		case LNG_ES:
			return(LOCALE_ES);

		default:
			return(LOCALE_EN);
		}
	}

}
