package com.denghb.eorm.plugin;

import com.denghb.eorm.generator.*;
import com.denghb.eorm.generator.model.TableModel;
import com.denghb.eorm.plugin.utils.JSON;
import com.denghb.eorm.plugin.utils.MD5Utils;
import com.denghb.eorm.plugin.utils.StringUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Idea Plugin
 *
 * @author denghb
 * @since 2020/2/27
 */
public class EormEntityGeneratorMenu extends AnAction {

    // 存Java源代码的文件目录 src/java/main
    private final static String SOURCE_DIR = "src" + File.separator + "main" + File.separator + "java";

    // 存实体类的包名
    private final static List<String> ENTITY_NAMES = Arrays.asList("entity", "domain");

    private static String KEY_CONFIG = null;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project project = event.getProject();
        String basePath = project.getBasePath();

        KEY_CONFIG = MD5Utils.text(basePath) + Consts.GENERATOR_CONFIG;

        List<String> sourceDirs = new ArrayList<>();
        findSourceDir(sourceDirs, basePath);

        // com.denghb, /Users/denghb/IdeaProjects/xxx/com/denghb
        Map<String, String> packageNamePath = new HashMap<>();
        for (String path : sourceDirs) {
            int start = path.indexOf(SOURCE_DIR);
            if (path.length() > start + SOURCE_DIR.length() + 1) {
                String name = path.substring(start + SOURCE_DIR.length() + 1);
                name = name.replaceAll("\\\\", ".");
                name = name.replaceAll("/", ".");
                packageNamePath.put(name, path);
            }
        }
        Config config = getConfig();
        if (null == config) {
            config = new Config();
            config.setJdbc("jdbc:mysql://localhost:3306/test?connectTimeout=3000&useUnicode=true&characterEncoding=utf8&user=root&password=123456");
            config.setAuthor(System.getProperties().getProperty("user.name"));
        }
        config.setPackageNamePath(packageNamePath);

        EntityGeneratorDialog dialog = new EntityGeneratorDialog(config);
        dialog.setEntityGeneratorHandler(new EntityGeneratorHandler() {

            @Override
            public void onCallback(List<TableModel> data) {
                try {
                    doExc(data);
                } catch (Exception e) {
                    showMessage(e.getMessage(), "Error");
                }
            }

            @Override
            public void onMessage(String message) {
                showMessage(message, "Warning");
            }

            @Override
            public void onConfig(Config config) {
                setConfig(config);
            }
        });
        dialog.setSize(560, 400);
        dialog.setAlwaysOnTop(true);
        // dialog.setResizable(false);
//        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
    }

    private void doExc(List<TableModel> data) {
        String generateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        int succ = 0;
        int fail = 0;
        Config config = getConfig();
        if (StringUtils.isBlank(config.getPackageName())) {
            showMessage("Package Not Empty \nPackage name eq 'entity' or 'domain'", "Error");
            return;
        }
        for (TableModel table : data) {
            if (table.isChecked()) {
                try {
                    EntityGeneratorCode.doExec(table, config, generateTime);
                    succ++;
                } catch (Exception e) {
                    fail++;
                    e.printStackTrace();
                    showMessage(e.getMessage(), "Error");
                }
            }
        }
        if (succ > 0 || fail > 0) {
            showMessage(String.format("Success:%d\nFail:%d", succ, fail), "Info");
        }
    }


    private Config getConfig() {
        Config config = null;
        try {
            PropertiesComponent pc = PropertiesComponent.getInstance();
            String json = pc.getValue(KEY_CONFIG);
            System.out.println(json);

            config = JSON.parseJSON(Config.class, json);
        } catch (Exception e) {
            showMessage(e.getMessage(), "Warning");
        }
        return config;
    }

    private void setConfig(Config config) {
        try {
            PropertiesComponent pc = PropertiesComponent.getInstance();
            pc.setValue(KEY_CONFIG, JSON.toJSON(config));
        } catch (Exception e) {
            showMessage(e.getMessage(), "Warning");
        }
    }

    private void showMessage(String message, String title) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Messages.showErrorDialog(message, title);
            }
        });
    }

    private void findPackage(String filePath, Set<String> list) {
        File dir = new File(filePath);
        if (!dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (null == files) {
            return;
        }
        if (0 == files.length) {
            list.add(filePath);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                findPackage(filePath + File.separator + file.getName(), list);
            } else {
                list.add(filePath);
            }
        }
    }

    private static void findSourceDir(List<String> sourceDirs, String fileDir) {
        File file = new File(fileDir);
        File[] files = file.listFiles();// 获取目录下的所有文件或文件夹
        if (files == null) {// 如果目录为空，直接退出
            return;
        }
        // 所有目录
        for (File f : files) {
            if (!f.isDirectory()) {
                continue;
            }

            String path = f.getAbsolutePath();
            if (path.contains(SOURCE_DIR) && ENTITY_NAMES.contains(f.getName())) {
                sourceDirs.add(path);
            }
            findSourceDir(sourceDirs, f.getAbsolutePath());
        }
    }

    public static void main(String[] args) {


        String basePath = "/Users/mac/IdeaProjects/tengyue";

        List<String> sourceDirs = new ArrayList<>();
        findSourceDir(sourceDirs, basePath);

        Map<String, String> packageNamePath = new HashMap<>();
        for (String path : sourceDirs) {
            int start = path.indexOf(SOURCE_DIR);
            if (path.length() > start + SOURCE_DIR.length() + 1) {
                String name = path.substring(start + SOURCE_DIR.length() + 1);
                name = name.replaceAll("/", ".");
                packageNamePath.put(name, path);
            }
        }
        System.out.println(packageNamePath);

    }
}
