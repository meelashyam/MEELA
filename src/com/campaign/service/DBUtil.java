package com.campaign.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	
	private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:/data/test;AUTO_SERVER=TRUE";
    //jdbc:h2:tcp://localhost/~/test  -- server
    //jdbc:h2:~/test -- embded
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";
	
	 static Connection getDBConnection(){	
	 Connection dbConnection = null;
     try {
         Class.forName(DB_DRIVER);
     } catch (ClassNotFoundException e) {
         System.out.println(e.getMessage());
     }
     try {
         dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,
                 DB_PASSWORD);
         return dbConnection;
     } catch (SQLException e) {
         System.out.println(e.getMessage());
     }
     return dbConnection;
 }
}

