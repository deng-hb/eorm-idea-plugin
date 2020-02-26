package com.denghb.eorm.generator;

public interface Consts {


    String SQL_TABLE = "select table_name, table_comment from information_schema.tables where table_schema = ?";

    String SQL_COLUMN = "select column_name, column_type, column_comment from information_schema.columns where table_schema = ? and table_name = ? ";
}
