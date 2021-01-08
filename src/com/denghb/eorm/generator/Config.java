package com.denghb.eorm.generator;

import java.util.Map;

/**
 * 界面属性配置
 */
public class Config {

    private String jdbc;

    private String database;

    private boolean lombok;

    private boolean override;

    private boolean bigDecimal;

    private boolean since;

    private boolean schema;

    private String prefix;

    private String author;

    private String packageName;

    private Map<String, String> packageNamePath;

    private String version;// 版本

    public String getJdbc() {
        return jdbc;
    }

    public void setJdbc(String jdbc) {
        this.jdbc = jdbc;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isLombok() {
        return lombok;
    }

    public void setLombok(boolean lombok) {
        this.lombok = lombok;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public boolean isBigDecimal() {
        return bigDecimal;
    }

    public void setBigDecimal(boolean bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    public boolean isSince() {
        return since;
    }

    public void setSince(boolean since) {
        this.since = since;
    }

    public boolean isSchema() {
        return schema;
    }

    public void setSchema(boolean schema) {
        this.schema = schema;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Map<String, String> getPackageNamePath() {
        return packageNamePath;
    }

    public void setPackageNamePath(Map<String, String> packageNamePath) {
        this.packageNamePath = packageNamePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Config{" +
                "jdbc='" + jdbc + '\'' +
                ", database='" + database + '\'' +
                ", lombok=" + lombok +
                ", override=" + override +
                ", bigDecimal=" + bigDecimal +
                ", since=" + since +
                ", schema=" + schema +
                ", prefix='" + prefix + '\'' +
                ", author='" + author + '\'' +
                ", packageName='" + packageName + '\'' +
                ", packageNamePath=" + packageNamePath +
                ", version='" + version + '\'' +
                '}';
    }
}
