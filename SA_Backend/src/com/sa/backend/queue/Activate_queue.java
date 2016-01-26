package com.sa.backend.queue;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("/activatequeue")
public class Activate_queue {

	@POST
	@Path("/{bool}")
	@Produces("application/json")
	public Response change_state(@PathParam("bool") boolean state)
	{		
		Database_queue.getInstance().setActivationState(state);
		
		JSONObject message = new JSONObject();
		String result = "";
		
		message.put("Success", "Changed ActivationState of Database queue to " + state);
		result = "" + message;
		return Response.status(200).entity(result).build();			
	}	
}