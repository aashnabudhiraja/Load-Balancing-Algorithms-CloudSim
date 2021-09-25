package cloudsim.ext.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A utility class providing methods for serializing objects as XML and deserializing
 * XML in to objects.
 *
 * @author Bhathiya Wickremasinghe
 *
 */
public class IOUtil {

	public static void saveAsXML(Object obj, File outputFile) throws IOException{
		saveAsXML(obj, outputFile.getAbsolutePath());
	}

	public static void saveAsXML(Object obj, String outputFile) throws IOException{
		FileOutputStream fos = new FileOutputStream(outputFile);
		XMLEncoder enc = new XMLEncoder(fos);

		enc.writeObject(obj);

		enc.close();
		fos.close();
	}

	public static Object loadFromXml(File inputFile) throws IOException{
		return loadFromXml(inputFile.getAbsolutePath());
	}

	public static Object loadFromXml(String inputFile) throws IOException{
		FileInputStream fis = new FileInputStream(inputFile);
		XMLDecoder xdec = new XMLDecoder(fis);

		Object obj = xdec.readObject();

		xdec.close();
		fis.close();

		return obj;
	}

	public static Object loadFromXml(InputStream inputFile) throws IOException{
		XMLDecoder xdec = new XMLDecoder(inputFile);

		Object obj = xdec.readObject();

		xdec.close();
		inputFile.close();

		return obj;
	}
}
