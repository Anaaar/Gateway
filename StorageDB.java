package testInterpreter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import testInterpreter.JsonDecoder;
//import java.util.Date;


@SuppressWarnings("unused")
public class StorageDB implements ISTORE,Runnable {


	private static String Temperature;
	private static String Humidite;
	private static String Luminosite;
	private static String Batterie;
	private static String Module;
	private static String Rssi_value;
	private static String[] valeur_a_envoyer;// Stocke les donnees concatenees
	private static String[] capteurs ={"RSSI","Temperature","Humidite","Luminosite","Batterie","Cov","CO2","Pression"};

	private BlockingQueue<Integer> queue; // File d'attente avec une taille sans l'initialisee
	private int Id_send;
	private JsonDecoder JsonParametres;

	//constructeur de la classe: reçoit une file d'attente, l'objet avec les donnees Json et l'Id de la base de donnees
	public StorageDB(BlockingQueue<Integer> queue,JsonDecoder Parametres,int Id){
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

		try {
			while(true){
				while (queue.size() !=0) // Prendre tous les donnees de la file d'attente pour les concatener 
				{
					for(i=0;i<taille;i++){
						valeurs[i] = queue.take();
						valeurs_concatenees[i] = valeurs_concatenees[i] + Integer.toString(valeurs[i])+","; // enchaîne les valeurs des capteurs
						System.out.println(valeurs_concatenees[i] );
					}
				}

				switch(Id_send){
				case 0:// 0 envoie pour tous les bases de donnees
					broadMultipleData(valeurs_concatenees,JsonParametres);
					break;
				default: // Envoie pour une base de donnees specifique
					sendMultipleData(Id_send,valeurs_concatenees,JsonParametres);
					break;
				}
				Thread.sleep(15000);// Met en veille pour 15 secondes/ Pas besoin pour les bases de donnees
				for(i=0;i<taille;i++){
					valeurs_concatenees[i] =""; // enchaîne les valeurs des capteurs
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();   
		}
	}

	// Methode pour envoier les donnees concatenees pour tous les bases de donnees: reçoit l'objet Json et les valeurs concatennes de chaque capteur
	public void broadMultipleData(String[] valeurs,JsonDecoder Parametres){  

		int i;		 
		valeur_a_envoyer = new String[valeurs.length-1];
		Module = valeurs[0];
		//0: RSSI, 1: Temeprature, 2: Humidite, 3: Luminosite, 4: Batterie, 5
		for(i=0;i<valeurs.length-1;i++){
			valeur_a_envoyer[i] = valeurs[i+1];
		}

		Date oj = new Date(); // Prendre la date d'envoi
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");		
		//			 Temperature = Integer.toString(valeurs[2]);
		//			 Humidite    = Integer.toString(valeurs[4]);
		//			 Luminosite  = Integer.toString(valeurs[3]);
		//			 Batterie    = Integer.toString(valeurs[5]);
		//			 Module      = Integer.toString(valeurs[0]);
		//			 Rssi_value   = Integer.toString(valeurs[1]);

		System.out.println(" |  Send DataBase broadcast data |");
		for(i=0;i< Parametres.db.length;i++){

			for(int j=0;j<valeurs.length-1;j++){
				// Methode pour envoier les donnees pour une base de donnees
				Parametres.db[i].Connectdb(formater.format(oj).toString(), Module,capteurs[j] , valeur_a_envoyer[j],"0");
				//System.out.println(formater.format(oj).toString() + " " + Module + " "+ capteurs[j] + " "+  valeur_a_envoyer[j]);
			}

			//				 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "Temperature", Temperature,"0");
			//				// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Temperature" + " "+ Temp);
			//				 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "Luminosite", Luminosite,"0");
			//				// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Luminosite" + " "+ Luminosite);
			//				 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "Humidite", Humidite,"0");			
			//				// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Humidite" + " "+ Humidite);
			//				 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "Batterie",Batterie,"0");
			//				// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Batterie" + " "+ Bat);
			//				 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "RSSI",Rssi_value,"0");

		}

	}

	// Methode pour envoier les donnees concatenees pour une base de donnees specifique: reçoit la base de donnees à envoyer, l'objet Json et les valeurs concatennes de chaque capteur
	public void sendMultipleData(int IdDB,String[] valeurs,JsonDecoder Parametres){ 

		int i;		 
		valeur_a_envoyer = new String[valeurs.length-1];
		Module = valeurs[0];

		for(i=0;i<valeurs.length-1;i++){
			valeur_a_envoyer[i] = valeurs[i+1];
		}
		//0: RSSI, 1: Temeprature, 2: Humidite, 3: Luminosite, 4: Batterie, 5

		//			
		//			 Temperature = Integer.toString(valeurs[2]);
		//			 Humidite    = Integer.toString(valeurs[4]);
		//			 Luminosite  = Integer.toString(valeurs[3]);
		//			 Batterie    = Integer.toString(valeurs[5]);
		//			 Rssi_value   = Integer.toString(valeurs[1]);

		Date oj = new Date(); // Prendre la date d'envoi
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");		
		System.out.println(" |  Send DataBase  data |");

		for(i=0;i<valeurs.length-1;i++){
			// Methode pour envoier les donnees pour une base de donnees
			Parametres.db[IdDB].Connectdb(formater.format(oj).toString(), Module,capteurs[i] , valeur_a_envoyer[i],"0");
			//System.out.println(formater.format(oj).toString() + " " + Module + " "+ capteurs[i] + " "+  valeur_a_envoyer[i]);
		}

		//			 Parametres.db[IdDB].Connectdb(formater.format(oj).toString(), Module, "Temperature", Temperature,"0");
		//			// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Temperature" + " "+ Temp);
		//			 Parametres.db[IdDB].Connectdb(formater.format(oj).toString(), Module, "Luminosite", Luminosite,"0");
		//			// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Luminosite" + " "+ Luminosite);
		//			 Parametres.db[IdDB].Connectdb(formater.format(oj).toString(), Module, "Humidite", Humidite,"0");			
		//			// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Humidite" + " "+ Humidite);
		//			 Parametres.db[IdDB].Connectdb(formater.format(oj).toString(), Module, "Batterie",Batterie,"0");
		//			// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Batterie" + " "+ Bat);
		//			 Parametres.db[IdDB].Connectdb(formater.format(oj).toString(), Module, "RSSI",Rssi_value,"0");

	}


	// Methode pour envoier les donnees pour tous les bases des donnees: reçoit l'objet Json et les valeurs concatennes de chaque capteur
	public void broadData(int[] valeurs,JsonDecoder Parametres){  //(int Temp, int Lumi, int Humi, int Bat, int Num_Module,int Rssi, JsonDecoder Parametres){

		int i;		 
		valeur_a_envoyer = new String[valeurs.length-1];
		Module = Integer.toString(valeurs[0]);
		//0: RSSI, 1: Temeprature, 2: Humidite, 3: Luminosite, 4: Batterie, 5
		for(i=0;i<valeurs.length-1;i++){
			valeur_a_envoyer[i] = Integer.toString(valeurs[i+1]);
		}

		Date oj = new Date();// Prendre la date d'envoi
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");		
		//		 Temperature = Integer.toString(valeurs[2]);
		//		 Humidite    = Integer.toString(valeurs[4]);
		//		 Luminosite  = Integer.toString(valeurs[3]);
		//		 Batterie    = Integer.toString(valeurs[5]);
		//		 Module      = Integer.toString(valeurs[0]);
		//		 Rssi_value   = Integer.toString(valeurs[1]);

		System.out.println(" |  Send DataBase broadcast data |");
		for(i=0;i< Parametres.db.length;i++){

			for(int j=0;j<valeurs.length-1;j++){
				// Methode pour envoier les donnees pour une base de donnees
				Parametres.db[i].Connectdb(formater.format(oj).toString(), Module,capteurs[j] , valeur_a_envoyer[j],"0");
				//System.out.println(formater.format(oj).toString() + " " + Module + " "+ capteurs[j] + " "+  valeur_a_envoyer[j]);
			}

			//			 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "Temperature", Temperature,"0");
			//			// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Temperature" + " "+ Temp);
			//			 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "Luminosite", Luminosite,"0");
			//			// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Luminosite" + " "+ Luminosite);
			//			 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "Humidite", Humidite,"0");			
			//			// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Humidite" + " "+ Humidite);
			//			 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "Batterie",Batterie,"0");
			//			// System.out.println(formater.format(oj).toString() + " " + Module + " "+ "Batterie" + " "+ Bat);
			//			 Parametres.db[i].Connectdb(formater.format(oj).toString(), Module, "RSSI",Rssi_value,"0");

		}

	}

	// Methode pour envoier les donnees pour une base de donnees specifique:reçoit le canal à envoyer, l'objet Json et les valeurs concatennes de chaque capteur
	public void sendData(int IdDB,int[] valeurs,JsonDecoder Parametres){ 

		int i;		 
		valeur_a_envoyer = new String[valeurs.length-1];
		Module = Integer.toString(valeurs[0]);

		for(i=0;i<valeurs.length-1;i++){
			valeur_a_envoyer[i] = Integer.toString(valeurs[i+1]);
		}
		//0 RSSI, 1 Temeprature, 2 Humidite, 3 Luminosite, 4 Batterie, 5

		Date oj = new Date();// Prendre la date d'envoi
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");		
		System.out.println(" |  Send DataBase  data |");

		for(i=0;i<valeurs.length-1;i++){
			// Methode pour envoier les donnees pour une base de donnees
			Parametres.db[IdDB].Connectdb(formater.format(oj).toString(), Module,capteurs[i] , valeur_a_envoyer[i],"0");
		}
	}
}
