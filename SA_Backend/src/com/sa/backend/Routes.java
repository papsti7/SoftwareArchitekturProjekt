package com.sa.backend;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sa.backend.helper.DBConnector;
import com.sa.backend.queue.Database_queue;

@Path("/routes")
public class Routes {
	
	@GET
	@Path("/getstations/{id}")
	@Produces("application/json")
	public Response getConnectionStations(
			@PathParam("id") double RouteID
			) throws SQLException {
						
		String query = "SELECT public_transport_stops.id, public_transport_stops.name, public_transport_stops.lon, public_transport_stops.lat "
						+"FROM public_transport_stops "
						+"INNER JOIN station_mapping "
						+"ON public_transport_stops.id = station_mapping.stop_id "
						+"WHERE station_mapping.route_id = ? ";
		
		//Create connection to db and create a statement
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement statement = connection.prepareStatement(query);
		
		statement.setBigDecimal(1, BigDecimal.valueOf(RouteID));

		//Execute the query
		ResultSet res = statement.executeQuery();

		JSONObject ret = new JSONObject();
		JSONArray array = new JSONArray();
		
		//Iterate over the result of the query
		while (res.next()) {
			JSONObject json_obj = new JSONObject();
			json_obj.put("id", res.getLong("id"));
			json_obj.put("name", res.getString("name"));
			json_obj.put("lon", res.getDouble("lon"));
			json_obj.put("lat", res.getDouble("lat"));
			
			array.put(json_obj);
		}
		
		ret.put("Content", array);
		//Send the result to the client
		return Response.status(200).entity(ret.toString()).build();
	}	
	
	@GET
	@Path("/getconnections/{firstStop}/{secondStop}")
	@Produces("application/json")
	public Response getConnections(
		@PathParam("firstStop") String firstStop,
		@PathParam("secondStop") String secondStop
			) throws SQLException {
				
		//Setup the query string
		String query = "SELECT routes.route_id, routes.name, A.oname, A.vname FROM routes INNER JOIN"
	+ "(SELECT o.route_id, public_transport_stops.name AS oname, o.stop_id, c.stop_id AS vstop, v.name AS vname FROM  "
	+ "	station_mapping AS o INNER JOIN public_transport_stops ON public_transport_stops.id = o.stop_id, "
	+ "	station_mapping AS c INNER JOIN public_transport_stops AS v ON v.id = c.stop_id "
	+ "	WHERE (o.route_id = c.route_id)"
	+ "		AND (LOWER(public_transport_stops.name) LIKE ?)"
	+ "		AND (LOWER(v.name) LIKE ? )"
	+ "		AND (o.map_id < c.map_id))"
	+ "		  A ON routes.route_id = A.route_id	"
	+ "		 GROUP BY routes.route_id, A.oname, A.vname;";
		
		//Create connection to db and create a statement
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement statement = connection.prepareStatement(query);
		
		firstStop = firstStop.toLowerCase();
		secondStop = secondStop.toLowerCase();
		
		statement.setString(1, firstStop+"%");
		statement.setString(2, secondStop+"%");
		
		//Execute the query
		ResultSet res = statement.executeQuery();
		
		JSONObject ret = new JSONObject();
		JSONArray array = new JSONArray();
	
		//Iterate over the result of the query
		while (res.next()) {
			JSONObject json_obj = new JSONObject();
			json_obj.put("route_id", res.getDouble("route_id"));
			json_obj.put("name", res.getString("name"));
			json_obj.put("oname", res.getString("oname"));
			json_obj.put("vname", res.getString("vname"));
			
			array.put(json_obj);
		}
		
		ret.put("Content", array);
		
		//Send the result to the client
		return Response.status(200).entity(ret.toString()).build();
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Response addRoute(String input) throws SQLException {
		JSONObject newRoute = new JSONObject(input);
		
		String name = newRoute.getString("name");
		
//		System.out.println("NAME OF NEW ROUTE: " + name);
//		System.out.println(newRoute.toString());
		
		long route_id = insertNewRoute(name);

		
		JSONArray stations  = newRoute.getJSONArray("stations");
		boolean result = addStationsToRoute(route_id, stations);
		
		
		JSONObject message = new JSONObject();
		if(result)
			message.put("Success", "Added new route!");
		else
			message.put("Error", "Adding new route failed!");
		
		return Response.status(200).entity(message.toString()).build();
	}
	
	@PUT
	@Path("/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response editRoute(
		@PathParam("id") double RouteID,
		String body) throws SQLException 
	{			
		JSONObject json_body_obj = new JSONObject(body);
		
		JSONObject message = new JSONObject();

		if(json_body_obj.length() != 1 || !(json_body_obj.has("name") && json_body_obj.get("name") instanceof String))
		{
			message.put("Error", "Check the sent json!");
			return Response.status(200).entity(message.toString()).build();		
		}
		
		String put_query = "UPDATE routes SET name = ? " 
				+ "WHERE(route_id = ?)";
		
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement statement = connection.prepareStatement(put_query);

		statement.setString(1, json_body_obj.getString("name"));
		statement.setBigDecimal(2, BigDecimal.valueOf(RouteID));
		
		if(Database_queue.getInstance().getActivationState())
		{
			Database_queue.getInstance().addStatement(statement);
			
			message.put("Success", "Added Statement to database queue!");
			return Response.status(200).entity(message.toString()).build();
		}
		else
		{
			int res = statement.executeUpdate();
			
			if(res == 0)
			{
				message.put("error", "Modifying route name failed!");
				return Response.status(400).entity(message.toString()).build();
			}
			else
			{
				message.put("Success", "Modified route name");
				return Response.status(200).entity(message.toString()).build();			
			}
		}
	}
	
	@DELETE
	@Path("byName/{name}")
	@Produces("application/json")
	public Response deleteRoute(
		@PathParam("name") String RouteName
			) throws SQLException 
	{	
		//we could also give the lon and lat in the request instead of this DB query. Just a matter of design which we will choose later.
		String delete_connection = "DELETE FROM routes WHERE(name = ?)";
		
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement connection_statement = connection.prepareStatement(delete_connection);
		
		connection_statement.setString(1, RouteName);
		
		JSONObject message = new JSONObject();
		String result ="";
	
		if(Database_queue.getInstance().getActivationState())
		{
			Database_queue.getInstance().addStatement(connection_statement);
			
			message.put("Success", "Added Statement to database queue!");
			result = "" + message;
			return Response.status(200).entity(result).build();
		}
		else
		{
			int res_connection = connection_statement.executeUpdate();
			
			
			if(res_connection == 0)
			{
				message.put("error", "Deleting failed!");
				result = "" + message;
				return Response.status(400).entity(result).build();
			}
			else
			{
				message.put("Success", "Deleted route");
				result = "" + message;
				return Response.status(200).entity(result).build();			
			}
		}
	}
	
	@DELETE
	@Path("/{id}")
	@Produces("application/json")
	public Response deleteRoute(
		@PathParam("id") double ConnectionID
			) throws SQLException 
	{	
		//we could also give the lon and lat in the request instead of this DB query. Just a matter of design which we will choose later.
		String delete_connection = "DELETE FROM routes WHERE(route_id = ?)";
		
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement connection_statement = connection.prepareStatement(delete_connection);
		
		connection_statement.setBigDecimal(1, BigDecimal.valueOf(ConnectionID));
			
		String delete_mapping = "DELETE FROM station_mapping WHERE(route_id = ?)";
	
		PreparedStatement mapping_statement = connection.prepareStatement(delete_mapping);
		
		mapping_statement.setBigDecimal(1, BigDecimal.valueOf(ConnectionID));
		
		JSONObject message = new JSONObject();
		String result ="";
	
		if(Database_queue.getInstance().getActivationState())
		{
			Database_queue.getInstance().addStatement(connection_statement);
			Database_queue.getInstance().addStatement(mapping_statement);
			
			message.put("Success", "Added Statement to database queue!");
			result = "" + message;
			return Response.status(200).entity(result).build();
		}
		else
		{
			int res_mapping = mapping_statement.executeUpdate();
			int res_connection = connection_statement.executeUpdate();
			
			
			if(res_connection == 0 || res_mapping == 0)
			{
				message.put("error", "Deleting failed!");
				result = "" + message;
				return Response.status(400).entity(result).build();
			}
			else
			{
				message.put("Success", "Deleted route");
				result = "" + message;
				return Response.status(200).entity(result).build();			
			}
		}
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------Functions------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------------------------
	private long insertNewRoute(String name) throws SQLException {
		//Insert new Route with auto_increment
		String query = "INSERT INTO routes (name) VALUES(?) RETURNING route_id";
		PreparedStatement routeInsert = DBConnector.establishDBConnection().prepareStatement(query);
		routeInsert.setString(1, name);
		routeInsert.execute();
		//Get the route id

		long route_id = 0;
		while(routeInsert.getResultSet().next())
			route_id = routeInsert.getResultSet().getLong(1);
//		System.out.println("ROUTE ID: " + route_id);

		return route_id;
	}

	private boolean addStationsToRoute(long route_id, JSONArray station_ids) throws SQLException {
		String query = "INSERT INTO station_mapping (route_id, stop_id) VALUES(?, ?)";
		PreparedStatement statement = DBConnector.establishDBConnection().prepareStatement(query);
		
		for(int i = 0; i < station_ids.length(); i++) {
			System.out.println("DATA: " + route_id + " " + station_ids.getLong(i));
			statement.setLong(1, route_id);
			statement.setLong(2, station_ids.getLong(i));
			statement.addBatch();
		}
		
		int result[] = statement.executeBatch();
		for(int i = 0; i < result.length; i++) {
			if(result[i] != 1)
				return false;
		}
		
		return true;
	}
}
