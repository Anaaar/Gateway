package testInterpreter;
import java.util.Map;

public class ConfigModule {

	public static int status=-1;
	public static short[] message;
	public static int module = 0;

	//Constructeur de la classe, reçoit les commandes et parametres de la requete reçue pour une configuration à distance
	public ConfigModule(Map<String, String> params){
		status = config(params);

	}

// Methode pour interpreter les commandes et parametres et creer un message de configuration
	int config(Map<String, String> params){

		String cmd= null;
		short[] cfg = new short[15];
		String value = null,value_t = null ;

		if(!params.containsKey("cmd")){// Verifie si le champs cmd est present
			System.out.println("ERROR: champ 'cmd' manquant");
			return -1;
		}
		if(!params.containsKey("module")){// Verifie si le champs module est present
			System.out.println("ERROR: champ 'module' manquant");
			return -2;
		}
		cmd = params.get("cmd"); // Recupere le commande cmd reçu
		module = Integer.parseInt(params.get("module"));// Recupere le module à configurer
		switch (cmd)
		{
		case "cfg":
			if(!params.containsKey("tmr")||!params.containsKey("nbv")){// Verifie si le champs tmr et nbv sont presents
				
				System.out.println("ERROR: mauveaise option de configuration");
				return -3;
				
				
			}
			else
			{
				//Prendre les parametres e commence a former le message de configuration
				value_t = (params.get("tmr"));	
				value = (params.get("nbv"));
				cfg[0]=0x74; //"tmr:"; 
				cfg[1]=0x6d;
				cfg[2]=0x72;
				cfg[3]=0x3a;
				
				cfg[8]=0x6e;//"nbv:"
				cfg[9]=0x62;
				cfg[10]=0x76;
				cfg[11]=0x3a;
				
				//  Cree le message de configuration 
				message = new short[8+value.length()+value_t.length()];
				for(int i=0; i<message.length;i++)
				{
					if(i<4 || (i>7 && i <12))
						message[i] = cfg[i];
					else if(i>3 && i<8)
						message[i] =(short)value_t.charAt(i-4);
					
					else
						message[i] =(short)value.charAt(i-12);
				}
				
			}
			break;
		case "slp":
			value = (params.get("tmp"));
			System.out.println("Attention!Le capteur va dormir pour " + value + " secondes");
			
			//Prendre les parametres e commence a former le message de configuration
			cfg[0]=0x73; //"slp:";
			cfg[1]=0x6c;
			cfg[2]=0x70;
			cfg[3]=0x3a;
			
			//  Cree le message de configuration 
			message = new short[4+value.length()];
			for(int i=0; i<(4 + value.length());i++)
			{
				if(i<4)
					message[i] = cfg[i];
				else
					message[i]=(short)value.charAt(i-4);
			}
			
			break;
			
		default:
			System.out.println("Mauvaise commande");
			return -3;
		}


		for(int i=0;i<message.length;i++)
			System.out.print(message[i]+" ");

		return 0;
	}


}
