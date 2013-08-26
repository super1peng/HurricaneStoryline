package fiu.kdrg.crawler;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import fiu.kdrg.util.IOUtil;
import org.htmlparser.beans.StringBean;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.DefaultExtractor;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.TextParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class WikiCrawler  extends WebCrawler{

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
												            + "|png|tiff?|mid|mp2|mp3|mp4"
								 				            + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
												            + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	
	private Pattern anchorPattern = Pattern.compile("(?i)katrina");
	private static File storageFolder;
	private DefaultExtractor extractor = DefaultExtractor.getInstance();
	
	public static void configure(String storageFolderName) {
		storageFolder = new File(storageFolderName);
		if (!storageFolder.exists()) {
			storageFolder.mkdirs();
		}
	}

	
	
	/**
	 * you should implement this to specify whether the given 
	 * url should be crawled or not.
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		if(url.getAnchor() == null)
			return false;
		else
		{
			boolean flag = !FILTERS.matcher(href).matches() && anchorPattern.matcher(url.getAnchor()).find();
//			if(flag)
//			{
//				System.out.println("URL Anchor: " + url.getAnchor());
//				System.out.println("URL: " + url.getURL());
//			}
			return flag;
		}
	}
	
	

//    /**
//     * This function is called when a page is fetched and ready 
//     * to be processed by your program.
//     */
//    @Override
//    public void visit(Page page) {          
//            WebURL webURL = page.getWebURL();
//            boolean flag = true;
//
//            if (page.getParseData() instanceof HtmlParseData) {
//                    HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
//
//                    String html = htmlParseData.getHtml();
//                    List<WebURL> links = htmlParseData.getOutgoingUrls();                    
//
//                    System.out.println("Number of outgoing links: " + links.size());
//                    
//                    // get a unique name for storing this html
//            		String extension = ".html";
//            		String hashedName = Cryptography.MD5(webURL.getURL()) + extension;
//            		
//            		// store html
//            		flag = IOUtil.writeStringToFile(html, 
//            				storageFolder.getAbsolutePath() + "/" + hashedName, page.getContentCharset());
//            		
//            		if(flag){
//                        System.out.println("URL: " + webURL.getURL());
//                        System.out.println("Anchor: " + webURL.getAnchor());
//                		System.out.println("Stored: " + webURL.getURL());
//            		}
//                    
//            }else if(page.getParseData() instanceof TextParseData){
//            	TextParseData textParseDate = (TextParseData) page.getParseData();
//            	String text = textParseDate.getTextContent();
//            	
//                // get a unique name for storing this text
//        		String extension = ".txt";
//        		String hashedName = Cryptography.MD5(webURL.getURL()) + extension;
//        		// store text
//        		flag = IOUtil.writeStringToFile(text, 
//        				storageFolder.getAbsolutePath() + "/" + hashedName, page.getContentCharset());
//        		
//        		if(flag){
//                    System.out.println("URL: " + webURL.getURL());
//            		System.out.println("Stored: " + webURL.getURL());
//        		}
//            }
//    }
//    
    
    
    /**
     * This function is called when a page is fetched and ready 
     * to be processed by your program.
     */
      @Override
      public void visit(Page page) {
   	// TODO Auto-generated method stub
   	   
        WebURL webURL = page.getWebURL();
        
        // get a unique name for storing this html
        String extension = ".html";
        String hashedName = Cryptography.MD5(webURL.getURL()) + extension;
        		

        String mainContent = "";
//      String mainContent = extractMainContent(webURL.getURL());
        
		try {
			mainContent = extractor.getText(new URL(webURL.getURL()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        
        // store mainContent whose length no less than 1000
		if(mainContent.length() >= 1000){
			 boolean flag = IOUtil.writeStringToFile(mainContent, 
       		 			storageFolder.getAbsolutePath() + "/" + hashedName, page.getContentCharset());
        
        if(flag){
                  System.out.println("URL: " + webURL.getURL());
                  System.out.println("Anchor: " + webURL.getAnchor());
                  System.out.println("Stored: " + webURL.getURL());
        		}
		}
       
      } 
      
      
      
      /**
       * extract main content from a url  
       * @param url
       * @return main content(plain text of this url)
       */
        private String extractMainContent(String url)
        {
     	   StringBean sb = new StringBean();
     	    
     	    //设置�?需�?得到页�?�所包�?�的链接信�?�  
     	    sb.setLinks(false);  
     	    //设置将�?间断空格由正规空格所替代  
     	    sb.setReplaceNonBreakingSpaces(true);  
     	    //设置将一�?列空格由一个�?�一空格所代替  
     	    sb.setCollapse(true);  
     	    //传入�?解�?的URL  
     	    sb.setURL(url);  
     	    //返回解�?�?�的网页纯文本信�?�  
     	    return sb.getStrings();  
        }
      
        
       public static List<WebURL> geWebURLHasAnchor(List<WebURL> urls)
        	{
        		List<WebURL> anchorURLs = new ArrayList<WebURL>();
        		
        		for(WebURL url : urls)
        		{
        			if(url.getAnchor() != null)
        				anchorURLs.add(url);
        		}
        		
        		
        		return anchorURLs;
        	}
      

	
}
