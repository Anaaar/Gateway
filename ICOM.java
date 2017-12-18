package testInterpreter;

// Interface pour tous les protocoles IoT utilises 
public interface ICOM {
	
	int[] readMsg(long temps) ;
	void sendMsg(short[] msg,int num_module);

}
