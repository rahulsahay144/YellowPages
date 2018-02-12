/**
 * 
 */
package com.java.yellowpages.scrap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * @author n689716
 *
 */
public class StartScrapping extends AbstractScrapper {
	
	private static final int START_PAGE = 1;
	private static final String PROCESS_FOLDER = "colors";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		disableSSLCertCheck();
		
		Map<String, String> folderURLNameMap = new LinkedHashMap<String, String>();
		Document docMain = Jsoup.connect("http://eskipaper.com").get();
		System.out.println("==================================================================");
		System.out.println(docMain.title());
		System.out.println("==================================================================");
		
		Elements folderListItems = docMain.select(".toplevel ul li");
		for (Element folderElem : folderListItems) {
			String folderURL = folderElem.select("a").get(0).absUrl("href");
			String folderName = folderElem.select("a").get(0).text();
			
			folderURLNameMap.put(folderName, folderURL);
		}
		
		for (Map.Entry<String, String> entry : folderURLNameMap.entrySet()) {
		    String folderName = entry.getKey();
		    String folderURL = entry.getValue();
		    
		    if(!PROCESS_FOLDER.equals("") && !PROCESS_FOLDER.equalsIgnoreCase(folderName)) {
		    	continue;
		    }
		    
		    System.out.println("==================================================================");
		    System.out.println("Processing Folder - " + folderName + " : START");
			System.out.println("==================================================================");
		    
		    File file = new File("./images/" + folderName);
	        if (!file.exists()) {
	            if (file.mkdir()) {
	                System.out.println("Directory is created!");
	            } else {
	                System.out.println("Failed to create directory!");
	            }
	        }
	        
	        try {
		        int TOTAL_PAGES = 0;
		        Document parentDoc = Jsoup.connect(folderURL).get();
		        Elements paginationElems = parentDoc.select(".pagination2 li");
		        for (Element p : paginationElems) {
		        	if(p.select("a").size() == 0)
		        		continue;
		        	
		        	Element aTag = p.select("a").get(0);
		        	String t = aTag.select("span").get(0).text();
		        	if(t.equalsIgnoreCase("End")) {
		        		String link = aTag.absUrl("href");
		        		TOTAL_PAGES = Integer.valueOf(link.substring(folderURL.length(), link.length() - 1));
		        		break;
		        	}
		        }
		        
		        List<String> imageURLs = new ArrayList<String>();
				for(int i=START_PAGE; i <= TOTAL_PAGES; i++) {
					Document doc = Jsoup.connect(folderURL + i + "/").get();
					
					System.out.println("==================================================================");
					System.out.println(doc.title() + " Page - " + i);
					System.out.println("==================================================================");
					Elements newsHeadlines = doc.select(".category-list-item");
					
					for (Element headline : newsHeadlines) {
							Elements linkTagElem = headline.select("a");
							Document newDoc = Jsoup.connect(linkTagElem.get(0).absUrl("href")).get();
							Elements rowElements = newDoc.select(".imgdata .download-img");
							
							String resolution = rowElements.get(0).text();
							String url = rowElements.get(0).absUrl("href");
							
							System.out.println("Resolution : " + resolution + " ========= Image Link : " +  url);
							imageURLs.add(url);
					}
				}
				
				bulkSaveImage(imageURLs, folderName);
				
				System.out.println("==================================================================");
				System.out.println("Processing Folder - " + folderName + " : END");
				System.out.println("==================================================================");
	        }
	        catch(Exception e) {
	        	System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	        	System.out.println("Error while processing Folder : " + folderName);
	        	System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	        	e.printStackTrace();
	        }
		    
		}
	}
	
}
