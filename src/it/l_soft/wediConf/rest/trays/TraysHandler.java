package it.l_soft.wediConf.rest.trays;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
	boolean useDoubleExtension = false;
	
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
				case "073737001":
					if (useExtension)
					{
						otherParts.get(i).setSelected(true);
					}
					break;
					
				case "073737002":
					if (useDoubleExtension)
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
		int profileLength = 1200;
		int profileTerminalLen = 1000;
		
		profiles = null;
		
		JsonObject jObj = null;
		try
		{
			jObj = jsonIn.getJsonObject("requestedSize");
			
			if (jObj.getInt("requestedLen") < 1200)
			{
				profileLength = 1200;
			}
			else if (jObj.getInt("requestedLen") < 1600)
			{
				profileLength = 1600;
			}
			else 
			{
				profileLength = 1800;
			}

			if ((jObj.getInt("requestedWidth") < 1000) ||
				(jObj.getInt("requestedWidth") > 1200))
			{
				profileTerminalLen = 1000;
			}
			else if (jObj.getInt("requestedWidth") > 2000)
			{
				profileTerminalLen = 1200;
			}
			else
			{
				profileTerminalLen = 1200;
			}
		}
		catch(Exception e)
		{
			return;
		}
		
		jObj = jsonIn.getJsonObject("profiles");
		if ((jsonValue = checkJsonAttribute(jObj, "tileHeight")) != null)
		{
			sql += "tileHeight = " + jsonValue + " AND (";
		}
		else
			return;

		if ((jsonValue = checkJsonAttribute(jObj, "est")) != null)
		{
			sql += or + "(length = " +  profileLength + " AND side = 'R'";
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
			sql += or + "(length = " + profileLength + " AND side = 'L'";
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

		sql += or + "(length = " + profileTerminalLen + " AND profileType = 'T')";

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
			log.trace("Getting trays from DB");
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
				jBuild.add("trayType", checkJsonAttribute(jsonIn, "trayType"));
				jBuild.add("thickness", checkJsonAttribute(jsonIn, "thickness"));
				
				if (checkJsonAttribute(jsonIn, "trayType").compareTo("L") == 0)
				{
					jBuild.add("WMin", checkJsonAttribute(jsonIn, "WMin"));
					jBuild.add("Width", checkJsonAttribute(jsonIn, "WMin"));
					if (Integer.parseInt(checkJsonAttribute(jsonIn, "LMin")) < 90)
					{
						jBuild.add("LMin", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "LMin"))));
						jBuild.add("Length", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "LMin"))));
					}
					else
					{
						jBuild.add("LMin", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "LMin")) - 600));
						jBuild.add("Length", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "LMin")) - 600));
						jBuild.add("draintype", "E");
					}
				}
				else
				{
					jBuild.add("Width", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "WMin")) - 1200));					
					jBuild.add("Length", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "LMin")) - 1200));					
				}
				JsonObject newJson = jBuild.build();				
				trays = Trays.findArticles(conn, traysGetWhereClause(newJson), 
										   Constants.getLanguageCode(language));
				if (checkJsonAttribute(jsonIn, "trayType").compareTo("P") == 0)
				{
					double sidesRatio = ((double)Integer.parseInt(checkJsonAttribute(jsonIn, "WMin"))) / 
										Integer.parseInt(checkJsonAttribute(jsonIn, "LMin"));
					ArrayList<Trays> toDelete = new ArrayList<Trays>();
					for(Trays tray : trays)
					{
						if (Math.abs((((double) tray.getWidth()) / tray.getLength()) - sidesRatio) > .1)
						{
							toDelete.add(tray);
						}
					}
					trays.removeAll(toDelete);
				}
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
		jsonResponse.put("useDoubleExtension", useDoubleExtension);
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
		if ((jsonValue = checkJsonAttribute(jsonIn, "useDoubleExtension")) != null)
		{
			useDoubleExtension = Boolean.parseBoolean(jsonValue);
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

	
	private Response generateOrderFileTXT(String token, JsonArray jsonIn) throws Exception
	{
		String filePath = context.getRealPath("/") + "/spool/orders/" + token;
    	FileWriter writer = new FileWriter(new File(filePath).getAbsolutePath());
    	BufferedWriter bw = new BufferedWriter(writer);
		bw.write(
				String.format("%-12.12s %-80.80s %-20.20s %6.6s\r\n",
					"Articolo",
					"Descrizione",
					"Dimensioni",
					"Prezzo")
		);
		for(int i = 0; i < jsonIn.asJsonArray().size(); i++)
		{
			JsonObject item = jsonIn.getJsonObject(i); 
			bw.write(
				String.format("%-12.12s %-80.80s %-20.20s %6.6s\r\n",
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
	}

	private Response generateOrderFileEXC(String token, JsonArray jsonIn) throws Exception
	{
    	XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ordini");
        Row row;
        Cell cell;
        
        String[] headers = {
				"Articolo",
				"Descrizione",
				"Dimensioni",
				"Prezzo"
		};
		int rowCount = 0;

		row = sheet.createRow(++rowCount);
		for (String header : headers)
		{
			int columnCount = 0;
			cell = row.createCell(++columnCount);
			cell.setCellValue(header);
		}

		for(int i = 0; i < jsonIn.asJsonArray().size(); i++)
		{
			int columnCount = 0;
			JsonObject item = jsonIn.getJsonObject(i); 
			row = sheet.createRow(++rowCount);
			cell = row.createCell(++columnCount);
			cell.setCellValue(item.getString("articleNumber"));
			cell = row.createCell(++columnCount);
			cell.setCellValue(item.getString("description"));
			cell = row.createCell(++columnCount);
			cell.setCellValue(item.getString("size"));
			cell = row.createCell(++columnCount);
			cell.setCellValue(item.getJsonNumber("price").doubleValue());             
		}

		String filePath = context.getRealPath("/") + "/spool/orders/" + token + ".xlsx";
		FileOutputStream outputStream = new FileOutputStream(filePath);
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();
		
        File fileDownload = new File(filePath);
        ResponseBuilder response = Response.ok((Object) fileDownload);
        response.header("Content-type", "application/vnd.ms-excel");
        response.header("Content-Disposition", "attachment; filename=" + filePath);
        return response.build();
    }

	private Response generateOrderFileCSV(String token, JsonArray jsonIn) throws Exception
	{
		String filePath = context.getRealPath("/") + "/spool/orders/" + token + ".csv";
    	FileWriter writer = new FileWriter(new File(filePath).getAbsolutePath());
    	BufferedWriter bw = new BufferedWriter(writer);
		bw.write(
				String.format("\"%s\",\"%s\",\"%s\",\"%s\"\r\n",
					"Articolo",
					"Descrizione",
					"Dimensioni",
					"Prezzo")
		);
		for(int i = 0; i < jsonIn.asJsonArray().size(); i++)
		{
			JsonObject item = jsonIn.getJsonObject(i); 
			bw.write(
				String.format("\"%s\",\"%s\",\"%s\",%6.6s\r\n",
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
        response.header("Content-type", "application/vnd.ms-excel");
        response.header("Content-Disposition", "attachment; filename=" + fileDownload.getName());
        return response.build();
	}
	
	@POST
	@Path("/order/{type}")
    @Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response dowloadOrder(String body, @PathParam("type") String type, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);
		String token = UUID.randomUUID().toString();
		
		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonArray jsonIn = jsonReader.readArray();
		jsonReader.close();


//			HashMap<String, Object> jsonResponse = new HashMap<>();
//			jsonResponse.put("link", "/spool/orders/" + token);
//			JsonHandler jh = new JsonHandler();
//			if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
//			{
//				return Response.status(Response.Status.UNAUTHORIZED)
//						.entity(jh.json).build();
//			}
//			return Response.status(Response.Status.OK).entity(jh.json).build();
		try 
		{
			switch(type)
			{
			case "txt":
				return generateOrderFileTXT(token, jsonIn);
			
			case "csv":
				return generateOrderFileCSV(token, jsonIn);
				
			case "exc":
				return generateOrderFileEXC(token, jsonIn);
				
			default:
				log.error("File type '" + type + "' is invalid");
				return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, null, languageId, "generic.execError");
			}
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
