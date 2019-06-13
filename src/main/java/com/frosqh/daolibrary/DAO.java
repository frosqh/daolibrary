package com.frosqh.daolibrary;

import jdk.nashorn.internal.objects.annotations.Constructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO<M extends Model> {

    private M tmp;

    private static Logger logger = LogManager.getLogger();

    private final Connection connect = ConnectionSQLite.getInstance();
    
    private String tableName;

    public DAO(String tableName, M t) throws ConnectionNotInitException {
        this.tableName = tableName;
        tmp = t;
    }
    
    public M find(int id){
        try (Statement stm = connect.createStatement()){
            String select = getFindRequest(id);
            try (ResultSet result = stm.executeQuery(select)) {
                M m = newObject(id);
                for (Field f : getFields()) {
                    Class type = f.getType();
                    if (type.equals(String.class))f.set(m, result.getString(f.getName()));
                    else if (type.equals(int.class)) f.set(m, result.getInt(f.getName()));
                    else if (type.equals(float.class)) f.set(m, result.getFloat(f.getName()));
                    else f.set(m, result.getObject(f.getName()));
                }
                stm.close();
                return m;
            }
        } catch (SQLException | IllegalAccessException e) {
            logger.log(Level.ERROR, e);
        }
        return null;
    }

    public M create(M obj) {
        try (Statement stm = connect.createStatement()) {
            String select = getMaxRequest();

            int id = 1;
            try (ResultSet result = stm.executeQuery(select)){
                if (result.next())
                    id = result.getInt(1)+ 1;
                stm.close();
            } catch (SQLException e){
                logger.log(Level.ERROR, e);
            }

            Field[] fields = getFields();
            String request = "INSERT INTO "+tableName+"(id,";
            String values = "(?,";
            for (Field f : fields){
                request += f.getName()+",";
                values += "?,";
            }

            values = values.substring(0,values.length()-1)+")";
            request = request.substring(0,request.length()-1)+ ") VALUES "+values;

            int i = 2;
            try  (PreparedStatement prepare = connect.prepareStatement(request)) {
                for (Field f: fields){
                    Class type = f.getType();
                    if (type.equals(String.class)) prepare.setString(i, (String) f.get(obj));
                    else if (type.equals(int.class)) prepare.setInt(i, (int) f.get(obj));
                    else if (type.equals(double.class)) prepare.setDouble(i, (double) f.get(obj));
                    else prepare.setObject(i,f.get(obj));
                    i++;
                }
                prepare.executeUpdate();
                prepare.close();
                return find(id);
            } catch (IllegalAccessException e) {
                logger.log(Level.ERROR, e);
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, e);
        }
        return null;
    }

    public void delete(M obj){
        try (Statement stm = connect.createStatement()){
            String del = getDeleteRequest(obj.getId());
            stm.executeUpdate(del);
        } catch (SQLException e) {
            logger.log(Level.ERROR, e);
        }
    }

    public void delete(int id){
        try (Statement stm = connect.createStatement()){
            String del = getDeleteRequest(id);
            stm.executeUpdate(del);
        } catch (SQLException e) {
            logger.log(Level.ERROR, e);
        }
    }

    public List<M> getList(){
        List<M> list = new ArrayList<>();
        try (Statement stm = connect.createStatement()){
            String request = "SELECT id FROM "+tableName;
            System.out.println(request);
            try (ResultSet res = stm.executeQuery(request)){
                while (res.next())
                    list.add(find(res.getInt("id")));
                stm.close();
                return list;
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, e);
        }
        return null;
    }

    public List<M> filter(String... args){
        String where;
        {
            StringBuilder whereB = new StringBuilder();
            boolean b = true;
            for (String s : args){
                whereB.append(!b?"'":"").append(s).append(!b?"'":"").append(b?"=":" AND ");
                b=!b;
            }
            where = whereB.toString().substring(0,whereB.lastIndexOf("A")-1);
            List<M> list = new ArrayList<>();
            try (Statement stm = connect.createStatement()){
                String request = "SELECT id FROM "+tableName+" WHERE "+where;
                try (ResultSet result = stm.executeQuery(request)) {
                    while (result.next())
                        list.add(find(result.getInt("id")));
                    stm.close();
                    return list;
                }
            } catch (SQLException e) {
                logger.log(Level.ERROR, e);
            }
        }
        return null;
    }



    private String getFindRequest(int id){
        return "SELECT * FROM "+tableName+" WHERE id = "+id;
    }

    private String getMaxRequest(){
        return "SELECT MAX(id) FROM "+tableName;
    }

    private String getDeleteRequest(int id){
        return "DELETE FROM "+tableName+" WHERE id = "+id;
    }

    private String getDeleteForeignRequest(String tableName, int id){
        return "DELETE FROM "+tableName+" WHERE "+tableName+"_id = "+id;
    }

    private String getUpdateRequest(String upd, int id){
        return "UPDATE "+tableName+" SET "+upd+" WHERE id = "+id;
    }

    private Field[] getFields(){
        return tmp.getClass().getDeclaredFields();
    }

    private M newObject(int id){
        try {
            return (M) tmp.getClass().getDeclaredConstructor(int.class).newInstance(id);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.log(Level.ERROR, e);
        };
        return null;
    }

    public static DAO construct(Class<? extends Model> clas) {
        try {
            return new DAO<>(clas.getSimpleName().toLowerCase(), clas.cast( clas.getDeclaredConstructor(int.class).newInstance(0)));
        } catch (ConnectionNotInitException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            logger.log(Level.ERROR,e);
        }
        return null;
    }
    
    
}
