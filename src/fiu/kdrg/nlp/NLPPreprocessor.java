package fiu.kdrg.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NLPPreprocessor {

	
	/**
	 * preprocess this file, remove line whose length shorter than minLength and characters 
	 * representing special character in html file
	 * @param path:	path of file that we gonna preprocess
	 * @param minLength: get rid of line whose length is less than minLength
	 * @return the string get after preprocess
	 */
	public String preprocess(String path, int minLength)
	{
		File file = new File(path);
		if(!file.exists())
		{
			System.out.println("file not exist");
			return null;			
		}
		
		StringBuffer sb = new StringBuffer();
		String line = "";
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			while((line = br.readLine()) != null)
			{
				line = removeSpeicalHtmlCharacter(line);
				if(line.length() >= minLength)//remove reference line
					sb.append(line).append("\t\n");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb.toString();

	}
	
	
	
	
	
	/**
	 * there are some very special character in HTML file, for &#160; represents space
	 * there are also reference like [45]. This function is to remove these contents
	 * @param line
	 * @return return line after process
	 */
	public String removeSpeicalHtmlCharacter(String line)
	{
		if(line.startsWith("^"))
			return "";
		
		//This pattern can match space character in html and reference
		//change tab to space
		
		String strPattern = "(&#[0-9]{2,3};)|(\\[[0-9]{1,3}\\])|([\\s]+)";
		Pattern p = Pattern.compile(strPattern);
		Matcher match = p.matcher(line);
		match.find();
		
		line = match.replaceAll(" ");
		return line;
	}
	
	
	public String preprocessFile(String file, int minLengthOfLine)
	{
		return this.preprocess(file, minLengthOfLine);
	}
	
	
	public String preprocessDirectory(String directory, int minLengthOfLine)
	{
		File dir = new File(directory);
		StringBuffer sb = new StringBuffer();
		
		if(!dir.isDirectory())
			return null;
		
		String[] files = dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return (new File(dir.getAbsoluteFile() + "/" + name)).isFile();//only accept file
			}
		});
		
		
		for(int i = 0; i < files.length; i++)
		{
			String tmpStr = preprocessFile(directory + "/" + files[i], minLengthOfLine);
			sb.append(tmpStr);
		}
		
		return sb.toString();
		
	}
	
	
}
