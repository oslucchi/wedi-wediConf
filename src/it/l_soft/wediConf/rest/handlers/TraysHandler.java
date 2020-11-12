package it.l_soft.wediConf.rest.handlers;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import it.l_soft.wediConf.rest.ApplicationProperties;
import it.l_soft.wediConf.rest.Constants;
import it.l_soft.wediConf.rest.JsonHandler;
import it.l_soft.wediConf.rest.Utils;
import it.l_soft.wediConf.rest.dbUtils.DBConnection;
import it.l_soft.wediConf.rest.dbUtils.DBInterface;
import it.l_soft.wediConf.rest.dbUtils.Drains;
import it.l_soft.wediConf.rest.dbUtils.Grids;
import it.l_soft.wediConf.rest.dbUtils.JournalProposals;
import it.l_soft.wediConf.rest.dbUtils.JournalSearches;
import it.l_soft.wediConf.rest.dbUtils.OtherParts;
import it.l_soft.wediConf.rest.dbUtils.Profiles;
import it.l_soft.wediConf.rest.dbUtils.Trays;
import it.l_soft.wediConf.rest.dbUtils.User;
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
	boolean useDoubleExtension = false;

	DBConnection conn = null;

	private String checkJsonAttribute(JsonObject jsonIn, String attribute)
	{
		String retVal = null;
		try
		{
			switch(jsonIn.get(attribute).getValueType())
			{
			case STRING:
				return jsonIn.getJsonString(attribute).getString();

			case NUMBER:
				double d = jsonIn.getJsonNumber(attribute).doubleValue();
				retVal = String.valueOf(d);
			default:
				break;
			}
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
	private void getDrains(String trayArticleNumber, int trayThickness, int creedThickness, String language)
	{
		String sql = "";
		if (creedThickness == 200)
		{
			try 
			{
				conn = DBInterface.connect();
				log.trace("Getting drains from DB");
				sql = "SELECT a.* " +
						"FROM drains a INNER JOIN trayDrainPair b ON ( " +
						"	  b.drainArticleNumber = a.articleNumber " +
						") " +
						"WHERE b.trayArticleNumber = '" + trayArticleNumber + "' AND " +
						"      b.active = 1";
				//"      a.height + " + trayThickness + " <= " + creedThickness;
				drains = (ArrayList<Drains>) Drains.populateCollection(conn, sql, false, Drains.class);
				for(Drains item : drains)
				{
					if ((item.getArticleNumber().compareTo("077500003") == 0) ||
							(item.getArticleNumber().compareTo("077000015") == 0))
					{
						item.setSelected(true);
					}
				}
			}
			catch(Exception e) 
			{
				log.error("Exception '" + e.getMessage() + "' on Drain.populateCollection with language " + language, e);
			}
		}
		else
		{
			drains = new ArrayList<Drains>(); 
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
					"WHERE b.trayArticleNumber = '" + trayArticleNumber + "' AND " +
					"      b.active = 1";
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

		JsonObject jsonProfiles = jsonIn.getJsonObject("profiles");
		try
		{
			if (jsonIn.getInt("lMin") <= 1200)
			{
				profileLength = 1200;
			}
			else if (jsonIn.getInt("lMin") <= 1600)
			{
				profileLength = 1600;
			}
			else 
			{
				profileLength = 1800;
			}

			if ((jsonIn.getInt("wMin") < 1000) ||
					(jsonIn.getInt("wMin") > 1200))
			{
				profileTerminalLen = 1000;
			}
			else if (jsonIn.getInt("wMin") > 2000)
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

		if ((jsonValue = checkJsonAttribute(jsonProfiles, "tileHeight")) != null)
		{
			sql += "tileHeight = " + jsonValue + " AND (";
		}
		else
			return;

		if ((jsonValue = checkJsonAttribute(jsonProfiles, "est")) != null)
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

		if ((jsonValue = checkJsonAttribute(jsonProfiles, "west")) != null)
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
		if ((jsonValue = checkJsonAttribute(jsonIn, "wMin")) != null)
		{
			where += and + " widthMin <= " + jsonValue;
			and = " AND ";
		}
		if ((jsonValue = checkJsonAttribute(jsonIn, "lMin")) != null)
		{
			where += and + " lengthMin <= " + jsonValue ;
			and = " AND ";
		}

		// If no matches on the first round, recall the search with different sizes to check
		// id any by using prolongues
		if ((jsonValue = checkJsonAttribute(jsonIn, "width")) != null)
		{
			where += and + "width >= " + jsonValue;
			and = " AND ";
		}
		else
		{
			where += and + "width >= " + checkJsonAttribute(jsonIn, "wMin");
			and = " AND ";
		}
		if ((jsonValue = checkJsonAttribute(jsonIn, "length")) != null)
		{
			where += and + "length >= " + jsonValue;
			and = " AND ";
		}
		else
		{
			where += and + "length >= " + checkJsonAttribute(jsonIn, "lMin");
			and = " AND ";
		}

		if ((jsonValue = checkJsonAttribute(jsonIn, "screedThickness")) != null)
		{
			switch(Double.valueOf(jsonValue).intValue())
			{
			case 99:
				where += and + " drainType = 'I'";
				break;
			case 200:
				where += and + " drainType = 'E'";
			}
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
		User user = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			//Getting the user who made the request. If the token does not exists, register it to noUser.
			log.trace("Getting the user");
			String token = jsonIn.getJsonObject("user").getString("token");
			user = User.getUserByToken(conn, token);

			if (user.getEmail().compareTo(jsonIn.getJsonObject("user").getString("email")) != 0)
			{
				user.setEmail(jsonIn.getJsonObject("user").getString("email"));
				user.setFirstName(jsonIn.getJsonObject("user").getString("firstname"));
				user.setLastName(jsonIn.getJsonObject("user").getString("lastname"));
				user.setOrganization(jsonIn.getJsonObject("user").getString("organization"));
				if (user.getIdUser() == 0)
				{
					user.setIdUser(user.insertAndReturnId(conn, "idUser", user));
				}
				else
				{
					user.update(conn, "idUser");
				}		
			}

			String session = jsonIn.getString("session");
			JournalSearches js = new JournalSearches();
			js.setSession(session);
			js.setTimestamp(new Date());
			js.setSearchCriteria(jsonIn.getJsonObject("searchCriteria").toString());
			js.insert(conn, "idJournalSearches", js);

			log.trace("Getting trays from DB");
			trays = Trays.findArticles(conn, traysGetWhereClause(jsonIn.getJsonObject("searchCriteria")), 
					Constants.getLanguageCode(language));
			if ((trays == null) || (trays.size() == 0))
			{
				useExtension = true;
				log.trace("No record on these conditions '" +
						checkJsonAttribute(jsonIn, "trayType") + "' - " +
						checkJsonAttribute(jsonIn, "wMin") + "' - " +
						checkJsonAttribute(jsonIn, "lMin"));
				log.trace("softening constraints using prolongues");

				JsonObjectBuilder jBuild = Json.createObjectBuilder();
				jBuild.add("trayType", checkJsonAttribute(jsonIn, "trayType"));
				jBuild.add("screedThickness", checkJsonAttribute(jsonIn.getJsonObject("searchCriteria"), "screedThickness"));

				if (checkJsonAttribute(jsonIn, "trayType").compareTo("L") == 0)
				{
					jBuild.add("wMin", checkJsonAttribute(jsonIn, "wMin"));
					jBuild.add("width", checkJsonAttribute(jsonIn, "wMin"));
					if (Integer.parseInt(checkJsonAttribute(jsonIn, "lMin")) < 90)
					{
						jBuild.add("lMin", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "lMin"))));
						jBuild.add("length", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "lMin"))));
					}
					else
					{
						jBuild.add("length", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "lMin")) - 600));
					}
				}
				else
				{
					jBuild.add("width", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "wMin")) - 1200));					
					jBuild.add("length", String.valueOf(Integer.parseInt(checkJsonAttribute(jsonIn, "lMin")) - 1200));					
				}
				JsonObject newJson = jBuild.build();				
				trays = Trays.findArticles(conn, traysGetWhereClause(newJson), 
						Constants.getLanguageCode(language));

				if (checkJsonAttribute(jsonIn, "trayType").compareTo("P") == 0)
				{
					double sidesRatio = ((double)Integer.parseInt(checkJsonAttribute(jsonIn, "wMin"))) / 
							Integer.parseInt(checkJsonAttribute(jsonIn, "lMin"));
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
		jsonResponse.put("user", user);
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
		
		Trays tray = (Trays) JavaJSONMapper.JSONToJava(jsonIn.getJsonObject("tray"), Trays.class);
		JsonObject searchCriteria = jsonIn.getJsonObject("searchCriteria");
		
		int screedHeight = 200;
		try 
		{
			screedHeight = searchCriteria.getInt("screedThickness", 200);
		}
		catch(Exception e)
		{
			return Utils.jsonizeResponse(Response.Status.BAD_REQUEST, e, languageId, "generic.execError");
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
			getGrids(tray.getArticleNumber(), language);
			getOtherParts(tray.getArticleNumber(), language);
			getDrains(tray.getArticleNumber(), tray.getThickness(), screedHeight, language);
			getProfiles(searchCriteria, language);
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

	private Response generateOrderFileTXT(String token, JsonArray orderList, String reference) throws Exception
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
		for(int i = 0; i < orderList.asJsonArray().size(); i++)
		{
			JsonObject item = orderList.getJsonObject(i);
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
		response.header("Content-type", "text/plain");
		response.header("Content-Disposition", "attachment; filename=" + token + ".txt");
		return response.build();
	}

	private XSSFWorkbook createExcel(JsonArray orderList) throws Exception
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
		int columnCount = 0;

		row = sheet.createRow(rowCount++);
		for (int i = 0; i < headers.length; i++)
		{
			cell = row.createCell(i);
			cell.setCellValue(headers[i]);
		}

		for(int i = 0; i < orderList.asJsonArray().size(); i++)
		{
			columnCount = 0;
			JsonObject item = orderList.getJsonObject(i); 
			row = sheet.createRow(rowCount++);
			cell = row.createCell(columnCount++);
			cell.setCellValue(item.getString("articleNumber"));
			cell = row.createCell(columnCount++);
			cell.setCellValue(item.getString("description"));
			cell = row.createCell(columnCount++);
			cell.setCellValue(item.getString("size"));
			cell = row.createCell(columnCount++);
			cell.setCellValue(item.getJsonNumber("price").doubleValue());             
		}

		columnCount = 0;
		for (int i = 0; i < headers.length; i++)
		{
			sheet.autoSizeColumn(columnCount++);
		}
		return workbook;
	}


	private Response generateOrderFileEXC(String token, JsonArray orderList, String reference) throws Exception
	{
		//    	XSSFWorkbook workbook = new XSSFWorkbook();
		//        Sheet sheet = workbook.createSheet("ordini");
		//        Row row;
		//        Cell cell;
		//        
		//        String[] headers = {
		//				"Articolo",
		//				"Descrizione",
		//				"Dimensioni",
		//				"Prezzo"
		//		};
		//        
		//		int rowCount = 0;
		//		int columnCount = 0;
		//
		//		row = sheet.createRow(rowCount++);
		//		for (int i = 0; i < headers.length; i++)
		//		{
		//			cell = row.createCell(i);
		//			cell.setCellValue(headers[i]);
		//		}
		//
		//		for(int i = 0; i < jsonIn.asJsonArray().size(); i++)
		//		{
		//			columnCount = 0;
		//			JsonObject item = jsonIn.getJsonObject(i); 
		//			row = sheet.createRow(rowCount++);
		//			cell = row.createCell(columnCount++);
		//			cell.setCellValue(item.getString("articleNumber"));
		//			cell = row.createCell(columnCount++);
		//			cell.setCellValue(item.getString("description"));
		//			cell = row.createCell(columnCount++);
		//			cell.setCellValue(item.getString("size"));
		//			cell = row.createCell(columnCount++);
		//			cell.setCellValue(item.getJsonNumber("price").doubleValue());             
		//		}
		//
		//		columnCount = 0;
		//		for (int i = 0; i < headers.length; i++)
		//		{
		//			sheet.autoSizeColumn(columnCount++);
		//		}

		XSSFWorkbook workbook = createExcel(orderList);

		ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
		byte[] byteRpt = null;

		workbook.write(excelOutput);
		byteRpt = excelOutput.toByteArray();
		excelOutput.close();
		ResponseBuilder response = Response.ok((Object) byteRpt);

		String filePath = context.getRealPath("/") + "/spool/orders/" + token;
		FileOutputStream outputStream = new FileOutputStream(filePath);
		workbook.write(outputStream);
		outputStream.close();
		workbook.close();

		response.header("Content-type", "application/vnd.ms-excel");
		response.header("Content-Disposition", "attachment; filename=" + token + ".xlsx");


		return response.build();
	}

	private Response generateOrderFileCSV(String token, JsonArray orderList, String reference) throws Exception
	{
		String filePath = context.getRealPath("/") + "/spool/orders/" + token;
		FileWriter writer = new FileWriter(new File(filePath).getAbsolutePath());
		BufferedWriter bw = new BufferedWriter(writer);
		bw.write(
				String.format("\"%s\",\"%s\",\"%s\",\"%s\"\r\n",
						"Articolo",
						"Descrizione",
						"Dimensioni",
						"Prezzo")
				);
		for(int i = 0; i < orderList.asJsonArray().size(); i++)
		{
			JsonObject item = orderList.getJsonObject(i);
			bw.write(
					String.format("\"=\"\"%s\"\"\",\"%s\",\"%s\",%6.6s\r\n",
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
		response.header("Content-type", "text/csv");
		response.header("Content-Disposition", "attachment; filename=" + token + ".csv");
		return response.build();
	}

	private Response generateOrderFileDOC(String token, JsonArray orderList) throws Exception
	{
		JsonObject item = orderList.getJsonObject(0); 
		String zipFile = context.getRealPath("/") + "/spool/techDetails/" + token + ".zip";
		String articleNumber = item.getString("articleNumber");

		String[] srcFiles = {
				"IstruzioniMontaggio.pdf",
				"IstruzioniMontaggioScarico.pdf",
				"IstruzioniCanalinaPiastrellabile.pdf",
				"IstruzioniCanalina.pdf",
				"DN40.pdf",
				"DN50.pdf",
				articleNumber + ".png",
				articleNumber + ".pdf",
				articleNumber + ".dwg"
		};

		// create byte buffer
		byte[] buffer = new byte[1024];
		FileOutputStream fos = new FileOutputStream(zipFile); 
		ZipOutputStream zos = new ZipOutputStream(fos);
		for (String srcFile : srcFiles) 
		{
			srcFile = context.getRealPath("/") + "/assets/productDetails/" +
					(item.getString("trayType").compareTo("P") == 0 ? "Primo" : "Riolito") + "/" + srcFile;
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(srcFile);
			}
			catch(Exception e)
			{
				continue;
			}

			// begin writing a new ZIP entry, positions the stream to the start of the entry data
			File zipEntry = new File(srcFile);
			zos.putNextEntry(new ZipEntry(zipEntry.getName()));
			int length;
			while ((length = fis.read(buffer)) > 0)
			{
				zos.write(buffer, 0, length);
			}
			zos.closeEntry();
			// close the InputStream
			fis.close();
		}
		// close the ZipOutputStream
		zos.close();
		fos.close();

		File fileDownload = new File(zipFile);
		ResponseBuilder response = Response.ok((Object) fileDownload);
		response.header("Content-type", "application/zip");
		response.header("Content-Disposition", "attachment; filename=Documentazione.zip");
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
		JsonObject jsonIn = jsonReader.readObject();
		jsonReader.close();
		
		JsonArray orderList = jsonIn.getJsonArray("order");

		JournalProposals jp;
		try 
		{
			conn = DBInterface.connect();
			jp = new JournalProposals();
			jp.setOrderObject(orderList.toString());
			jp.setReference(jsonIn.getString("reference"));
			jp.setSession(jsonIn.getString("session"));
			jp.insert(conn, "idJournalProposals", jp);
			DBInterface.disconnect(conn);
			
			String reference = jp.getReference();
			reference.replaceAll(" ", "");
			
			switch(type)
			{
			case "txt":
				return generateOrderFileTXT(token, orderList, jp.getReference());

			case "csv":
				return generateOrderFileCSV(token, orderList, jp.getReference());

			case "exc":
				return generateOrderFileEXC(token, orderList, jp.getReference());

			case "doc":
				return generateOrderFileDOC(token, orderList);

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
