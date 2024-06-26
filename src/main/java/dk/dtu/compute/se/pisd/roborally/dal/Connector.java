/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.dal;

import com.mysql.cj.util.StringUtils;
import dk.dtu.compute.se.pisd.roborally.fileaccess.IOUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
class Connector {
	
    private static final String HOST     = "localhost"; //<-- update this to your own host
    private static final int    PORT     = 3306; //<-- update this to your own port
    private static final String DATABASE = "pisd"; //<-- update this to your own database
    private static final String USERNAME = "root"; //<-- update this to your own username
    private static final String PASSWORD = "***"; //<-- update this to your own password

    private static final String DELIMITER = ";;";
    
    private Connection connection;

	/**
	 * Constructor for connector. Writes the URL for the database, and connects with login information defined in this class.
	 * Calls the createDataSchema method.
	 * @Author Ekkart Kindler
	 */
	Connector() {
        try {
			// String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;
			String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?serverTimezone=UTC&autoReconnect=true&useSSL=false";
			connection = DriverManager.getConnection(url, USERNAME, PASSWORD);

			createDatabaseSchema();
		} catch (SQLException e) {
			// TODO we should try to diagnose and fix some problems here and
			//      exit in a more graceful way
			e.printStackTrace();
			// Platform.exit();
		}
    }

	/**
	 * Method that uses the file createschema.sql to create new tables in the database if they don't exist.
	 * createschema.sql contains pure SQL code for creation of the tables.
	 *
	 * @Author Ekkart Kindler
	 */
	private void createDatabaseSchema() {

    	String createTablesStatement =
				IOUtil.readResource("schemas/createschema.sql");

    	try {
    		connection.setAutoCommit(false);
    		Statement statement = connection.createStatement();
    		for (String sql : createTablesStatement.split(DELIMITER)) {
    			if (!StringUtils.isEmptyOrWhitespaceOnly(sql)) {
    				statement.executeUpdate(sql);
    			}
    		}

    		statement.close();
    		connection.commit();
    	} catch (SQLException e) {
    		e.printStackTrace();
    		// TODO error handling
    		try {
				connection.rollback();
			} catch (SQLException e1) {}
    	} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {}
		}
    }
    
    Connection getConnection() {
    	return connection; 
    }
    
}
