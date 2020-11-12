package it.l_soft.wediConf.rest.handlers;

import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import it.l_soft.wediConf.rest.dbUtils.JournalSessions;
import it.l_soft.wediConf.rest.dbUtils.User;
import it.l_soft.wediConf.utils.JavaJSONMapper;
import it.l_soft.wediConf.utils.Mailer;

@Path("/user")
public class UsersHandler {
	@Context
	private ServletContext context;

	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());

	DBConnection conn = null;


	@POST
	@Path("/search")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchUserByToken(String body, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonObject jsonIn = jsonReader.readObject();
		jsonReader.close();
		
		DBConnection conn = null;
		User user = (User) JavaJSONMapper.JSONToJava(jsonIn.getJsonObject("user"), User.class);
		JournalSessions js = null;
		try 
		{
			conn = DBInterface.connect();
			//Getting the user who made the request. If the token does not exists, register it to noUser.
			log.trace("Getting the user");
			user = User.getUserByToken(conn, user.getToken());

			js = JournalSessions.getBySessionId(conn, jsonIn.getString("session"));
			if (js.getIdJournalSessions() == 0)
			{
				js.setTimestamp(new Date());
				js.setIdUser(user.getIdUser());
				js.setSession(UUID.randomUUID().toString());
				js.insert(conn, "idJournalSessions", js);
			}
			DBInterface.disconnect(conn);
		}
		catch(Exception e)
		{
			log.error("Exception '" + e.getMessage() + "' on Trays.findArticles with language " + language, e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("user", user);
		jsonResponse.put("session", js.getSession());
		
		JsonHandler jh = new JsonHandler();
		if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
	
	@POST
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getTrayParts(String body, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonObject jsonIn = jsonReader.readObject();
		jsonReader.close();
		
		User user = null;
		try 
		{
			conn = DBInterface.connect();
			user = (User) JavaJSONMapper.JSONToJava(jsonIn.getJsonObject("user"), User.class);
			user.update(conn, "idUser");
			String mailBody = 
				"<html>" + 
				"<head></head>" + 
				"<body>" + 
				"    <div style='font-family: Courier;'>" + 
				"	    <p>" + 
				"	        <span>" + 
				"		        Cliccare su questo link per attivare la funzione di scarico dei documenti dal configuratore wedi" + 
				"		        </br>" + 
				"				<a>http://it-configurator.wedi.eu/wediConfTest/restcall/user/confirm/" + user.getToken() + "</a>" +
				"	        </span>" + 
				"	    </p>" + 
				"	    <span>" + 
				"	        Cordiali saluti<br>" + 
				"	        --<br>" + 
				"	        Wedi Italia Srl<br>" + 
				"	        via Vimercate 44<br>" + 
				"	        20876 Ornago (MB)<br>" + 
				"	        Thp: 039 245 9420" + 
				"	    </span>" + 
				"	</div>" + 
				"	<br>" + 
				"	<br>" + 
				"	<b>Mail inviata automaticamente. Non rispondere a questa mail. L'indirizzo mittente non &egrave; presidiato.</b>" + 
				"</body>" + 
				"</html>";
			
			Mailer.sendMail(user.getEmail(), mailBody);
		}
		catch(Exception e)
		{
			DBInterface.disconnect(conn);
			log.error("Exception '" + e.getMessage() + "' sending confirmation email", e);		
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}

		return Response.status(Response.Status.OK)
					   .entity("{ \"message\" : \"Mail per la abilitazione spedita a '" + user.getEmail() + "'.\" }").build();
	}	

	@GET
	@Path("/confirm/{token}")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response confirmUser(@PathParam("token") String token, 
								@HeaderParam("Language") String language)
	{
		DBConnection conn = null;
		User user = null;
		try 
		{
			conn = DBInterface.connect();
			//Getting the user who made the request. If the token does not exists, register it to noUser.
			log.trace("Getting the user");
			user = User.getUserByToken(conn, token);
			user.setActive(true);
			user.update(conn, "idUser");
			DBInterface.disconnect(conn);
		}
		catch(Exception e)
		{
			log.error("Exception '" + e.getMessage() + "' on Trays.findArticles with language " + language, e);
			
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						   .entity("{ \"error\" : \"Il link per l'attivazione dell'utente non e' valido. Contattare il supporto wedi.\" }")
						   .build();
		}

		return Response.status(Response.Status.OK)
					   .entity("<html><head></head><body>" +
							   "<span>L'utente '" + user.getEmail() + "' &egrave; da questo momento abilitato a scaricare documenti dal configuratore wedi.</H1>" +
							   "</body></html>")
					   .build();

	}	

}
