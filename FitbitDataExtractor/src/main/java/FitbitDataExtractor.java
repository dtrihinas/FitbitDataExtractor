import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FitbitDataExtractor {
	
	private static final String USAGE = "java -jar FitbitDataExtractor.jar <email> <password> [<rememberMe>]";
	
	private static final String FITBIT_LOGIN_ENDPOINT = "https://www.fitbit.com/login";
	private static final String FITBIT_AJAX_ENDPOINT = "https://www.fitbit.com/ajaxapi";

	
	private CloseableHttpClient client;
	private String cookie;
	private String token;
	
	public FitbitDataExtractor() {
		this.client = HttpClientBuilder.create().build();
		this.cookie = null;
	}
	
	public void getAuthenticated(String email, String password) {
		this.getAuthenticated(email, password, "false");
	}
	
	public void getAuthenticated(String email, String password, String rememberMe) {
		HttpPost req = new HttpPost(FitbitDataExtractor.FITBIT_LOGIN_ENDPOINT);
	    req.addHeader("Host", "www.fitbit.com");
	    req.addHeader("Connection", "keep-alive");
	    req.addHeader("Cache-Control", "max-age=0");
	    req.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
	    req.addHeader("Origin", "https://www.fitbit.com");
	    req.addHeader("Content-Type", "application/x-www-form-urlencoded");
	    req.addHeader("Referer", "https://www.fitbit.com/login");
	    req.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
	    req.addHeader("Accept-Encoding", "gzip, deflate");
	    req.addHeader("Content-Language", "en-GB");
	    
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("email", email));
	    params.add(new BasicNameValuePair("password", password));
	    params.add(new BasicNameValuePair("rememberMe", rememberMe));
	    params.add(new BasicNameValuePair("login", "Log In"));
	 
	    try {
			req.setEntity(new UrlEncodedFormEntity(params));
			CloseableHttpResponse resp = client.execute(req);
		
			System.out.println("Authentication: " + resp.getStatusLine());
			
			this.cookie = null;
			for(Header h : this.getCookies(resp)) {
				//if (h.getName().equals("u")) {
				String[] biscuit = h.getValue().split("=");
				if (biscuit[0].equals("u"))
					//this.token = h.getValue().split("|")[2];
					this.token = biscuit[1].trim().split("\\|")[2];
				this.cookie = h.getValue() + ";";
			}			
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	//return null;
	    }
	}
	
	public void getIntraDayHeartandCalories(String day, boolean csv) {
	    try {
	    	
	    	String json = this.getIntraDayData(day, "heart-rate");
	    	if (json == null) {
	    		System.out.println("could not get the data you requested");
	    		return;
	    	}
	    	
    		System.out.println("getIntraDayHeartandCalories: Got Heart and Calorie Data");
    		System.out.println(json);

	    	if (csv) {
	    		BufferedWriter bw = new BufferedWriter(new FileWriter("heart-calories-" + day));

	    		JSONParser parser = new JSONParser();
	    		JSONArray arr = (JSONArray) parser.parse(json);
	    		JSONObject obj = ((JSONObject) arr.get(0));
	    		JSONObject datasets = (JSONObject) obj.get("dataSets");
	    		JSONObject activity = (JSONObject) datasets.get("activity");
	    		JSONArray datapoints = (JSONArray) activity.get("dataPoints");
	    		
        		System.out.println("Saving to csv file the date for: " + day);
        		
	    		Iterator<JSONObject> iterator = datapoints.iterator();
	    		while (iterator.hasNext()) {
	    			JSONObject point = iterator.next();
	    			String dateTime = (String) point.get("dateTime");
	    			long bpm = (Long) point.get("bpm");
	    			long confidence = (Long) point.get("confidence");
	    			double caloriesBurned = (Double) point.get("caloriesBurned");
	    			
	    			bw.write(dateTime + "," + bpm + "," + caloriesBurned + "," + confidence + "\n");
	    		}
	    		bw.flush();
	    		bw.close();

	    	}
	    	else
	    		System.out.println(json);
		}
	    catch (Exception e) {
			e.printStackTrace();
	    	//return null;
	    }	
	}
	
	public void getIntraDaySteps(String day, boolean csv) {
		this.metrics15mindata(day, csv, "steps");
	}
	
	public void getIntraDayCalories(String day, boolean csv) {
		this.metrics15mindata(day, csv, "calories-burned");
	}
	
	public void getIntraDayFloors(String day, boolean csv) {
		this.metrics15mindata(day, csv, "floors");
	}
	
	public void getIntraDayDistance(String day, boolean csv) {
		this.metrics15mindata(day, csv, "distance");
	}
	
	public void getIntraDayActiveMins(String day, boolean csv) {
		this.metrics15mindata(day, csv, "active-minutes");
	}
	
	private String getIntraDayData(String day, String datatype) {
		HttpPost req = new HttpPost(FitbitDataExtractor.FITBIT_AJAX_ENDPOINT);
		req.addHeader("Host", "www.fitbit.com");
	    req.addHeader("Origin", "https://www.fitbit.com");
	    req.addHeader("Referer", "https://www.fitbit.com/");
	    req.addHeader("Connection", "keep-alive");
	    req.addHeader("X-Requested-With", "XMLHttpRequest");
	    req.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
	    req.addHeader("Content-Type", "application/x-www-form-urlencoded");
	    req.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
	    req.addHeader("Accept-Encoding", "gzip, deflate");
	    req.addHeader("Content-Language", "en-GB");
	    req.addHeader("Cookie", this.cookie);
	    
		String request = "{\"template\":\"/ajaxTemplate.jsp\",\"serviceCalls\":[{\"name\":\"activityTileData\","
					   + "\"args\":{\"date\":\"" + day + "\",\"dataTypes\":\"" + datatype + "\"},"
					   + "\"method\":\"getIntradayData\"}]}";
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("request", request));
	    params.add(new BasicNameValuePair("csrfToken", this.token));

	    try {
	    	req.setEntity(new UrlEncodedFormEntity(params));
	    	CloseableHttpResponse resp = client.execute(req);
	    	
			String json = EntityUtils.toString(resp.getEntity());
    		System.out.println("getIntraDayData: " + resp.getStatusLine());
    		
    		return json;

	    }
	    catch (Exception e) {
			e.printStackTrace();
	    }
	    
    	return null;	
	}
	
	private void metrics15mindata(String day, boolean csv, String datatype) {
	    try {
	    		    	
	    	String json = this.getIntraDayData(day, datatype);
	    	
	    	if (json == null) {
	    		System.out.println("could not get the data you requested");
	    		return;
	    	}
	    	
    		System.out.println("Got " + datatype + " Data");
    		
    		if (csv) {
    			File f= new File(datatype);
    			f.mkdir();
	    		BufferedWriter bw = new BufferedWriter(new FileWriter(f.getAbsolutePath() + File.separator +datatype + "-" + day));

	    		JSONParser parser = new JSONParser();
	    		JSONArray arr = (JSONArray) parser.parse(json);
	    		JSONObject obj = ((JSONObject) arr.get(0));
	    		JSONObject datasets = (JSONObject) obj.get("dataSets");
	    		JSONObject activity = (JSONObject) datasets.get("activity");
	    		JSONArray datapoints = (JSONArray) activity.get("dataPoints");
	    		
        		System.out.println("Saving to csv file the date for: " + day);
        		
	    		Iterator<JSONObject> iterator = datapoints.iterator();
	    		while (iterator.hasNext()) {
	    			JSONObject point = iterator.next();
	    			String dateTime = (String) point.get("dateTime");
	    			double value = (Double) point.get("value");
	    			
	    			bw.write(dateTime + "," + value + "\n");
	    		}
	    		bw.flush();
	    		bw.close();

	    	}
	    	else
	    		System.out.println(json);
	    }
	    catch (Exception e) {
			e.printStackTrace();
	    	//return null;
	    }	
	}
	
	private Header[] getCookies(CloseableHttpResponse resp) {
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {	
			System.out.println("Successfully Authenticated...");
			Header[] headerlist = resp.getHeaders("Set-Cookie");
			
			return headerlist;
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		FitbitDataExtractor extractor = new FitbitDataExtractor();
		
		if (args.length == 3)
			extractor.getAuthenticated(args[0], args[1], args[2]);
		else if (args.length == 2)
			extractor.getAuthenticated(args[0], args[1]);
		else {
			System.out.println(USAGE);
			System.exit(-1);
		}


//		extractor.getIntraDayHeartandCalories("2016-01-31", true);
//		extractor.getIntraDaySteps("2016-01-31", true);
		
//		for (int i = 1; i <= 31; i++)
//			extractor.getIntraDaySteps("2016-03-" + i, true);

		for (int i = 1; i <= 31; i++) {
			extractor.getIntraDaySteps("2016-07-" + i, true);
			extractor.getIntraDayCalories("2016-07-" + i, true);
			extractor.getIntraDayFloors("2016-07-" + i, true);
			extractor.getIntraDayActiveMins("2016-07-" + i, true);
			extractor.getIntraDayDistance("2016-07-" + i, true);
		}
	}
}