package fiu.kdrg.crawler;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class WikiController {
	
	
	
	 public static void main(String[] args) throws Exception {

 		 String path = System.getProperty("user.dir") + "/data";
 		 String storageFrontierFolder = path;
         int numberOfCrawlers = 10;
         List<String> seeds = new ArrayList<String>();
         seeds.add("http://en.wikipedia.org/wiki/Hurricane_Katrina");
//         seeds.add("http://en.wikipedia.org/wiki/Hurricane_Sandy");
         int depth = 2;
         
         for(int i = 0; i < seeds.size(); i++)
         {
        	 String seed = seeds.get(i);
             String crawlStorageFolder = path + "/rawData2/" + seed.substring(seed.lastIndexOf("/"));
             WikiController controller1 = new WikiController();
             controller1.startCrawling(storageFrontierFolder, crawlStorageFolder, numberOfCrawlers, seeds.subList(i, i+1),depth); 
         } 
 }
	
	
	
	
	/**
	 * 
	 * @param rootFolder: it will contain intermediate crawl data
	 * @param storageFolder: a folder for storing downloaded files
	 * @param numOfCrawler: number of concurrent threads
	 */
	public void startCrawling(String rootFolder, String storageFolder, 
									 int numberOfCrawlers, List<String> seeds, int depth) throws Exception
	{
		CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(rootFolder);
        config.setMaxDepthOfCrawling(depth);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        for(String seed : seeds)
        	controller.addSeed(seed);

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        WikiCrawler.configure(storageFolder);
        controller.start(WikiCrawler.class, numberOfCrawlers);    
	}
	
	
}
