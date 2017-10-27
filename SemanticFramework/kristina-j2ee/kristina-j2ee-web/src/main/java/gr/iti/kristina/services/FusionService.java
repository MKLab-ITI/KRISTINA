package gr.iti.kristina.services;

import java.text.DateFormat;
import java.util.HashSet;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gr.iti.kristina.model.fusion.Observation;

@RequestScoped
@Path("fusion")
public class FusionService {

	@EJB
	gr.iti.kristina.services.FusionServiceBean fusionService;

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_PLAIN+ ";charset=utf-8")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response update(@FormParam("observations") String observations) {
		System.out.println("observations: " + observations);
		Gson gson = new Gson();
		Observation[] observationObjects = gson.fromJson(observations, Observation[].class);
		fusionService.insert(observationObjects);
		return Response.ok() //200
              .entity(gson.toJson(observationObjects))
              .header("Access-Control-Allow-Origin", "*")
              .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
              .build();
		//return gson.toJson(observationObjects);
	}
	
	@POST
	@Path("/updateOffline")
	@Produces(MediaType.TEXT_PLAIN+ ";charset=utf-8")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response updateOffline(@FormParam("observations") String observations) {
		System.out.println("http:// insert offline observations: " + observations);
		fusionService.clearOffline();
		Gson gson = new Gson();
		Observation[] observationObjects = gson.fromJson(observations, Observation[].class);
		fusionService.insert(observationObjects);
		return Response.ok() //200
              .entity(gson.toJson(observationObjects))
              .header("Access-Control-Allow-Origin", "*")
              .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
              .build();
		//return gson.toJson(observationObjects);
	}

	@GET
	@Path("/latest")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response get(@FormParam("interval") int interval) {
		HashSet<Observation> latest = fusionService.getLatest(interval);
		Gson gson = new Gson();
		return Response.ok() //200
	              .entity(gson.toJson(latest))
	              .header("Access-Control-Allow-Origin", "*")
	              .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
	              .build();
	}
	
	@GET
	@Path("/latestOffline")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response getOffline(@FormParam("interval") int interval) {
		HashSet<Observation> latest = fusionService.getOfflineLatest(interval);
		Gson gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+02:00'").create();
		String json = gson.toJson(latest);
		System.out.println("/latestOffline " + json);
		return Response.ok() //200
	              .entity(json)
	              .header("Access-Control-Allow-Origin", "*")
	              .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
	              .build();
	}
	
	@GET
	@Path("/clearOffline")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response clearOffline() {
		fusionService.clearOffline();
		return Response.ok() //200
	              .header("Access-Control-Allow-Origin", "*")
	              .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
	              .build();
	}


}
