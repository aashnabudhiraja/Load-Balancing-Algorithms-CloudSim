package cloudsim.ext.gui.utils;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.xml.datatype.DatatypeConfigurationException;

/**
 * A table model that gets the data from a {@link List} of type DataType.
 * @author Bhathiya Wickremasinghe
 *
 * @param <DataType>
 */
abstract public class AbstractListTableModel<DataType> extends AbstractTableModel {
    protected String[] columnNames;
    protected List<Integer> uniqueColumns;
    protected List<Integer> notNullColumns;
    protected List<Integer> notEditableColumns;
    protected Component holder;
    protected List<DataType> data;
    	   
	public AbstractListTableModel(Component holder){
    	this.holder = holder;
    }
	
	public AbstractListTableModel(){
    	
    }
	
	public void setColumnNames(String[] columnNames){
		this.columnNames = columnNames;
	}
	
    public int getColumnCount() {
    	if (columnNames == null){
    		throw new RuntimeException("Column names list has to be set using setColumnNames(String[])");
    	}
        return columnNames.length;
    }    
    
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Class getColumnClass(int c) {    	
        Object firstDataVal = getValueAt(0, c);
        if (firstDataVal != null){
        	return firstDataVal.getClass();
        } else {
        	return String.class;
        }
    }
    
    public int getRowCount() {
    	if (data == null){
    		return 0;
    	} else {
    		return data.size();
    	}
	}

    public boolean isCellEditable(int row, int col) {
        if ((notEditableColumns != null) && notEditableColumns.contains(col)){
        	return false;
        } else {
        	return true;
        }
    }
    
    abstract protected void setValueAtInternal(Object value, int row, int col);
    
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
	    	setValueAtInternal(value, row, col);
	        fireTableCellUpdated(row, col);
    	} 
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
			if ((val != null) && (val.equals(getValueAt(i, col)))){
				return false;
			}
		}
		return true;
	}
    
    public void setNotEditableColumns(int[] cols){
    	if (notEditableColumns == null){
    		notEditableColumns = new ArrayList<Integer>();
		}
		
		for (int col : cols){
			notEditableColumns.add(col);
		}
    }
    
    public void addRow(DataType newRow){
    	int lastRow = data.size();
    	data.add(newRow);
    	fireTableRowsInserted(lastRow + 1, lastRow + 1);
    }
    
    public void deleteRow(int row){
    	data.remove(row);
    	fireTableRowsDeleted(row, row);
    }
}

