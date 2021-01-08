package com.denghb.eorm.provider;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.generator.Consts;
import com.denghb.eorm.generator.model.ColumnModel;
import com.denghb.eorm.generator.model.TableModel;
import com.denghb.eorm.impl.EormImpl;
import com.denghb.eorm.utils.JdbcUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableDataMySQLTask implements Runnable {

    private String jdbc;
    private TableDataCallback tableDataCallback;

    public TableDataMySQLTask(String jdbc, TableDataCallback tableDataCallback) {
        this.jdbc = jdbc;
        this.tableDataCallback = tableDataCallback;
    }

    @Override
    public void run() {

        Connection connection = null;
        try {

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(jdbc);
            Eorm db = new EormImpl(connection);

            String database = JdbcUtils.getDatabase(jdbc);
            List<TableModel> tables = db.select(TableModel.class, Consts.SQL_TABLE, database);

            List<ColumnModel> columns = db.select(ColumnModel.class, Consts.SQL_COLUMN, database);
            for (TableModel table : tables) {
                table.setColumns(new ArrayList<ColumnModel>());

                // 查询DDL 只能拼接
                /* String ddlSql = "show create table `" + database + "`.`" + table.getTableName() + "`";
                Map map = db.selectOne(Map.class, ddlSql);
                String ddl = (String) map.get("Create Table");
                table.setDDL(ddl); */

                for (int i = 0; i < columns.size(); i++) {
                    ColumnModel column = columns.get(i);
                    if (column.getTableName().equals(table.getTableName())) {
                        table.getColumns().add(column);
                        columns.remove(i);
                        i--;
                    }
                }
            }

            tableDataCallback.onData(tables);
        } catch (Exception e) {
            e.printStackTrace();
            tableDataCallback.onMessage(e.getMessage());
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
