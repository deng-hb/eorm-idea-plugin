package com.denghb.eorm.generator;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.generator.model.DataModel;
import com.denghb.eorm.generator.model.DataTableModel;
import com.denghb.eorm.generator.model.PackagePathModel;
import com.denghb.eorm.generator.model.Table;
import com.denghb.eorm.impl.EormImpl;
import com.denghb.eorm.plugin.MultiLineSQLSmartTipHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.List;

public class EntityGenerator extends JDialog {
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

    private Vector<DataModel> data = new Vector<DataModel>();

    public EntityGenerator(String basePath, List<PackagePathModel> packagePath) {
        this.setTitle("Eorm entity generator");
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        for (PackagePathModel pp : packagePath) {
            packageComboBox.addItem(pp);
        }
        packageComboBox.setSelectedItem("org.example");
        packageComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                PackagePathModel pp = (PackagePathModel) e.getItem();
                System.out.println(pp);
            }
        });
        packageComboBox.updateUI();

        loadButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = jdbcField.getText();
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection connection = DriverManager.getConnection(url);
                    Eorm db = new EormImpl(connection);

                    List<Table> tables = db.select(Table.class, Consts.SQL_TABLE, "test");

                    System.out.println(tables);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        DataModel dataModel = new DataModel();
        dataModel.setChecked(false);
        dataModel.setTableName("asdasdsa");
        dataModel.setTableComment("dasdada");
        data.add(dataModel);


        dataTable.setModel(new DataTableModel(data));
        dataTable.getTableHeader().setDefaultRenderer(new CheckHeaderCellRenderer(dataTable));

        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 20));
        //设置表内容行高
        dataTable.setRowHeight(25);
        //设置单选模式
        dataTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //设置单元格不可拖动
        dataTable.getTableHeader().setReorderingAllowed(false);
        dataTable.getColumnModel().getColumn(2).setPreferredWidth(30);

        dataTable.updateUI();

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
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        List<PackagePathModel> packagePathModelList = new ArrayList<>();
        EntityGenerator dialog = new EntityGenerator("/", packagePathModelList);
        dialog.pack();
        dialog.setVisible(true);
        //System.exit(0);
    }

}
