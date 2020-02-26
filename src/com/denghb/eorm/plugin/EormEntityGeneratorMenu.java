package com.denghb.eorm.plugin;

import com.denghb.eorm.generator.EntityGenerator;
import com.denghb.eorm.generator.model.PackagePathModel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since
 */
public class EormEntityGeneratorMenu extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project project = event.getProject();
        String basePath = project.getBasePath();


        String sourceDir = "/src/main/java";
        // 查找package
        List<String> modules = new ArrayList<>();
        modules.add(basePath);
        File modulesXmlFile = new File(basePath + "/.idea/modules.xml");
        if (modulesXmlFile.exists()) {
            getModules(modulesXmlFile, basePath, modules);
        }

        Set<String> list = new HashSet<>();
        for (String dir : modules) {
            findPackage(dir + sourceDir, list);
        }

        List<PackagePathModel> packagePathModelList = new ArrayList<>();
        for (String dir : list) {
            String p = dir.substring(dir.indexOf(sourceDir) + sourceDir.length() + 1);

            p = p.replaceAll("/", ".");
            packagePathModelList.add(new PackagePathModel(p, dir));
        }

        System.out.println(list);
        EntityGenerator dialog = new EntityGenerator(basePath, packagePathModelList);
        dialog.setSize(550, 350);
        dialog.setAlwaysOnTop(true);
        dialog.setResizable(false);
//        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
    }

    private void getModules(File modulesXmlFile, String basePath, List<String> dirs) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(modulesXmlFile);
            NodeList modules = document.getElementsByTagName("module");

            for (int j = 0; j < modules.getLength(); j++) {
                Node node = modules.item(j);
                NamedNodeMap attr = node.getAttributes();
                String filepath = attr.getNamedItem("filepath").getNodeValue();
                // System.out.println(filepath);
                String[] ss = filepath.split("/");
                if (ss.length <= 2) {
                    continue;
                }

                StringBuilder sb = new StringBuilder(basePath);
                for (int i = 1; i < ss.length - 1; i++) {
                    sb.append("/");
                    sb.append(ss[i]);
                }
                dirs.add(sb.toString());

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                findPackage(filePath + "/" + file.getName(), list);
            } else {
                list.add(filePath);
            }
        }
    }

    public static void main(String[] args) {
        Set<String> list = new HashSet<String>();
        String[] dirs = {"/Users/mac/Documents/idea-plugin/test1/src/main/java",
                "/Users/mac/Documents/idea-plugin/test2/test2-module1/src/main/java"};

        EormEntityGeneratorMenu menuAnAction = new EormEntityGeneratorMenu();
        for (String dir : dirs) {
            menuAnAction.findPackage(dir, list);
        }
        for (String p : list) {
            for (String dir : dirs) {
                if (p.startsWith(dir)) {
                    String pg = p.replace(dir + "/", "").replaceAll("/", ".");
                    System.out.println(pg);
                }
            }
        }
        System.out.println(list);
    }
}
