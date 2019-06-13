package com.frosqh.daolibrary;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide necessary function to deal with database relatives to a model
 * @param <M> Class corresponding to the model treated by the DAO
 * @author Frosqh
 * @since 0.1.0
 * @version 0.1.0
 */
public class DAO<M extends Model> {

    private static Logger logger = LogManager.getLogger();

    /**
     * Placeholder value used for getting the fields of the M class
     * @since 0.1.0
     */
    private M tmp;

    /**
     * Instance dealing with database
     * @since 0.1.0
     */
    private final Connection connect = ConnectionSQLite.getInstance();

    /**
     * Table name
     * @since 0.1.0
     */
    private String tableName;

    /**
     * Create a new DAO based on the table name and a M object
     * @param tableName Table's name in the database
     * @param  t Object of type M, value don't matter (not null)
     * @throws ConnectionNotInitException if the connection is not initialized
     * @since 0.1.0
     */
    public DAO(String tableName,@NotNull M t) throws ConnectionNotInitException {
        this.tableName = tableName;
        tmp = t;
    }

    /**
     * Allow to get an object linked to a certain id from the database
     * @param id the identifier (primary key) of the object to find in the database
     * @return The model referring to the given id
     * @since 0.1.0
     */
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
                return m;
            }
        } catch (SQLException | IllegalAccessException e) {
            logger.log(Level.ERROR, e);
        }
        return null;
    }

    /**
     * Create an entry in the database corresponding to a model
     * @param obj The model to add to the database
     * @return The model strictly corresponding to the one saved in database (id changed)
     * @since 0.1.0
     */
    public M create(M obj) {
        try (Statement stm = connect.createStatement()) {
            String select = getMaxRequest();

            int id = 1;
            try (ResultSet result = stm.executeQuery(select)){
                if (result.next())
                    id = result.getInt(1)+ 1;
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
                return find(id);
            } catch (IllegalAccessException e) {
                logger.log(Level.ERROR, e);
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, e);
        }
        return null;
    }

    /**
     * Update an entry in the database to match with the given model
     * @param obj The model to update in the database <br>
     *            &nbsp;&nbsp;&nbsp;&nbsp;Its id will be used to find the original entry
     * @return The model updated such as saved in the database
     * @since 0.1.0
     */
    public M update(M obj){
        try (Statement stm = connect.createStatement()){
            String upd = "";
            for (Field f : getFields()){
                upd+=f.getName()+" = '"+f.get(obj)+"',";
            }
            upd = upd.substring(0,upd.length()-1);
            upd = getUpdateRequest(upd, obj.getId());
            stm.executeUpdate(upd);
            obj = find(obj.getId());
            return obj;
        } catch (SQLException | IllegalAccessException e) {
            logger.log(Level.ERROR, e);
        }
        return null;
    }

    /**
     * Delete an entry in the database corresponding to giver model
     * @param obj The model to delete<br>
     *            &nbsp;&nbsp;&nbsp;&nbsp;Its id will be to find the original entry
     * @since 0.1.0
     */
    public void delete(M obj){
        try (Statement stm = connect.createStatement()){
            String del = getDeleteRequest(obj.getId());
            stm.executeUpdate(del);
        } catch (SQLException e) {
            logger.log(Level.ERROR, e);
        }
    }

    /**
     * Delete an entry in the database corresponding to giver model
     * @param id The id of the model to delete
     * @since 0.1.0
     */
    public void delete(int id){
        try (Statement stm = connect.createStatement()){
            String del = getDeleteRequest(id);
            stm.executeUpdate(del);
        } catch (SQLException e) {
            logger.log(Level.ERROR, e);
        }
    }

    /**
     * Return the entire list of models saved in the database
     * @return A list of model
     * @since 0.1.0
     */
    public List<M> getList(){
        List<M> list = new ArrayList<>();
        try (Statement stm = connect.createStatement()){
            String request = "SELECT id FROM "+tableName;
            try (ResultSet res = stm.executeQuery(request)){
                while (res.next())
                    list.add(find(res.getInt("id")));
                return list;
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, e);
        }
        return null;
    }

    /**
     * Return the list of models saved in the database matching the parameters
     * @param args list of parameters specifiyng the WHERE condition. <br>
     *             For WHERE var1=val, use filter(var1,val1), and continue for more criteria
     * @return A list fo model resulting of a SELECT * FROM table WHERE args
     */
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
                    return list;
                }
            } catch (SQLException e) {
                logger.log(Level.ERROR, e);
            }
        }
        return null;
    }


    /**
     * Provide a template of a SELECT * FROM request
     * @param id The id of the entry to request
     * @return The request completed
     * @since 0.1.0
     */
    private String getFindRequest(int id){
        return "SELECT * FROM "+tableName+" WHERE id = "+id;
    }

    /**
     * Provide a template for getting the max id of a table
     * @return The request completed
     * @since 0.1.0
     */
    private String getMaxRequest(){
        return "SELECT MAX(id) FROM "+tableName;
    }

    /**
     * Provide a template for a delete request
     * @param id The id of the entry to delete
     * @return The request completed
     * @since 0.1.0
     */
    private String getDeleteRequest(int id){
        return "DELETE FROM "+tableName+" WHERE id = "+id;
    }

    /**
     * Provide a template for an update request
     * @param upd The full update string
     * @param id The id of the entry to update
     * @return The request completed
     * @since 0.1.0
     */
    private String getUpdateRequest(String upd, int id){
        return "UPDATE "+tableName+" SET "+upd+" WHERE id = "+id;
    }

    /**
     * Return the array of public fields contained in the model
     * @since 0.1.0
     */
    private Field[] getFields(){
        return tmp.getClass().getFields();
    }

    /**
     * Create a new model of class M
     * @param id The id to create the new model with
     * @return A new model
     */
    private M newObject(int id){
        try {
            return (M) tmp.getClass().getDeclaredConstructor(int.class).newInstance(id);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.log(Level.ERROR, e);
        };
        return null;
    }

    /**
     * Create a new DAO
     * @param clas The model class the DAO should be for
     * @return A new DAO initialized with the class name as tableName and the class type as generic type
     */
    public static DAO construct(Class<? extends Model> clas) {
        try {
            return new DAO<>(clas.getSimpleName().toLowerCase(), clas.cast( clas.getDeclaredConstructor(int.class).newInstance(0)));
        } catch (ConnectionNotInitException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            logger.log(Level.ERROR,e);
        }
        return null;
    }
    
    
}
