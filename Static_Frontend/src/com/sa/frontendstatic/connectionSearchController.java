package com.sa.frontendstatic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/searchConnection")
public class connectionSearchController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("name_from", request.getParameter("name_from"));
		request.setAttribute("name_to", request.getParameter("name_to"));
		request.setAttribute("type", 1);
				
		request.getRequestDispatcher("index.jsp").forward(request, response);
	}
}
