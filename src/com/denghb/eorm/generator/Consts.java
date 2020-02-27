package com.denghb.eorm.generator;


public interface Consts {

    String SQL_TABLE = "select table_name, table_comment from information_schema.tables where table_schema = ?";

    String SQL_COLUMN = "select table_name, column_name, column_type, data_type, column_comment, column_key, " +
            "ifnull(character_maximum_length, numeric_precision) length, extra, is_nullable, column_default " +
            "from information_schema.columns where table_schema = ? ";

    // 配置
    String GENERATOR_CONFIG = "GENERATOR_CONFIG";


}
