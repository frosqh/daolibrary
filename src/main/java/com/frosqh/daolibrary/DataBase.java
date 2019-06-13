package com.frosqh.daolibrary;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DataBase {

    public static final List<Class<? extends Model>> models = new ArrayList<>();

    private static Logger logger = LogManager.getLogger();


    public DataBase(String name) throws ConnectionNotInitException {
        File db = new File(name);
        if (!db.exists()){
            Connection connect = ConnectionSQLite.getInstance();

            try(Statement stm = connect.createStatement()) {
                for (Class<? extends Model> c : models){
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
                    stm.execute(create);
                }
            } catch (SQLException e) {
                logger.log(Level.ERROR, e);
            }
        }
    }

    private String createTable(String name, String Table){
        return "CREATE TABLE IF NOT EXISTS " + name + "(\n"
                +"id INTEGER PRIMARY KEY, \n"
                + Table + "\n);";
    }

}
