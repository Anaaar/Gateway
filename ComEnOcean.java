package testInterpreter;

import comlora.ILORA;

public class ComEnOcean implements ICOM{

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
	
	@Override
	public int[] readMsg(long temps) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMsg(short[] msg, int num_module) {
		// TODO Auto-generated method stub
		
	}
	

}
