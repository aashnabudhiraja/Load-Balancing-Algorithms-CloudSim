package cloudsim.ext.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Miscellaneous util methods.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class MiscUtil {
		
	public static Object deepCopy(Object src){
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos); 
			oos.writeObject(src); 
			oos.flush();
			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray()); 
			ois = new ObjectInputStream(bin); 
			
			Object copy = ois.readObject();
			return  copy;
		} catch (Exception e) {
			System.out.println("An error occured in MiscUtil.deepCopy(...)");
			e.printStackTrace();
//			return src;
			return null;
		} finally {
			try {
				oos.close();
				ois.close();
			} catch (IOException e){
				
			}
		}	
	}

}
