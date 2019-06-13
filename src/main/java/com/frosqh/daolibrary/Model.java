package com.frosqh.daolibrary;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public abstract class Model {
    private final int id;

    protected  Model(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
