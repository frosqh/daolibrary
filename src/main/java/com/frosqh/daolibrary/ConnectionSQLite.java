package com.frosqh.daolibrary;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handle the connection to the database through the whole running of the application via a singleton
 * @author Frosqh
 * @version 0.1.0
 * @since 0.1.0
 */
public class ConnectionSQLite {
    private static Logger logger = LogManager.getLogger();

    /**
     * The database file's name
     * @since 0.1.0
     */
    private static String filename = null;

    /**
     * Connection instance
     * @since 0.1.0
     */
    private static Connection connect;

    private ConnectionSQLite(){};

    /**
     * Set the database file's name
     * @param name Name to use from now on
     * @since 0.1.0
     */
    public static void init(String name){
        filename = name;
    }

    /**
     * Create (if needed) and return connection to the database
     * @return The unique instance of the connection to the database
     * @throws ConnectionNotInitException if the database file's name is not specified yet
     * @since 0.1.0
     */
    public static Connection getInstance() throws ConnectionNotInitException {
        if (filename == null){
            throw new ConnectionNotInitException();
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e){
            logger.log(Level.ERROR, e.getMessage());
        }
        if (connect == null) {
            try {
                String url = "jdbc:sqlite:";
                connect = DriverManager.getConnection(url + filename);
            } catch (SQLException e) {
                logger.log(Level.ERROR, e);
            }
        }
        return connect;
    }
}
