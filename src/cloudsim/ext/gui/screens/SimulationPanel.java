package cloudsim.ext.gui.screens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.gui.SimulationUIElement;
import cloudsim.ext.gui.UserBaseUIElement;
import cloudsim.ext.gui.utils.CommunicationPath;
import cloudsim.ext.gui.utils.SimMeasure;
import cloudsim.ext.util.InternetEntitityRegistry;

/**
 * The main simulation panel displaying the map of the world.
 *
 * @author Bhathiya Wickremasinghe
 *
 */
public class SimulationPanel extends JPanel implements CloudSimEventListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final int mapWidth = 800;
	private final int mapHeight = 460;
	private BufferedImage map1;
	private BufferedImage map2;
	private final Map<String, SimulationUIElement> internetEntities;
	private final Set<CommunicationPath> communicationPaths;
	private Set<JLabel> resultsPanels;
	private boolean showBoundaries = false;

	/** Constructor. */
	public SimulationPanel(){
		//File mapFile1 = new File("resources/map1.png");
		//File mapFile2 = new File("resources/map2.png");
		InputStream mapFile1 = getClass().getClassLoader().getResourceAsStream("map1.png");
		InputStream mapFile2 = getClass().getClassLoader().getResourceAsStream("map2.png");

		try {
			map1 = ImageIO.read(mapFile1);
			map2 = ImageIO.read(mapFile2);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load the map file.");
		}

		Dimension dim = new Dimension(mapWidth, mapHeight);
		this.setPreferredSize(dim);
		this.setMaximumSize(dim);
		this.setMinimumSize(dim);

		InternetEntitityRegistry interenetEntityRegistry = InternetEntitityRegistry.getInstance();
		internetEntities = interenetEntityRegistry.getInternetEntities();
		communicationPaths = interenetEntityRegistry.getCommunicationPaths();
		interenetEntityRegistry.addCloudSimEventListener(this);

	}

	@Override
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) g;

		if (!showBoundaries){
			g2.drawImage(map1, 0, 0, mapWidth, mapHeight, null);
		} else {
			g2.drawImage(map2, 0, 0, mapWidth, mapHeight, null);
		}

		drawInternetEntities(g2);

		drawCommPaths(g2);

		if (resultsPanels != null){
			try {
				for (JLabel pnl : resultsPanels){
					pnl.paint(g);
				}
			} catch (Exception e){

			}
		}
	}

	private void drawCommPaths(Graphics2D g2){

		try{
			g2.setColor(Color.GRAY);

			for (CommunicationPath path : communicationPaths){
				Point2D src = path.getSrc().getLocation();
				Point2D dest = path.getDest().getLocation();
				g2.drawLine((int) src.getX(), (int) src.getY(), (int) dest.getX(), (int) dest.getY());
			}
		} catch (ConcurrentModificationException e){
			//Just skip this paint cycle
		}
	}

	private void drawInternetEntities(Graphics2D g2){

		for (SimulationUIElement e : internetEntities.values()){
			e.paint(g2);
		}
	}

	public void cloudSimEventFired(CloudSimEvent e) {
		this.updateUI();
		this.repaint();
	}

	public void setShowBoundaries(boolean status){
		showBoundaries = status;
		repaint();
	}

	public void setResults(Map<String, Object> results){
		Map<String, SimMeasure> ubStats = (Map<String, SimMeasure>) results.get(Constants.UB_STATS);
		resultsPanels = Collections.synchronizedSet(new HashSet<JLabel>());
		int count = 0;
		NumberFormat df = new DecimalFormat("#.0");

		for (String entityName : internetEntities.keySet()){
			SimulationUIElement elem = internetEntities.get(entityName);


			if (elem instanceof UserBaseUIElement) {
				String statName = elem.getName() + "||" + Constants.UB_RESPONSE_TIME;

				StringBuffer buff = new StringBuffer();
				buff.append("<html>Resp. time<br/><table>");

				SimMeasure stat = ubStats.get(statName);
				buff.append("<tr><td>Avg:</td><td>");
				buff.append(df.format(stat.getAvg()));
				buff.append("ms</td></tr><tr><td>Max:</td><td>");
				buff.append(df.format(stat.getMax()));
				buff.append("ms</td></tr><tr><td>Min:</td><td>");
				buff.append(df.format(stat.getMin()));
				buff.append("ms</td></tr>");

				buff.append("</html></html>");

				SummaryResultsPanel result = new SummaryResultsPanel(buff.toString());
				Point2D location = elem.getLocation();
				result.setLocation((int) location.getX(), (int) location.getY() + 5);

				resultsPanels.add(result);

				count++;
			}


		}

		repaint();
	}

	/**
	 * The yellow on screen results labels.
	 */
	private class SummaryResultsPanel extends JLabel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private int x;
		private int y;
		private final int width = 80;
		private final int height = 60;

		public SummaryResultsPanel(String results){
			super(results);

			super.setFont(new Font(super.getFont().getName(), Font.PLAIN, 9) );
			super.setBackground(Color.YELLOW);
			super.setOpaque(true);
			super.setForeground(Color.BLACK);
			super.setBounds(x, y, width, height);
		}

		@Override
		public void setLocation(int x, int y){
			this.x = x;
			this.y = y;

		}


		@Override
		public void paint(Graphics g){
			g.translate(x, y);
			super.paint(g);

			g.translate(-x , -y);
		}
	}
}
