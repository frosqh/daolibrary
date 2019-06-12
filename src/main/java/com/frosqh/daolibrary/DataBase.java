package com.frosqh.daolibrary;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DataBase {

    public static List<Class<? extends Model>> models = new ArrayList<>();

    public DataBase(String name) throws ConnectionNotInitException {
        File db = new File(name);
        if (!db.exists()){
            Connection connect = ConnectionSQLite.getInstance();
            Statement stm = null;

            try {
                stm = connect.createStatement();
                for (Class<? extends Model> c : models){
                    System.out.println(c);
                    String TABLE ="";
                    for (Field f : c.getDeclaredFields()){
                        TABLE+=f.getName()+" ";
                        Class type = f.getType();
                        String SQLType="";
                        if (type.equals(String.class)) SQLType = "TEXT";
                        if (type.equals(int.class)) SQLType = "INTEGER";
                        if (type.equals(double.class)) SQLType = "REAL";
                        TABLE+=SQLType+" NOT NULL,\n";
                    }
                    TABLE = TABLE.substring(0, TABLE.length()-2);
                    String create = createTable(c.getSimpleName().toLowerCase(),TABLE);
                    System.out.println(create);
                    stm.execute(create);
                }
                stm.close();
            } catch (SQLException e) {
                //TODO Error Handler
            }
        }
    }

    private String createTable(String name, String Table){
        return "CREATE TABLE IF NOT EXISTS " + name + "(\n"
                +"id INTEGER PRIMARY KEY, \n"
                + Table + "\n);";
    }

}
