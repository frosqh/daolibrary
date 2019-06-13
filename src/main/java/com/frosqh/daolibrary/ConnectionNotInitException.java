package com.frosqh.daolibrary;

/**
 * Raised when the database file is not specified to the Connection Manager
 * @author Frosqh
 * @version 0.1.0
 * @since 0.1.0
 */
public class ConnectionNotInitException  extends  Exception{
    public ConnectionNotInitException(){
        super("Database file not specified");
    }
}
