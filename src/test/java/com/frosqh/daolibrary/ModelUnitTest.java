package com.frosqh.daolibrary;

import org.junit.Test;

public class ModelUnitTest {

    @Test
    public void getFieldsFirstTest() throws ConnectionNotInitException {
        ConnectionSQLite.init("database.db");
        DataBase.models.add(MyModel.class);
        DataBase.models.add(MyModelBis.class);
        new DataBase("database.db");
        MyModel m = new MyModel(14,42,3);
        DAO.construct(MyModel.class).create(m);
        System.out.println(DAO.construct(MyModel.class).getList());
        m = (MyModel) DAO.construct(MyModel.class).find(1);
        m.val = 53;
        DAO.construct(MyModel.class).update(m);
        System.out.println(m);
        System.out.println(DAO.construct(MyModel.class).getList());
        DAO.construct(MyModel.class).delete(4);
        System.out.println(DAO.construct(MyModel.class).filter("val","3"));
    }
}
