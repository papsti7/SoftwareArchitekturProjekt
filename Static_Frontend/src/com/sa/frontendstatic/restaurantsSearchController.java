package com.sa.frontendstatic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/searchRestaurants")
public class restaurantsSearchController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	  
     	request.setAttribute("id", request.getParameter("id"));
		request.setAttribute("name", request.getParameter("stationname"));
		request.setAttribute("distance", request.getParameter("distance"));
		request.setAttribute("type", 2);
		
		System.out.println("ID: " + request.getParameter("id"));
		System.out.println("Name: " + request.getParameter("name"));
		System.out.println("Dis: " + request.getParameter("distance"));
			
		request.getRequestDispatcher("index.jsp").forward(request, response);	
	}
}
