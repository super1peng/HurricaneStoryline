package fiu.kdrg.storyline.event;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializeFactory {

	public static  void serialize(String outFile, Object object) throws IOException{
		
		FileOutputStream fos = new FileOutputStream(outFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(object);
		oos.close();
	}
	
	
	
	public static Object deSerialize(String inFile) throws IOException, ClassNotFoundException{
		
		FileInputStream fis = new FileInputStream(inFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		return ois.readObject();
	}
	
}
