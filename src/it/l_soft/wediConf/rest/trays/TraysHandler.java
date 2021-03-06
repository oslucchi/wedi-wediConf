package it.l_soft.wediConf.rest.trays;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;

import it.l_soft.wediConf.rest.ApplicationProperties;
import it.l_soft.wediConf.rest.Constants;
import it.l_soft.wediConf.rest.JsonHandler;
import it.l_soft.wediConf.rest.Utils;
import it.l_soft.wediConf.rest.dbUtils.DBConnection;
import it.l_soft.wediConf.rest.dbUtils.DBInterface;
import it.l_soft.wediConf.rest.dbUtils.Drains;
import it.l_soft.wediConf.rest.dbUtils.Grids;
import it.l_soft.wediConf.rest.dbUtils.OtherParts;
import it.l_soft.wediConf.rest.dbUtils.Profiles;
import it.l_soft.wediConf.rest.dbUtils.Trays;
import it.l_soft.wediConf.utils.JavaJSONMapper;

@Path("/trays")
public class TraysHandler {
	@Context
	private ServletContext context;
	
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Trays t = null;
	String contextPath = null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	ArrayList<Grids> grids= null;
	ArrayList<Drains> drains= null;
	ArrayList<OtherParts> otherParts= null;
	ArrayList<Profiles> profiles= null;

	boolean useExtension = false;
	
	DBConnection conn = null;

	private String checkJsonAttribute(JsonObject jsonIn, String attribute)
	{
		String retVal = null;
		try
		{
			retVal = jsonIn.getString(attribute);
		}
		catch(Exception e)
		{
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	private void getGrids(String trayArticleNumber, String language)
	{
		String sql = "";
		try 
		{
			log.trace("Getting grids from DB");
			sql = "SELECT a.* " +
				  "FROM grids a INNER JOIN trayGridPair b ON ( " +
				  "	  b.gridArticleNumber = a.articleNumber " +
				  ") " +
				  "WHERE b.trayArticleNumber = '" + trayArticleNumber + "'";
			grids = (ArrayList<Grids>) Grids.populateCollection(conn, sql, false, Grids.class);
		}
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Grids.getGrids with language " + language, e);
		}
	}

	@SuppressWarnings("unchecked")
	private void getDrains(String trayArticleNumber, String language)
	{
		String sql = "";
		try 
		{
			conn = DBInterface.connect();
			log.trace("Getting drains from DB");
			sql = "SELECT a.* " +
					  "FROM drains a INNER JOIN trayDrainPair b ON ( " +
					  "	  b.drainArticleNumber = a.articleNumber " +
					  ") " +
					  "WHERE b.trayArticleNumber = '" + trayArticleNumber + "'";
			drains = (ArrayList<Drains>) Drains.populateCollection(conn, sql, false, Drains.class);
		}
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Drain.populateCollection with language " + language, e);
		}
	}

	@SuppressWarnings("unchecked")
	private void getOtherParts(String trayArticleNumber, String language)
	{
		String sql = "";
		try 
		{
			log.trace("Getting otherParts from DB");
			sql = "SELECT a.* " +
					  "FROM otherParts a INNER JOIN trayOtherPair b ON ( " +
					  "	  b.otherArticleNumber = a.articleNumber " +
					  ") " +
					  "WHERE b.trayArticleNumber = '" + trayArticleNumber + "'";
			otherParts = (ArrayList<OtherParts>) OtherParts.populateCollection(conn, sql, false, OtherParts.class);
			for(int i = 0; i < otherParts.size(); i++)
			{
				switch(otherParts.get(i).getArticleNumber())
				{
				case "073796000":
					otherParts.get(i).setSelected(true);
					break;
					
				case "073737003":
				case "073737002":
				case "073737001":
					if (useExtension)
					{
						otherParts.get(i).setSelected(true);
					}
					break;
				}
			}
		}
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Drain.populateCollection with language " + language, e);
		}
	}

	@SuppressWarnings("unchecked")
	private void getProfiles(JsonObject jsonIn, String language)
	{
		String jsonValue;
		String sql = "SELECT * " +
					 "FROM profiles " +
					 "WHERE ";
		String or = "";
		
		profiles = null;
		if (checkJsonAttribute(jsonIn, "tray") == null)
			return;
		
		JsonObject jObj = null;
		Trays tray = null;
		try
		{
			System.out.println(jsonIn.getJsonString("tray").getString());
			jObj = JavaJSONMapper.StringToJSON(jsonIn.getJsonString("tray").getString());
			tray = (Trays) JavaJSONMapper.JSONToJava(jObj, Trays.class);
		}
		catch(Exception e)
		{
			return;
		}
		
		jObj = JavaJSONMapper.StringToJSON(jsonIn.getJsonString("profiles").getString());
		if ((jsonValue = checkJsonAttribute(jObj, "tileHeight")) != null)
		{
			sql += "tileHeight = " + jsonValue + " AND (";
		}
		else
			return;

		if ((jsonValue = checkJsonAttribute(jObj, "est")) != null)
		{
			sql += or + "(length >= " + tray.getLength() + " AND side = 'L'";
			switch(jsonValue)
			{
			case "wall":
				sql += " AND profileType = 'W')";
				break;

			case "floor":
				sql += " AND profileType = 'F')";
				break;
			}
			or = " OR ";
		}

		if ((jsonValue = checkJsonAttribute(jObj, "west")) != null)
		{
			sql += or + "(length >= " + tray.getLength() + " AND side = 'R'";
			switch(jsonValue)
			{
			case "wall":
				sql += " AND profileType = 'W')";
				break;

			case "floor":
				sql += " AND profileType = 'F')";
				break;
			}
			or = " OR ";
		}
		sql += ")";
		sql += or + "(length >= " + tray.getWidth() + " AND profileType = 'T')";

		try 
		{
			log.trace("Getting profiles from DB");
			profiles = (ArrayList<Profiles>) Profiles.populateCollection(conn, sql, false, Profiles.class);
		}
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Profiles.populateCollection with language " + language, e);
		}
	}

	private String traysGetWhereClause(JsonObject jsonIn)
	{
		String where = "";
		String and = "";
		String jsonValue;
		
		if ((jsonValue = checkJsonAttribute(jsonIn, "trayType")) != null)
		{
			where += and + " trayType = '" + jsonValue + "'";
			and = " AND ";
		}
		if ((jsonValue = checkJsonAttribute(jsonIn, "WMin")) != null)
		{
			where += and + " widthMin <= " + jsonValue;
			and = " AND ";
		}
		if ((jsonValue = checkJsonAttribute(jsonIn, "LMin")) != null)
		{
			where += and + " lengthMin <= " + jsonValue;
			and = " AND ";
		}
		if ((jsonValue = checkJsonAttribute(jsonIn, "Width")) != null)
		{
			where += and + "width >= " + jsonValue;
			and = " AND ";
		}
		else
		{
			where += and + "width >= " + checkJsonAttribute(jsonIn, "WMin");
			and = " AND ";
		}
		if ((jsonValue = checkJsonAttribute(jsonIn, "Length")) != null)
		{
			where += and + "length >= " + jsonValue;
			and = " AND ";
		}
		else
		{
			where += and + "length >= " + checkJsonAttribute(jsonIn, "LMin");
			and = " AND ";
		}

		if ((jsonValue = checkJsonAttribute(jsonIn, "thickness")) != null)
		{
			where += and + " thickness <= " + jsonValue;
			and = " AND ";
			if (Integer.valueOf(jsonValue) < 100)
			{
				where += " AND drainType = 'I'";
			}
		}
		
		if ((jsonValue = checkJsonAttribute(jsonIn, "draintype")) != null)
		{
			where += and + " draintype = '" + jsonValue + "'";
			and = " AND ";
		}

		if (where.trim().compareTo("WHERE") == 0)
		{
			where = "";
		}
		where += " ORDER BY width, length ASC";

		return(where);
	}
	
	
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchBySize(String body, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonObject jsonIn = jsonReader.readObject();
		jsonReader.close();
		
		ArrayList<Trays> trays= null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			log.trace("Getting trace from DB");
			trays = Trays.findArticles(conn, traysGetWhereClause(jsonIn), 
									   Constants.getLanguageCode(language));
			if ((trays == null) || (trays.size() == 0))
			{
				useExtension = true;
				log.trace("No record on these conditions '" +
						  checkJsonAttribute(jsonIn, "trayType") + "' - " +
						  checkJsonAttribute(jsonIn, "WMin") + "' - " +
						  checkJsonAttribute(jsonIn, "LMin"));
				log.trace("softening constraints using prolongues");

				JsonObjectBuilder jBuild = Json.createObjectBuilder();
				jBuild.add("trayType", checkJsonAttribute(jsonIn, "trayType"))
					  .add("Length", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "LMin")) - 600))
					  .add("draintype", "E");
				if (checkJsonAttribute(jsonIn, "trayType").compareTo("L") == 0)
				{
					jBuild.add("WMin", checkJsonAttribute(jsonIn, "WMin"));
				}
				else
				{
					jBuild.add("Width", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "WMin")) - 600));					
				}
				JsonObject newJson = jBuild.build();				
				trays = Trays.findArticles(conn, traysGetWhereClause(newJson), 
										   Constants.getLanguageCode(language));
			}
			log.trace("Retrieval completed");
			DBInterface.disconnect(conn);
		}
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Trays.findArticles with language " + language, e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		// No record found based on the current arguments. Try considering the panel extension
		if (trays == null)
		{
			log.trace("No records found");
			return Response.status(Response.Status.OK).entity("{}").build();
		}
		
		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("trays", trays);
		jsonResponse.put("useExtension", useExtension);
		JsonHandler jh = new JsonHandler();
		if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
	
	@POST
	@Path("/parts")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getTrayParts(String body, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);
		String jsonValue;

		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonObject jsonIn = jsonReader.readObject();
		jsonReader.close();

		String trayArticleNumber = "";
		if ((jsonValue = checkJsonAttribute(jsonIn, "trayArticleNumber")) != null)
		{
			trayArticleNumber = jsonValue;
		}
		else
		{
			return Utils.jsonizeResponse(Response.Status.BAD_REQUEST, null, languageId, "generic.execError");
		}

		if ((jsonValue = checkJsonAttribute(jsonIn, "useExtension")) != null)
		{
			useExtension = Boolean.parseBoolean(jsonValue);
		}
		try
		{
			conn = DBInterface.connect();
			getGrids(trayArticleNumber, language);
			getOtherParts(trayArticleNumber, language);
			getDrains(trayArticleNumber, language);
			getProfiles(jsonIn, language);
			DBInterface.disconnect(conn);
		}
		catch(Exception e)
		{
			log.error("Exception '" + e.getMessage() + "' on Trays.findArticles with language " + language, e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		
		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("grids", grids);
		jsonResponse.put("otherParts", otherParts);
		jsonResponse.put("drains", drains);
		jsonResponse.put("profiles", profiles);
		JsonHandler jh = new JsonHandler();
		if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
	
	@POST
	@Path("/order")
    @Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response dowloadOrder(String body, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);
		String token = UUID.randomUUID().toString();
		
		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonArray jsonIn = jsonReader.readArray();
		jsonReader.close();

		try 
		{
			String filePath = context.getRealPath("/") + "/spool/orders/" + token;
        	FileWriter writer = new FileWriter(new File(filePath).getAbsolutePath());
        	BufferedWriter bw = new BufferedWriter(writer);
			bw.write(
					String.format("%-12.12s %-80.80s %-20.20s %6.6s\n",
						"Articolo",
						"Descrizione",
						"Dimensioni",
						"Prezzo")
			);
			for(int i = 0; i < jsonIn.asJsonArray().size(); i++)
			{
				JsonObject item = jsonIn.getJsonObject(i); 
				bw.write(
					String.format("%-12.12s %-80.80s %-20.20s %6.6s\n",
						item.getString("articleNumber"),
						item.getString("description"),
						item.getString("size"),
						item.getJsonNumber("price"))
				);
			}
			bw.flush();
			bw.close();
			writer.close();
	        File fileDownload = new File(filePath);
	        ResponseBuilder response = Response.ok((Object) fileDownload);
	        response.header("Content-Disposition", "attachment; filename=" + fileDownload.getName());
	        return response.build();

//			HashMap<String, Object> jsonResponse = new HashMap<>();
//			jsonResponse.put("link", "/spool/orders/" + token);
//			JsonHandler jh = new JsonHandler();
//			if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
//			{
//				return Response.status(Response.Status.UNAUTHORIZED)
//						.entity(jh.json).build();
//			}
//			return Response.status(Response.Status.OK).entity(jh.json).build();

		}
		catch(Exception e)
		{
			log.error("Exception '" + e.getMessage() + "' on Trays.findArticles with language " + language, e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
	}

    @GET
    @Path("/download/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response downloadFileWithGet(@QueryParam("file") String file) {
        File fileDownload = new File(context.getRealPath("/") + file);
        ResponseBuilder response = Response.ok((Object) fileDownload);
        response.header("Content-Disposition", "attachment; filename=" + fileDownload.getName());
        return response.build();
    }
}
