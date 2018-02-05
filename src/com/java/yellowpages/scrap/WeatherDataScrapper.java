/**
 * 
 */
package com.java.yellowpages.scrap;

import java.io.File;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
public class WeatherDataScrapper {
	
	static String WEATHER_URL = "https://www.wunderground.com/history/airport/KDPA/{{YEAR}}/{{MONTH}}/{{DAY}}/DailyHistory.html?req_city=&req_state=&req_statename=&reqdb.zip=&reqdb.magic=&reqdb.wmo=";

	static final String STARTING_DATE = "01/01/2015";
	static final String END_DATE      = "12/31/2017";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		disableSSLCertCheck();
		
		boolean needRun = true;
		
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		Date endDate = format.parse(END_DATE);
		
		int i = 0;
		
		 //DATE, MEAN, MAX, MIN, EVENTS
		
		PrintWriter pw = new PrintWriter(new File("/Users/n689716/Documents/workspace/YellowPages/weather.csv"));
        StringBuilder sb = new StringBuilder();
        sb.append("DATE");
        sb.append(',');
        sb.append("MEAN");
        sb.append(',');
        sb.append("MAX");
        sb.append(',');
        sb.append("MIN");
        sb.append(',');
        sb.append("EVENTS");
        sb.append('\n');
        
        System.out.println("DATE" + ", " + "MEAN" + ", " + "MAX" + ", " + "MIN" + ", " + "EVENTS");
		
		while(needRun) {
			String originalURL = WEATHER_URL;
			
			SimpleDateFormat format1 = new SimpleDateFormat("MM/dd/yyyy");
			Date runningDate = format1.parse(STARTING_DATE);
			runningDate = DateUtil.addDays(runningDate, i);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(runningDate);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DAY_OF_MONTH);
			
			originalURL = originalURL.replace("{{YEAR}}", String.valueOf(year));
			originalURL = originalURL.replace("{{MONTH}}", String.valueOf(month));
			originalURL = originalURL.replace("{{DAY}}", String.valueOf(day));
			
			Document doc = Jsoup.connect(originalURL).get();
//			System.out.println("==================================================================");
//			System.out.println(doc.title());
//			System.out.println("==================================================================");
			
			//DATE, MEAN, MAX, MIN, EVENTS
			
			Elements weatherRows = doc.select(".airport-history-summary-table tr");
			
			String meanTemp = null;
			for(Element row : weatherRows) {
				Elements meanElemColumns = row.select("td");
				if(!meanElemColumns.isEmpty() && meanElemColumns.get(0).text().equalsIgnoreCase("Mean Temperature")) {
					meanTemp = meanElemColumns.get(1).text();
					break;
				}
			}
			
//			Element meanElem = weatherRows.get(2);
//			Elements meanElemColumns = meanElem.select("td");
//			String meanTemp = meanElemColumns.get(1).text();
			
//			Element maxElem = weatherRows.get(3);
//			Elements maxElemColumns = maxElem.select("td");
//			String maxTemp = maxElemColumns.get(1).text();
			
			String maxTemp = null;
			for(Element row : weatherRows) {
				Elements meanElemColumns = row.select("td");
				if(!meanElemColumns.isEmpty() && meanElemColumns.get(0).text().equalsIgnoreCase("Max Temperature")) {
					maxTemp = meanElemColumns.get(1).text();
					break;
				}
			}
			
//			Element minElem = weatherRows.get(4);
//			Elements minElemColumns = minElem.select("td");
//			String minTemp = minElemColumns.get(1).text();
			
			String minTemp = null;
			for(Element row : weatherRows) {
				Elements meanElemColumns = row.select("td");
				if(!meanElemColumns.isEmpty() && meanElemColumns.get(0).text().equalsIgnoreCase("Min Temperature")) {
					minTemp = meanElemColumns.get(1).text();
					break;
				}
			}
			
//			Element eventsElem = weatherRows.get(28);
//			Elements eventsElemColumns = eventsElem.select("td");
//			String event = eventsElemColumns.get(1).text();
//			event = event.replaceAll(",", "");
			
			String event = null;
			for(Element row : weatherRows) {
				Elements meanElemColumns = row.select("td");
				if(!meanElemColumns.isEmpty() && meanElemColumns.get(0).text().equalsIgnoreCase("Events")) {
					event = meanElemColumns.get(1).text();
					event = event.replaceAll(",", "");
					break;
				}
			}
			
			
			System.out.println(new SimpleDateFormat("MM/dd/yyyy").format(runningDate) + ", " + meanTemp + ", " + maxTemp + ", " + minTemp + ", " + event);
			
			sb.append(runningDate);
	        sb.append(',');
	        sb.append(meanTemp);
	        sb.append(',');
	        sb.append(maxTemp);
	        sb.append(',');
	        sb.append(minTemp);
	        sb.append(',');
	        sb.append(event);
	        sb.append('\n');
			
			
			if(endDate.compareTo(runningDate) == 0) {
				needRun = false;
			}
			
			i++;
		}
		
		pw.write(sb.toString());
        pw.close();
		

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
