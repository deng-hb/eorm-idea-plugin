package com.denghb.eorm.generator.model;

import java.math.BigInteger;

/**
 * 字段信息
 */
public class ColumnModel {

    private String tableName;

    private String columnName;

    private String columnType;

    private String columnComment;

    private String columnKey;

    private String columnDefault;

    private String dataType;

    private String isNullable;

    private String extra;

    private BigInteger length;

    // java类型
    private String javaType;

    // updated_by -> updatedBy
    private String objectName;
    // updated_by -> UpdatedBy
    private String methodName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public String getColumnDefault() {
        return columnDefault;
    }

    public void setColumnDefault(String columnDefault) {
        this.columnDefault = columnDefault;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public BigInteger getLength() {
        return length;
    }

    public void setLength(BigInteger length) {
        this.length = length;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "ColumnModel{" +
                "tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                ", columnComment='" + columnComment + '\'' +
                ", columnKey='" + columnKey + '\'' +
                ", columnDefault='" + columnDefault + '\'' +
                ", dataType='" + dataType + '\'' +
                ", isNullable='" + isNullable + '\'' +
                ", extra='" + extra + '\'' +
                ", length=" + length +
                ", javaType='" + javaType + '\'' +
                ", objectName='" + objectName + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}