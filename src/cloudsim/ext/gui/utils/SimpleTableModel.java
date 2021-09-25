package cloudsim.ext.gui.utils;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 * A simple table model to be used in screens. Incorporates some validation functionality.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class SimpleTableModel extends AbstractTableModel {
    private String[] columnNames;
    private List<Object[]> data;
    private List<Integer> uniqueColumns;
    private List<Integer> notNullColumns;
    private List<Integer> notEditableColumns;
    private Component holder;
    	   
	public SimpleTableModel(String[] columns, Component holder){
    	this.columnNames = columns;
    	this.holder = holder;
    	data = new ArrayList<Object[]>();
    }
	
	public SimpleTableModel(String[] columns){
    	this(columns, null);
    }
	
    
    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        Object[] dataRow = data.get(row);
    	return dataRow[col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        if ((notEditableColumns != null) && notEditableColumns.contains(col)){
        	return false;
        } else {
        	return true;
        }
    }
    
    public void setValueAt(Object value, int row, int col) {
    	boolean notNull = true;
    	if ((notNullColumns != null) && (notNullColumns.contains(col))){
    		if ((value instanceof String) && (value.equals(""))){
    			notNull = false;
    		} else {
    			notNull = (value == null) ? false: true;
    		}
    	}
    	
    	boolean unique = true;
    	if ((uniqueColumns != null) && (uniqueColumns.contains(col))){
    		unique = isUnique(value, col);
    	}
    		    	
    	if (!notNull) {
    		JOptionPane.showMessageDialog(holder, 
    									  getColumnName(col) + " cannot be null!",
    									  "Invalid Data",
    									  JOptionPane.ERROR_MESSAGE);
    	} else if (!unique){
    		JOptionPane.showMessageDialog(holder, 
					  getColumnName(col) + " needs to be unique!",
					  "Invalid Data",
					  JOptionPane.ERROR_MESSAGE);
    	} else {
	    	Object[] dataRow = data.get(row);
	    	dataRow[col] = value;
	        fireTableCellUpdated(row, col);
    	} 
    }
    
    public void addRow(Object[] newRow){
    	int lastRow = data.size();
    	data.add(newRow);
    	fireTableRowsInserted(lastRow + 1, lastRow + 1);
    }
    
    public void deleteRow(int row){
    	data.remove(row);
    	fireTableRowsDeleted(row, row);
    }
    
    public void setData(List<Object[]> data){
    	this.data = data;
    }
    
    public List<Object[]> getData(){
    	return data;
    }
    
    /**
	 * @return the uniqueColumns
	 */
	public List<Integer> getUniqueColumns() {
		return uniqueColumns;
	}

	public void setUniqueColumns(int[] cols){
		if (uniqueColumns == null){
			uniqueColumns = new ArrayList<Integer>();
		}
		
		for (int col : cols){
			uniqueColumns.add(col);
		}
	}
	/**
	 * @param uniqueColumns the uniqueColumns to set
	 */
	public void setUniqueColumns(List<Integer> uniqueColumns) {
		this.uniqueColumns = uniqueColumns;
	}

	public void setNotNullColumns(int[] cols){
		if (notNullColumns == null){
			notNullColumns = new ArrayList<Integer>();
		}
		
		for (int col : cols){
			notNullColumns.add(col);
		}
	}
	/**
	 * @return the notNullColumns
	 */
	public List<Integer> getNotNullColumns() {
		return notNullColumns;
	}

	/**
	 * @param notNullColumns the notNullColumns to set
	 */
	public void setNotNullColumns(List<Integer> notNullColumns) {
		this.notNullColumns = notNullColumns;
	}

    public boolean isUnique(Object val, int col){
		for (int i = 0; i < getRowCount(); i++){
			if (val.equals(getValueAt(i, col))){
				return false;
			}
		}
		return true;
	}
    
    public void clearData(){
    	data.clear();
    }
    
    public void setNotEditableColumns(int[] cols){
    	if (notEditableColumns == null){
    		notEditableColumns = new ArrayList<Integer>();
		}
		
		for (int col : cols){
			notEditableColumns.add(col);
		}
    }
    
}

