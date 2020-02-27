package com.denghb.eorm.generator;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.generator.model.ColumnModel;
import com.denghb.eorm.generator.model.TableModel;
import com.denghb.eorm.impl.EormImpl;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class EntityGeneratorDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable dataTable;
    private JCheckBox lombokCheckBox;
    private JTextField filterField;
    private JCheckBox overrideCheckBox;
    private JButton loadButton;
    private JComboBox packageComboBox;
    private JTextField jdbcField;
    private JCheckBox schemaCheckBox;
    private JCheckBox sinceCheckBox;
    private JTextField authorField;
    private JTextField prefixField;
    private JCheckBox DDLCheckBox;
    private JCheckBox bigDecimalCheckBox;

    private List<TableModel> data = new ArrayList<TableModel>();
    private List<TableModel> origin = new ArrayList<TableModel>();
    private Config config;
    private EntityGeneratorHandler entityGeneratorHandler;

    public EntityGeneratorDialog(Config config) {
        this.setTitle("Eorm entity generator");
        this.config = config;
        initConfig();

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        // 表格
        dataTable.setModel(new DataTableModel(data));
        dataTable.getTableHeader().setDefaultRenderer(new DataTableHeaderCellRenderer(dataTable));
        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 20));
        //设置表内容行高
        dataTable.setRowHeight(25);
        //设置单选模式
        dataTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //设置单元格不可拖动
        dataTable.getTableHeader().setReorderingAllowed(false);
        dataTable.getColumnModel().getColumn(2).setPreferredWidth(30);

        dataTable.updateUI();

        loadButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        onLoad();
                    }
                });
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // 过滤
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterData(filterField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterData(filterField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterData(filterField.getText());
            }
        });
    }

    private void initConfig() {
        jdbcField.setText(config.getJdbc());
        prefixField.setText(config.getPrefix());
        authorField.setText(config.getAuthor());

        lombokCheckBox.setSelected(config.isLombok());
        bigDecimalCheckBox.setSelected(config.isBigDecimal());
        sinceCheckBox.setSelected(config.isSince());

        overrideCheckBox.setSelected(config.isOverride());
        DDLCheckBox.setSelected(config.isDDL());
        schemaCheckBox.setSelected(config.isSchema());

        Map<String, String> pathData = config.getPackageNamePath();
        for (String name : pathData.keySet()) {
            packageComboBox.addItem(name);
        }
        packageComboBox.setSelectedItem(config.getPackageName());
        packageComboBox.updateUI();

    }

    private void filterData(String key) {
        data.clear();

        if (StringUtils.isBlank(key)) {
            data.addAll(origin);
        } else {
            for (TableModel table : origin) {
                if (table.getTableName().toLowerCase().contains(key.toLowerCase())
                        || table.getTableComment().contains(key)) {
                    data.add(table);
                }
            }
        }
        dataTable.updateUI();

    }

    private void refreshConfig() {
        config.setJdbc(jdbcField.getText());
        config.setPrefix(prefixField.getText());
        config.setAuthor(authorField.getText());

        config.setLombok(lombokCheckBox.isSelected());
        config.setOverride(overrideCheckBox.isSelected());
        config.setBigDecimal(bigDecimalCheckBox.isSelected());

        config.setSince(sinceCheckBox.isSelected());
        config.setDDL(DDLCheckBox.isSelected());
        config.setSchema(schemaCheckBox.isSelected());

        config.setPackageName((String) packageComboBox.getSelectedItem());

        config.setDatabase(getDatabase(jdbcField.getText()));
    }

    private String getDatabase(String jdbc) {
        if (null == jdbc) {
            return null;
        }
        int start = jdbc.lastIndexOf("/");
        int end = jdbc.indexOf("?");
        if (-1 == start || -1 == end) {
            return null;
        }
        return jdbc.substring(start + 1, end);
    }

    private void onOK() {
        // add your code here
        refreshConfig();

        System.out.println(config);
        // dispose();

        if (null != entityGeneratorHandler) {
            entityGeneratorHandler.onCallback(data, config);
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    private void onLoad() {
        Connection connection = null;
        try {
            refreshConfig();
            String jdbc = config.getJdbc();

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(jdbc);
            Eorm db = new EormImpl(connection);


            String database = config.getDatabase();
            List<TableModel> tables = db.select(TableModel.class, Consts.SQL_TABLE, database);

            data.clear();
            data.addAll(tables);
            dataTable.updateUI();

            origin.clear();
            origin.addAll(tables);


            List<ColumnModel> columns = db.select(ColumnModel.class, Consts.SQL_COLUMN, database);
            for (TableModel table : tables) {
                table.setColumns(new ArrayList<ColumnModel>());

                // 查询DDL 只能拼接
                String ddlSql = "show create table `" + database + "`.`" + table.getTableName() + "`";
                Map map = db.selectOne(Map.class, ddlSql);
                String ddl = (String) map.get("Create Table");
                table.setDDL(ddl);

                for (int i = 0; i < columns.size(); i++) {
                    ColumnModel column = columns.get(i);
                    if (column.getTableName().equals(table.getTableName())) {
                        table.getColumns().add(column);
                        columns.remove(i);
                        i--;
                    }
                }
            }

            System.out.println(tables);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public EntityGeneratorHandler getEntityGeneratorHandler() {
        return entityGeneratorHandler;
    }

    public void setEntityGeneratorHandler(EntityGeneratorHandler entityGeneratorHandler) {
        this.entityGeneratorHandler = entityGeneratorHandler;
    }

    public static void main(String[] args) {

        String packageName = "com.denghb.eorm.generator.model";

        Map<String, String> packageNamePath = new HashMap<>();
        packageNamePath.put(packageName, "/Users/mac/IdeaProjects/eorm-idea-plugin/src/com/denghb/eorm/generator/model");

        Config config = new Config();
        config.setJdbc("jdbc:mysql://10.113.31.231:3406/ppdai_xyg_credit?user=root&password=Abcd@1234&useUnicode=true&characterEncoding=utf8");
        config.setAuthor(System.getProperties().getProperty("user.name"));
        config.setPackageName(packageName);
        config.setPackageNamePath(packageNamePath);

        EntityGeneratorDialog dialog = new EntityGeneratorDialog(config);
        dialog.setSize(580, 350);
        dialog.setAlwaysOnTop(true);
        //dialog.setResizable(false);
//        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
        dialog.setEntityGeneratorHandler(new EntityGeneratorHandler() {
            @Override
            public void onCallback(List<TableModel> data, Config config) {

                String generateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                for (TableModel table : data) {
                    if (table.isChecked()) {
                        EntityGeneratorCode.doExec(table, config, generateTime);
                    }
                }
            }
        });

        //System.exit(0);
    }

}
