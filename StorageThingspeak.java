package testInterpreter;
 
import java.util.concurrent.BlockingQueue;

import lora_temperature.Entry;
import lora_temperature.ThingSpeakException;

import com.mashape.unirest.http.exceptions.UnirestException;
@SuppressWarnings("unused")

public class StorageThingspeak implements ISTORE,Runnable {


	private static String Temperature;
	private static String Humidite;
	private static String Luminosite;
	private static String Batterie;
	private static String Rssi_value;
	private static String Module;

	private static String[] valeur_a_envoyer;
	private BlockingQueue<Integer> queue; // File d'attente avec une taille sans l'initialisee
	private int Id_send; 
	private JsonDecoder JsonParametres;

	//constructeur de la classe: reçoit une file d'attente, l'objet avec les donnees Json et l'Id du canal Thingspeak
	public StorageThingspeak(BlockingQueue<Integer> queue,JsonDecoder Parametres,int Id){
		this.queue = queue; 
		this.Id_send = Id;
		this.JsonParametres = Parametres;
	}

	// Methode utilisee par l'interface Runnable pour une Thread
	public void run() {

		int taille = 6;
		int i;
		Integer[] valeurs= new Integer[taille];
		String [] valeurs_concatenees = {"","","","","",""};
		String separateur = "-"; //Separateur pour concatener les donnees des capteurs dans un seul block
		try {
			while(true){
				while (queue.size() !=0) // Prendre tous les donnees de la file d'attente pour les concatener 
				{

					if(queue.size()<=6)
						separateur = "\0"; // Fin des donnees concatenees
					else
						separateur = "-"; // Separateur des donnees

					for(i=0;i<taille;i++){
						valeurs[i] = queue.take();
						valeurs_concatenees[i] = valeurs_concatenees[i] + Integer.toString(valeurs[i])+ separateur; // enchaîne les valeurs des capteurs
						//System.out.println(valeurs_concatenees[i] );
					}
					
				}
				
				switch(Id_send){ 
				case 0: // 0 envoie pour tous les canaux
					broadMultipleData(valeurs_concatenees,JsonParametres);
					break;
				default: // Envoie pour un canal specifique
					sendMultipleData(Id_send,valeurs_concatenees,JsonParametres);
					break;
				}
				Thread.sleep(15000); // Met en veille pour 15 secondes
				for(i=0;i<taille;i++){
					valeurs_concatenees[i] =""; // enchaîne les valeurs des capteurs
			
				}
			
			}

		} catch (InterruptedException e) {
			e.printStackTrace();   
		}
	}

// Methode pour envoier les donnees concatenees pour tous les canaux: reçoit l'objet Json et les valeurs concatennes de chaque capteur
	public void broadMultipleData(String[] valeurs,JsonDecoder Parametres){ 
		int i;

		valeur_a_envoyer = new String[valeurs.length-1];
		Module = valeurs[0];
		valeur_a_envoyer[valeur_a_envoyer.length-1] = valeurs[1]; // Rssi

		for(i=0;i<valeurs.length-2;i++){
			valeur_a_envoyer[i] = valeurs[i+2];
		}

		//0: RSSI, 1: Temeprature, 2: Luminosite, 3: Humidite, 4: Batterie, 5

		Entry entry = new Entry(); // Objet pour stocker les donnees a envoyer pour les canaux

		//System.out.println(Parametres.channel[0].isAvailable());

		for(i=0;i<valeurs.length-1;i++){
			entry.setField(i+1, valeur_a_envoyer[i]);		
		}
		entry.setField(i+1, Module );

		System.out.println(" |  Send Multiple broadcast Thingspeak data |");			
		for(i=0;i< Parametres.channel.length;i++){

			try {					
				Parametres.channel[i].update(entry); // Methode pour envoyer les donnees pour le canal				
			} catch (UnirestException e) {

				e.printStackTrace();				
			} catch (ThingSpeakException e) {

				e.printStackTrace();		
			}	
		}


	}
	// Methode pour envoier les donnees concatenees pour un canal specifique: reçoit le canal à envoyer, l'objet Json et les valeurs concatennes de chaque capteur
	public void sendMultipleData (int IdDB,String[] valeurs,JsonDecoder Parametres){

		int i; 


		valeur_a_envoyer = new String[valeurs.length-1];
		Module = valeurs[0];
		valeur_a_envoyer[valeur_a_envoyer.length-1] = valeurs[1]; // Rssi

		for(i=0;i<valeurs.length-2;i++){
			valeur_a_envoyer[i] = valeurs[i+2];
		}

		//0: RSSI, 1: Temeprature, 2: Luminosite, 3: Humidite, 4: Batterie,


		Entry entry = new Entry(); // Objet pour stocker les donnees a envoyer pour les canaux
		//System.out.println(Parametres.channel[0].isAvailable());

		for(i=0;i<valeurs.length-1;i++){
			entry.setField(i+1, valeur_a_envoyer[i]);		
		}
		entry.setField(i+1, Module );

		System.out.println(" |  Send Multiple Tihngspeak data |");		

		for(i=0;i< Parametres.channel.length;i++)				
			if (Parametres.channel[i].channelId == IdDB )	// Trouve le canal choisi				
				break;	


		if(i>=Parametres.channel.length)
		{
			i--;
			System.out.println("Error, mauvais channel! ");
			return;
		}	

		try {			
			Parametres.channel[i].update(entry); // Methode pour envoyer les donnees pour le canal			
		} catch (UnirestException e) {			
			e.printStackTrace();			
		} catch (ThingSpeakException e) {

			e.printStackTrace();
		}
	}

	// Methode pour envoier les donnees pour tous les canaux: reçoit l'objet Json et les valeurs concatennes de chaque capteur
	public void broadData(int[] valeurs,JsonDecoder Parametres){


		int i;

		valeur_a_envoyer = new String[valeurs.length-1];
		Module = Integer.toString(valeurs[0]);
		valeur_a_envoyer[valeur_a_envoyer.length-1] = Integer.toString(valeurs[1]); // Rssi

		for(i=0;i<valeurs.length-2;i++){
			valeur_a_envoyer[i] = Integer.toString(valeurs[i+2]);
		}

		//0: RSSI, 1: Temeprature, 2: Luminosite, 3: Humidite, 4: Batterie, 5
		
		//		
		//		 Temperature = Integer.toString(valeurs[2]);
		//		 Humidite    = Integer.toString(valeurs[4]);
		//		 Luminosite  = Integer.toString(valeurs[3]);
		//		 Batterie    = Integer.toString(valeurs[5]);
		//		 Module      = Integer.toString(valeurs[0]);
		//		 Rssi_value   = Integer.toString(valeurs[1]);

		Entry entry = new Entry();// Objet pour stocker les donnees a envoyer pour les canaux

		//System.out.println(Parametres.channel[0].isAvailable());

		for(i=0;i<valeurs.length-1;i++){
			entry.setField(i+1, valeur_a_envoyer[i]);		
		}
		entry.setField(i+1, Module );

		System.out.println(" |  Send broadcast Tihngspeak data |");			
		for(i=0;i< Parametres.channel.length;i++){

			try {					
				Parametres.channel[i].update(entry);// Methode pour envoyer les donnees pour le canal					
			} catch (UnirestException e) {

				e.printStackTrace();				
			} catch (ThingSpeakException e) {

				e.printStackTrace();		
			}	
		}


	}

	// Methode pour envoier les donnees pour un canal specifique:reçoit le canal à envoyer, l'objet Json et les valeurs concatennes de chaque capteur
	public void sendData (int IdDB,int[] valeurs,JsonDecoder Parametres){ 

		int i; 
		valeur_a_envoyer = new String[valeurs.length-1];
		Module = Integer.toString(valeurs[0]);
		valeur_a_envoyer[valeur_a_envoyer.length-1] = Integer.toString(valeurs[1]); // Rssi

		for(i=0;i<valeurs.length-2;i++){
			valeur_a_envoyer[i] = Integer.toString(valeurs[i+2]);
		}

		//0:RSSI, 1: Temeprature, 2: Luminosite, 3: Humidite, 4: Batterie, 5


		Entry entry = new Entry(); // Objet pour stocker les donnees a envoyer pour les canaux
		//System.out.println(Parametres.channel[0].isAvailable());

		for(i=0;i<valeurs.length-1;i++){
			entry.setField(i+1, valeur_a_envoyer[i]);		
		}
		entry.setField(i+1, Module );

		System.out.println(" |  Send Tihngspeak data |");		

		for(i=0;i< Parametres.channel.length;i++)// Trouve le canal choisi					
			if (Parametres.channel[i].channelId == IdDB )					
				break;	


		if(i>=Parametres.channel.length)
		{
			i--;
			System.out.println("Error, mauvaise channel! ");
			return;
		}	

		try {			
			Parametres.channel[i].update(entry);// Methode pour envoyer les donnees pour le canal					
		} catch (UnirestException e) {			
			e.printStackTrace();			
		} catch (ThingSpeakException e) {

			e.printStackTrace();
		}
	}


}
