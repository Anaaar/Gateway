package testInterpreter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import lora_temperature.Channel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class JsonDecoder {

	private static String pROXY_URL;
	private static int pROXY_PORT;
	private static int channelID;
	private static String apiWriteKey;
	private static String apiReadKey;
	
	public DataBaseConnexion[] db;
	public Channel[] channel;
	
	public String serialPort;
	public String device;
	public int[] capteurs;

	public static void main(String[] args) {
		
		String filePath = new File("").getAbsolutePath();
		new JsonDecoder().parseJSONParams(filePath);    //"C://Users//Utilisateur//workspace//SensEnvAcq//src//file.json");
										   
	}
	
	public boolean parseJSONParams(String filePath){
		
		  JSONObject o2; 
	      JSONParser parser = new JSONParser();
	      String count;
	      
	      try{
	    	  
	    	 FileReader f = new FileReader(filePath + "/file.json"); // Prendre le fichier json dans le docier racine du projet
	         Object obj = parser.parse(f);
	         JSONArray a = (JSONArray)obj;
	         
	         // loop array
	         for (Object o : a)
	         {
	           JSONObject person = (JSONObject) o;
	           
	           o2 = (JSONObject) person.get("Proxy");
	           pROXY_URL = (String) o2.get("url");
	           pROXY_PORT = Integer.parseInt((String) o2.get("port"));	            		            	
	              System.out.println("Proxy: url="+pROXY_URL+" port="+pROXY_PORT);
	              
	           serialPort = (String) person.get("SerialPort");
	           System.out.println("COM = "+serialPort);
	           
	           device = (String) person.get("Device");
	           System.out.println("device = "+device);
	           
	           
	           o2 = (JSONObject) person.get("Capteurs");
	           capteurs = new int[o2.size()];
	 
	           for(int i=0;i<o2.size();i++){
	        	   count = Integer.toString(i+1);
	        	   capteurs[i] = Integer.parseInt((String) o2.get(count));
	        	   System.out.println(capteurs[i]);
	           }
	           
	           
	           // loop array database
	            JSONArray cars = (JSONArray) person.get("DataBase");
	            System.out.println("size = "+cars.size());
	            db = new DataBaseConnexion[cars.size()];
	            
	            @SuppressWarnings("unchecked")
				Iterator<JSONObject> iterator = cars.iterator();
	            int i =0;
	            while (iterator.hasNext()) {
	            	
	            	db[i] = new DataBaseConnexion();
	            	o2 = (JSONObject)iterator.next();	            	
	            	db[i].setSERVER_URL((String) o2.get("url")); 
	            	System.out.println("url = "+ db[i].SERVER_URL);
	            	
	            	//db[i].setSERVER_PORT((String) o2.get("port"));	
	            	//System.out.println("url = "+ db[i].SERVER_PORT);
	            	
	            	if (Boolean.parseBoolean((String) o2.get("needproxy")))
	            	{
	            		db[i].PROXY_NEEDED = true;
	            		db[i].setPROXY_URL(pROXY_URL);
	            		db[i].setPROXY_PORT(pROXY_PORT);
	            		db[i].setExtraInfo("distant");	
	            	}
	            	else
	            	{
	            		db[i].PROXY_NEEDED = false;
	            		db[i].setPROXY_URL(pROXY_URL);
	            		db[i].setPROXY_PORT(pROXY_PORT);
	            		db[i].setExtraInfo("local");
	            		
	            	}
	 	            
	            		System.out.println("Database: url="+db[i].getSERVER_URL()+" port="+db[i].getSERVER_PORT() + " needproxy="+db[i].PROXY_NEEDED+" URL proxy="+db[i].getPROXY_URL()+" port_proxy="+db[i].getPROXY_PORT());
	            		i++;
	            }
	            
	         // loop array Thingspeak Channels
	            JSONArray carsThingSpeak = (JSONArray) person.get("Thingspeak");
	            System.out.println("size = "+carsThingSpeak.size());
	            channel = new Channel[carsThingSpeak.size()];
	            channel = new Channel[carsThingSpeak.size()];
	            @SuppressWarnings("unchecked")
				Iterator<JSONObject> iterator_channel = carsThingSpeak.iterator();
	            i =0;
	            while (iterator_channel.hasNext()) {	            	
	            	o2 = (JSONObject)iterator_channel.next();	
	            	channelID = Integer.parseInt((String) o2.get("channelID"));
	            	apiWriteKey = (String) o2.get("writekey");
	            	apiReadKey = (String) o2.get("readkey");

	            	System.out.println("ID = "+ channelID + " WriteKey = "+ apiWriteKey );
	            	
	            	if (Boolean.parseBoolean((String) o2.get("channel_public")))
	            	{
	            		channel[i] = new Channel(channelID, apiWriteKey);
	      
	            	}
	            	else
	            	{
		            	channel[i] = new Channel(channelID, apiWriteKey,apiReadKey);
	            	}
	            	i++; 
	            }
	            	
	            }
	                      

	       /*      cars = (JSONArray) person.get("Proxy");
	             iterator = cars.iterator();
	            while (iterator.hasNext()) {
	            	o2 = (JSONObject)iterator.next();
	            	url = (String) o2.get("url");
	            	port = Integer.parseInt((String) o2.get("port"));	            		            	
	 	            System.out.println("Proxy: url="+url+" port="+port);
	            }*/
	            

	            /* String job = (String) person.get("job");
	           System.out.println(job);

	           JSONArray cars = (JSONArray) person.get("cars");

	           for (Object c : cars)
	           {
	             System.out.println(c+"");
	           }*/
	         
	         
	      
	         /*
	         System.out.println("The 2nd element of array");
	         System.out.println(array.get("title"));
	         System.out.println();

	         JSONObject obj2 = (JSONObject)array.get(1);
	         System.out.println("Field \"1\"");
	         System.out.println(obj2.get("1"));    

	         s = "{}";
	         obj = parser.parse(s);
	         System.out.println(obj);

	         s = "[5,]";
	         obj = parser.parse(s);
	         System.out.println(obj);

	         s = "[5,,2]";
	         obj = parser.parse(s);
	         System.out.println(obj);
	         
	         */
	      } catch (org.json.simple.parser.ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	      
	      return true;
	   }
	
}

