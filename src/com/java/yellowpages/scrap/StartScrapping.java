/**
 * 
 */
package com.java.yellowpages.scrap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
	private static final int TOTAL_PAGES = 2;
	private static final String FOLDER_NAME = "3d";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		String searchString;  
//
//		System.out.println("Enter Search Name : ");
//		searchString = br.readLine();
		
		disableSSLCertCheck();
		
		File file = new File("./images/" + FOLDER_NAME);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
		
		
        List<String> imageURLs = new ArrayList<String>();
        
		for(int i=START_PAGE; i <= TOTAL_PAGES; i++) {
			Document doc = Jsoup.connect("http://eskipaper.com/" + FOLDER_NAME + "/" + i + "/").get();
			System.out.println("==================================================================");
			System.out.println(doc.title());
			System.out.println("==================================================================");
			Elements newsHeadlines = doc.select(".category-list-item");
			
			//Map<String, String> advocateLinkMap = new HashMap<String, String>();
			for (Element headline : newsHeadlines) {
				//System.out.println(headline.html() + " : " + headline.absUrl("href"));
				//advocateLinkMap.put(headline.html(), headline.absUrl("href"));
				
//				if(headline.html().contains(searchString)) {
					//System.out.println("Got this image link ---> " + headline.absUrl("href"));
				
					Elements linkTagElem = headline.select("a");
					Document newDoc = Jsoup.connect(linkTagElem.get(0).absUrl("href")).get();
					Elements rowElements = newDoc.select(".imgdata .download-img");
					
					String resolution = rowElements.get(0).text();
					String url = rowElements.get(0).absUrl("href");
					
					System.out.println("Resolution : " + resolution + " ========= Image Link : " +  url);
					imageURLs.add(url);
					
//					for (Element row : rowElements) {
//						Elements e = row.select("span");
//						String resolution = e.text();
//						if(resolution != null && resolution.indexOf("Original Resolution:") != -1) {
//							Elements originalAnchors = e.select("a");
//							for (Element a : originalAnchors) {
//								System.out.println("Resolution : " + resolution + " ========= Image Link : " +  a.absUrl("href"));
//								
//								imageURLs.add(a.absUrl("href"));
//								//saveImage(a.absUrl("href"));
//							}
//						}
//						//System.out.println(e.text());
//					}
//				}
			}
		}
		
		bulkSaveImage(imageURLs, FOLDER_NAME);
	}
	
}
