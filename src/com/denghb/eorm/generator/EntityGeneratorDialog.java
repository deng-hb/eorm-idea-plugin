package com.denghb.eorm.generator;

import com.denghb.eorm.generator.model.TableModel;
import com.denghb.eorm.provider.TableDataCallback;
import com.denghb.eorm.provider.TableDataProvider;
import com.denghb.eorm.utils.JdbcUtils;
import com.denghb.eorm.plugin.utils.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
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
                onLoad();
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

    private void onLoad() {
        refreshConfig();

        String jdbc = config.getJdbc();
        if (StringUtils.isBlank(loadButton.getText())) {
            System.out.println("loading ..");
            return;
        }
        if (StringUtils.isBlank(jdbc) || !jdbc.contains("mysql")) {
            handlerMessage("JDBC connection only MySQL");
            return;
        }

        URL url = getClass().getResource("loading.gif");
        ImageIcon icon = new ImageIcon(url);
        loadButton.setIcon(icon);
        loadButton.setText("");

        TableDataProvider.load(jdbc, new TableDataCallback() {
            @Override
            public void on(List<TableModel> tables) {
                data.clear();
                data.addAll(tables);

                origin.clear();
                origin.addAll(tables);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dataTable.updateUI();

                        loadButton.setIcon(null);
                        loadButton.setText("Load");
                    }
                });
            }

            @Override
            public void onMessage(String message) {
                handlerMessage(message);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        loadButton.setIcon(null);
                        loadButton.setText("Load");
                    }
                });
            }
        });
    }

    private void handlerMessage(String message) {
        if (null != entityGeneratorHandler) {
            entityGeneratorHandler.onMessage(message);
        }
    }

    private void initConfig() {
        jdbcField.setText(config.getJdbc());
        prefixField.setText(config.getPrefix());
        authorField.setText(config.getAuthor());

        lombokCheckBox.setSelected(config.isLombok());
        bigDecimalCheckBox.setSelected(config.isBigDecimal());
        sinceCheckBox.setSelected(config.isSince());

        overrideCheckBox.setSelected(config.isOverride());
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
        config.setSchema(schemaCheckBox.isSelected());

        config.setPackageName((String) packageComboBox.getSelectedItem());

        config.setDatabase(JdbcUtils.getDatabase(jdbcField.getText()));

        if (null != entityGeneratorHandler) {
            entityGeneratorHandler.onConfig(config);
        }
    }

    private void onOK() {
        refreshConfig();

        if (null != entityGeneratorHandler) {
            entityGeneratorHandler.onCallback(data);
        }
    }

    private void onCancel() {
        dispose();
    }

    public EntityGeneratorHandler getEntityGeneratorHandler() {
        return entityGeneratorHandler;
    }

    public void setEntityGeneratorHandler(EntityGeneratorHandler entityGeneratorHandler) {
        this.entityGeneratorHandler = entityGeneratorHandler;
    }
}
