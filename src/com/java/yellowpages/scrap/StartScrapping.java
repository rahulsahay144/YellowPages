/**
 * 
 */
package com.java.yellowpages.scrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import us.monoid.web.Resty;


/**
 * @author n689716
 *
 */
public class StartScrapping {
	
	
	private static final int START_PAGE = 11;
	private static final int TOTAL_PAGES = 13;
	private static final String FOLDER_NAME = "anime";

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
			Document doc = Jsoup.connect("https://wallpapersite.com/" + FOLDER_NAME + "/?page="+i).get();
			System.out.println("==================================================================");
			System.out.println(doc.title());
			System.out.println("==================================================================");
			Elements newsHeadlines = doc.select(".pics p a");
			
			//Map<String, String> advocateLinkMap = new HashMap<String, String>();
			for (Element headline : newsHeadlines) {
				//System.out.println(headline.html() + " : " + headline.absUrl("href"));
				//advocateLinkMap.put(headline.html(), headline.absUrl("href"));
				
//				if(headline.html().contains(searchString)) {
					//System.out.println("Got this image link ---> " + headline.absUrl("href"));
					Document newDoc = Jsoup.connect(headline.absUrl("href")).get();
					Elements rowElements = newDoc.select(".pic-left .res-ttl");
					for (Element row : rowElements) {
						Elements e = row.select("span");
						String resolution = e.text();
						if(resolution != null && resolution.indexOf("Original Resolution:") != -1) {
							Elements originalAnchors = e.select("a");
							for (Element a : originalAnchors) {
								System.out.println("Resolution : " + resolution + " ========= Image Link : " +  a.absUrl("href"));
								
								imageURLs.add(a.absUrl("href"));
								//saveImage(a.absUrl("href"));
								//Thread.sleep(2000); 
							}
						}
						//System.out.println(e.text());
					}
//				}
			}
		}
		
		bulkSaveImage(imageURLs);
	}
	
	/**
	 * Sync Download
	 * @param imageUrl
	 * @throws IOException
	 */
	private static void saveImage(String imageUrl) throws IOException {
		URL url = new URL(imageUrl);
		String fileName = url.getFile();
		String destName = "./images/" + FOLDER_NAME + fileName.substring(fileName.lastIndexOf("/"));
		System.out.println(destName);
	 
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(destName);
	 
		byte[] b = new byte[2048];
		int length;
	 
		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}
	 
		is.close();
		os.close();
	}
	
	/**
	 * Bulk Download
	 * @param urls
	 */
	private static void bulkSaveImage(List<String> urls) {
		try {
			ExecutorService pool = Executors.newFixedThreadPool(urls.size());
			List<Callable<File>> tasks = new ArrayList<Callable<File>>(urls.size());

			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("Total Images to Download ---------------- > "  + urls.size());
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
			
			for (final String imageUrl : urls) {
				URL url = new URL(imageUrl);
				String fileName = url.getFile();
				String destName = "./images/" + FOLDER_NAME + fileName.substring(fileName.lastIndexOf("/"));

				tasks.add(new Callable<File>() {
					public File call() throws Exception {
						File f = new File(destName);
						return new Resty().bytes(imageUrl).save(f);
					}
				});
			}

			System.out.println("DOWNLOAD START.......... Please wait...");
			
			List<Future<File>> results = pool.invokeAll(tasks);
			int i = 1;
			for (Future<File> ff : results) {
				System.out.println(">>>>>>>>>>>>>>>> " + i + " -:- " + ff.get());
				i++;
			}
			System.out.println("DOWNLOAD END..........");
			
			//ShutDown
			pool.shutdown();
		} 
		catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * Trust SSLs
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	private static void disableSSLCertCheck() throws NoSuchAlgorithmException, KeyManagementException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

}
