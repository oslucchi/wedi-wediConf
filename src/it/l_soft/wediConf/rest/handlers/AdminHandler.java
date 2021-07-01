package it.l_soft.wediConf.rest.handlers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.l_soft.wediConf.rest.ApplicationProperties;
import it.l_soft.wediConf.rest.JsonHandler;
import it.l_soft.wediConf.rest.Utils;
import it.l_soft.wediConf.rest.dbUtils.DBConnection;
import it.l_soft.wediConf.rest.dbUtils.DBInterface;
import it.l_soft.wediConf.rest.dbUtils.JournalSearches;
import it.l_soft.wediConf.rest.dbUtils.JournalSessions;
import it.l_soft.wediConf.rest.dbUtils.User;
import it.l_soft.wediConf.utils.JavaJSONMapper;

@Path("/admin")
public class AdminHandler {
	@Context
	private ServletContext context;

	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());

	DBConnection conn = null;


	@SuppressWarnings("unchecked")
	@POST
	@Path("/users")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registeredUsers(String body, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonObject jsonIn = jsonReader.readObject();
		jsonReader.close();
		
		DBConnection conn = null;
		User user = (User) JavaJSONMapper.JSONToJava(jsonIn.getJsonObject("user"), User.class);
		ArrayList<User> registeredusers = new ArrayList<User>();
		try 
		{
			conn = DBInterface.connect();
			//Getting the user who made the request. If the token does not exists, register it to noUser.
			log.trace("Getting the user");
			user = User.getUserByToken(conn, user.getToken());
			if ((user == null) || (user.getRole() != User.ROLE_ADMIN))
			{
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null,languageId, "generic.execError");
			}
			registeredusers = (ArrayList<User>) User.populateCollection(conn, "SELECT * FROM users " +
																			  "WHERE email is not NULL AND email != \"\" ", User.class);
		}
		catch(Exception e)
		{
			log.error("Exception '" + e.getMessage() + "' on Trays.findArticles with language " + language, e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("users", registeredusers);
		
		JsonHandler jh = new JsonHandler();
		if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/sessions")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response userSessions(String body, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonObject jsonIn = jsonReader.readObject();
		jsonReader.close();
		
		DBConnection conn = null;
		User currentUser = (User) JavaJSONMapper.JSONToJava(jsonIn.getJsonObject("currentUser"), User.class);
		User sessionsOf = (User) JavaJSONMapper.JSONToJava(jsonIn.getJsonObject("sessionsOf"), User.class);
		ArrayList<JournalSessions> userSessions = null;
		try 
		{
			conn = DBInterface.connect();
			//Getting the user who made the request. If the token does not exists, register it to noUser.
			log.trace("Getting the user");
			currentUser = User.getUserByToken(conn, currentUser.getToken());
			if ((currentUser == null) || (currentUser.getRole() != User.ROLE_ADMIN))
			{
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null,languageId, "generic.execError");
			}
			userSessions = (ArrayList<JournalSessions>)
								JournalSessions.populateCollection(conn, 
																   "SELECT * FROM journalSessions " +
																   "WHERE idUser = " + sessionsOf.getIdUser() + " " +
//																   "GROUP BY sessionId " +
																   "ORDER BY timestamp", JournalSessions.class);
		}
		catch(Exception e)
		{
			log.error("Exception '" + e.getMessage() + "' on Trays.findArticles with language " + language, e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("sessions", userSessions);
		
		JsonHandler jh = new JsonHandler();
		if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/queries")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sessionQueries(String body, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonObject jsonIn = jsonReader.readObject();
		jsonReader.close();
		
		DBConnection conn = null;
		JournalSessions js = (JournalSessions) JavaJSONMapper.JSONToJava(jsonIn.getJsonObject("session"), JournalSessions.class);
		User currentUser = (User) JavaJSONMapper.JSONToJava(jsonIn.getJsonObject("currentUser"), User.class);
		ArrayList<JournalSearches> sessionSearches = null;
		try 
		{
			conn = DBInterface.connect();
			//Getting the user who made the request. If the token does not exists, register it to noUser.
			log.trace("Getting the user");
			if ((currentUser == null) || (currentUser.getRole() != User.ROLE_ADMIN))
			{
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null,languageId, "generic.execError");
			}
			sessionSearches = (ArrayList<JournalSearches>)
								JournalSearches.populateCollection(conn, 
																   "SELECT * FROM journalSearches " +
																   "WHERE session = '" + js.getSessionId() + "' " +
																   "ORDER BY timestamp", JournalSearches.class);
		}
		catch(Exception e)
		{
			log.error("Exception '" + e.getMessage() + "' on Trays.findArticles with language " + language, e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("queries", sessionSearches);
		
		JsonHandler jh = new JsonHandler();
		if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
}
