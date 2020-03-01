package com.denghb.eorm.generator;

import com.denghb.eorm.generator.model.ColumnModel;
import com.denghb.eorm.generator.model.TableModel;
import com.denghb.eorm.utils.JdbcUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class EntityGeneratorCode {

    public static void doExec(TableModel table, Config config, String generateTime) throws Exception {

        preprocessor(config, table.getColumns());

        String path = config.getPackageNamePath().get(config.getPackageName());
        //
        String tableName = table.getTableName();
        // 类名
        String className = tableName;
        if (tableName.toLowerCase().startsWith(config.getPrefix().toLowerCase())) {
            // 去掉前缀
            className = tableName.substring(config.getPrefix().length());
        }
        className = JdbcUtils.underlineToHump(className, true);

        String tableComment = table.getTableComment();
        if (null != tableComment) {
            tableComment = tableComment.replaceAll("\\*/", "\\*\\/");
            table.setTableComment(tableComment);
        }
        Writer out = null;
        try {

            Configuration conf = new Configuration(Configuration.VERSION_2_3_0);
            conf.setClassForTemplateLoading(EntityGeneratorCode.class, "");
            Template template = conf.getTemplate("entity.ftl", "UTF-8");
            // 创建数据模型
            Map<String, Object> root = new HashMap<String, Object>();

            root.put("config", config);
            root.put("table", table);
            root.put("className", className);
            root.put("generateTime", generateTime);

            File file = new File(path + "/" + className + ".java");

            if (!config.isOverride() && file.exists()) {
                return;
            }

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            template.process(root, out);
            out.flush();

        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 预处理
     *
     * @param config
     * @param columns
     */
    public static void preprocessor(Config config, List<ColumnModel> columns) {

        for (ColumnModel column : columns) {
            String columnName = column.getColumnName();
            column.setObjectName(JdbcUtils.underlineToHump(columnName, false));
            column.setMethodName(JdbcUtils.underlineToHump(columnName, true));

            String javaType = dataType2JavaType(column, config.isBigDecimal());
            column.setJavaType(javaType);
        }


    }

    /**
     * mysql 类型转换
     *
     * @param column
     * @param bigDecimal
     * @return
     */
    private static String dataType2JavaType(ColumnModel column, boolean bigDecimal) {
        String dataType = column.getDataType().toLowerCase();
        String columnType = column.getColumnType().toLowerCase();
        BigInteger length = column.getLength();

        String BIG_DECIMAL = "java.math.BigDecimal";

        switch (dataType) {
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "longtext":
            case "mediumtext":
                return "String";
            case "tinyint":
                return length.intValue() == 1 ? "Boolean" : "Integer";
            case "smallint":
            case "int":
            case "integer":
                return "Integer";
            case "long":
                return "Long";
            case "bigint":
                // 无符号
                return columnType.endsWith("unsigned") || length.intValue() > 19 ? "java.math.BigInteger" : "Long";
            case "float":
                return bigDecimal ? BIG_DECIMAL : "Float";
            case "double":
                return bigDecimal ? BIG_DECIMAL : "Double";
            case "decimal":
                return BIG_DECIMAL;
            case "blob":
            case "tinyblob":
            case "mediumblob":
            case "longblob":
                return "byte[]";
            case "boolean":
            case "bit":
                return "Boolean";
            case "date":
            case "time":
            case "year":
            case "datetime":
            case "timestamp":
                return "java.util.Date";
            default:
                return "Object";
        }

    }
}
