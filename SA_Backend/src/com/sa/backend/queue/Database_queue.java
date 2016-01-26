package com.sa.backend.queue;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Database_queue extends Thread{

	private static Database_queue instance_;
	private List<PreparedStatement> statements_ = new ArrayList<PreparedStatement>(); 
	private boolean queue_activated = true;
	
	private Database_queue(){}
	
	public static Database_queue getInstance()
	{
		if(instance_ == null)
		{
			instance_ = new Database_queue();
			instance_.start();
		}
		return instance_;
	}
	
	@Override
	public void run() 
	{		
		try 
		{	
			Thread.sleep(5000);			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		for (Iterator<PreparedStatement> iterator = statements_.iterator(); iterator.hasNext();) 
		{
			PreparedStatement statement = iterator.next();
			
			try 
			{
				statement.executeUpdate();
				
				iterator.remove();	
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		instance_.run();	
		
	}
	
	synchronized public void addStatement(PreparedStatement statement)
	{		
		statements_.add(statement);
		
		if(statements_.size() >= 10)
		{
			instance_.interrupt();
		}
	}
	
	public boolean getActivationState()
	{
		return queue_activated;
	}
	
	public void setActivationState(boolean state)
	{
		queue_activated = state;
	}
	
}
