package cloudsim.ext.gui.utils;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * Used to display multi-line table headers in JTables.
 * @author Bhathiya Wickremasinghe
 *
 */
public class MultilineTableHeaderRenderer extends JList implements
		TableCellRenderer {
	
	public MultilineTableHeaderRenderer() {
		setOpaque(true);
		setForeground(UIManager.getColor("TableHeader.foreground"));
		setBackground(UIManager.getColor("TableHeader.background"));
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		ListCellRenderer renderer = getCellRenderer();
		((JLabel) renderer).setHorizontalAlignment(JLabel.CENTER);
		setCellRenderer(renderer);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setFont(table.getFont());
		String str = (value == null) ? "" : value.toString();
		BufferedReader br = new BufferedReader(new StringReader(str));
		String line;
		Vector v = new Vector();
		try {
			while ((line = br.readLine()) != null) {
				v.addElement(line);
			}
		} catch (IOException ex) {
			System.out.println("Error occured in multiline table header rendering");
			ex.printStackTrace();
		}
		setListData(v);
		return this;
	}
}