package com.frosqh.daolibrary;

/**
 * Define a model to be used in DAO
 * @author Frosqh
 * @since 0.1.0
 */
public abstract class Model {
    /**
     * The id of the object in the database
     * @since 0.1.0
     */
    private final int id;

    /**
     * Create a new Model
     * @param id The id in the database
     */
    protected  Model(int id) {
        this.id = id;
    }

    /**
     * Getter of the field id
     * @return Value of the field id
     */
    public int getId() {
        return id;
    }
}
