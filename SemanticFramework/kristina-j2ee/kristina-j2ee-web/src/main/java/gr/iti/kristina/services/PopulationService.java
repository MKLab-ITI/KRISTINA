package gr.iti.kristina.services;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("populate")
public class PopulationService {

	@EJB
	gr.iti.kristina.services.FusionServiceBean fusionService;

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON })
	public Response update(@FormParam("data") String data) {
		System.out.println("population data: " + data);
		// Gson gson = new Gson();
		// Observation[] observationObjects = gson.fromJson(observations,
		// Observation[].class);
		// fusionService.insert(observationObjects);
		return Response.ok() // 200
				.entity("true").header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
		// return gson.toJson(observationObjects);
	}

}
