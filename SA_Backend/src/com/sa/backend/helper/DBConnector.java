package com.sa.backend.helper;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnector {

	static Connection connection = null;

	public static Connection establishDBConnection() {
		if (connection == null) {
			try {
				Class.forName("org.postgresql.Driver");
				connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sa_db", "postgres", "sa2015");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
				System.exit(0);
			}
			System.out.println("Opened database successfully");
		}

		return connection;
	}
}
