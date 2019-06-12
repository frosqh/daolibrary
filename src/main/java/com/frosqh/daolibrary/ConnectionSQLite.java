package com.frosqh.daolibrary;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionSQLite {
    private static Logger logger = LogManager.getLogger();

    private static String filename = null;

    private static Connection connect;

    public static void init(String name){
        filename = name;
    }

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
