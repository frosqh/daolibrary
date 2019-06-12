package com.frosqh.daolibrary;

public class MyModelBis extends Model {

    public double myVariable;
    public int val;
    public String monTexte;

    protected MyModelBis(int id) {
        super(id);
    }

    public MyModelBis(int i, int i1, int i2, String t) {
        super(i);
        myVariable = i1;
        val = i2;
        monTexte = t;
    }

    @Override
    public String toString() {
        return getId() +"-"+myVariable+"-"+val+"-"+monTexte;
    }
}
