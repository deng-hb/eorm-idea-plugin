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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

/**
 * @author denghb 2019-06-27 00:16
 */
public class MultiLineSQLSmartTipHandler extends TypedHandlerDelegate {

    private static final String DB_URL = "jdbc:mysql://%s:%s/%s?useUnicode=true&amp;characterEncoding=utf-8";
    private Connection connection;

    private Eorm db;

    private String schema;

    private String sql1 = "SELECT column_name FROM information_schema.COLUMNS WHERE table_schema = ? AND table_name = ? ";

    private Map<String, String> aliasTableHub = new HashMap<String, String>();

    public MultiLineSQLSmartTipHandler() {
        System.out.println("MultiLineSQLSmartTipHandler init");
    }

    private void conn(String basePath) {
        try {
            if (null != connection) {
                return;
            }
            Properties properties = new Properties();
            File file1 = new File(basePath + "/eorm.config");
            if (!file1.exists()) {
                return;
            }
            properties.load(new FileInputStream(file1));
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

            System.out.println("Connection ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testParseAlias(String sql) {
        try {
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect) selectBody;
            Table table = (Table) plainSelect.getFromItem();

            aliasTableHub.put(table.getName(), table.getName());

            if (table.getAlias() != null) {
                aliasTableHub.put(table.getAlias().getName(), table.getName());
            }
            if (null != plainSelect.getJoins()) {

                for (Join join : plainSelect.getJoins()) {
                    Table table1 = (Table) join.getRightItem();
                    if (table1.getAlias() != null) {
                        aliasTableHub.put(table1.getAlias().getName(), table1.getName());

                        aliasTableHub.put(table.getName(), table.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSql(int caretOffset, String text) {
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

    @NotNull
    @Override
    public Result beforeCharTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, @NotNull FileType fileType) {
        conn(project.getBasePath());

        Document document = editor.getDocument();
        String text = document.getText();
        int caretOffset = editor.getCaretModel().getOffset();
        String sql = getSql(caretOffset, text);
        if (' ' == c && null != sql) {
            testParseAlias(sql);
        }
        if ('.' == c && null != sql) {
            document.insertString(caretOffset, String.valueOf(c));
            editor.getCaretModel().moveToOffset(caretOffset + 1);

            // 获取表名
            if (' ' != text.charAt(caretOffset - 1)) {
                StringBuilder tableName = new StringBuilder();
                for (int i = caretOffset - 1; i > 0; i--) {
                    char c2 = text.charAt(i);
                    if (' ' == c2 || ',' == c2) {
                        break;
                    }
                    tableName.append(c2);
                }

                tableName.reverse();
                String table = tableName.toString();

                table = aliasTableHub.get(table);
                if (null != table) {

                    List<String> tableNames = db.select(String.class, sql1, schema, table);
                    LookupElement[] lookupElement = new LookupElement[tableNames.size()];
                    for (int i = 0; i < lookupElement.length; i++) {
                        lookupElement[i] = LookupElementBuilder.create(tableNames.get(i));
                    }
                    LookupManager.getInstance(project).showLookup(editor, lookupElement);
                }
            }


            return Result.STOP;
        }
        return super.beforeCharTyped(c, project, editor, file, fileType);
    }
}
