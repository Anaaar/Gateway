package testInterpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;


/**
 * The class starting with "RT_" are useful only for the Zigbee Real-time uploading.
 * A major part of the code is from the project SensEnv did by other students.
 * Some modifications were necessary because of GME.
 * @author Yves
 *
 */
public class DataBaseConnexion{// extends RT_TestThread{ // **MODIF** extend Main

//	private HttpClient httpclient;

	/**
	 * Due to issues with GME, we had to change the communication process.
	 * BasicNameValuePair is no longer useful, we here only use it for esthetic.
	 * 
	 * @param date
	 * @param idModule
	 * @param sensor
	 * @param valeur
	 * @param gps
	 */
	
	public String PROXY_URL="proxy.esiee.fr";
	public String getPROXY_URL() {
		return PROXY_URL;
	}


	public void setPROXY_URL(String pROXY_URL) {
		PROXY_URL = pROXY_URL;
	}

	public int PROXY_PORT=3128;
	public int getPROXY_PORT() {
		return PROXY_PORT;
	}


	public void setPROXY_PORT(int pROXY_PORT) {
		PROXY_PORT = pROXY_PORT;
	}

	private Proxy proxy;
	public boolean PROXY_NEEDED=false;
	public String SERVER_URL="localhost";
	public String SERVER_PORT="";
	public String extraInfo;
	
	public String getExtraInfo() {
		return extraInfo;
	}


	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}


	public String getSERVER_URL() {
		return SERVER_URL;
	}


	public void setSERVER_URL(String sERVER_URL) {
		SERVER_URL = sERVER_URL;
	}


	public String getSERVER_PORT() {
		return SERVER_PORT;
	}


	public void setSERVER_PORT(String sERVER_PORT) {
		SERVER_PORT = sERVER_PORT;
	}

	public void Connectdb(String date, String idModule,String sensor,String valeur,String gps){

		String	httpdate = "date";
		String	httpmodule = "idmodule";
		String	httpsensor = "sensor";
		String	httpvaleur = "valeur";
		
		String	httpgps = "gps";

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(httpdate,date));
		nameValuePairs.add(new BasicNameValuePair(httpmodule, idModule));
		nameValuePairs.add(new BasicNameValuePair(httpsensor, sensor));
		nameValuePairs.add(new BasicNameValuePair(httpvaleur, valeur));
		nameValuePairs.add(new BasicNameValuePair(httpgps, gps));
		String uRLComplement="";
		int i=0;
		for(NameValuePair tmp: nameValuePairs){
			uRLComplement=uRLComplement+tmp;
			i++;
			if(i<nameValuePairs.size())
				uRLComplement=uRLComplement+"&";
		}

		final String strURL;

		if (SERVER_PORT.equals(""))
			strURL = "http://"+SERVER_URL+"/sensenv/ConnectDatabaseTMP.php";
		else 
			strURL = "http://"+SERVER_URL+":"+SERVER_PORT+"/sensenv/ConnectDatabaseTMP.php";
		
		System.out.println(" |  strURL  |="+this.extraInfo+":"+this.SERVER_PORT+"?"+uRLComplement);

		String charset = "UTF-8";
	
		HttpURLConnection connection=null;
		try {
			if(PROXY_NEEDED){
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_URL, PROXY_PORT));
			connection = (HttpURLConnection) new URL(strURL+"?"+uRLComplement).openConnection(proxy);
			}
			else
				{connection = (HttpURLConnection) new URL(strURL+"?"+uRLComplement).openConnection();
				
				}
			
			
		    connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestMethod("GET");			
			connection.connect();
				
			BufferedReader br=null;
			
			br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    StringBuilder sb = new StringBuilder();
	        String line;
			while ((line = br.readLine()) != null) {
			    sb	.append(line+"\n");
			}
			br.close();	    		
	    
			// Here is the result of the request sent by us towards the Database
	        @SuppressWarnings("unused")
			String container = sb.toString();
			
		}catch (ProtocolException e1) {
			System.out.println("erreur GET");
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		nameValuePairs.clear();
	}
}