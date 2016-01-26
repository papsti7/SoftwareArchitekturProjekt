 package com.sa.backend;

import java.math.*;
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

@Path("/restaurants")
public class Restaurants {
	
	private static List<String> allowed_fields = Arrays.asList("name", "street", "housenumber", 
			"city", "opening_hours", "phone", 
			"amenity", "cuisine", "outside_seating",
			"lon","lat");
	
	@GET
	@Path("/byID/{stopid}/{distance}")
	@Produces("application/json")
	public Response getRestaurantsByID(
		@PathParam("stopid") double stopid,
		@PathParam("distance") float distance
			) throws SQLException 
	{	
		//we could also give the lon and lat in the request instead of this DB query. Just a matter of design which we will choose later.
		String query = "SELECT * FROM public_transport_stops WHERE(id = ?)";
		
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement statement = connection.prepareStatement(query);
		
		statement.setBigDecimal(1, BigDecimal.valueOf(stopid));
		
		ResultSet res = statement.executeQuery();
		
		return createResult(res, distance);	
	}
	
	@GET
	@Path("/byNameAndDistance/{name}/{distance}")
	@Produces("application/json")
	public Response getRestaurantsByName(
		@PathParam("name") String name,
		@PathParam("distance") float distance
			) throws SQLException 
	{	
		//we could also give the lon and lat in the request instead of this DB query. Just a matter of design which we will choose later.
		String query = "SELECT * FROM public_transport_stops WHERE(lower(name) like lower(?))";
		
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement statement = connection.prepareStatement(query);
		
		name = "%" + name + "%";
		statement.setString(1, name);
		
		System.out.println("query: " + statement.toString());
		
		ResultSet res = statement.executeQuery();
		
		return createResult(res, distance);	
	}
	
	Response createResult(ResultSet res, float distance) throws SQLException{
		double lon = -4242;
		double lat = -4242;

		while (res.next()) 
		{
			lon = res.getDouble("lon");
			lat = res.getDouble("lat");
		}
		
		JSONObject message = new JSONObject();
		String result = "";
		
		if(lon == -4242 || lat == -4242)
		{
			message.put("error ", "the id could not be found");
			result = ""+message;
			return Response.status(400).entity(result).build();
		}
			
		//FROM: http://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
		//-> kilometer / 111.111 = y direction degree to add and substract
		//-> kilometer / 111.111 * cos(latitude of station) = x
		//y
		double lat_distance_in_degrees = Math.abs(distance / 111.111); 
		//x
		double lon_distance_in_degrees = Math.abs((distance / 111.111) * Math.cos(lat));
		
		double max_lat = lat + lat_distance_in_degrees;
		double min_lat = lat - lat_distance_in_degrees;
		
		double max_lon = lon + lon_distance_in_degrees;
		double min_lon = lon - lon_distance_in_degrees;
		
		String query_restaurants = "SELECT * FROM restaurants WHERE (lon >= ?) AND (lon <= ?) AND (lat >= ?) AND (lat <= ?);";
		
		PreparedStatement statement_restaurants = DBConnector.establishDBConnection().prepareStatement(query_restaurants);
		statement_restaurants.setDouble(1, min_lon);
		statement_restaurants.setDouble(2, max_lon);
		statement_restaurants.setDouble(3, min_lat);
		statement_restaurants.setDouble(4, max_lat);
		
		ResultSet res_restaurants = statement_restaurants.executeQuery();
		
		JSONObject ret = new JSONObject();
		JSONArray array = new JSONArray();
		
		while (res_restaurants.next()) {
			JSONObject json_obj = new JSONObject();
			json_obj.put("id", res_restaurants.getLong("id"));
			json_obj.put("name", res_restaurants.getString("name"));
			json_obj.put("street", res_restaurants.getString("street"));
			json_obj.put("housenumber", res_restaurants.getString("housenumber"));
			json_obj.put("city", res_restaurants.getString("city"));
			json_obj.put("opening_hours", res_restaurants.getString("opening_hours"));
			json_obj.put("phone", res_restaurants.getString("phone"));
			json_obj.put("amenity", res_restaurants.getString("amenity"));
			json_obj.put("cuisine", res_restaurants.getString("cuisine"));
			json_obj.put("outside_seating", res_restaurants.getString("outside_seating"));
			json_obj.put("lon", res_restaurants.getDouble("lon"));
			json_obj.put("lat", res_restaurants.getDouble("lat"));
			array.put(json_obj);
		}
		
		ret.put("Content", array);
		
		//Send the result to the client
		result = ""+message;
		
		return Response.status(200).entity(ret.toString()).build();
	}
	
	@POST
	@Path("")
	@Consumes("application/json")
	@Produces("application/json")
	public Response addRestaurant(String body) throws SQLException 
	{
		JSONObject json_body_obj = new JSONObject(body);
	
		JSONObject message = new JSONObject();
		String fields = "";
		String values = "";
		
		Iterator<?> keys = json_body_obj.keys();
		while( keys.hasNext() ) {
		    String key = (String)keys.next();
		    if(!(allowed_fields.contains(key)))	
		    {
				message.put("Error", "Check the sent json!");
				return Response.status(200).entity(message.toString()).build();		
		    }
		    
		    if(keys.hasNext())
		    {
		    	fields += key + ",";
		    	values += "?, ";
		    }
		    else
		    {
		    	fields += key;
		    	values += "?";
		    }
		}	
		
		String query = "INSERT INTO restaurants (" + fields + ") VALUES(" + values + ")";
		PreparedStatement statement = DBConnector.establishDBConnection().prepareStatement(query);
		
		int i = 1;
		
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
				return Response.status(200).entity(message.toString()).build();	
		    }   	
		}
		
		
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
				message.put("error", "Adding Restaurant failed!");
				return Response.status(400).entity(message.toString()).build();
			}
			else
			{
				message.put("Success", "Added restaurant");
				return Response.status(200).entity(message.toString()).build();			
			}
		}		
	}
	
	@PUT
	@Path("/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response editRestaurant(
		@PathParam("id") double RestaurantID,
		String body) throws SQLException 
	{			
		JSONObject json_body_obj = new JSONObject(body);
		
		JSONObject message = new JSONObject();
		String fields = "";
				
		Iterator<?> keys = json_body_obj.keys();
		while( keys.hasNext() ) {
		    String key = (String)keys.next();
		    if(!(allowed_fields.contains(key)))	
		    {
				message.put("Error", "Check the sent json!");
				return Response.status(200).entity(message.toString()).build();		
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
		
		String put_query = "UPDATE restaurants SET " + fields
				+ "WHERE(id = ?)";
		
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
				return Response.status(200).entity(message.toString()).build();	
		    }   	
		}

		statement.setBigDecimal(json_body_obj.length() + 1, BigDecimal.valueOf(RestaurantID));

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
				message.put("error", "Modifying failed!");
				return Response.status(400).entity(message.toString()).build();
			}
			else
			{
				message.put("Success", "Modified restaurant");
				return Response.status(200).entity(message.toString()).build();			
			}
		}
	}
	
	@DELETE
	@Path("/{id}")
	@Produces("application/json")
	public Response deleteRestaurant(
		@PathParam("id") double RestaurantID
			) throws SQLException 
	{	
		//we could also give the lon and lat in the request instead of this DB query. Just a matter of design which we will choose later.
		String delete_query = "DELETE FROM restaurants WHERE(id = ?)";
		
		Connection connection = DBConnector.establishDBConnection();
		PreparedStatement statement = connection.prepareStatement(delete_query);
		
		statement.setBigDecimal(1, BigDecimal.valueOf(RestaurantID));
		
		JSONObject message = new JSONObject();
		
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
				message.put("error", "Deleting failed!");
				return Response.status(400).entity(message.toString()).build();
			}
			else
			{
				message.put("Success", "Deleted restaurant");
				return Response.status(200).entity(message.toString()).build();		
			}
		}
	}
}