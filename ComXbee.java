package testInterpreter;



import java.io.InputStream;
import java.util.ArrayList;





import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.ZigBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBeeMessage;

public class ComXbee implements ICOM{


	static String character;
	static boolean brut=true;
	private static final int BAUD_RATE = 9600;

	public static int Temperature=0;
	public static int Luminosite=0;
	public static int Humidite=0;
	public static int Batterie=0;
	public static int RSSI_Value = 0;
	public static int Num_Module=0;
	public static int[] donnees ;
	public static String[] devices;

	XBeeDevice XbeeDevice;
	private ZigBeeDevice zbd;
	//private OutputStream sortie;
	static InputStream entree = null;


//Constructeur de la classe, il reçoit les parametres du fichier json et initialise la communication avec le module Xbee
	public ComXbee(JsonDecoder jDecoder){

		this.XbeeDevice = new XBeeDevice(jDecoder.serialPort,BAUD_RATE);
		
		//ZigBeeDevice zbd = new ZigBeeDevice(jDecoder.serialPort,BAUD_RATE);
		
		try {
			XbeeDevice.open();
			System.out.println(" |   XBeeDevice.open  |");
		} catch (XBeeException e) {
			System.out.println(" |   XBeeDevice erreur  |");
			e.printStackTrace();
			System.exit(1);
		}
		//devices = new String[jDecoder.capteurs.length];
		//devices = jDecoder.capteurs;

	}

//Methode pour envoyer un message par Xbee: elle reçoit le messqge et le numero du module
	public void sendMsg(short[] msg,int num_module){

		byte[] message = new byte[msg.length +3];

		
		//Cree la trame avec le message reçu comme parametre
		message[0]=(byte) 0xff;
		message[1]=(byte) num_module;
		for(int k =2;k<msg.length+2;k++){
			message[k] = (byte)(msg[k-2]);
		}
		message[message.length-1]=(byte) 0xfe;

		for(int k =0;k<message.length;k++){
			System.out.print(message[k]+" ");
		}
		System.out.println("");
		
		
		try {
			XbeeDevice.sendBroadcastData(message); // Methode de la bibliotheque xbjlib pour envoier un message
		} catch (XBeeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//Methode pour recevoir un message par Xbee: elle reçoit le temps pour etre en ecoute
	public int[] readMsg(long temps){

		int taille;

		System.out.println(" |   XBeeDevice  |");		

		byte[] rssi = null;
		@SuppressWarnings("unused")
		int ret,confere=1,i;
		int [] capteurs = {0,0,0,0,0,0} ;

		System.out.println(" |  read data XBee |");
		XBeeMessage xbeeMessage = XbeeDevice.readData((int)temps); // Methode de la bibliotheque xbjlib pour attendre un message pour un certain temps
		
		if (xbeeMessage==null)
			return null;
		else if(xbeeMessage.getData()[1]==79 &&xbeeMessage.getData()[2]==75){ // Message de confirmation de configuration
			int [] reponse = {0,0};
			return reponse;
		}
		else{

			taille = 1;
			for(i=0;i<xbeeMessage.getData().length;i++){ // Obtient la taille de la trame
				if((int)xbeeMessage.getData()[i]==254)
					break;
				taille++; 
			}
			taille = (taille+4)/3; 
			capteurs = new int[taille]; // Cree une trame donc la taille est la quantite de capteurs + Num_module + Rssi
			donnees =  new int[taille-2]; //Cree une trame donc la taille est la quantite de capteurs
			
			ArrayList<String> tab = new ArrayList<String>(taille);
			
			for( i = 0; i< xbeeMessage.getData().length; i++){
				ret = (int) (xbeeMessage.getData()[i] & 0xFF); // Prendre chaque element du message reçu
				System.out.print(ret + " ");				
				character= Integer.toString(ret);
				tab.add(i, character);

			}
			System.out.println("");
			//System.out.println(tab);
			XbeeTrad(tab,taille); //Methode pour faire la "traduction" des donnees


			try {
				rssi = XbeeDevice.getParameter("DB"); // Prendre la valeur d'intensité RSSI du message reçu
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XBeeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			RSSI_Value = rssi[0];
			//			for(int j=0; j < rssi.length;j++)
			//			 System.out.println(rssi[j]);
				
			capteurs[0] = Num_Module;
			capteurs[1] = RSSI_Value;
			for(i=0;i<donnees.length;i++){
				capteurs[i+2] = donnees[i];

			}
			
//			capteurs[2] = Temperature;
//			capteurs[3] = Humidite; 			
//			capteurs[4] = Luminosite;
//			capteurs[5] = Batterie;

			return capteurs; 

		} 


	}
	//Methode pour faire la "traduction" des donnees
	public static void XbeeTrad(ArrayList<String> tableau,int taille){


		int msb, lsb;

		for (int indice=0;indice<taille*3-4;indice++) { 

			if(indice>1 && ((indice+1)%3)==0){
				switch (Integer.parseInt(tableau.get(indice))) {
				case 0:
					break;
				case 1:
					msb=Integer.parseInt(tableau.get(indice+1));
					lsb=Integer.parseInt(tableau.get(indice+2));
					Temperature= (brut)?(msb*256+lsb):(((msb*256+lsb)));
					donnees[0] = Temperature;
					break;
				case 2:
					msb=Integer.parseInt(tableau.get(indice+1));
					lsb=Integer.parseInt(tableau.get(indice+2));
					Humidite= (brut)?(msb*256+lsb):(msb*256+lsb);
					donnees[1] = Humidite;
					break;
				case 3:
					msb=Integer.parseInt(tableau.get(indice+1));
					lsb=Integer.parseInt(tableau.get(indice+2));
					Luminosite= (brut)?(msb*256+lsb):(msb*256+lsb);
					donnees[2] = Luminosite;
					break;
				case 4: // CO2 ?
					msb=Integer.parseInt(tableau.get(indice+1));
					lsb=Integer.parseInt(tableau.get(indice+2));
					donnees[4] = (brut)?(msb*256+lsb):(msb*256+lsb);
				case 5: //Cov ?
					msb=Integer.parseInt(tableau.get(indice+1));
					lsb=Integer.parseInt(tableau.get(indice+2));
					donnees[5] = (brut)?(msb*256+lsb):(msb*256+lsb);

				case 6:
					msb=Integer.parseInt(tableau.get(indice+1));
					lsb=Integer.parseInt(tableau.get(indice+2));
					Batterie = (brut)?(msb*256+lsb):(msb*256+lsb);//Integer.parseInt(tableau.get(indice+1));
					donnees[3] = Batterie;
					//Batterie=tableau.get(indice+1);
					break;
				case 7: // Pression ?
					msb=Integer.parseInt(tableau.get(indice+1));
					lsb=Integer.parseInt(tableau.get(indice+2));
					donnees[6] = (brut)?(msb*256+lsb):(msb*256+lsb);;

				case 254:
					break;
				default:
					System.out.println("erreur ");
					break;
				}
			}

		}

		Num_Module = Integer.parseInt(tableau.get(1));
	}



}
