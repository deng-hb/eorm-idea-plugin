package com.denghb.eorm.generator.model;

import java.util.List;

/**
 * Created by denghb
 */
public class TableModel {

    // 表名
    private String tableName;

    // 表备注
    private String tableComment;

    // 列信息
    private List<ColumnModel> columns;

    // 默认不选中
    private boolean checked = false;

    // DDL
    private String DDL;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public List<ColumnModel> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnModel> columns) {
        this.columns = columns;
    }
    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getDDL() {
        return DDL;
    }

    public void setDDL(String DDL) {
        this.DDL = DDL;
    }

    @Override
    public String toString() {
        return "TableModel{" +
                "tableName='" + tableName + '\'' +
                ", tableComment='" + tableComment + '\'' +
                ", columns=" + columns +
                ", checked=" + checked +
                ", DDL='" + DDL + '\'' +
                '}';
    }
}
