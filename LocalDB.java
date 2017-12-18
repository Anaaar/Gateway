package testInterpreter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;








import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class LocalDB {

	public static int cmd =0; // Flag pour indiquer qu'il y a un message de configuration a envoier
	public static short[] message; //Reçoit la message de configuration
	public static int module = 0;
	public static  int status = 0;

//Constructeur de la classe, reçoit un port et cree une base de donnees local
	public  LocalDB(int port) throws IOException {

		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/", new ConfigHandler());
		server.setExecutor(null); // creates a default executor
		server.start();

	}
	
//Change la valeur de la flag status
	 public void setVarOne(int value) {
         LocalDB.status = value;
    }
//Recupere la valeur de la flag status
	 public static int getVar() {
         return LocalDB.status;
    }

	 // Mathode utilise lorsque une requete est faite par l'utilisateur pour configurer un module
	static class ConfigHandler implements HttpHandler {
		@SuppressWarnings("static-access")
		public void handle(HttpExchange t) throws IOException {
			String response = "En train de configurer\n"; // Premiere reponse pour l'utilisateur
			long fin = 0,debut,trigger=0;
			int stop;
			//System.out.println(t.getRequestURI().getQuery());
			//t.sendResponseHeaders(200, response.length());
			Map<String, String> params = queryToMap(t.getRequestURI().getQuery());// Contient les parametres et commandes de la requete
			ConfigModule config = new ConfigModule(params);// Methode pour interpreter les commandes et parametres et creer un message de configuration
			
			//Envoie la reponse pour le navigateur de l'utilisateur
			t.sendResponseHeaders(200, 0);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes(),0,response.length());

			
			switch (config.status){ //Status du message de configuration
			case 0:// Commandes reçus sont OK et message de configuration cree
				
				response = "Commande valide: configuration en execution\n";
				cmd = 1; // Met la flag en 1 pour indiquer qu'il y a un message a envoyer
				message = config.message; // Recupere le message de configuration
				module = config.module; // Recupere le module a configurer
				break;
			case -1:
				response = "ERROR: champ 'cmd' manquant\n";
				break;
			case -2:
				response = "ERROR: champ 'module' manquant\n";
				break;
			case -3:
				response = "ERROR: mauveaise option de configuration\n";
				break;
			case -4:
				response = "ERROR: " + params.get("cmd") + " ce n'est pas un commande valide\n";
				break;

			}
			
			//Envoie la reponse pour le navigateur de l'utilisateur
			os.write(response.getBytes(),0,response.length());
			
			// Attends 100 secondes pour recevoir le message de confirmation de configuration du module 
			debut = System.currentTimeMillis();
			stop = 0; //Flag pour indiquer que le module a ete correctement configure
			while(stop== 0 && fin<=100000){
				 
				 fin = System.currentTimeMillis() - debut;
				
				 if((fin-trigger)>=1000){ //tourne a chaque 1 seconde
				 try {
					 stop = getVar();
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					//System.out.println(stop);
				 
				trigger = fin;
				 }
			}
			
			if(stop==1)
				response = "Module correctement configuré\n";
			else
				response = "ERROR: Module ce n'est pas correctement configuré\n";
			
			status = 0;
			
			//Envoie la reponse pour le navigateur de l'utilisateur
			os.write(response.getBytes(),0,response.length());
			os.close();
		

		}

//methode pour recuperer les commandes et parametres de la requete
		public Map<String, String> queryToMap(String query){
			Map<String, String> result = new HashMap<String, String>();
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				if (pair.length>1) {
					result.put(pair[0], pair[1]);
				}else{
					result.put(pair[0], "");
				}
			}
			return result;
		}
	}



}


