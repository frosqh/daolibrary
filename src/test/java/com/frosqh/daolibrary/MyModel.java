package com.frosqh.daolibrary;

public class MyModel extends Model {

    public double myVariable;
    public int val;

    protected MyModel(int id) {
        super(id);
    }

    public MyModel(int i, int i1, int i2) {
        super(i);
        myVariable = i1;
        val = i2;
    }

    @Override
    public String toString() {
        return getId() +"-"+myVariable+"-"+val;
    }
}
