package testInterpreter;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.mashape.unirest.http.Unirest;




/**
 * XBee Java Library Receive Data polling sample application.
 * 
 * <p>This example reads data from the local device using the polling mechanism.</p>
 * 
 * <p>For a complete description on the example, refer to the 'ReadMe.txt' file
 * included in the root directory.</p>
 */
@SuppressWarnings("deprecation")
public class Test64bits implements Runnable{

	public static int Id_Thingspeak = 0; // Id du channel ThingSpeak ou 0 = broadcast 
	public static int Id_DB = 0;         // Id de la base de donnees ou 0 = broadcast
	public static BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(90); // File d'attente avec une taille de 90
	public static LocalDB dataBase; //Base de donnees local creee pour la configuration a distance

	/**
	 * Application main method.
	 * 
	 * @param args Command line arguments PORTCOM true (if proxy needed) SERVER_URL SERVER_PORT PROXY_URL PROXY_PORT  
	 * @throws IOException 
	 * 
	 */
	//@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException {

		//		String filePath = new File("").getAbsolutePath();
		//		JsonDecoder jDecoder = new JsonDecoder();
		//		jDecoder.parseJSONParams(filePath);
		//		ICOM in = null;
		//		@SuppressWarnings("unused")
		//		int Id=0;
		//
		//		switch(jDecoder.device){
		//		case "LORAusb":
		//			in = new ComLora(jDecoder);
		//			break;
		//		case "LORAspi":
		//			in = new ComLora(jDecoder);
		//			break;
		//		case "XBEE":
		//			in = new ComXbee(jDecoder);
		//			break;
		//		default:
		//			System.out.println("Device inconnu: "+jDecoder.device);
		//			break;
		//		}

		//ISTORE out1 = new StorageDB(queue,jDecoder,Id_Thingspeak);
		//ISTORE out = new StorageThingspeak(queue,jDecoder,Id_DB)


		Unirest.setProxy(new HttpHost("proxy.esiee.fr", 3128)); // Change le proxy pour être possible utiliser l'internet
		dataBase = new LocalDB(2828); // Cree une base de donnees avec le port 2828
		Thread In = new Thread(new Test64bits()); //Cree une Thread pour executer l'aplication de la calsse Test64bits
		In.start(); // Initialise la Thread ci-dessus
		//		//short[] msg = {0x54, 0x45, 0x53, 0x54 ,0x45, 0x20, 0x31}; // "TESTE 1"
		//		while(true){
		//			int[] donnees = in.readMsg();	
		//
		//			//in.sendMsg(msg, 0);
		//			if(donnees!= null){
		//				for(int i=0;i<jDecoder.capteurs.length;i++){
		//					if(donnees[0] == jDecoder.capteurs[i])
		//						Id = jDecoder.channel[i].channelId;
		//				}
		//				//out.sendData(Id, donnees, jDecoder);
		//				//out.broadData (donnees,jDecoder);//(donnees[2],donnees[3],donnees[4],donnees[5],donnees[0],donnees[1],jDecoder);						
		//				out1.broadData(donnees, jDecoder);
		//
		//			}
		//			try {			
		//				Thread.sleep(1);		
		//			} 		
		//			catch (InterruptedException e) 		
		//			{			
		//				e.printStackTrace();		
		//			}
		//		}
	}

	//Thread qui va executer la lecture des donnees et les stocker dans la file d'attente
	@SuppressWarnings("static-access")
	public void run() {

		String filePath = new File("").getAbsolutePath(); // Racine du projet
		JsonDecoder jDecoder = new JsonDecoder();
		jDecoder.parseJSONParams(filePath); //Prendre le fichier Json dans le docier racine du projet
		ICOM in = null;
		//Integer[] donnees =new Integer[6];
		int[] donnees_int = null;
		int i;
		//boolean status;

		switch(jDecoder.device){ // Selectionne le module utilise
		case "LORAusb": // Module gateway USB
			in = new ComLora(jDecoder);
			break;
		case "LORAspi":
			in = new ComLora(jDecoder);
			break;
		case "XBEE":
			in = new ComXbee(jDecoder);
			break;
		default:
			System.out.println("Device inconnu: "+jDecoder.device);
			break;
		}


		ISTORE out1 = new StorageDB(queue,jDecoder,Id_Thingspeak); // Cree un objet ISTORE en utilisant la classe StorageDB
		Thread out = new Thread(new StorageThingspeak(queue,jDecoder,Id_DB)); //Cree une Thread pour executer l'aplication de la calsse StorageThingspeak
		out.start(); // Initialise la Thread ci-dessus
		short[] message;

		while(true){
			donnees_int = in.readMsg(20000);// Écoute un message pour 20 secondes
			if(donnees_int != null && donnees_int.length >=6){ // Verifie si c'est bien la Trame avec les capteurs
				for(i=0; i < 6; i++){
					queue.offer(Integer.valueOf(donnees_int[i])); // Sauvegarde les valeurs dans la queue
					System.out.println(" donnees_int= "+ donnees_int[i]);
				}
				//System.out.println("Final elements in the queue: ");
				out1.broadData(donnees_int, jDecoder); // Utilise le moetode broadData pour envoyer par les bases de donnees
			}
			else
				System.out.println("Message Vide");
			// Si il a reçu un commande pour reconfigurer a distance e le dernier message reçu c'etait du module pour configurer
			if(dataBase.cmd != 0 && donnees_int != null && dataBase.module == donnees_int[0]){ 
				message = new short[dataBase.message.length];

				for( i=0;i<dataBase.message.length;i++)
					message[i] = dataBase.message[i];

				for( i=0;i<dataBase.message.length;i++)
					System.out.print(message[i]+" ");
				System.out.println("");


				switch(jDecoder.device ){ // Si on utilise le module gateway attend 12secondes pour envoyer le message
				case "LORAusb":
//					try {
//						Thread.sleep(12000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}

				in.sendMsg(dataBase.message, dataBase.module); // Envoye le message de configuration
				switch(jDecoder.device ){
				case "LORAusb":
//					try {
//						Thread.sleep(12000);
//
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					break;
				}

				donnees_int = in.readMsg(15000); // Attend un message de confirmation de la configuration
				if(donnees_int==null) // Si il reçoit un message vide
				{
					dataBase.setVarOne(-1);
					System.out.println(dataBase.status);
				}
				else if (donnees_int[0]==0 && donnees_int[1]==0) {// Si il reçoit le message de confirmation
					dataBase.setVarOne(1); 
					System.out.println(dataBase.status);
				}
				else{
					dataBase.setVarOne(-1);  // si il ne rien reçoit
					System.out.println(dataBase.status);
				}

				dataBase.cmd = 0; // Met a zero la flag de commande de configuration
			}

		}


	}

	// Creer un nouveau channel Thingspeak
	public static HttpResponse http() {

		@SuppressWarnings({ "resource" })
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("https://api.thingspeak.com/channels.json");

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("api_key","38I6LPTEGNOGN5JQ"));
			nameValuePairs.add(new BasicNameValuePair("name", "My New Channel"));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			System.out.println("test ");
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			System.out.println("this is the response "+response);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			System.out.println("CPE "+e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IOE "+e);
		}  

		return null;
	}

}

