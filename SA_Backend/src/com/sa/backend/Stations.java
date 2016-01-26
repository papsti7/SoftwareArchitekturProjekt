package com.sa.backend;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

@Path("/stations")
public class Stations {

	private static List<String> allowed_fields = Arrays.asList("name", "lon","lat");
	
	@GET
	@Path("/{name}/{minLon}/{maxLon}/{minLat}/{maxLat}")
	@Produces("application/json")
	public Response getStations(
		@PathParam("name") String name,
		@PathParam("minLon") double minLon,
		@PathParam("maxLon") double maxLon,
		@PathParam("minLat") double minLat,
		@PathParam("maxLat") double maxLat
			) throws SQLException {
		
		//Create connection to db and create a statement

		name = name.toLowerCase();		
		String query = "SELECT * FROM public_transport_stops WHERE(LOWER(name) LIKE ?)"
				+ "AND (lon >= ?) AND (lon <= ?) AND (lat >= ?) AND (lat <= ?);";
		
		PreparedStatement statement = DBConnector.establishDBConnection().prepareStatement(query);
		statement.setString(1, "%"+name+"%");
		statement.setDouble(2, minLon);
		statement.setDouble(3, maxLon);
		statement.setDouble(4, minLat);
		statement.setDouble(5, maxLat);
		
		ResultSet res = statement.executeQuery();
		
		
		//Execute the query
				
		JSONObject ret = new JSONObject();
		
		JSONArray array = new JSONArray();
		//Iterate over the result of the query
		while (res.next()) {
			JSONObject json_obj = new JSONObject();
			json_obj.put("id", res.getLong("id"));
			json_obj.put("name", res.getString("name"));
			json_obj.put("lon", res.getString("lon"));
			json_obj.put("lat", res.getString("lat"));

			array.put(json_obj);
		}
		
		ret.put("Content", array);
		
		//Send the result to the client
		return Response.status(200).entity(ret.toString()).build();
	}
	
	@POST
	@Path("")
	@Produces("application/json")
	public Response addStation(String input) throws SQLException 
	{	
		
		JSONObject message = new JSONObject();
		String result ="";
		
		JSONObject newStation = new JSONObject(input);
		
		if(newStation.length() != 3 
			|| (!newStation.has("name") && !(newStation.getString("name") instanceof String))
			|| (!newStation.has("lon") && !(newStation.get("lon") instanceof Double))
			|| (!newStation.has("lat") && !(newStation.get("lat") instanceof Double))
			)
		{
			message.put("Error", "Check the sent json!");
			result = "" + message;
			return Response.status(200).entity(result).build();		
		}
		
		//we could also give the lon and lat in the request instead of this DB query. Just a matter of design which we will choose later.
		String query = "INSERT INTO public_transport_stops (lon, lat, name) VALUES(?,?,?)";
		PreparedStatement statement = DBConnector.establishDBConnection().prepareStatement(query);

		statement.setDouble(1, newStation.getDouble("lon"));
		statement.setDouble(2, newStation.getDouble("lat"));
		statement.setString(3, newStation.getString("name"));
	
		if(Database_queue.getInstance().getActivationState())
		{
			Database_queue.getInstance().addStatement(statement);
			
			message.put("Success", "Added Statement to database queue!");
			result = "" + message;
			return Response.status(200).entity(result).build();
		}
		else
		{
			int res = statement.executeUpdate();
			
			if(res == 0)
			{
				message.put("error", "Adding failed!");
				result = "" + message;
				return Response.status(400).entity(result).build();
			}
			else
			{
				message.put("Success", "Added station");
				result = "" + message;
				return Response.status(200).entity(result).build();			
			}
		}	
	}
	
	@PUT
	@Path("/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response editStation(
		@PathParam("id") double StationID,
		String body) throws SQLException 
	{			
		JSONObject json_body_obj = new JSONObject(body);
		
		JSONObject message = new JSONObject();
		String result ="";
		String fields = "";
				
		Iterator<?> keys = json_body_obj.keys();
		while( keys.hasNext() ) {
		    String key = (String)keys.next();
		    if(!(allowed_fields.contains(key)))	
		    {
				message.put("Error", "Check the sent json!");
				result = "" + message;
				return Response.status(200).entity(result).build();		
		    }
		    
		    if(keys.hasNext())
		    {
		    	fields += key + " = ? ,";
		    }
		    else
		    {
		    	fields += key + " = ? ";	
		    }
		}
		
		String put_query = "UPDATE public_transport_stops SET " + fields
				+ " WHERE(id = ?)";
		
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement statement = connection.prepareStatement(put_query);
	
		int i = 1;
		
		//fill it with values
		Iterator<?> keys_1 = json_body_obj.keys();
		while( keys_1.hasNext() ) {
		    String key = (String)keys_1.next();
		    if ( json_body_obj.get(key) instanceof String ) {
		    	statement.setString(i, json_body_obj.getString(key));
		    	i++;
		    }
		    else if(json_body_obj.get(key) instanceof Number)
		    {
		    	statement.setDouble(i, json_body_obj.getDouble(key));
		    	i++;
		    }
		    else
		    {
				message.put("Error", "Check the sent json!");
				result = "" + message;
				return Response.status(200).entity(result).build();	
		    }   	
		}

		statement.setBigDecimal(json_body_obj.length() + 1, BigDecimal.valueOf(StationID));
		
		if(Database_queue.getInstance().getActivationState())
		{
			Database_queue.getInstance().addStatement(statement);
			
			message.put("Success", "Added Statement to database queue!");
			result = "" + message;
			return Response.status(200).entity(result).build();
		}
		else
		{
			int res = statement.executeUpdate();
			
			if(res == 0)
			{
				message.put("error", "Modifying failed!");
				result = "" + message;
				return Response.status(400).entity(result).build();
			}
			else
			{
				message.put("Success", "Modified station");
				result = "" + message;
				return Response.status(200).entity(result).build();			
			}
		}
	}
	
	@DELETE
	@Path("/byName/{name}")
	@Produces("application/json")
	public Response deleteStation(
			@PathParam("name") String StationName) throws SQLException
	{
		String delete_query = "DELETE FROM public_transport_stops WHERE(name = ?);";
		
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement statement = connection.prepareStatement(delete_query);
		
		statement.setString(1, StationName);
		
		JSONObject message = new JSONObject();
		String result = "";
		
		if(Database_queue.getInstance().getActivationState())
		{
			Database_queue.getInstance().addStatement(statement);
			
			message.put("Success", "Added Statement to database queue!");
			result = "" + message;
			return Response.status(200).entity(result).build();
		}
		else
		{
			int res = statement.executeUpdate();
			
			if(res == 0)
			{
				message.put("error", "Deleting failed!");
				result = "" + message;
				return Response.status(400).entity(result).build();
			}
			else
			{
				message.put("Success", "Deleted station");
				result = "" + message;
				return Response.status(200).entity(result).build();			
			}
		}
	}
	
	@DELETE
	@Path("/{id}")
	@Produces("application/json")
	public Response deleteStation(
		@PathParam("id") double StationID
			) throws SQLException 
	{	
		//we could also give the lon and lat in the request instead of this DB query. Just a matter of design which we will choose later.
		String delete_query = "DELETE FROM public_transport_stops WHERE(id = ?)";
		
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement statement = connection.prepareStatement(delete_query);
		
		statement.setBigDecimal(1, BigDecimal.valueOf(StationID));
		
		JSONObject message = new JSONObject();
		String result ="";

		if(Database_queue.getInstance().getActivationState())
		{
			Database_queue.getInstance().addStatement(statement);
			
			message.put("Success", "Added Statement to database queue!");
			result = "" + message;
			return Response.status(200).entity(result).build();
		}
		else
		{
			int res = statement.executeUpdate();
			
			if(res == 0)
			{
				message.put("error", "Deleting failed!");
				result = "" + message;
				return Response.status(400).entity(result).build();
			}
			else
			{
				message.put("Success", "Deleted station");
				result = "" + message;
				return Response.status(200).entity(result).build();			
			}
		}
	}
}