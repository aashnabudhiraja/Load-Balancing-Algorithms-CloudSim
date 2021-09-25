package cloudsim.ext.gui.screens;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import cloudsim.ext.Constants;
import cloudsim.ext.Simulation;
import cloudsim.ext.gui.utils.SimMeasure;
import cloudsim.ext.gui.utils.SimpleGraph;
import cloudsim.ext.gui.utils.SimpleTableModel;
import cloudsim.ext.stat.HourlyEventCounter;
import cloudsim.ext.util.PdfExporter;

import com.lowagie.text.DocumentException;

/**
 * The Results screen.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class ResultsScreen extends JPanel implements ActionListener {
	private static final String CMD_EXPORT_RESULTS = "export_results";
	
	private JPanel mainPanel;
	private double avgResponseTime;
	private double minResponseTime;
	private double maxResponseTime;
	private double avgProcessingTime;
	private double minProcessingTime;
	private double maxProcessingTime;
	private DecimalFormat df;
	private Map<String, Object> results;
	private Map<String, BufferedImage> ubResponseGraphs;
	private Map<String, BufferedImage> dcProcTimeGraphs;
	private Map<String, BufferedImage> dcLoadingGraphs;
	private SimpleTableModel ubStatsTableModel;
	private SimpleTableModel dcProcTimeTableModel;
	private SimpleTableModel costTableModel;
	private double totalCost;
	private double vmCost;
	private double dataCost;
	
	/** Constructor */
	public ResultsScreen(Simulation simulation){
		df = new DecimalFormat("#0.00");
		
		initUI();
		Map<String, Object> results = simulation.getResults();
		setResults(results);
		
	}
	
	private void initUI(){
		mainPanel = new JPanel();
		this.add(mainPanel, BorderLayout.CENTER);
	}
	
	private void setResults(Map<String, Object> results){
		this.results = results;
		
		JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new BorderLayout());
		
		JPanel mainContentPanel = new JPanel();
		mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));		
		
		Map<String, SimMeasure> ubStats = (Map<String, SimMeasure>) results.get(Constants.UB_STATS);		
		mainContentPanel.add(createResponseTimeStatsPanel(ubStats));
		
		Map<String, SimMeasure> dcProcTimes = (Map<String, SimMeasure>) results.get(Constants.DC_PROCESSING_TIME_STATS);
		mainContentPanel.add(createProcessingTimeStatsPanel(dcProcTimes));
		
		Map<String, HourlyEventCounter> dcArrivalStats = (Map<String, HourlyEventCounter>) results.get(Constants.DC_ARRIVAL_STATS);
		mainContentPanel.add(createDcArrivalRateGraphsPanel(dcArrivalStats));		
		
		Map<String, Map<String, Double>> costs = (Map<String, Map<String, Double>>) results.get(Constants.COSTS);
		mainContentPanel.add(createCostsPanel(costs));
		
		resultsPanel.add(mainContentPanel, BorderLayout.CENTER);
		
		resultsPanel.add(createSummaryPanel(), BorderLayout.NORTH);
				
		mainPanel.add(resultsPanel);
		this.revalidate();
	}
	
	private JPanel createCostsPanel(Map<String, Map<String, Double>> costs){
		JPanel costPanel = new JPanel();
		costPanel.setLayout(new BorderLayout());
		costPanel.setBorder(new EmptyBorder(20, 5, 5, 5));
		
		costTableModel = new SimpleTableModel(new String[]{"Data Center", "VM Cost", "Data Transfer Cost", "Total"});
		double dcVmCost;
		double dcDataCost;
		double dcTotalCost;
		
		for (String dcName : costs.keySet()){
			Map<String, Double> dcCosts = costs.get(dcName);
			
			dcVmCost = dcCosts.get(Constants.VM_COST);
			vmCost += dcVmCost;
			dcDataCost = dcCosts.get(Constants.DATA_COST);
			dataCost += dcDataCost;
			dcTotalCost = dcCosts.get(Constants.TOTAL_COST);
			totalCost += dcTotalCost;
			
			costTableModel.addRow(new Object[]{dcName, dcVmCost, dcDataCost, dcTotalCost});			
		}		
		
		String resText = "<html><h2>Cost</h2>" 
						+ "<table><tr><td>Total Virtual Machine Cost :</td><td>$" + df.format(vmCost) + "</td></tr>" 
						+ "<tr><td>Total Data Transfer Cost   : </td><td>$" + df.format(dataCost) + "</td></tr>"
						+ "<tr><td><h3>Grand Total                : </h3></td><td>$" + df.format(totalCost) + "</td></tr>"
						+ "</table></html>";
		costPanel.add(new JLabel(resText), BorderLayout.NORTH);
		
		JTable costTable = new JTable(costTableModel);
		costTable.setPreferredScrollableViewportSize(new Dimension(300, 20 * costs.size()));
		JScrollPane scrollPane = new JScrollPane(costTable);
		scrollPane.setBorder(new EmptyBorder(20, 0, 0, 0));
		costPanel.add(scrollPane, BorderLayout.CENTER);

		return costPanel;
	}
	
	private JPanel createSummaryPanel(){
		JPanel summaryPanel = new JPanel();
		summaryPanel.setLayout(new BorderLayout());
		
		JLabel summaryHeading = new JLabel("<html><h2>Overall Response Time Summary</h2></html>");
		summaryPanel.add(summaryHeading, BorderLayout.NORTH);
		
		String detailsText = "<html><table>"
							+ "<tr><th></th><th>Average (ms)</th><th>Minimum (ms)</th><th>Maximum (ms)</th></tr>"
							+ "<tr><td>Overall Response Time:</td><td>" + df.format(avgResponseTime) + "</td><td>"
							+ df.format(minResponseTime) + "</td><td>" + df.format(maxResponseTime) + "</td><td></tr>"
							+ "<tr><td>Data Center Processing Time:</td><td>" + df.format(avgProcessingTime) + "</td><td>" 
							+ df.format(minProcessingTime) + "</td><td>" + df.format(maxProcessingTime) + "</td><td></tr>"
							+ "</table></html>";							
		JLabel details = new JLabel(detailsText);
					
		summaryPanel.add(details, BorderLayout.CENTER);
		
		JButton btnExportResults = new JButton("Export Results");
		btnExportResults.setActionCommand(CMD_EXPORT_RESULTS);
		btnExportResults.addActionListener(this);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new EmptyBorder(0, 30, 0, 0));
		buttonPanel.add(btnExportResults);
		summaryPanel.add(buttonPanel, BorderLayout.EAST);
		
		return summaryPanel;
	}
	
	private JPanel createResponseTimeStatsPanel(Map<String, SimMeasure> ubStats) {
		int count = 0;
		int rowCount = 0;
		double avgTotal = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double maxAvg = Double.MIN_VALUE;
		double currMax;
		double currMin;
		double currAvg;
		
		JPanel responseStatsPanel = new JPanel();
		responseStatsPanel.setBorder(new EmptyBorder(20, 5, 5, 5));
		responseStatsPanel.setLayout(new BorderLayout());
		
		ubStatsTableModel = new SimpleTableModel(new String[]{"Userbase", "Avg (ms)", "Min (ms)", "Max (ms)"});
		JTable resTable = new JTable(ubStatsTableModel);
		
		List<SimMeasure> sortedStats = new ArrayList<SimMeasure>(ubStats.values());
		Map<String, long[]> hourlyResponseTimes = new HashMap<String, long[]>();
		
		for(SimMeasure m : sortedStats){
			String measureName = m.getName();
			if (measureName.equals(Constants.UB_RESPONSE_TIME)){
				//Update results table
				ubStatsTableModel.addRow(new Object[]{m.getEntityName(), m.getAvg(), m.getMin(), m.getMax()});
				
				//Use the loop to calculate overall results
				if (m.getType().equals(Constants.MEASURE_TYPE_USER_BASE_RESPONSE)){
					currMax = m.getMax();
					if (currMax > max){
						max = currMax;
					}
					
					currMin = m.getMin();
					if (currMin < min){
						min = currMin;
					}
					
					currAvg = m.getAvg();
					avgTotal += (currAvg * m.getCount());
										
					count += m.getCount();
					rowCount++;
				} 
			} else {
				String ub = m.getEntityName();
				long[] avgTimes = hourlyResponseTimes.get(ub);
				if (avgTimes == null){
					avgTimes = new long[24];
					hourlyResponseTimes.put(ub, avgTimes);
				}
				
				String hourStr = measureName.substring(measureName.lastIndexOf(Constants.STANDARD_SEPARATOR) + 1);
				int hour = Integer.parseInt(hourStr);
				currAvg = m.getAvg();
				avgTimes[hour] = (long) currAvg;
				
				if (currAvg > maxAvg){
					maxAvg = currAvg;
				}
				
			}
		}						
		
		avgResponseTime = avgTotal / count;
		minResponseTime = min;
		maxResponseTime = max;
		
		resTable.setPreferredScrollableViewportSize(new Dimension(300, 20 * rowCount));
		resTable.setEnabled(false);
		JScrollPane tblPanel = new JScrollPane(resTable);
		responseStatsPanel.add(new JLabel("<html><h3>Response Time By Region</h3></html>"), BorderLayout.NORTH);
		responseStatsPanel.add(tblPanel, BorderLayout.CENTER);
		
		//Create graphs
		JPanel graphPanel = createUbResponseTimeGraphsPanel(hourlyResponseTimes, maxAvg);//maxResponseTime);
		responseStatsPanel.add(graphPanel, BorderLayout.SOUTH);
		
		return responseStatsPanel;
	}
	
	private JPanel createUbResponseTimeGraphsPanel(Map<String, long[]> hourlyResponseTimes, double overallMax) {
		JPanel graphPanel = new JPanel();
		int cols = 2;
		int graphCount = hourlyResponseTimes.size();
		int rows = (graphCount % cols == 0) ? (graphCount / cols)
				: (graphCount / cols) + 1;
		graphPanel.setLayout(new GridLayout(rows, cols));

		List<String> sortedStatsList = new LinkedList<String>();
		sortedStatsList.addAll(hourlyResponseTimes.keySet());
		Collections.sort(sortedStatsList);		
		
		ubResponseGraphs = new HashMap<String, BufferedImage>();

		for (String statName : sortedStatsList) {
			JPanel p = new JPanel();
			p.add(new JLabel(statName), BorderLayout.NORTH);

			String[] xAxisLabels = new String[24];
			for (int i = 0; i < 24; i++) {
				xAxisLabels[i] = "" + i;
			}

			SimpleGraph graph = new SimpleGraph(hourlyResponseTimes.get(statName), 
											   xAxisLabels, 
											   new String[] {"Response Time (ms)", "Hrs" }, 
											   overallMax);
			// new HourlyStatGraph(arrivalStats.get(statName),
			// overloadingStats.get(statName), overallMax);
//			graph.setBorder(new LineBorder(Color.RED));

			p.add(graph, BorderLayout.CENTER);
			graphPanel.add(p);
			
			ubResponseGraphs.put(statName, graph.getGraphImage());
		}
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(new JLabel("<html><h3>User Base Hourly Average Response Times</h3></html>"),
						BorderLayout.NORTH);
		bottomPanel.add(graphPanel);
		bottomPanel.setBorder(new EmptyBorder(20, 5, 5, 5));
		return bottomPanel;
	}
	
	private JPanel createProcessingTimeStatsPanel(Map<String, SimMeasure> procTimes){
		int count = 0;
		int rowCount = 0;
		double avgTotal = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double currMax;
		double currMin;
		double maxAvg = Double.MIN_VALUE;
		double currAvg;
		
		JPanel procTimeStatsPanel = new JPanel();
		procTimeStatsPanel.setBorder(new EmptyBorder(20, 5, 5, 5));
		procTimeStatsPanel.setLayout(new BorderLayout());
		
		dcProcTimeTableModel = new SimpleTableModel(new String[]{"Data Center", "Avg (ms)", "Min (ms)", "Max (ms)"});
		JTable procTimeTable = new JTable(dcProcTimeTableModel);
		procTimeTable.setEnabled(false);
		
		Map<String, long[]> hourlyProcTimes = new HashMap<String, long[]>();
		
		for(SimMeasure m : procTimes.values()){
			
			String measureName = m.getName();
			if (measureName.equals(Constants.DC_SERVICE_TIME)){
				dcProcTimeTableModel.addRow(new Object[]{m.getEntityName(), m.getAvg(), m.getMin(), m.getMax()});			
			
				//Use the loop to calculate overall results
				if (m.getType().equals(Constants.MEASURE_TYPE_DC_PROCESSING_TIME)){
					currMax = m.getMax();
					if (currMax > max){
						max = currMax;
					}
					
					currMin = m.getMin();
					if (currMin < min){
						min = currMin;
					}
					
					currAvg = m.getAvg();
					avgTotal += (m.getAvg() * m.getCount());
					
					count += m.getCount();
					rowCount++;
				}
			} else {
				String dc = m.getEntityName();
				long[] avgTimes = hourlyProcTimes.get(dc);
				if (avgTimes == null){
					avgTimes = new long[24];
					hourlyProcTimes.put(dc, avgTimes);
				}
				
				String hourStr = measureName.substring(measureName.lastIndexOf(Constants.STANDARD_SEPARATOR) + 1);
				int hour = Integer.parseInt(hourStr);
				currAvg = m.getAvg();
				avgTimes[hour] = (long) currAvg;
				
				if (currAvg > maxAvg){
					maxAvg = currAvg;
				}
				
			}
		}						
		
		avgProcessingTime = avgTotal / count;
		minProcessingTime = min;
		maxProcessingTime = max;
		
		procTimeStatsPanel.add(new JLabel("<html><h3>Data Center Request Servicing Times</h3></html>"), BorderLayout.NORTH);
		
		procTimeTable.setPreferredScrollableViewportSize(new Dimension(300, 20 * rowCount));
		JScrollPane tblPanel = new JScrollPane(procTimeTable);
		
		procTimeStatsPanel.add(tblPanel, BorderLayout.CENTER);
		
		//Create graphs
		JPanel graphPanel = createDcProcTimeGraphsPanel(hourlyProcTimes, maxAvg);//maxResponseTime);
		procTimeStatsPanel.add(graphPanel, BorderLayout.SOUTH);
		
		return procTimeStatsPanel;
	}
	
	private JPanel createDcProcTimeGraphsPanel(Map<String, long[]> hourlyResponseTimes, double overallMax) {
		JPanel graphPanel = new JPanel();
		int cols = 2;
		int graphCount = hourlyResponseTimes.size();
		int rows = (graphCount % cols == 0) ? (graphCount / cols)
				: (graphCount / cols) + 1;
		graphPanel.setLayout(new GridLayout(rows, cols));

		List<String> sortedStatsList = new LinkedList<String>();
		sortedStatsList.addAll(hourlyResponseTimes.keySet());
		Collections.sort(sortedStatsList);		

		dcProcTimeGraphs = new HashMap<String, BufferedImage>();
		
		for (String statName : sortedStatsList) {
			JPanel p = new JPanel();
			p.add(new JLabel(statName), BorderLayout.NORTH);

			String[] xAxisLabels = new String[24];
			for (int i = 0; i < 24; i++) {
				xAxisLabels[i] = "" + i;
			}

			SimpleGraph graph = new SimpleGraph(hourlyResponseTimes.get(statName), 
											   xAxisLabels, 
											   new String[] {"Processing Time (ms)", "Hrs" }, 
											   overallMax);
//			graph.setBorder(new LineBorder(Color.YELLOW));

			p.add(graph, BorderLayout.CENTER);
			graphPanel.add(p);
			dcProcTimeGraphs.put(statName, graph.getGraphImage());
		}
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(new JLabel("<html><h3>Data Center Hourly Average Processing Times</h3></html>"),
						BorderLayout.NORTH);
		bottomPanel.add(graphPanel);
		bottomPanel.setBorder(new EmptyBorder(20, 5, 5, 5));
		return bottomPanel;
	}

	private JPanel createDcArrivalRateGraphsPanel(Map<String, HourlyEventCounter> arrivalStats) {
		JPanel graphPanel = new JPanel();
		int cols = 2;
		int graphCount = arrivalStats.size();
		int rows = (graphCount % cols == 0) ? (graphCount / cols) : (graphCount / cols) + 1;
		graphPanel.setLayout(new GridLayout(rows, cols));
		
		List<String> sortedStatsList = new LinkedList<String>();
		sortedStatsList.addAll(arrivalStats.keySet());
		Collections.sort(sortedStatsList);
		
		//Need to find the overall maximum for the graphs
		double overallMax = -1;
		double thisMax;
		for (HourlyEventCounter hs : arrivalStats.values()){
			thisMax = hs.getMax();
			if (thisMax > overallMax){
				overallMax = thisMax;
			}
		}
		
		dcLoadingGraphs = new HashMap<String, BufferedImage>();
		
		for (String statName : sortedStatsList){
			JPanel p = new JPanel();
			p.add(new JLabel(statName), BorderLayout.NORTH);
			
			String[] xAxisLabels = new String[24];
			for (int i = 0; i < 24; i++){
				xAxisLabels[i] = "" + i;
			}
			
			SimpleGraph graph = new SimpleGraph(arrivalStats.get(statName).getHourlyCount(), 
												xAxisLabels, 
												new String[]{"Req's per Hr", "Hrs"},
												overallMax); 
				//new HourlyStatGraph(arrivalStats.get(statName), overloadingStats.get(statName), overallMax);
//			graph.setBorder(new LineBorder(Color.RED));
			
			p.add(graph, BorderLayout.CENTER);
			graphPanel.add(p);
			
			dcLoadingGraphs.put(statName, graph.getGraphImage());
		}
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(new JLabel("<html><h3>Data Center Loading</h3></html>"), BorderLayout.NORTH);
		bottomPanel.add(graphPanel);
		bottomPanel.setBorder(new EmptyBorder(20, 5, 5, 5));
		return bottomPanel;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CMD_EXPORT_RESULTS)){
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileFilter(){
				@Override
				public boolean accept(File f) {
					if (f.getAbsolutePath().toLowerCase().endsWith(Constants.PDF_EXTENSION)){
						return true;
					} else if (f.isDirectory()){
						return true;
					} else {
						return false;
					}
				}

				@Override
				public String getDescription() {
					return Constants.PDF_EXTENSION;
				}
			});
			fileChooser.setDialogTitle("Save Results As");
			int status = fileChooser.showSaveDialog(this.getParent());
			if (status == JFileChooser.APPROVE_OPTION){
				File file = fileChooser.getSelectedFile();
				if (!file.getAbsolutePath().endsWith(Constants.PDF_EXTENSION)){
					file = new File(file.getAbsolutePath() + Constants.PDF_EXTENSION);
				}
				try {
					createPdf(file);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this.getParent(), 
												  "Exporting the results failed due to an internal error. " + e1.getMessage(),
												  "I/O Error",
												  JOptionPane.ERROR_MESSAGE);
				} catch (DocumentException e1) {
					JOptionPane.showMessageDialog(this.getParent(), 
												  "Exporting the results failed due to an internal error. " + e1.getMessage(),
												  "I/O Error",
												  JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}


	private void createPdf(File file) throws IOException, DocumentException{
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date finishedTime = (Date) results.get(Constants.SIMULATION_COMPLETED_TIME);
		String header = "Results of the Simulation Completed at: " + df.format(finishedTime);
		
		List<Object[]> summary = new ArrayList<Object[]>();
		summary.add(new Object[]{"Overall response time:", avgResponseTime, minResponseTime, maxResponseTime});
		summary.add(new Object[]{"Data Center processing time:", avgProcessingTime, minProcessingTime, maxProcessingTime});
		
		List<Object[]> ubStats = ubStatsTableModel.getData();
		List<Object[]> dcStats = dcProcTimeTableModel.getData();
		
		List<Object[]> costSummary = new ArrayList<Object[]>();
		costSummary.add(new Object[]{"Total Virtual Machine Cost ($):", vmCost});
		costSummary.add(new Object[]{"Total Data Transfer Cost ($):", dataCost});
		costSummary.add(new Object[]{"Grand Total: ($)", totalCost});
		
		List<Object[]> costDetails = costTableModel.getData();
		
		PdfExporter.saveToPdf(file, header, summary, ubStats, ubResponseGraphs, 
							  dcStats, dcProcTimeGraphs, dcLoadingGraphs, costSummary, costDetails);
		
		//TODO - Add simulation configuration to the pdf
	}
}
