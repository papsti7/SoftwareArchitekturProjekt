package com.sa.frontenddynamic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/searchStations")
public class stationSearchController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("name", request.getParameter("name"));
		request.setAttribute("lon_max", request.getParameter("lon_max"));
		request.setAttribute("lon_min", request.getParameter("lon_min"));
		request.setAttribute("lat_max", request.getParameter("lat_max"));
		request.setAttribute("lat_min", request.getParameter("lat_min"));
		request.setAttribute("type", 0);
		
		System.out.println("NAME: " + request.getParameter("name"));
		
		request.getRequestDispatcher("index.jsp").forward(request, response);
	}
}
