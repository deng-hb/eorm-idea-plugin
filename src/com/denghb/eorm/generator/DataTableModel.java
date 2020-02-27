package com.denghb.eorm.generator;

import com.denghb.eorm.generator.model.TableModel;

import javax.swing.table.AbstractTableModel;
import java.util.List;

// 表头数据绑定
public class DataTableModel extends AbstractTableModel {

    String[] n = {"Table Name", "Comment", ""};

    private List<TableModel> data;

    public DataTableModel(List<TableModel> data) {
        this.data = data;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return n.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TableModel model = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return model.getTableName();
            case 1:
                return model.getTableComment();
            case 2:
                return model.isChecked();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return n[column];
    }


    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2;// 复选框可编辑
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // System.out.println("setValueAt" + aValue);
        if (columnIndex == 2) {
            data.get(rowIndex).setChecked((Boolean) aValue);
        }

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void selectAllOrNull(boolean value) {
        // Select All. The last column
        for (int index = 0; index < getRowCount(); index++) {
            this.setValueAt(value, index, getColumnCount() - 1);
        }
    }
}
