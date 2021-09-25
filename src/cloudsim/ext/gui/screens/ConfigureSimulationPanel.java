package cloudsim.ext.gui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import cloudsim.ext.Constants;
import cloudsim.ext.Simulation;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
import cloudsim.ext.gui.DataCenterUIElement;
import cloudsim.ext.gui.MachineUIElement;
import cloudsim.ext.gui.UserBaseUIElement;
import cloudsim.ext.gui.VmAllocationUIElement;
import cloudsim.ext.gui.MachineUIElement.VmAllocationPolicy;
import cloudsim.ext.gui.utils.AbstractListTableModel;
import cloudsim.ext.gui.utils.MultilineTableHeaderRenderer;
import cloudsim.ext.util.IOUtil;
import cloudsim.ext.util.MiscUtil;
import cloudsim.ext.util.ObservableList;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

/**
 * The configuration panel of the simulator. This panel is a tabbed panel.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class ConfigureSimulationPanel extends JPanel 
									  implements ActionListener, CloudSimEventListener, Constants {

	private static final String CMD_COPY_MACHINE = "copy_machine";
	private static final String LBL_COPY = "Copy";
	private static final String CMD_REMOVE_VM_ALLOCATION = "remove_vm_allocation";
	private static final String CMD_ADD_VM_ALLOCATION = "add_vm_allocation";
	private static final String CMD_REMOVE_MACHINE = "Remove Machine";
	private static final String CMD_ADD_MACHINE = "add_machine";
	private static final String CMD_SAVE_CONFIG = "save_config_file";
	public static final String CMD_LOAD_CONFIG = "load_config_from_file";
	public static final String CMD_CANCEL_CONFIGURATION = "cancel_configuration";
	public static final String CMD_DONE_CONFIGURATION = "done_ configuration";
	private static final String CMD_REMOVE_DATACENTER = "remove datacenter";
	private static final String CMD_ADD_NEW_DATACENTER = "add new datacenter";
	private static final String CMD_REMOVE_USERBASE = "remove userbase";
	private static final String CMD_ADD_NEW_USERBASE = "add new userbase";
	
	private static final String COL_AVG_OFF_PEAK_USERS = "Avg Off-Peak \nUsers";
	private static final String COL_AVG_PEAK_USERS = "Avg Peak \nUsers";
	
	private static final String LBL_SAVE_CONFIGURATION = "Save Configuration";
	private static final String LBL_LOAD = "Load Configuration";
	private static final String LBL_CANCEL = "Cancel";
	private static final String LBL_DONE = "Done";
	private static final String LBL_REMOVE = "Remove";
	private static final String LBL_ADD_NEW = "Add New";
	private static final int TABLE_HEIGHT = 80;
	private static final Dimension TABLE_DIMENSION = new Dimension(650, TABLE_HEIGHT);
	private static final Dimension BTN_DIMENSION = new Dimension(100, 25);
	private static final String SIM_FILE_EXTENSION = ".sim";
	private static final String TIME_UNIT_DAYS = "days";
	private static final String TIME_UNIT_HOURS = "hours";
	private static final String TIME_UNIT_MIN = "min";
	
	private UserBaseTableModel ubTableModel;
	private JTable userBasesTable;
	private Simulation simulation;
	private ActionListener screenListener;
	private JTextField txtSimDuration;
	private JComboBox cmbTimeUnit;
	private JComboBox regionCombo;
	
	/** fileChooser is used for both open and save dialog for configurations. */
	private JFileChooser fileChooser;
	private DataCenterTableModel dcTableModel; 
	private JTable dataCentersTable;
	private MultilineTableHeaderRenderer multilineHeaderRenderer;
	
	/** Local copy of the data center list */
	private ObservableList<DataCenterUIElement> dataCenterList;
	
	/** Local copy of the user bases list */
	private ObservableList<UserBaseUIElement> userBasesList;
	
	/** Holds the vm allocations */
	private List<VmAllocationUIElement> vmAllocationList;
	
	private JPanel machineListPanel;
	private JTable machineTable;
	private JPanel machineListControlsPanel;
	private JComboBox archCombo;
	private JComboBox osCombo;
	private JComboBox vmmCombo;
	private JLabel lblDcName;
	private JPanel machineDetailsPanel;
	private VmTableModel vmAllocTableModel;
	private JTable vmAllocTable;
	private JComboBox dcCombo;
	private JTextField txtUserGroupingFactor;
	private JTextField txtDcRequestGroupingFactor;
	private JTextField txtInstructionLength;
	private JComboBox cmbServiceBroker;
	private JComboBox cmbLoadBalancingPolicy;
	

	/** 
	 * Constructor.
	 * 
	 * @param sim
	 * @param screenListener
	 */
	public ConfigureSimulationPanel(Simulation sim, ActionListener screenListener){
		this.simulation = sim;
		this.screenListener = screenListener;
		
		initListLocalCopies();
		
		regionCombo = new JComboBox(new Integer[]{0, 1, 2, 3, 4, 5});
		archCombo = new JComboBox(new String[]{DEFAULT_ARCHITECTURE});
		osCombo = new JComboBox(new String[]{DEFAULT_OS});
		vmmCombo = new JComboBox(new String[]{DEFAULT_VMM});
		multilineHeaderRenderer = new MultilineTableHeaderRenderer();
		initUI();
	}
	
	/**
	 * Creates local (deep) copies of the user base and data center lists. Need copies as we
	 * don't want to update the original copies in {@link Simulation} until user clicks 'Done' button. 
	 */
	private void initListLocalCopies(){
		userBasesList = (ObservableList<UserBaseUIElement>) MiscUtil.deepCopy(simulation.getUserBases());
		
		dataCenterList = (ObservableList<DataCenterUIElement>) MiscUtil.deepCopy(simulation.getDataCenters());
		dataCenterList.addCloudSimEventListener(this);
		
		vmAllocationList = new ArrayList<VmAllocationUIElement>();
		VmAllocationUIElement vmAllocation;
		for (DataCenterUIElement dc : dataCenterList){
			vmAllocation = dc.getVmAllocation();
			if (vmAllocation != null){
				vmAllocationList.add(vmAllocation);
			}
		}
	}
	
	/** Sets up the main UI elements */
	private void initUI(){
		
		int leftMargin = 50;
		
		setComponentSize(this, new Dimension(900, 700));
		this.setLayout(null);
		int x = leftMargin;
		int y = 0;
		int compW = 500;
		int compH = leftMargin;
		int hGap = 10;
		int vGap = hGap;
		
		JLabel heading = new JLabel("<html><h1>Configure Simulation</h1></html>");
		heading.setBounds(x, y, compW, compH);
		this.add(heading);
		
		y += compH + 20;
		compH = 500;
		compW = 900;
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Main Configuration", createMainTab());
		tabbedPane.addTab("Data Center Configuration", createDcTab());
		tabbedPane.addTab("Advanced", createAdvancedTab());
		tabbedPane.setBounds(x, y, compW, compH);
		this.add(tabbedPane);
		
		y += compH + vGap;
		compW = 700;
		compH = 40;
		JPanel controlPanel = createControlPanel();
		controlPanel.setBounds(x, y, compW, compH);
		this.add(controlPanel);
		
		//Init the file chooser as well.
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				if (f.getAbsolutePath().toLowerCase().endsWith(SIM_FILE_EXTENSION)){
					return true;
				} else {
					return false;
				}
			}

			@Override
			public String getDescription() {
				return SIM_FILE_EXTENSION;
			}});
	}
	
	/**
	 * 
	 * @return
	 */
	private JPanel createMainTab(){
		int leftMargin = 10;
		int x = leftMargin;
		int y = 30;
		int compW = 500;
		int compH = leftMargin;
		int hGap = 10;
		int vGap = 20;
		
		JPanel mainTab = new JPanel();
		mainTab.setLayout(null);
		
		compW = 120; 
		compH = 20;
		JLabel lblSimDuration = new JLabel("Simulation Duration:");
		lblSimDuration.setBounds(x, y, compW, compH);
		mainTab.add(lblSimDuration);
		
		x += compW + vGap; 
		compW = 70; 
		txtSimDuration = new JTextField("" + simulation.getSimulationTime() / (60000));
		txtSimDuration.setBounds(x, y, compW, compH);
		mainTab.add(txtSimDuration);
		
		x += compW + vGap;
		cmbTimeUnit = new JComboBox(new String[]{TIME_UNIT_MIN, TIME_UNIT_HOURS, TIME_UNIT_DAYS});
		cmbTimeUnit.setBounds(x, y, compW, compH+5);
		mainTab.add(cmbTimeUnit);
		
		x = leftMargin; 
		y += compH + vGap; 
		compW = 70; 
		JLabel lblUbHeading = new JLabel("User bases:");
		lblUbHeading.setBounds(x, y, compW, compH);
		mainTab.add(lblUbHeading);
		
		x += compW + hGap; 
		compW = 800; 
		compH = 180;
		JPanel ubPanel = createUserBasesPanel();
		ubPanel.setBounds(x, y, compW, compH);
		mainTab.add(ubPanel);
				
		x = leftMargin;
		y += compH + vGap;
		compW = 80; 
		compH = 60;
		JLabel lblVmHeading = new JLabel("<html>Application<br/>Deployment<br/>Configuration:</html>");
		lblVmHeading.setBounds(x, y, compW, compH);
		mainTab.add(lblVmHeading);
		
		x += compW + hGap * 2;
		compW = 150; 
		compH = 20;
		JLabel lblServiceBroker = new JLabel("Service Broker Policy:");
		lblServiceBroker.setBounds(x, y, compW, compH);
		mainTab.add(lblServiceBroker);
		
		x += compW;
		compW = 180;
		cmbServiceBroker = new JComboBox(new String[]{Constants.BROKER_POLICY_PROXIMITY, 
													  Constants.BROKER_POLICY_OPTIMAL_RESPONSE,
													  Constants.BROKER_POLICY_DYNAMIC});
		cmbServiceBroker.setSelectedItem(simulation.getServiceBrokerPolicy());
		cmbServiceBroker.setBounds(x, y, compW, compH+5);
		mainTab.add(cmbServiceBroker);
		
		x = leftMargin + 80;
		y += compH + vGap;
		compW = 800;
		compH = 150;
		JPanel vmPanel = createVmAllocationPanel();
		vmPanel.setBounds(x, y, compW, compH);
		mainTab.add(vmPanel);
		
		return mainTab;
	}
	
	/**
	 * @return
	 */
	private JPanel createDcTab(){
		int leftMargin = 10;
		int x = leftMargin;
		int y = 20;
		int compW = 500;
		int compH = leftMargin;
		int hGap = 10;
		int vGap = 20;
		
		JPanel dcTab = new JPanel();
		dcTab.setLayout(null);
		
		x = leftMargin;
		y += compH + vGap;
		compW = 70; 
		compH = 40;
		JLabel lblDcHeading = new JLabel("<html>Data<br/>Centers:</html>");
		lblDcHeading.setBounds(x, y, compW, compH);
		dcTab.add(lblDcHeading);
		
		x += compW + hGap;
		compW = 800;
		compH = 150;
		JPanel dcPanel = createDataCentersPanel();
		dcPanel.setBounds(x, y, compW, compH);
		dcTab.add(dcPanel);
		
		y += compH + vGap;
		x = leftMargin + 80;
		compW = 800;
		compH = 180;
		machineDetailsPanel = createDcDetailsPanel();
		machineDetailsPanel.setVisible(false);
		machineDetailsPanel.setBounds(x, y, compW, compH);
		dcTab.add(machineDetailsPanel);
		
		return dcTab;
	}
	
	/**
	 * @return
	 */
	private JPanel createAdvancedTab(){
		int leftMargin = 50;
		int x = leftMargin;
		int y = 50;
		int vGap = 20;
		
		JPanel advancedTab = new JPanel();
		advancedTab.setLayout(null);
		
		int compW = 500;
		int compH = 20;
		
		compW = 260;
		int lastCompH = compH = 60;
		JLabel lblUserGroup = new JLabel("<html>User grouping factor in User Bases:" +
										 "<br/>(Equivalent to number of simultaneous" +
										 "<br/> users from a single user base)</html>");
		lblUserGroup.setBounds(x, y, compW, compH);
		advancedTab.add(lblUserGroup);
		
		x += compW + vGap;
		y += 10;
		compW = 80;
		compH = 20;
		txtUserGroupingFactor = new JTextField("" + simulation.getUserGroupingFactor());
		txtUserGroupingFactor.setBounds(x, y, compW, compH);
		advancedTab.add(txtUserGroupingFactor);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 260;
		lastCompH = compH = 70;
		JLabel lblDcRequestGrouping = new JLabel("<html>Request grouping factor in Data Centers:" +
				                                  "<br/>(Equivalent to number of simultaneous" +
				                                  "<br/> requests a single applicaiton server" +
				                                  "<br/> instance can support.) </html>");
		lblDcRequestGrouping.setBounds(x, y, compW, compH);
		advancedTab.add(lblDcRequestGrouping);
		
		x += compW + vGap;
		y += 10;
		compW = 80;
		compH = 20;
		txtDcRequestGroupingFactor = new JTextField("" + simulation.getDcRequestGroupingFactor());
		txtDcRequestGroupingFactor.setBounds(x, y, compW, compH);
		advancedTab.add(txtDcRequestGroupingFactor);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 260;
		compH = 30;
		JLabel lblInstructionLength = new JLabel("<html>Executable instruction length per request:" +
				                                  "<br/>(bytes)</html>");
		lblInstructionLength.setBounds(x, y, compW+10, compH);
		advancedTab.add(lblInstructionLength);
		
		x += compW + vGap;
		compW = 80;
		compH = 20;
		txtInstructionLength = new JTextField("" + simulation.getInstructionLengthPerRequest());
		txtInstructionLength.setBounds(x, y, compW, compH);
		advancedTab.add(txtInstructionLength);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 260;
		compH = 30;
		JLabel lblLoadBalancing = new JLabel("<html>Load balancing policy<br/>" +
				                                  "across VM's in a single Data Center:</html>");
		lblLoadBalancing.setBounds(x, y, compW, compH);
		advancedTab.add(lblLoadBalancing);
		
		x += compW + vGap;
		compW = 240;
		compH = 25;
		cmbLoadBalancingPolicy = new JComboBox(new String[]{
				Constants.LOAD_BALANCE_POLICY_RR,
				Constants.LOAD_BALANCE_WRR,
				Constants.LOAD_BALANCE_ANT_COLONY,
				Constants.LOAD_BALANCE_HONEY_COLONY,
//				Constants.LOAD_BALANCE_PSO,
				Constants.LOAD_BALANCE_THRESHOLD
//				Constants.LOAD_BALANCE_SHORTEST_JOB_FIRST
		});
		cmbLoadBalancingPolicy.setSelectedItem(simulation.getLoadBalancePolicy());
		cmbLoadBalancingPolicy.setBounds(x, y, compW, compH);
		advancedTab.add(cmbLoadBalancingPolicy);
		
		return advancedTab;
	}
	
	/**
	 * Used to set size restriction on a component.
	 * @param comp
	 * @param size
	 */
	private void setComponentSize(JComponent comp, Dimension size){
		comp.setPreferredSize(size);
		comp.setMinimumSize(size);
		comp.setMaximumSize(size);
	}
		
	private JPanel createUserBasesPanel(){
		JPanel userBasesPanel = new JPanel();
		
		ubTableModel = new UserBaseTableModel(userBasesList);
		
		userBasesTable = new JTable(ubTableModel);		
		userBasesTable.setPreferredScrollableViewportSize(TABLE_DIMENSION);
		TableColumnModel ubTableColumnModel = userBasesTable.getColumnModel();
		Enumeration<TableColumn> e = ubTableColumnModel.getColumns();
	    while (e.hasMoreElements()) {
	    	(e.nextElement()).setHeaderRenderer(multilineHeaderRenderer);
	    }	    
		ubTableColumnModel.getColumn(1).setCellEditor(new DefaultCellEditor(regionCombo));
	    
		userBasesPanel.add(new JScrollPane(userBasesTable));
		
		JPanel pnlUBControls = new JPanel();
		pnlUBControls.setLayout(new BoxLayout(pnlUBControls, BoxLayout.Y_AXIS));
		
		addButton(pnlUBControls, LBL_ADD_NEW, CMD_ADD_NEW_USERBASE);
		addButton(pnlUBControls, LBL_REMOVE, CMD_REMOVE_USERBASE);
		
		userBasesPanel.add(pnlUBControls);
		
		return userBasesPanel;
	}
	
	private JPanel createVmAllocationPanel(){
		JPanel vmPanel = new JPanel();
		
		vmAllocTableModel = new VmTableModel(vmAllocationList);
		vmAllocTable = new JTable(vmAllocTableModel);
		vmAllocTable.setPreferredScrollableViewportSize(TABLE_DIMENSION);
		TableColumnModel vmTableColumnModel = vmAllocTable.getColumnModel();
		Enumeration<TableColumn> e = vmTableColumnModel.getColumns();
	    while (e.hasMoreElements()) {
	    	(e.nextElement()).setHeaderRenderer(multilineHeaderRenderer);
	    }	    
	    
		vmTableColumnModel.getColumn(0).setCellEditor(new DefaultCellEditor(initDcCombo()));
	    
		vmPanel.add(new JScrollPane(vmAllocTable));
		
		JPanel pnlVmControls = new JPanel();
		pnlVmControls.setLayout(new BoxLayout(pnlVmControls, BoxLayout.Y_AXIS));
		
		addButton(pnlVmControls, LBL_ADD_NEW, CMD_ADD_VM_ALLOCATION);
		addButton(pnlVmControls, LBL_REMOVE, CMD_REMOVE_VM_ALLOCATION);
		
		vmPanel.add(pnlVmControls);
		
		return vmPanel;
	}
	
	private JComboBox initDcCombo(){
		if (dcCombo == null){
			dcCombo = new JComboBox();
		} else {
			dcCombo.removeAllItems();			
		}
		
		for (DataCenterUIElement dc : dataCenterList){
			dcCombo.addItem(dc);
		}
		dcCombo.revalidate();
		
		return dcCombo;
	}
	
	private JPanel createDataCentersPanel(){
		JPanel dataCentersPanel = new JPanel();
		
		dcTableModel = new DataCenterTableModel(dataCenterList);
		dcTableModel.setUniqueColumns(new int[]{0});
		dcTableModel.setNotNullColumns(new int[]{0, 1, 2, 3, 4, 5, 6});
		dcTableModel.setNotEditableColumns(new int[]{6});		
		
		dataCentersTable = new JTable(dcTableModel);
		dataCentersTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DcTableSelectionListener lis = new DcTableSelectionListener(dataCentersTable);
		dataCentersTable.getSelectionModel().addListSelectionListener(lis);
		
		TableColumnModel dcTableColumnModel = dataCentersTable.getColumnModel();
		Enumeration<TableColumn> e = dcTableColumnModel.getColumns();
	    while (e.hasMoreElements()) {
	    	(e.nextElement()).setHeaderRenderer(multilineHeaderRenderer);
	    }
	    dcTableColumnModel.getColumn(1).setCellEditor(new DefaultCellEditor(regionCombo));
	    dcTableColumnModel.getColumn(2).setCellEditor(new DefaultCellEditor(archCombo));
		dcTableColumnModel.getColumn(3).setCellEditor(new DefaultCellEditor(osCombo));
		dcTableColumnModel.getColumn(4).setCellEditor(new DefaultCellEditor(vmmCombo));
	    
		dataCentersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane dcScroller = new JScrollPane(dataCentersTable);
		dataCentersTable.setPreferredScrollableViewportSize(TABLE_DIMENSION);		
		dataCentersPanel.add(dcScroller);//new JScrollPane(dataCentersTable));
		
		JPanel pnlDcControls = new JPanel();
		pnlDcControls.setLayout(new BoxLayout(pnlDcControls, BoxLayout.Y_AXIS));
		
		addButton(pnlDcControls, LBL_ADD_NEW, CMD_ADD_NEW_DATACENTER);
		addButton(pnlDcControls, LBL_REMOVE, CMD_REMOVE_DATACENTER);
		
		dataCentersPanel.add(pnlDcControls);
	
		return dataCentersPanel;
	}
	
	private JPanel createDcDetailsPanel(){
		JPanel detailPanel = new JPanel();
		detailPanel.setBorder(new LineBorder(Color.GRAY));
		
		JLabel header = new JLabel("Physical Hardware Details of Data Center :");
		lblDcName = new JLabel();
		JPanel headerPanel = new JPanel();
		headerPanel.add(header);
		headerPanel.add(lblDcName);
		detailPanel.add(headerPanel, BorderLayout.NORTH);
		
		JPanel bodyPanel = new JPanel();
		bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
		bodyPanel.add(createMachineListPanel());
		
		detailPanel.add(bodyPanel, BorderLayout.CENTER);
		
		return detailPanel;
	}
	
	private JPanel createMachineListPanel(){
		machineListPanel = new JPanel();
		machineListControlsPanel = new JPanel();
		machineListControlsPanel.setLayout(new BoxLayout(machineListControlsPanel, BoxLayout.Y_AXIS));
		
		addButton(machineListControlsPanel, LBL_ADD_NEW, CMD_ADD_MACHINE);
		addButton(machineListControlsPanel, LBL_COPY, CMD_COPY_MACHINE);
		addButton(machineListControlsPanel, LBL_REMOVE, CMD_REMOVE_MACHINE);
				
		return machineListPanel;
	}
	
	private JPanel createControlPanel(){
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
		
		JButton btnCancel = addButton(controlPanel, LBL_CANCEL, CMD_CANCEL_CONFIGURATION);
		btnCancel.addActionListener(screenListener);
		JButton btnLoad = addButton(controlPanel, LBL_LOAD, CMD_LOAD_CONFIG);
		btnLoad.addActionListener(screenListener);	
		JButton btnSave = addButton(controlPanel, LBL_SAVE_CONFIGURATION, CMD_SAVE_CONFIG);
		btnSave.addActionListener(screenListener);	
		JButton btnDone = addButton(controlPanel, LBL_DONE, CMD_DONE_CONFIGURATION);
		btnDone.addActionListener(screenListener);
		
		return controlPanel;
	}

	private void addNewDC(){
		int dcs = dataCenterList.size();			
		
		DataCenterUIElement newDc = new DataCenterUIElement("DC" + (dcs + 1), 
															 DEFAULT_DC_REGION,
															 DEFAULT_ARCHITECTURE,
															 DEFAULT_OS,
															 DEFAULT_VMM,
															 DEFAULT_COST_PER_PROC,
															 DEFAULT_COST_PER_MEM,
															 DEFAULT_COST_PER_STOR,
															 DEFAULT_COST_PER_BW);
		
		addNewDefaultMachine(newDc);
		dataCenterList.add(newDc);
		dcTableModel.fireTableDataChanged();
	}	
	
	private void addNewDefaultMachine(DataCenterUIElement dc){
		dc.getMachineList().add(new MachineUIElement(DEFAULT_MC_MEMORY,
													 DEFAULT_MC_STORAGE,
													 DEFAULT_MC_BW,
													 DEFAULT_MC_PROCESSORS,
													 DEFAULT_MC_SPEED,
													 MachineUIElement.VmAllocationPolicy.TIME_SHARED ));
		if (machineTable != null){
			machineTable.revalidate();
		}
	}
	
	private void showDcDetails(int row){
		machineListPanel.removeAll();
		
		if (row < 0){
			machineDetailsPanel.setVisible(false);
			lblDcName.setText("");
			machineListPanel.revalidate();
			repaint();
			return;
		}
		
		machineDetailsPanel.setVisible(true);
		DataCenterUIElement dc = dataCenterList.get(row);
		lblDcName.setText(dc.getName());
		MachineTableModel machineTableModel = dcTableModel.getChildTableModel(row);
		
		if (machineTableModel == null){
			// Create child table models for the machine lists
			List<MachineUIElement> mcl = dc.getMachineList();
			machineTableModel = new MachineTableModel(mcl);
			dcTableModel.addChildTableModel(row, machineTableModel);			
		}			
			
		machineTable = new JTable(machineTableModel);
		TableColumnModel dcTableColumnModel = machineTable.getColumnModel();
		Enumeration<TableColumn> e = dcTableColumnModel.getColumns();
		while (e.hasMoreElements()) {
			(e.nextElement()).setHeaderRenderer(multilineHeaderRenderer);
		}
		
		machineTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);	
		machineTable.setPreferredScrollableViewportSize(TABLE_DIMENSION);	
		JScrollPane machineTableScrollPane = new JScrollPane(machineTable);				
		
		machineListPanel.add(machineTableScrollPane);//new JScrollPane(dataCentersTable));
		machineListPanel.add(machineListControlsPanel);			
	
		
		machineListPanel.revalidate();
		repaint();
	}

	private JButton addButton(JPanel pnlUBControls, String label, String actionCommand) {
		JButton btn = new JButton(label);
		
		FontMetrics fm = this.getFontMetrics(this.getFont());
		int labelWidth = fm.stringWidth(label);
		if (labelWidth < BTN_DIMENSION.getWidth()){
			btn.setPreferredSize(BTN_DIMENSION);
			btn.setMaximumSize(BTN_DIMENSION);
			btn.setMinimumSize(BTN_DIMENSION);
		} else {
			Dimension dimension = new Dimension(labelWidth + 40, (int) BTN_DIMENSION.getHeight());
			btn.setPreferredSize(dimension);
			btn.setMaximumSize(dimension);
			btn.setMinimumSize(dimension);
		}
		btn.setActionCommand(actionCommand);
		btn.addActionListener(this);
		pnlUBControls.add(btn);
		pnlUBControls.add(Box.createVerticalStrut(10));
		
		return btn;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CMD_ADD_NEW_USERBASE)){
			int userBases = userBasesList.size();// ubTableModel.getRowCount();
			String ubName;
			do {
				ubName = "UB" + userBases++;
			} while (!ubTableModel.isUnique(ubName, 0));
			userBasesList.add(new UserBaseUIElement(ubName, 
													 DEFAULT_UB_REGION, 
													 DEFAULT_REQ_PER_USER_PER_HR,
													 DEFAULT_REQ_SIZE,
													 DEFAULT_PEAK_HOURS,
													 DEFAULT_PEAK_USERS,
													 DEFAULT_OFFPEAK_USERS));	
			ubTableModel.fireTableDataChanged();
		} else if (e.getActionCommand().equals(CMD_REMOVE_USERBASE)){
			int selectedRow = userBasesTable.getSelectedRow();
			if (selectedRow != -1){
				ubTableModel.deleteRow(selectedRow);
			}
		} else if (e.getActionCommand().equals(CMD_DONE_CONFIGURATION)){
			finishConfiguration();
		} else if (e.getActionCommand().equals(CMD_CANCEL_CONFIGURATION)){
			initListLocalCopies();
			
			ubTableModel.setData(userBasesList);
			dcTableModel.setData(dataCenterList);
			vmAllocTableModel.setData(vmAllocationList);
			initDcCombo();
		} else if (e.getActionCommand().equals(CMD_LOAD_CONFIG)){
			loadSimulationFromFile();
		} else if (e.getActionCommand().equals(CMD_SAVE_CONFIG)){
			saveSimulation();
		} else if (e.getActionCommand().equals(CMD_ADD_NEW_DATACENTER)){
			addNewDC();
		} else if (e.getActionCommand().equals(CMD_REMOVE_DATACENTER)){
			int selectedRow = dataCentersTable.getSelectedRow();
			if (selectedRow != -1){
				dcTableModel.deleteRow(selectedRow);
			}
		} else if (e.getActionCommand().equals(CMD_ADD_MACHINE)) {
			int dcRow = dataCentersTable.getSelectedRow();
			DataCenterUIElement dc = dataCenterList.get(dcRow);
			addNewDefaultMachine(dc);
		} else if (e.getActionCommand().equals(CMD_REMOVE_MACHINE)){
			if (machineTable != null){
				int selectedDc = dataCentersTable.getSelectedRow();
				int selectedMc = machineTable.getSelectedRow();
				if (selectedMc != -1){
					MachineTableModel selectedDcChild = dcTableModel.getChildTableModel(selectedDc);
					selectedDcChild.deleteRow(selectedMc);
				}
			}
		} else if (e.getActionCommand().equals(CMD_COPY_MACHINE)){
			if (machineTable != null){
				
				int selectedDcRow = dataCentersTable.getSelectedRow();
				int selectedMcRow = machineTable.getSelectedRow();
				int numCopies = 0;
				
				if (selectedMcRow != -1){
					String copies = JOptionPane.showInputDialog("Number of Copies to Create:");
					numCopies = Integer.parseInt(copies);
				} else {
					JOptionPane.showMessageDialog(this, "Please select a machine to copy.");
				}
				
				if (numCopies > 0){
					DataCenterUIElement selectedDc = dataCenterList.get(selectedDcRow);
					MachineUIElement selectedMc = selectedDc.getMachineList().get(selectedMcRow);
					
					for (int i = 0; i < numCopies; i++){
						selectedDc.getMachineList().add(new MachineUIElement(
								selectedMc.getMemory(),
								selectedMc.getStorage(),
								selectedMc.getBw(),
								selectedMc.getProcessors(),
								selectedMc.getSpeed(),
								selectedMc.getVmAllocationPolicy()));						
					}					
					
					machineTable.revalidate();
				}
			}
		} else if (e.getActionCommand().equals(CMD_ADD_VM_ALLOCATION)){
			addVmAllocation();																	 
		} else if (e.getActionCommand().equals(CMD_REMOVE_VM_ALLOCATION)){
			
			int selectedRow = vmAllocTable.getSelectedRow();
			VmAllocationUIElement vmAlloc = vmAllocationList.get(selectedRow);
			DataCenterUIElement dc = vmAlloc.getDc();
			if (dc != null){
				dc.setVmAllocation(null);
			}
			vmAlloc.setDc(null);
			vmAllocationList.remove(vmAlloc);
			vmAllocTableModel.fireTableDataChanged();
		} 
	}

	private void addVmAllocation() {
		if (dataCenterList.size() == 0){
			JOptionPane.showMessageDialog(this, "Please create at least one Data Center before you" +
												" can allocate virtual machines.");
			return;
		}		
		
		int incompleteRow = -1;
		for (int i = 0; i < vmAllocationList.size(); i++){
			VmAllocationUIElement vm = vmAllocationList.get(i);
			if (vm.getDc() == null){
				incompleteRow = i;
				break;
			}
		}
		if (incompleteRow != -1){
			JOptionPane.showMessageDialog(this, "Please complete the current allocation by selecting a Data Center.");
			vmAllocTable.getSelectionModel().setSelectionInterval(incompleteRow, incompleteRow);
			return;
		}
		
		if (dataCenterList.size() <= vmAllocationList.size()){
			JOptionPane.showMessageDialog(this, "All available Data Centers seems to be allocated." +
												" Please create a new Data Center before you can create further allocations.");
			return;
		}
		
		VmAllocationUIElement vmAlloc = new VmAllocationUIElement(null,
																  DEFAULT_VM_COUNT,
																  DEFAULT_VM_IMAGE_SIZE,
																  DEFAULT_VM_MEMORY,
																  DEFAULT_VM_BW);
		vmAllocationList.add(vmAlloc);
		vmAllocTableModel.fireTableDataChanged();
	}

	private void finishConfiguration() {		
//		if (isValidConfiguration()){
			simulation.getUserBases().replaceContent(userBasesList);
			simulation.getDataCenters().replaceContent(dataCenterList);
			
			double simDuration = Double.parseDouble(txtSimDuration.getText().trim());
			String timeUnit = (String) cmbTimeUnit.getSelectedItem();
			if (timeUnit.equals(TIME_UNIT_MIN)){
				simulation.setSimulationTime(simDuration * MILLI_SECONDS_TO_MINS);
			} else if (timeUnit.equals(TIME_UNIT_HOURS)) {
				simulation.setSimulationTime(simDuration * MILLI_SECONDS_TO_HOURS);
			} else if (timeUnit.equals(TIME_UNIT_DAYS)){
				simulation.setSimulationTime(simDuration * MILLI_SECONDS_TO_DAYS);
			} else if (timeUnit.equals("sec")){
				simulation.setSimulationTime(simDuration * 1000);
			}
			
			String serviceBrokerPolicy = (String) cmbServiceBroker.getSelectedItem();
			simulation.setServiceBrokerPolicy(serviceBrokerPolicy);
			
			int userGroupingFactor = Integer.parseInt(txtUserGroupingFactor.getText().trim());
			simulation.setUserGroupingFactor(userGroupingFactor);
			
			int dcRequestGroupingFactor = Integer.parseInt(txtDcRequestGroupingFactor.getText().trim());
			simulation.setDcRequestGroupingFactor(dcRequestGroupingFactor);
			
			int instructionLength = Integer.parseInt(txtInstructionLength.getText().trim());
			simulation.setInstructionLengthPerRequest(instructionLength);
			
			String loadBalancePolicy = (String) cmbLoadBalancingPolicy.getSelectedItem();
			simulation.setLoadBalancePolicy(loadBalancePolicy);
//		}
	}
	
	public boolean isValidConfiguration(){
		//TODO - complete
		
		int userGroupingFactor;
		int reqGroupingFactor;
		
		//Validate if grouping factors are numbers
		try {
			userGroupingFactor = Integer.parseInt(txtUserGroupingFactor.getText());
			reqGroupingFactor = Integer.parseInt(txtDcRequestGroupingFactor.getText());
		} catch (ParseException e){
			JOptionPane.showMessageDialog(this, "User Grouping Factor, DC Request Grouping Factor needs to be a number.");
			return false;
		} catch (NumberFormatException e){
			JOptionPane.showMessageDialog(this, "User Grouping Factor, DC Request Grouping Factor needs to be a number.");
			return false;
		}
		
		//Check if user grouping factor is less than req grouping factor
		if (userGroupingFactor < reqGroupingFactor){
			JOptionPane.showMessageDialog(this, "User Grouping Factor can not be less than DC Request Grouping Factor.");
			return false;
		}
		
//		int leastUserCount = Integer.MAX_VALUE;
//		
//		for (UserBaseUIElement ub : userBasesList){
//			if (ub.getOffPeakUserCount() < leastUserCount){
//				leastUserCount = ub.getOffPeakUserCount();
//			}
//			if (ub.getPeakUserCount() < leastUserCount){
//				leastUserCount = ub.getPeakUserCount();
//			}
//		}
//		
//		if (userGroupingFactor * 10 > leastUserCount){
//			JOptionPane.showMessageDialog(this, "The user grouping factor needs to be at least 10 times" +
//												" smaller than the least user count.");
//			return false;
//		}
		
		return true;
	}	
	
	/**
	 * Saves the simulation to a file.
	 */
	private void saveSimulation() {
		
		if (isValidConfiguration()){
			fileChooser.setDialogTitle("Save Configuration As");
			int status = fileChooser.showSaveDialog(this);
			if (status == JFileChooser.APPROVE_OPTION){
				File simFile = fileChooser.getSelectedFile();
				if (!simFile.getAbsolutePath().endsWith(SIM_FILE_EXTENSION)){
					simFile = new File(simFile.getAbsolutePath() + SIM_FILE_EXTENSION);
				}
				
				try {
					List<Object> entities = new ArrayList<Object>();
					entities.add(userBasesList);
					entities.add(dataCenterList);
					entities.add(vmAllocationList);
					entities.add(txtSimDuration.getText());
					entities.add(cmbTimeUnit.getSelectedItem());
					entities.add(txtUserGroupingFactor.getText());
					entities.add(txtDcRequestGroupingFactor.getText());
					entities.add(txtInstructionLength.getText());
					entities.add((String) cmbServiceBroker.getSelectedItem());
					entities.add((String) cmbLoadBalancingPolicy.getSelectedItem());
					
					IOUtil.saveAsXML(entities, simFile);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, 
												  "Saving of the configuration file failed!" + e1.getMessage(),
												  "I/O Error",
												  JOptionPane.ERROR_MESSAGE);
				}
			}		
		}
	}
	
	/**
	 * Loads simulation from a file.
	 */
	private void loadSimulationFromFile() {
		fileChooser.setDialogTitle("Open Configuration");
		int status =fileChooser.showOpenDialog(this);
		if (status == JFileChooser.APPROVE_OPTION){
			File simFile = fileChooser.getSelectedFile();
			
			try {
				List<Object> entities = (List<Object>) IOUtil.loadFromXml(simFile);
				
				List<UserBaseUIElement> ubData = (List<UserBaseUIElement>) entities.get(0);
				userBasesList.replaceContent(ubData);
				ubTableModel.fireTableDataChanged();
				
				List<DataCenterUIElement> dcData = (List<DataCenterUIElement>) entities.get(1);
				dataCenterList.replaceContent(dcData);
				dcTableModel.fireTableDataChanged();
				
				vmAllocationList = (List<VmAllocationUIElement>) entities.get(2);
				vmAllocTableModel.setData(vmAllocationList);
				vmAllocTableModel.fireTableDataChanged();
				
				String simDuration = (String) entities.get(3);
				txtSimDuration.setText(simDuration);
				
				String timeUnit = (String) entities.get(4);
				cmbTimeUnit.setSelectedItem(timeUnit);
				
				String userGroupingFactor = (String) entities.get(5);
				txtUserGroupingFactor.setText(userGroupingFactor);
				
				String reqGroupingFactor = (String) entities.get(6);
				txtDcRequestGroupingFactor.setText(reqGroupingFactor);
				
				String instLength = (String) entities.get(7);
				txtInstructionLength.setText(instLength);
				
				String serviceBrokerPolicy = (String) entities.get(8);
				cmbServiceBroker.setSelectedItem(serviceBrokerPolicy);
				
				String loadBalancePolicy = (String) entities.get(9);
				cmbLoadBalancingPolicy.setSelectedItem(loadBalancePolicy);
				
				//System.out.println("loaded config");
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, 
											  "Loading of the configuration file failed!" + e1.getMessage(),
											  "I/O Error",
											  JOptionPane.ERROR_MESSAGE);
			}
		}
		
		
	}
	
	public void cloudSimEventFired(CloudSimEvent e) {
		//Synchronise vmAllocationList content with additions or removals to
		//  dataCentersList
		if ((e.getId() == CloudSimEvents.EVENT_LIST_CONTENT_ADDED)
				|| (e.getId() == CloudSimEvents.EVENT_LIST_CONTENT_REMOVED)){
			Object param = e.getParameter(Constants.PARAM_DATA_ELEMENT);
			if ((param != null) && (param instanceof DataCenterUIElement)){
				DataCenterUIElement dc = (DataCenterUIElement) param;
				if (e.getId() == CloudSimEvents.EVENT_LIST_CONTENT_ADDED){
					if (dc.getVmAllocation() != null){
						vmAllocationList.add(dc.getVmAllocation());
					}
				} else {
					vmAllocationList.remove(dc.getVmAllocation());
				}
				initDcCombo();				
			}
		}
	}
	
	/**
	 * Table model used by the user bases table, main tab.
	 */
	private class UserBaseTableModel extends AbstractListTableModel<UserBaseUIElement> {

		public UserBaseTableModel(List<UserBaseUIElement> data){
			this.data = data;
			
			setColumnNames(new String[]{"Name", 
										 "Region", 
										 "Requests per\nUser \nper Hr",
										 "Data Size \nper Request \n(bytes)",
										 "Peak Hours \nStart (GMT)",
										 "Peak Hours \nEnd (GMT)",
										 COL_AVG_PEAK_USERS,
										 COL_AVG_OFF_PEAK_USERS});
			setUniqueColumns(new int[]{0});
			setNotNullColumns(new int[]{0, 1, 2, 3, 4, 5, 6, 7});
		}
		
		@Override
		protected void setValueAtInternal(Object value, int row, int col) {
			UserBaseUIElement ub = data.get(row);
			
			switch (col){
			case 0:
				ub.setName((String) value);
				break;
			case 1:
				ub.setRegion((Integer) value);
				break;
			case 2:
				ub.setReqPerHrPerUser((Integer) value);
				break;
			case 3:
				ub.setReqSize((Long) value);
				break;
			case 4:
				ub.setPeakHoursStart((Integer) value);
				break;
			case 5:
				ub.setPeakHoursEnd((Integer) value);
				break;
			case 6:
				ub.setPeakUserCount((Integer) value);
				break;
			case 7:
				ub.setOffPeakUserCount((Integer) value);
				break;
			}
		}		

		public Object getValueAt(int row, int col) {
			UserBaseUIElement ub = data.get(row);
			Object value = null;
			
			switch (col){
			case 0:
				value = ub.getName();
				break;
			case 1:
				value = ub.getRegion();
				break;
			case 2:
				value = ub.getReqPerHrPerUser();
				break;
			case 3:
				value = ub.getReqSize();
				break;
			case 4:
				value = ub.getPeakHoursStart();
				break;
			case 5:
				value = ub.getPeakHoursEnd();
				break;
			case 6:
				value = ub.getPeakUserCount();
				break;
			case 7:
				value = ub.getOffPeakUserCount();
				break;
			}
			
			return value;
		}
		
		public void setData(List<UserBaseUIElement> data){
			this.data = data;
		}
	}
	
	/**
	 * Table model used by the data centers table, data centers tab.
	 */
	private class DataCenterTableModel extends AbstractListTableModel<DataCenterUIElement> {

		private Map<Integer, MachineTableModel> childTableModels = new HashMap<Integer, MachineTableModel>();		
				
		public DataCenterTableModel(List<DataCenterUIElement> data) {
			super();
			setColumnNames(new String[]{"Name", 
										 "Region", 
										 "Arch",
										 "OS",
										 "VMM",
										 "Cost per \nVM $/Hr",
										 "Memory \nCost $/s",
										 "Storage \nCost $/s",
										 "Data \nTransfer \nCost $/Gb",
										 "Physical \nHW \nUnits"});
			setUniqueColumns(new int[]{0});
			setNotNullColumns(new int[]{0,1,2,3,4,5,6,7,8,9});
			this.data = data;
		}
		
		public void setData(List<DataCenterUIElement> data){
			this.data = data;
			fireTableDataChanged();
		}
		
		public int getRowCount() {
			return data.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			DataCenterUIElement dc = data.get(rowIndex);
			Object value = null;
			
			switch (columnIndex){
			case 0: 
				value = dc.getName();
				break;
			case 1: 
				value = dc.getRegion();
				break;			
			case 2:
				value = dc.getArchitecture();
				break;
			case 3:
				value = dc.getOs();
				break;
			case 4:
				value = dc.getVmm();
				break;
			case 5: 
				value = dc.getCostPerProcessor();
				break;
			case 6: 
				value = dc.getCostPerMem();
				break;
			case 7: 
				value = dc.getCostPerStorage();
				break;
			case 8: 
				value = dc.getCostPerBw();
				break;
			case 9: 
				value = dc.getMachineList().size();
				break;
			default: 
				break;
			}
			
			return value;
		}
		
		public void addChildTableModel(int row, MachineTableModel ctm){
			childTableModels.put(row, ctm);
		}
		
		public MachineTableModel getChildTableModel(int row){
			return childTableModels.get(row);
		}

		@Override
		protected void setValueAtInternal(Object value, int row, int col) {
			DataCenterUIElement dc = data.get(row);
			
			switch (col){
			case 0: 
				dc.setName((String) value);
				break;
			case 1: 
				dc.setRegion((Integer) value);
				break;			
			case 2:
				dc.setArchitecture((String) value);
				break;
			case 3:
				dc.setOs((String) value);
				break;
			case 4:
				dc.setVmm((String) value);
				break;
			case 5: 
				dc.setCostPerProcessor((Double) value);
				break;
			case 6: 
				dc.setCostPerMem((Double) value);
				break;
			case 7: 
				dc.setCostPerStorage((Double) value);
				break;
			case 8: 
				dc.setCostPerBw((Double) value);
				break;
			case 9: 
				//Can't set this value
				break;
			default: 
				break;
			}
		}		
	}
	
	/**
	 * Table model used by the machine details table, data centers tab.
	 */
	private class MachineTableModel extends AbstractListTableModel<MachineUIElement> {
		
		public MachineTableModel(List<MachineUIElement> data) {
			super();
			String[] columns = new String[]{"Id",
										   "Memory \n(Mb)",
										   "Storage \n(Mb)",
										   "Available \nBW",
										   "Number of \nProcessors",
										   "Processor \nSpeed",
										   "VM \nPolicy"};
			setColumnNames(columns);
			setUniqueColumns(new int[]{0});
			setNotNullColumns(new int[]{0, 1, 2, 3, 4, 5, 6});
			this.data = data;
		}

		@Override
		protected void setValueAtInternal(Object value, int row, int col) {
			MachineUIElement mc = data.get(row);
			
			switch (col){
			case 0:
				//Nothing to do
				break;
			case 1:
				mc.setMemory((Integer) value);
				break;
			case 2:
				mc.setStorage((Long) value);
				break;
			case 3:
				mc.setBw((Integer) value);
				break;
			case 4:
				mc.setProcessors((Integer) value);
				break;
			case 5:
				mc.setSpeed((Integer) value);
				break;
			case 6:
				mc.setVmAllocationPolicy((VmAllocationPolicy) value);
				break;
			}
		}

		public Object getValueAt(int row, int col) {
			MachineUIElement mc = data.get(row);
			Object value = null;
			
			switch (col){
			case 0:
				value = row;
				break;
			case 1:
				value = mc.getMemory();
				break;
			case 2:
				value = mc.getStorage();
				break;
			case 3:
				value = mc.getBw();
				break;
			case 4:
				value = mc.getProcessors();
				break;
			case 5:
				value = mc.getSpeed();
				break;
			case 6:
				value = mc.getVmAllocationPolicy();
				break;
			}
			
			return value;
		}		
	}
	
	/**
	 * Table model used by the allocations table, main tab.
	 */
	private class VmTableModel extends AbstractListTableModel<VmAllocationUIElement> {

		public VmTableModel(List<VmAllocationUIElement> data){
			setColumnNames(new String[]{"Data Center", "# VMs", "Image Size", "Memory", "BW"});
			setNotNullColumns(new int[]{0,1,2,3,4});
			setUniqueColumns(new int[]{0});
			
			this.data = data;
		}
		
		public void setData(List<VmAllocationUIElement> data){
			this.data = data;
		}
		
		@Override
		protected void setValueAtInternal(Object value, int row, int col) {
			//Make sure the same DC can not be allocated more than once in the VM Allocation table
			if ((col == 0) && (value instanceof DataCenterUIElement)){
				DataCenterUIElement dc = (DataCenterUIElement) value;
				for (int i = 0; i < data.size(); i++){
					if (i != row){
						VmAllocationUIElement vm = data.get(i);
						if ((vm.getDc() != null) && (vm.getDc().getName().equals(dc.getName()))){
							JOptionPane.showMessageDialog(ConfigureSimulationPanel.this, dc.getName() + " is already allocated.");
							return;
						}
					}
				}
			}
			
			VmAllocationUIElement vm = data.get(row);
			
			switch (col){
			case 0:
				DataCenterUIElement selectedDc = (DataCenterUIElement) value;
				vm.setDc(selectedDc);
				selectedDc.setVmAllocation(vm);
				break;
			case 1:
				vm.setVmCount((Integer) value);
				break;
			case 2:
				vm.setImageSize((Long) value);
				break;
			case 3:
				vm.setMemory((Integer) value);
				break;
			case 4:
				vm.setBw((Long) value);
				break;
			}
		}

		public Object getValueAt(int row, int col) {
			VmAllocationUIElement vm = data.get(row);
			Object value = null;
			
			switch (col){
			case 0:
				value = (vm.getDc() != null) ? vm.getDc().getName() : "";
				break;
			case 1:
				value = vm.getVmCount();
				break;
			case 2:
				value = vm.getImageSize();
				break;
			case 3:
				value = vm.getMemory();
				break;
			case 4:
				value = vm.getBw();
				break;
			}
			
			return value;
		}
		
	}
	
	/**
	 * Table selection listener
	 */
	private class DcTableSelectionListener implements ListSelectionListener {

		JTable table;
	    
        public DcTableSelectionListener(JTable table) {
            this.table = table;
        }
        
        public void valueChanged(ListSelectionEvent e) {
            
        	if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
            } else {
                int row = table.getSelectedRow();
                showDcDetails(row);
            }
        }
	}
}
