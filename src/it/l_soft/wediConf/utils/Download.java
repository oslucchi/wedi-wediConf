package it.l_soft.wediConf.utils;

import java.io.StringReader;
import java.util.HashMap;
//import java.util.UUID;

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

import it.l_soft.wediConf.rest.JsonHandler;
import it.l_soft.wediConf.rest.Utils;

@Path("/download")
public class Download {
	@Context
	private ServletContext context;
	
	final Logger log = Logger.getLogger(this.getClass());

	@POST
	@Path("/order")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchBySize(String body, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);
//		String token = UUID.randomUUID().toString();
		
		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonObject jsonIn = jsonReader.readObject();
		jsonReader.close();
		String order = "";
		try 
		{
//        	FileWriter writer = new FileWriter("/resources/orders/" + token);
//        	BufferedWriter bw = new BufferedWriter(writer);
			for(int i = 0; i < jsonIn.asJsonArray().size(); i++)
			{
				JsonObject item = jsonIn.asJsonArray().getJsonObject(i); 
//				bw.write(
				order += String.format("%12.12s %45.45s %15.15s %6.6s\n",
						item.getString("articleNumber"),
						item.getString("description"),
						item.getString("size"),
						item.getString("price"));
//				);
			}
//			bw.flush();
//			bw.close();
//	        File fileDownload = new File("/resources/orders/" + token);
//	        ResponseBuilder response = Response.ok((Object) fileDownload);
//	        response.header("Content-Disposition", "attachment;filename=" + fileDownload.getPath());
//	        return response.build();

			HashMap<String, Object> jsonResponse = new HashMap<>();
			jsonResponse.put("order", order);
			JsonHandler jh = new JsonHandler();
			if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
			{
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(jh.json).build();
			}
			return Response.status(Response.Status.OK).entity(jh.json).build();

		}
		catch(Exception e)
		{
			log.error("Exception '" + e.getMessage() + "' on Trays.findArticles with language " + language, e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
	}
}
