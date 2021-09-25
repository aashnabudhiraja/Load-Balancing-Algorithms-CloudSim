package cloudsim.ext.gui.screens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import cloudsim.ext.Constants;
import cloudsim.ext.InternetCharacteristics;
import cloudsim.ext.Simulation;
import cloudsim.ext.gui.utils.SimpleTableModel;
import cloudsim.ext.util.IOUtil;

/**
 * The screen used to configure Internet characteristics.
 *
 * @author Bhathiya Wickremasinghe
 *
 */
public class InternetCharacteristicsScreen extends JPanel implements ActionListener, Constants {

	public static final String CMD_CANCEL_INTERNET_CONFIG = "cancel_internet_behaviour";
	public static final String CMD_DONE_INTERNET_CONFIG = "done_internet_behaviour";
	private static final String LBL_CANCEL = "Cancel";
	private static final String LBL_DONE = "Done";
	private static final Dimension BTN_DIMENSION = new Dimension(100, 25);
	private static final int LBL_HEIGHT = 20;
	private static final int LABEL_WIDTH = 150;


	private SimpleTableModel delayMatixModel;
	private JTable delayMatrixTable;
	private final Simulation simulation;
	private final ActionListener screenListener;
	private final int numRegions = 6;
	private SimpleTableModel bwMatrixModel;
	private JTable bwTable;
	private final BorderLayout borderLayout = new BorderLayout();


	/** Constructor. */
	public InternetCharacteristicsScreen(Simulation sim, ActionListener screenListener){
		this.simulation = sim;
		this.screenListener = screenListener;
		init();
		loadDelayMatrix();
		loadBwMatrix();
	}

	private void init(){
		this.setLayout(null);

		Insets insets = this.getInsets();
		int leftMargin = insets.left + 50;
		int topMargin = insets.top;

		int x = leftMargin;
		int y = topMargin;

		JLabel heading = new JLabel("<html><p align='center'><h1>Configure Internet Characteristics</h1></p></html>");
		heading.setBounds(x, y, 500, 50);
		this.add(heading);

		y += 80;
		JLabel lblSimDuration = new JLabel("<html>Use this screen to configure the Internet characteristics.</html>");
		lblSimDuration.setBounds(x, y, 500, 20);
		this.add(lblSimDuration);

		y += 50;
		JPanel matrixPanel = createDelayMatrixPanel();
		matrixPanel.setBounds(x, y, 600, 220);
		this.add(matrixPanel);

		y += 250;
		JPanel bwPanel = createBwMatrixPanel();
		bwPanel.setBounds(x, y, 600, 220);
		this.add(bwPanel);

		y += 250;
		JPanel controlPanel = createControlPanel();
		controlPanel.setBounds(x, y, 500, 50);
		this.add(controlPanel);
	}

	private void loadDelayMatrix(){
		delayMatixModel.clearData();
		double[][] delayMatrix = InternetCharacteristics.getInstance().getLatencyMatrix();
		if (delayMatrix != null){
			int num = delayMatrix.length;
			for (int row = 0; row < num; row++){
				Object[] rowData = new Object[num + 1];
				rowData[0] = Integer.toString(row);
				for (int col = 0; col < num; col++){
					rowData[col + 1] = delayMatrix[row][col];
				}
				delayMatixModel.addRow(rowData);
			}
		} else {
			//Populate a default matrix
			JOptionPane.showMessageDialog(this, "Displaying default delay matrix.");
			for (int i = 0; i < numRegions; i++){
				Object[] row = new Object[numRegions + 1];
				row[0] = Integer.toString(i);
				for (int j = 1; j < row.length; j++){
					row[j] = new Double(100.0);
				}
				delayMatixModel.addRow(row);
			}
		}
	}

	private JPanel createDelayMatrixPanel(){
		JPanel delayMatrixPanel = new JPanel();
		delayMatrixPanel.setLayout(new BorderLayout());
		delayMatrixPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel lblHeader = new JLabel("<html><h3>Delay Matrix</h3><br/>" +
									  "The transmission delay between regions. Units in milliseconds");
		lblHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
		delayMatrixPanel.add(lblHeader, BorderLayout.NORTH);

		int[] cols = new int[numRegions + 1];
		String[] colHeadings = new String[numRegions + 1];
		colHeadings[0] = "Region\\Region";
		for (int i = 0; i < numRegions; i++){
			cols[i + 1] = i;
			colHeadings[i + 1] = Integer.toString(i);
		}

		delayMatixModel = new MatrixTableModel(colHeadings, this);
		delayMatixModel.setNotNullColumns(cols);
		delayMatrixTable = new JTable(delayMatixModel);
		TableColumn column = delayMatrixTable.getColumn("Region\\Region");
		column.setPreferredWidth(100);
		column.setMinWidth(100);
		column.setMaxWidth(100);
		DefaultTableCellRenderer fillRenderer = new DefaultTableCellRenderer();
		fillRenderer.setBackground(delayMatrixPanel.getBackground());
		fillRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
		column.setCellRenderer(fillRenderer);

		delayMatrixPanel.add(new JScrollPane(delayMatrixTable), BorderLayout.CENTER);

		return delayMatrixPanel;
	}

	private void loadBwMatrix(){
		bwMatrixModel.clearData();
		double[][] bwMatrix = InternetCharacteristics.getInstance().getBwMatrix();
		if (bwMatrix != null){
			int num = bwMatrix.length;
			for (int row = 0; row < num; row++){
				Object[] rowData = new Object[num + 1];
				rowData[0] = Integer.toString(row);
				for (int col = 0; col < num; col++){
					rowData[col + 1] = bwMatrix[row][col];
				}
				bwMatrixModel.addRow(rowData);
			}
		} else {
			//Populate a default matrix
			JOptionPane.showMessageDialog(this, "Displaying default bandwidth matrix.");
			for (int i = 0; i < numRegions; i++){
				Object[] row = new Object[numRegions + 1];
				row[0] = Integer.toString(i);
				for (int j = 1; j < row.length; j++){
					row[j] = new Double(1000.0);
				}
				bwMatrixModel.addRow(row);
			}
		}
	}

	private JPanel createBwMatrixPanel(){
		JPanel bwMatrixPanel = new JPanel();
		bwMatrixPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		bwMatrixPanel.setLayout(borderLayout);

		JLabel lblHeader = new JLabel("<html><h3>Bandwidth Matrix</h3><br/>" +
		  							  "The available bandwidth between regions for the simulated application." +
		  							  " Units in Mbps");
		lblHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
		bwMatrixPanel.add(lblHeader, BorderLayout.NORTH);

		int[] cols = new int[numRegions + 1];
		String[] colHeadings = new String[numRegions + 1];
		colHeadings[0] = "Region\\Region";
		for (int i = 0; i < numRegions; i++){
			cols[i + 1] = i;
			colHeadings[i + 1] = Integer.toString(i);
		}

		bwMatrixModel = new MatrixTableModel(colHeadings, this);
		bwMatrixModel.setNotNullColumns(cols);
		bwTable = new JTable(bwMatrixModel);
		TableColumn column = bwTable.getColumn("Region\\Region");
		column.setPreferredWidth(100);
		column.setMinWidth(100);
		column.setMaxWidth(100);
		DefaultTableCellRenderer fillRenderer = new DefaultTableCellRenderer();
		fillRenderer.setBackground(bwMatrixPanel.getBackground());
		fillRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
		column.setCellRenderer(fillRenderer);

		bwMatrixPanel.add(new JScrollPane(bwTable));

		return bwMatrixPanel;
	}

	private JPanel createControlPanel(){
		JPanel controlPanel = new JPanel();

		JButton btnDone = addButton(controlPanel, LBL_DONE, CMD_DONE_INTERNET_CONFIG);
		btnDone.addActionListener(screenListener);
		JButton btnCancel = addButton(controlPanel, LBL_CANCEL, CMD_CANCEL_INTERNET_CONFIG);
		btnCancel.addActionListener(screenListener);

		return controlPanel;
	}

	private JButton addButton(JPanel pnlUBControls, String label, String actionCommand) {
		JButton btn = new JButton(label);
		btn.setPreferredSize(BTN_DIMENSION);
		btn.setMaximumSize(BTN_DIMENSION);
		btn.setMinimumSize(BTN_DIMENSION);
		btn.setActionCommand(actionCommand);
		btn.addActionListener(this);
		pnlUBControls.add(btn);
		pnlUBControls.add(Box.createVerticalStrut(10));

		return btn;
	}


	private double[][] getAs2DArray(List<Object[]> data) {
		double[][] delayMatrix = new double[numRegions][numRegions];

		for (int row = 0; row < data.size(); row++){
			for (int col = 0; col < numRegions; col++){
				Double val = (Double) data.get(row)[col + 1];
				delayMatrix[row][col] = val;
			}
		}

		return delayMatrix;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CMD_DONE_INTERNET_CONFIG)){

			List<Object[]> data = delayMatixModel.getData();
			double[][] delayMatrix = getAs2DArray(data);
			InternetCharacteristics.getInstance().setLatencyMatrix(delayMatrix);

			try {
				IOUtil.saveAsXML(delayMatrix, new File(getClass().getClassLoader().getResource(DELAYMATRIX_FILE).getFile()));
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, "Failed to save delay matrix file!", "I/O Error", JOptionPane.WARNING_MESSAGE);
			}

			data = bwMatrixModel.getData();
			double[][] bwMatrix = getAs2DArray(data);
			InternetCharacteristics.getInstance().setBwMatrix(bwMatrix);

			try {
				IOUtil.saveAsXML(bwMatrix, new File(getClass().getClassLoader().getResource(BWMATRIX_FILE).getFile()));
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, "Failed to save bandwidth matrix file!", "I/O Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}



	public boolean validateConfiguration(){


		return false;
	}

	/** Table model used by the two tables on this screen. Just makes SimpleTableModel non-editable. */
	private class MatrixTableModel extends SimpleTableModel {
		public MatrixTableModel(String[] columns, Component holder){
			super(columns, holder);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
	        if (col == 0){
	        	return false;
	        } else {
	        	return true;
	        }
	    }
	}


}
