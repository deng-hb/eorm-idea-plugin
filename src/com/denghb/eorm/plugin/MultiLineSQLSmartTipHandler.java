package com.denghb.eorm.plugin;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author denghb 2019-06-27 00:16
 */
public class MultiLineSQLSmartTipHandler implements TypedActionHandler {

    private static final String DB_URL = "jdbc:mysql://%s:%s/%s?useUnicode=true&amp;characterEncoding=utf-8";
    private Connection connection;

    private String sql = "SELECT column_Name,data_Type,column_Key,column_Comment FROM information_schema.COLUMNS WHERE table_schema = 'test' AND table_name = 'user' ";

    @Override
    public void execute(@NotNull Editor editor, char c, @NotNull DataContext dataContext) {
        Document document = editor.getDocument();
        Project project = editor.getProject();

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {

                // 判断当前是否是在 ""/*{}*/; 中编辑，然后获取数据库链接，缓存表结构（2分钟），根据表名提示字段，达到自动提示
                String text = document.getText();
                int caretOffset = editor.getCaretModel().getOffset();
                document.insertString(caretOffset, String.valueOf(c));
                System.out.println(c);
                editor.getCaretModel().moveToOffset(caretOffset + 1);

                if (null == connection) {
                    try {
                        String url = String.format(DB_URL, "localhost", "3306", "test");
                        // 注册 JDBC 驱动
                        Class.forName("com.mysql.jdbc.Driver");

                        // 打开链接
                        System.out.println("Connection ...");
                        connection = DriverManager.getConnection(url, "root", "");

                        PreparedStatement ps = connection.prepareStatement(sql);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
