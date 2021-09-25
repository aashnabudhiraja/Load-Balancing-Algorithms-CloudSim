package cloudsim.ext.gui.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

import javax.swing.JPanel;

/**
 * A very simple area graph painted on a {@link JPanel}.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class SimpleGraph extends JPanel {
		
	private final Dimension GRAPH_DIM = new Dimension(300, 100);
	private final Dimension PANEL_DIM = new Dimension(330, 110);
	private int colWidth;
	private double maxHeight;
	private double maxSize;
	private Font dataLabelFont;
	private Color graphColor;
	private long[] dataValues;
	private String[] dataLabels;
	private long[] dataValueLabels;
	private String[] axisLabels;
	private BufferedImage graph;
	
	private int xMargin = 30;
	private int yMargin = 15;
	private Font axisLabelFont;
	private FontMetrics dataLabelFontMetrics;
			
	/** Constructor. */
	public SimpleGraph(long[] dataValues, 
					   String[] dataLabels,
					   String[] axisNames,
					   double overallMax){
		this.dataValues = dataValues;
		this.dataLabels = dataLabels;
		this.axisLabels = axisNames;
		
		this.setPreferredSize(PANEL_DIM);
		this.setMinimumSize(PANEL_DIM);
		this.setMaximumSize(PANEL_DIM);
				
		colWidth = (int) (GRAPH_DIM.getWidth() - 20) / 24;
		maxHeight = (GRAPH_DIM.getHeight() - 20);
		if (overallMax > 0){
			maxSize = overallMax;//stat.getMax();
		} else {
			long localMax = -1;
			for (long val : dataValues){
				if (val > localMax){
					localMax = val;
				}
			}
			
			maxSize = localMax;
		}
		
		Font currfont = this.getFont();
		dataLabelFont = new Font(currfont.getName(), Font.PLAIN, 8);
		axisLabelFont = new Font(currfont.getName(), Font.BOLD, 9);
		
		dataLabelFontMetrics = this.getFontMetrics(dataLabelFont);
		graphColor = new Color(50, 50, 100);
		
		prepareDataValueLabels();
		prepareGraph();
	}
	
	private void prepareDataValueLabels(){
		double currMax = maxSize / 10;
		int orderOfMax = 0;
		while (currMax >= 1){
			orderOfMax++;
			currMax /= 10;
		}
		
		String maxStr = Double.toString(maxSize);
		String first = maxStr.substring(0,1);
		int firstDigit = Integer.parseInt(first);

		if (firstDigit == 1){
			int labelCount;
			if (maxStr.length() > 1){
				try {
					labelCount = Integer.parseInt(maxStr.substring(0,2)) / 2;
				} catch (NumberFormatException e){
					labelCount = 5;
				}
			} else {
				labelCount = 5;
			}
			dataValueLabels = new long[labelCount];
			for (int i = 0; i < labelCount; i++){
				dataValueLabels[i] = (long) ((i + 1) * 2  * Math.pow(10, (orderOfMax - 1)));
			}
		} else {
			dataValueLabels = new long[firstDigit];
			for (int i = 0; i < firstDigit; i++){
				dataValueLabels[i] = (long) ((i + 1) * Math.pow(10, orderOfMax));
			}
		}
	}
	
	private void prepareGraph(){
		GeneralPath graphShape = new GeneralPath();
		graph = new BufferedImage(PANEL_DIM.width, PANEL_DIM.height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = graph.createGraphics();
		
		g2.setColor(Color.black);
		g2.setFont(dataLabelFont);
		
		g2.translate(xMargin, yMargin);
		
		//Draw axes
		g2.drawLine(0, (int) maxHeight, 0, 0);
		g2.drawLine(0, (int) maxHeight, (int) colWidth * dataLabels.length, (int) maxHeight);
		
		int x = 0; 
		int x1;
		
		//Draw X axis data labels
		if (dataLabels != null){
			for (int i = 0; i <  dataLabels.length; i++){
				x1 = (int) (x);// - (colWidth / 2));
				g2.drawLine(x, (int) maxHeight + 2, x, (int) maxHeight);
				g2.drawString("" + i, x1, (int) (maxHeight + 10));	
				
				x += colWidth;
			}
		}
			
		//Draw axis names
		if ((axisLabels != null) && (axisLabels.length == 2)){
			g2.setFont(axisLabelFont);
			
			//Y axis name. Using the x value from above
			g2.drawString(axisLabels[1], (int) (x + (colWidth / 2)), (int) (maxHeight + 10));
			
			//X axis name
			g2.drawString(axisLabels[0], 0, -5);
		}
		
		//Draw Y axis data labels
		if (dataValueLabels != null){
			g2.setFont(dataLabelFont);
			
			int y;
			long val;
			String lbl;
			for (int i = 0; i < dataValueLabels.length; i++){
				val = dataValueLabels[i];
				if (val >= 1000000){
					lbl = NumberFormat.getInstance().format(val / 1000000) + "M";
				} else {
					lbl = (NumberFormat.getInstance().format(val));
				}
				x = - dataLabelFontMetrics.stringWidth(lbl); 
				y = (int)((maxHeight - val * maxHeight / maxSize) 
					+ (dataLabelFontMetrics.getMaxAscent() / 2));
				g2.drawString(lbl, x, y);
			}
		}
		
		x = 0; 
		double y = 0;
		double colHeight; 
		graphShape.moveTo(0, maxHeight);
		boolean first = true;		
		
		//Draw graph body
		for (int i = 0; i <  dataValues.length; i++){
			colHeight = ((dataValues[i] / maxSize) * maxHeight);
			
			//If value is almost zero round it to 1 pixels
			if ((colHeight == 0) && (dataValues[i] != 0)){
				colHeight = 1;
			}
			y = (maxHeight - colHeight);
			
			if (first){
				graphShape.lineTo(x, y);
				x += (colWidth / 2);	
				first = false;
			}
			
			
			graphShape.lineTo(x, y);
			
			//*************************************************
//			g2.drawString("" + dataValues[i], x, (int) y );
			//*************************************************
			
			x += colWidth;
		}
		
		x -= (colWidth / 2);
		graphShape.lineTo(x, y);
		graphShape.lineTo(x, maxHeight);
		graphShape.closePath();
		
		g2.setColor(graphColor);
		g2.fill(graphShape);
	}
	
	
	public void paint(Graphics g){
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.drawImage(graph, 0, 0, PANEL_DIM.width, PANEL_DIM.height, null);
	}

	/**
	 * @return the graph
	 */
	public BufferedImage getGraphImage() {
		return graph;
	}
	
	
}
