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

import us.monoid.web.Resty;

/**
 * @author n689716
 *
 */
public abstract class AbstractScrapper {

	/**
	 * Trust SSLs
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static void disableSSLCertCheck() throws NoSuchAlgorithmException, KeyManagementException {
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
	
	/**
	 * Sync Download
	 * @param imageUrl
	 * @throws IOException
	 */
	public static void saveImage(String imageUrl, String folderName) throws IOException {
		URL url = new URL(imageUrl);
		String fileName = url.getFile();
		String destName = "./images/" + folderName + fileName.substring(fileName.lastIndexOf("/"));
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
	public static void bulkSaveImage(List<String> urls, String folderName) {
		try {
			ExecutorService pool = Executors.newFixedThreadPool(urls.size());
			List<Callable<File>> tasks = new ArrayList<Callable<File>>(urls.size());

			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("Total Images to Download ---------------- > "  + urls.size());
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
			
			for (final String imageUrl : urls) {
				URL url = new URL(imageUrl);
				String fileName = url.getFile();
				String destName = "./images/" + folderName + fileName.substring(fileName.lastIndexOf("/"));

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
}
