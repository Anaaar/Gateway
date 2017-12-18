package testInterpreter;
import java.io.IOException;
import java.util.ArrayList;

import comlora.COMLora;
import comlora.COMLoraSPI;
import comlora.ILORA;

public class ComLora implements ICOM {

	static String character;
	static boolean brut=true;

	public static int Temperature;
	public static int Luminosite;
	public static int Humidite;
	public static int Batterie;
	public static int RSSI_Value;
	public static int Num_Module;
	public static int[] donnees ;
	private  ILORA LoraDevice;
	public int [] capteurs;
	short[] ack = {0x4f, 0x4b}; // message d'acquittement: "OK"

	public static int[] devices; // Liste de modules de capteurs acceptables
//Constructeur de la classe, initialise la communication avec le module LoRa par USB ou SPI
	public ComLora(JsonDecoder jDecoder){

		switch(jDecoder.device){
		case "LORAusb":
			this.LoraDevice = new COMLora(jDecoder.serialPort);
			break;
		case "LORAspi":
			this.LoraDevice = new COMLoraSPI();
			break;
		default:
			System.out.println("Error creating a new Lora Device");
			break;
		}

		devices = jDecoder.capteurs;

	}
	
	//Methode pour envoier un message, elle reçoit le message et le module
	public void sendMsg(short[] msg,int num_module){
		try {
			LoraDevice.sendMsg(msg, num_module); // Methode de la bibliotheque COM_Lora_with_spi pour envoier un message

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//Methode pour recevoir un message par LoRa: elle reçoit le temps pour etre en ecoute
	public int[] readMsg(long temps){
		int i;
		int [] LoraMessage = null;
		int taille;

		System.out.println(" |  read data Lora |");
		try {
			LoraMessage = LoraDevice.readMsg(temps); // Methode de la bibliotheque COM_Lora_with_spi pour attendre un message pour un certain temps 

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		if (LoraMessage==null)
			return null;
		else if(LoraMessage[1]==79 && LoraMessage[2]==75){ // Message de confirmation de configuration
			int [] reponse = {0,0};
			return reponse;
		}
		else{

			taille =1;
			for(i=0;i<LoraMessage.length;i++){ // Obtient la taille de la trame
				if(LoraMessage[i]==254)
					break;
				taille++; 
			}
			taille = (taille+4)/3;
			capteurs = new int[taille]; // Cree une trame donc la taille est la quantite de capteurs + Num_module + Rssi
			donnees =  new int[taille-2]; ///Cree une trame donc la taille est la quantite de capteurs

			ArrayList<String> tab = new ArrayList<String>(taille);

			//		 for (int i=0; i<LoraMessage.length; i++) {
			//		        System.out.print(LoraMessage[i]+" ");
			//		   }
			//		   System.out.println("");	

			if (LoraMessage != null){
				for(i = 0; i< taille*3-4; i++){
					character= Integer.toString(LoraMessage[i]);
					tab.add(i, character);
				}

				LoraTrad(tab,taille);//Methode pour faire la "traduction" des donnees			
			}	


			try {
				RSSI_Value = LoraDevice.GetRSSI();  // Prendre la valeur d'intensité RSSI du message reçua evc une methode de la bibliotheque
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			capteurs[0] = Num_Module;
			capteurs[1] = RSSI_Value;
			for(i=0;i<donnees.length;i++){
				capteurs[i+2] = donnees[i];

			}
			//		capteurs[2] = Temperature;
			//		capteurs[3] = Luminosite;
			//		capteurs[4] = Humidite;
			//		capteurs[5] = Batterie;

			//sendMsg(ack, Num_Module);
			return capteurs;
		}
	}
	//Methode pour faire la "traduction" des donnees
	public static void LoraTrad(ArrayList<String> tableau,int taille){


		int msb, lsb;

		for (int indice=0;indice<taille*3-4;indice++) { 

			if(indice>1 && ((indice+1)%3)==0){												//if((indice==3 || indice==5 || indice==8 || indice==11) ){
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
