package com.denghb.eorm.plugin;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.impl.EormImpl;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * @author denghb 2019-06-27 00:16
 */
public class MultiLineSQLSmartTipHandler extends TypedHandlerDelegate {

    private static final String EORM_CONFIG = "eorm.config";
    private static final String DB_URL = "jdbc:mysql://%s:%s/%s?useUnicode=true&amp;characterEncoding=utf-8";
    private static final String sql1 = "SELECT column_name,column_type,column_comment FROM information_schema.COLUMNS WHERE table_schema = ? AND table_name = ? ";

    private Eorm db;
    private String schema;
    private Connection connection;

    public MultiLineSQLSmartTipHandler() {
        System.out.println("MultiLineSQLSmartTipHandler init");
    }

    @NotNull
    @Override
    public Result beforeCharTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, @NotNull FileType fileType) {
        if (EORM_CONFIG.equals(file.getName())) {
            close();
        }
        if ('.' != c) {
            return super.beforeCharTyped(c, project, editor, file, fileType);
        }

        Document document = editor.getDocument();
        int caretOffset = editor.getCaretModel().getOffset();

        String text = document.getText();
        String sql = getSql(caretOffset, text);
        if (null == sql) {
            return super.beforeCharTyped(c, project, editor, file, fileType);
        }

        document.insertString(caretOffset, String.valueOf(c));
        editor.getCaretModel().moveToOffset(caretOffset + 1);

        // 获取表名
        String tableName = getTableName(caretOffset, text, sql);
        if (null != tableName) {

            conn(project.getBasePath());

            if (null == db) {
                return Result.STOP;
            }

            List<Column> columns = db.select(Column.class, sql1, schema, tableName);
            if (null == columns || columns.isEmpty()) {
                return Result.STOP;
            }
            LookupElement[] lookupElement = new LookupElement[columns.size()];
            for (int i = 0; i < lookupElement.length; i++) {
                Column column = columns.get(i);
                lookupElement[i] = LookupElementBuilder.create(column.getColumnName())
                        .withTailText(" " + column.getColumnComment(), true)
                        .withTypeText(column.getColumnType());
            }
            LookupManager.getInstance(project).showLookup(editor, lookupElement);
        }

        return Result.STOP;
    }


    private void conn(String basePath) {

        try {
            if (null != db) {
                return;
            }
            File file1 = new File(basePath + "/" + EORM_CONFIG);
            if (!file1.exists()) {
                return;
            }
            Properties properties = new Properties();
            FileInputStream is = new FileInputStream(file1);
            properties.load(is);
            is.close();

            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            String port = properties.getProperty("port");
            String host = properties.getProperty("host");
            String database = properties.getProperty("database");

            schema = database;
            String url = String.format(DB_URL, host, port, database);
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);

            db = new EormImpl(connection);

            System.out.println("Connected");
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    private void close() {
        if (null != db) {
            db = null;
        }
        if (null != connection) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException ex) {

            }
        }
    }

    private String getSql(int caretOffset, String text) {
        if (!text.contains("\"\"/*{") || !text.contains("}*/;")) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 往前找 /*{
        for (int i = caretOffset; i > 0; i--) {
            char c = text.charAt(i);
            if (c == '/' && text.charAt(i - 1) == '*' && '}' == text.charAt(i - 2)) {
                return null;
            }
            if (c == '{' && text.charAt(i - 1) == '*' && '/' == text.charAt(i - 2)) {
                break;
            }
            sb.append(c);
        }
        sb.reverse();// 翻转
        // 往后找 }*/
        for (int i = caretOffset; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '}' && text.charAt(i + 1) == '*' && '/' == text.charAt(i + 2)) {
                break;
            }
            sb.append(c);
        }

        return sb.toString();
    }

    private static String getTableName(int caretOffset, String text, String sql) {

        StringBuilder sb = new StringBuilder();

        for (int i = caretOffset - 1; i > 0; i--) {
            char c = text.charAt(i);
            if (' ' == c || ',' == c || '\n' == c) {
                break;
            }
            sb.append(c);
        }
        if (sb.length() == 0) {
            return null;
        }
        String alias = sb.reverse().toString().toLowerCase();

        // 转小写来匹配获取 from xxx ...  alias 结束这段
        String sql1 = sql.toLowerCase();
        // 排除换行等符号影响
        sql1 = sql1.replaceAll("\n", " ")
                .replaceAll("\r", " ")
                .replaceAll("\t", " ");
        String ss = sql1.substring(sql1.indexOf(" from "));
        if (ss.length() > 6) {
            ss = ss.replaceAll(",", " ,");
            int end = ss.indexOf(" " + alias + " ");
            if (end < 6) {
                return alias;
            }

            ss = ss.substring(6, end).trim();

            StringBuilder tableName = new StringBuilder();
            for (int j = ss.length() - 1; j >= 0; j--) {
                char c = ss.charAt(j);
                if (' ' == c || ',' == c || '\n' == c) {
                    break;
                }
                tableName.append(c);

            }
            if (tableName.length() == 0) {
                return alias;
            }
            String name = tableName.reverse().toString();
            if ("join".equals(name)) {
                return alias;
            }
            if (name.endsWith(")")) {
                return null;
            }
            return name;
        }

        return alias;
    }

    public static class Column {

        public String columnName;

        public String columnType;

        public String columnComment;

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

        @Override
        public String toString() {
            return "Column{" +
                    "columnName='" + columnName + '\'' +
                    ", columnType='" + columnType + '\'' +
                    ", columnComment='" + columnComment + '\'' +
                    '}';
        }
    }
}