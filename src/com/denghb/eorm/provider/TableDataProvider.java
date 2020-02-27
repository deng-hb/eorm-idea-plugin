package com.denghb.eorm.provider;

public class TableDataProvider {

    public static void load(String jdbc, TableDataCallback handler) {
        new Thread(new TableDataTask(jdbc, handler)).start();
    }
}
