/**
 * 
 */
package com.java.yellowpages.scrap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

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


/**
 * @author n689716
 *
 */
public class StartScrapping {
	
	
	private static final int TOTAL_PAGES = 141;

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
		
		
		
		for(int i=7; i <= TOTAL_PAGES; i++) {
			Document doc = Jsoup.connect("https://wallpapersite.com/automotive/?page="+i).get();
			System.out.println(doc.title());
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
								Thread.sleep(1000); 
								saveImage(a.absUrl("href"));
							}
						}
						//System.out.println(e.text());
					}
//				}
			}
		}
	}
	
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
	
	public static void saveImage(String imageUrl) throws IOException {
		URL url = new URL(imageUrl);
		String fileName = url.getFile();
		String destName = "./images" + fileName.substring(fileName.lastIndexOf("/"));
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

}
