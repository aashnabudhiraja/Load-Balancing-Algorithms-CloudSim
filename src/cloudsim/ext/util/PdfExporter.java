package cloudsim.ext.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Exports results in to PDF format using iText. 
 * @author Bhathiya Wickremasinghe
 *
 */
public class PdfExporter {
	
	private static final DecimalFormat df = new DecimalFormat("#0.00");

	public static void saveToPdf(File file, 
								 String header,
								 List<Object[]> summary,
								 List<Object[]> ubStats,
								 Map<String, BufferedImage> ubResponseGraphs,
								 List<Object[]> dcStats,
								 Map<String, BufferedImage> dcProcTimeGraphs,
								 Map<String, BufferedImage> dcLoadingGraphs,
								 List<Object[]> costSummary,
								 List<Object[]> costDetails) throws IOException, DocumentException {
		Document pdf = new Document();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
	
		PdfWriter.getInstance(pdf, bout);
		pdf.open();
		
		addHeader(pdf, header, 14, true);
		pdf.add(new Paragraph(" "));
				
		addSummary(pdf, summary);
		
		addUbResults(pdf, ubStats);
		addGraphs(pdf, ubResponseGraphs, "User Base Hourly Response Times");
		
		addDcProcessingStats(pdf, dcStats);
		
		addGraphs(pdf, dcProcTimeGraphs, "Data Center Hourly Average Processing Times");
		addGraphs(pdf, dcLoadingGraphs, "Data Center Hourly Loading");
		
		addCostSummary(pdf, costSummary);
		addCostDetails(pdf, costDetails);
		
		pdf.close();

		FileOutputStream out = new FileOutputStream(file);
		bout.writeTo(out);
		out.flush();
		
		out.close();
		bout.close();
	}
	
	public static void addHeader(Document pdf, String text, int fontSize) throws DocumentException{
		addHeader(pdf, text, fontSize, false);
	}
	
	public static void addHeader(Document pdf, String text, int fontSize, boolean centered) throws DocumentException{
		Paragraph header = new Paragraph(text);
		header.setFont(new Font(Font.TIMES_ROMAN, fontSize, Font.BOLD));
		if (centered){
			header.setAlignment(Paragraph.ALIGN_CENTER);
		} else {
			header.setAlignment(Paragraph.ALIGN_LEFT);
		}
		pdf.add(header);
		pdf.add(new Paragraph(" "));
	}
	
	private static void addSummary(Document pdf, List<Object[]> summary) throws DocumentException{
		addHeader(pdf, "Overall Response Time Summary", 12);
		
		PdfPTable summaryTable = new PdfPTable(new float[]{0.4f, 0.2f, 0.2f, 0.2f});
		summaryTable.addCell(getBorderlessHeadingCell(""));
		summaryTable.addCell(getBorderlessHeadingCell("Avg (ms)"));
		summaryTable.addCell(getBorderlessHeadingCell("Min (ms)"));
		summaryTable.addCell(getBorderlessHeadingCell("Max (ms)"));
		
		populateBorderlessTable(summary, summaryTable);
		pdf.add(summaryTable);
		pdf.add(new Paragraph(" "));
		pdf.add(new Paragraph(" "));
		
	}
	
	private static void addUbResults(Document pdf, List<Object[]> ubStats) throws DocumentException{
		addHeader(pdf, "Response Time by Region", 12);
		
		PdfPTable table = new PdfPTable(new float[]{0.25f, 0.25f, 0.25f, 0.25f});
		table.addCell(getHeadingCell("Userbase"));
		table.addCell(getHeadingCell("Avg (ms)"));
		table.addCell(getHeadingCell("Min (ms)"));
		table.addCell(getHeadingCell("Max (ms)"));
		
		populateTable(ubStats, table);
		pdf.add(table);
		pdf.add(new Paragraph(" "));
	}
	
	private static void addDcProcessingStats(Document pdf, List<Object[]> dcStats) throws DocumentException{
		addHeader(pdf, "Data Center Request Servicing Times", 12);
		
		PdfPTable table = new PdfPTable(new float[]{0.25f, 0.25f, 0.25f, 0.25f});
		table.addCell(getHeadingCell("Data Center"));
		table.addCell(getHeadingCell("Avg (ms)"));
		table.addCell(getHeadingCell("Min (ms)"));
		table.addCell(getHeadingCell("Max (ms)"));
		
		populateTable(dcStats, table);
		pdf.add(table);
		pdf.add(new Paragraph(" "));
	}

	private static void addCostSummary(Document pdf, List<Object[]> summary) throws DocumentException{
		addHeader(pdf, "Cost", 12);
		
		PdfPTable costSummaryTable = new PdfPTable(new float[]{0.4f, 0.4f});		
		populateBorderlessTable(summary, costSummaryTable);
		pdf.add(costSummaryTable);
		pdf.add(new Paragraph(" "));
	}
	
	private static void addCostDetails(Document pdf, List<Object[]> costs) throws DocumentException{
		
		PdfPTable table = new PdfPTable(new float[]{0.25f, 0.25f, 0.25f, 0.25f});
		table.addCell("Data Center");
		table.addCell(getHeadingCell("VM Cost $"));
		table.addCell(getHeadingCell("Data Transfer Cost $"));
		table.addCell(getHeadingCell("Total $"));
		
		populateTable(costs, table);
		pdf.add(table);
		pdf.add(new Paragraph(" "));
	}
	
	private static void addGraphs(Document pdf, Map<String, BufferedImage> graphs, String title) throws DocumentException, IOException{
		addHeader(pdf, title, 12);
		
		List<String> graphNames = new LinkedList<String>();
		graphNames.addAll(graphs.keySet());
		Collections.sort(graphNames);	
		
		for (String name : graphNames){
			pdf.add(new Paragraph(name));		
			pdf.add(Image.getInstance(graphs.get(name), null));
		}
		pdf.add(new Paragraph(" "));
	}
	
	private static void populateTable(List<Object[]> data, PdfPTable table) {
		for (Object[] row : data){
			for (int i = 0; i < row.length; i++){
				String item = (row[i] instanceof Number) ? df.format(row[i]) : (String) row[i];
				table.addCell(getCell(item));
			}
		}
	}
	
	private static void populateBorderlessTable(List<Object[]> data, PdfPTable table) {
		for (Object[] row : data){
			for (int i = 0; i < row.length; i++){
				String item = (row[i] instanceof Number) ? df.format(row[i]) : (String) row[i];
				table.addCell(getBorderlessCell(item));
			}
		}
	}
	
	private static PdfPCell getHeadingCell(String str){		
		Phrase phrase = new Phrase(str, new Font(Font.TIMES_ROMAN, 12, Font.BOLD));
		PdfPCell cell = new PdfPCell(phrase);
//		cell.setColspan(2);
		cell.setPadding(5.0f);
		
		return cell;
	}
	
	private static PdfPCell getBorderlessHeadingCell(String str){		
		Phrase phrase = new Phrase(str, new Font(Font.TIMES_ROMAN, 12, Font.BOLD));
		PdfPCell cell = new PdfPCell(phrase);
		cell.setBorder(PdfPCell.NO_BORDER);
		cell.setPadding(5.0f);
		
		return cell;
	}
	
	private static PdfPCell getCell(String str)
	{
		Phrase phrase = new Phrase(str, new Font(Font.TIMES_ROMAN, 10, Font.NORMAL));
		PdfPCell cell = new PdfPCell(phrase);
		
		cell.setPadding(5.0f);
		
		return cell;
	 }
	
	private static PdfPCell getBorderlessCell(String str)
	{
		Phrase phrase = new Phrase(str, new Font(Font.TIMES_ROMAN, 10, Font.NORMAL));
		PdfPCell cell = new PdfPCell(phrase);
		
		cell.setBorder(PdfPCell.NO_BORDER);
		cell.setPadding(5.0f);
		
		return cell;
	 }
	
//	public static void main(String[] args){
//		PdfExporter app = new PdfExporter();
//		try {
//			app.saveToPdf(new File("test.pdf"), null);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DocumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
