package com.denghb.eorm.plugin;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.generator.model.ColumnModel;
import com.denghb.eorm.generator.model.TableModel;
import com.denghb.eorm.impl.EormImpl;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL智能提示
 *
 * <pre>
 *     1、点`.`后面会提示别名对应表字段
 *     2、空格` `在`from`和`join`后面会提示表名
 * </pre>
 *
 * @author denghb
 * @since 2019-06-27 00:16
 */
public class MultiLineSQLSmartTipHandler extends TypedHandlerDelegate {

    private static final String EORM_CONFIG = "eorm.config";
    private static final String DB_URL = "jdbc:mysql://%s:%s/%s?useUnicode=true&amp;characterEncoding=utf-8";
    private static final String SQL_TABLE = "select table_name, table_comment from information_schema.tables where table_schema = ?";
    private static final String SQL_COLUMN = "select column_name, column_type, column_comment from information_schema.columns where table_schema = ? and table_name = ? ";

    private static final List<TableModel> DATA_TABLES = new Vector<TableModel>();
    private static final Map<String, List<ColumnModel>> DATA_COLUMNS = new ConcurrentHashMap<String, List<ColumnModel>>();

    private String projectPath;

    public MultiLineSQLSmartTipHandler() {
        WindowManager.getInstance();

        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project project = ProjectManager.getInstance().getDefaultProject();
        outLog("MultiLineSQLSmartTipHandler init");
    }

    @NotNull
    @Override
    public Result beforeCharTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, @NotNull FileType fileType) {

        outLog("@@@@" + project.getBasePath());
        if (null == projectPath) {
            projectPath = project.getBasePath();
            outLog("projectPath init");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    loadTableColumns();// 定时更新
                }
            }, 0, 60 * 1000);// 每隔60秒执行一次
        }

        if ('.' != c && ' ' != c) {
            return super.beforeCharTyped(c, project, editor, file, fileType);
        }

        Document document = editor.getDocument();
        int caretOffset = editor.getCaretModel().getOffset();

        String text = document.getText();
        String sql = getEOrmSQL(caretOffset, text);
        if (null == sql) {
            return super.beforeCharTyped(c, project, editor, file, fileType);
        }

        document.insertString(caretOffset, String.valueOf(c));
        editor.getCaretModel().moveToOffset(caretOffset + 1);

        if (' ' == c && nextKeyInTable(caretOffset, text) && !DATA_TABLES.isEmpty()) {

            // 显示提示内容
            LookupElement[] lookupElement = new LookupElement[DATA_TABLES.size()];
            for (int i = 0; i < lookupElement.length; i++) {
                TableModel table = DATA_TABLES.get(i);
                lookupElement[i] = LookupElementBuilder.create(table.getTableName())
                        .withTypeText(table.getTableComment(), true);
            }
            LookupManager.getInstance(project).showLookup(editor, lookupElement);
            return Result.STOP;
        }

        if ('.' != c) {
            return Result.STOP;
        }

        // 获取表名
        String tableName = getTableName(caretOffset, text, sql);
        if (null == tableName) {
            return Result.STOP;
        }

        // 显示提示内容
        List<ColumnModel> columns = DATA_COLUMNS.get(tableName);
        if (null == columns || columns.isEmpty()) {
            return Result.STOP;
        }
        LookupElement[] lookupElement = new LookupElement[columns.size()];
        for (int i = 0; i < lookupElement.length; i++) {
            ColumnModel column = columns.get(i);
            lookupElement[i] = LookupElementBuilder.create(column.getColumnName())
                    .withTailText(" " + column.getColumnComment(), true)
                    .withTypeText(column.getColumnType());
        }
        LookupManager.getInstance(project).showLookup(editor, lookupElement);

        return Result.STOP;
    }

    private boolean nextKeyInTable(int caretOffset, String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = caretOffset; i > 0; i--) {
            char c = text.charAt(i);
            if ('\n' == c) {
                continue;
            }
            if (' ' == c) {
                if (sb.length() > 0) {
                    break;
                } else {
                    continue;
                }
            }
            sb.append(c);
        }
        sb.reverse();// 翻转

        // ？还有很多情况是要输入表名
        String s = sb.toString();
        if ("join".equalsIgnoreCase(s) || "from".equalsIgnoreCase(s)) {
            return true;
        }

        return false;
    }

    private void outLog(String log) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        System.out.println(sdf.format(new Date()) + ":" + log);

    }

    private void outLog(Exception e) {
        outLog(e.getMessage());
        e.printStackTrace();
    }


    private void loadTableColumns() {
        outLog("start");
        if (null == projectPath) {
            outLog("projectPath is null");
            return;
        }

        Connection connection = null;
        try {
            File file1 = new File(projectPath + "/" + EORM_CONFIG);
            if (!file1.exists()) {
                return;
            }
            Properties properties = new Properties();
            FileInputStream is = new FileInputStream(file1);
            properties.load(is);
            is.close();

            String username = getProperty(properties, "username");
            String password = getProperty(properties, "password");
            String port = getProperty(properties, "port");
            String host = getProperty(properties, "host");
            String database = getProperty(properties, "database");

            String url = String.format(DB_URL, host, port, database);
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            Eorm db = new EormImpl(connection);

            List<TableModel> tables = db.select(TableModel.class, SQL_TABLE, database);
            if (null != tables && !tables.isEmpty()) {
                DATA_TABLES.clear();

                for (TableModel table : tables) {
                    DATA_TABLES.add(table);
                    String tableName = table.getTableName();
                    List<ColumnModel> columns = db.select(ColumnModel.class, SQL_COLUMN, database, tableName);
                    DATA_COLUMNS.put(tableName, columns);
                }
            }

            outLog("end DATA_TABLES:" + DATA_TABLES.size());
        } catch (Exception e) {
            outLog(e);
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                    connection = null;
                }
            } catch (SQLException ex) {

            }
        }
    }

    private String getProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (null != value) {
            value = value.trim();
        }
        return value;
    }

    private String getEOrmSQL(int caretOffset, String text) {
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


}