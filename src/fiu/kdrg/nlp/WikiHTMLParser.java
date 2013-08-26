package fiu.kdrg.nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * get content of Wikipedia page specified by URL
 * after this, we may process to get some special content from it.
 * @author zhouwubai
 *
 */
public class WikiHTMLParser {
	
	public static void main(String[] args) throws IOException, ParserException {
		
		WikiHTMLParser wiki = new WikiHTMLParser();
		String url = "http://en.wikipedia.org/wiki/Jackie_chan";
		String path = System.getProperty("user.dir");
		String filename1 = path + "/" + url.substring(url.lastIndexOf("/")+1) + ".html";
		String filename2 = path + "/" + url.substring(url.lastIndexOf("/")+1) + ".txt";
		System.out.println(path);
		System.out.println(filename1);
		System.out.println(filename2);
//		
		wiki.extractOriginalContent2HtmlFile(url, path);
//        wiki.extractMainContent2File(filename1, filename2);
		
	}
	
	
	/**
	 * get content from url and write it into file, filename will be last several character of url
	 * @param strUrl
	 * @param path
	 * @return
	 * @throws IOException 
	 */
	public void extractOriginalContent2HtmlFile(String strUrl, String path) throws IOException
	{
		try {
			
			URL url = new URL(strUrl);
			URLConnection uc = url.openConnection();
			String contentType = uc.getContentType();
			
			String charset = getCharset(contentType);
//			System.out.println(charset);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), charset));
			
			String s = "";
			StringBuffer sb = new StringBuffer();
			while((s = br.readLine()) != null)
			{
				sb.append(s + "\r\n");
			}
			br.close();
			
			String strContent = sb.toString();
			
			//generate filename
			String filename = strUrl.substring(strUrl.lastIndexOf("/")) + ".html";
			//write it into file
			if(!path.endsWith("/"))
				path = path + "/" + filename;
			else
				path = path + filename;
			
			System.out.println(strContent);
			File file = new File(path);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
			bw.write(strContent);
			bw.flush();
			bw.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	
	
	
	/**
	 * Extract main content from resource into file filename, resource can be URL or file name. 
	 * Main content means only title and paragraph was extracted from wikipedia page 
	 * @param resource
	 * @param fileName
	 * @throws ParserException
	 * @throws IOException
	 */
	public void extractMainContent2File(String resource, String fileName) throws ParserException, IOException
	{
		
		//get charset first
		String contentType = getContentType(resource);
		String charset = getCharset(contentType);
//		System.out.println(charset);
		
		//start to extract main content 
		Parser parser = new Parser(resource);
		parser.setEncoding(charset);
		StringBuffer sb = new StringBuffer();

		//define filter to extract title of that article, that's h1
		NodeFilter filter4Title = new HasAttributeFilter("id", "firstHeading");
		
		//filter for extracting main content
		NodeFilter filter4_p = new TagNameFilter("p");
		NodeFilter filter4_h2 = new TagNameFilter("h2");
		NodeFilter filter4_h3 = new TagNameFilter("h3");
		NodeFilter[] filter4Tag = {filter4_p, filter4_h2, filter4_h3};
        
		NodeFilter filter4Content = new AndFilter(new HasParentFilter(new HasAttributeFilter("id", "mw-content-text")),  // find their parents
				                                 new OrFilter(filter4Tag));                                              // find tags p, h2, h3	
		
		//get title and main content
		NodeList nodes = parser.extractAllNodesThatMatch(new OrFilter(filter4Title,filter4Content));

		for(int i = 0; i < nodes.size(); i++)
		{
			
            if(!(nodes.elementAt(i) instanceof Tag))
            	continue;
            else
            {
            	Node node = nodes.elementAt(i);
    			sb.append(node.toPlainTextString().trim() + "\t\n");
            }
			
		}
		
		//write into file
		File file = new File(fileName);

		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), charset);
		osw.append(sb);
		osw.flush();
		osw.close();

		return;		
	}
	
	
	/**
	 * return default charset UTF-8 when not find charset, return charset type when find it
	 * @param contentType
	 * @return
	 */
	public String getCharset(String contentType)
	{
		//default charset
		String charSet = "UTF-8";
		final String CHARSET = "charset";
		int index;
		
		if(contentType.equals(null) || contentType.equals(""))
			return charSet;
			
		index = contentType.indexOf(CHARSET);
		
		if(index == -1)
		    return charSet;
		
		contentType = contentType.substring(index + CHARSET.length()).trim();
		if(contentType.startsWith("="))
		{
			contentType = contentType.substring(1).trim();
			index = contentType.indexOf(";");
			
			if(index != -1)
			{
				contentType = contentType.substring(0,index);
			}
			
			if(-1 != contentType.indexOf("'") && -1 != contentType.lastIndexOf("'") && 1 < contentType.length())
				contentType = contentType.substring(contentType.indexOf("'") + 1, contentType.lastIndexOf("'") );
			
			if(-1 != contentType.indexOf("\"") && -1 != contentType.lastIndexOf("\"") && 1 < contentType.length())
				contentType = contentType.substring(contentType.indexOf("\"") + 1, contentType.lastIndexOf("\""));
			
		}
		
		charSet = contentType;
		return charSet;
		
	}
	
	
	private String getContentType(String pathName)
	{
		String contentType = "";
		File f = new File(pathName);
		
		//find contentType in file
		if(f.exists())
		{
			try {
				Parser parser;
				parser = new Parser(pathName);
				parser.setEncoding("UTF-8");
				NodeFilter charsetFilter = new AndFilter(new TagNameFilter("meta"), new HasAttributeFilter("charset"));
				NodeList nodes = parser.parse(charsetFilter);
				
				for(int i = 0; i < nodes.size(); i++)
				{
		            	Node node = nodes.elementAt(i);
		    			contentType += node.toHtml();
				}
				
				
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else  //findContentType via internet
			if(pathName.startsWith("http"))
			{
				try {
					
					URL url = new URL(pathName);
					URLConnection uc = url.openConnection();
					contentType = uc.getContentType();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		
		return contentType;
	}
}