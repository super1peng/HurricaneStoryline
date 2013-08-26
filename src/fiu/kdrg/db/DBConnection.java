package fiu.kdrg.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.out.println("Cannot Load Driver!");
			return null;
		}
		
		try {
			return DriverManager.getConnection("jdbc:mysql://proceed.cs.fiu.edu:33061/ADSE", "cshen001",
					"1");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
