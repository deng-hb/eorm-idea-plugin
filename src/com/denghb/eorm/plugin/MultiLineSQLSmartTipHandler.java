package com.denghb.eorm.plugin;

import com.denghb.eorm.generator.Config;
import com.denghb.eorm.generator.Consts;
import com.denghb.eorm.generator.model.ColumnModel;
import com.denghb.eorm.generator.model.TableModel;
import com.denghb.eorm.provider.TableDataCallback;
import com.denghb.eorm.provider.TableDataProvider;
import com.google.gson.Gson;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

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

    private static final List<TableModel> DATA_TABLES = new ArrayList<TableModel>();

    private String projectPath;

    public MultiLineSQLSmartTipHandler() {
        outLog("MultiLineSQLSmartTipHandler init");
    }

    @NotNull
    @Override
    public Result beforeCharTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, @NotNull FileType fileType) {

        if (null == projectPath) {
            projectPath = project.getBasePath();
            outLog("projectPath init");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    loadTableColumns();// 定时更新
                }
            }, 0, 60 * 1000);
        }

        if ('.' != c && ' ' != c) {
            return super.beforeCharTyped(c, project, editor, file, fileType);
        }

        Document document = editor.getDocument();
        int caretOffset = editor.getCaretModel().getOffset();

        String text = document.getText();
        String sql = getEormSQL(caretOffset, text);
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
        List<ColumnModel> columns = null;

        for (TableModel table : DATA_TABLES) {
            if (tableName.equalsIgnoreCase(table.getTableName())) {
                columns = table.getColumns();
            }
        }
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

    private void loadTableColumns() {

        PropertiesComponent pc = PropertiesComponent.getInstance();
        String keyConfig = DigestUtils.md5Hex(projectPath) + Consts.GENERATOR_CONFIG;
        Config config = new Gson().fromJson(pc.getValue(keyConfig), Config.class);
        if (null != config) {
            TableDataProvider.load(config.getJdbc(), new TableDataCallback() {
                @Override
                public void on(List<TableModel> tables) {
                    DATA_TABLES.clear();
                    DATA_TABLES.addAll(tables);
                    System.out.println("tables init " + tables.size());
                }

                @Override
                public void onMessage(String message) {
                    outLog(message);
                }
            });
        }
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

    private String getEormSQL(int caretOffset, String text) {
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