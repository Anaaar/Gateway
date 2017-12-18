package testInterpreter;

// Interface pour les classes utilises pour stocker les donnees
public interface ISTORE {
	
	void broadData(int[] valeurs,JsonDecoder Parametres);
	void sendData (int IdDB,int[] valeurs,JsonDecoder Parametres);
	

}
