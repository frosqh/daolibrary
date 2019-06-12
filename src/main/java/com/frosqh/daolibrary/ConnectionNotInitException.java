package com.frosqh.daolibrary;

public class ConnectionNotInitException  extends  Exception{
    public ConnectionNotInitException(){
        super("Database file not specified");
    }
}
